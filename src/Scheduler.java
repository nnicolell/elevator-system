import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * A Scheduler to handle communication between the Elevator and Floor.
 */
public class Scheduler implements Runnable {

    /**
     * An integer representing the total number of requests.
     */
    private int numReqs;

    /**
     * An integer representing the number of requests that have been handled.
     */
    private int numReqsHandled;

    /**
     * The current state of the Scheduler state machine.
     */
    private SchedulerState currentState;

    /**
     * A HashMap of states in the Scheduler state machine.
     */
    private final HashMap<String, SchedulerState> states;

    /**
     * DatagramPackets to send and receive data to the Elevator subsystem
     */
    private DatagramPacket sendPacketElevator, receivePacketElevator;

    /**
     * A DatagramSocket to send and receive DatagramPackets from the Elevator subsystem.
     */
    private DatagramSocket sendReceiveSocket;

    /**
     * A DatagramPacket to send data to the Floor subsystem.
     */
    private DatagramPacket sendPacketFloor;

    /**
     * A DatagramSocket to send DatagramPackets to the Floor subsystem.
     */
    private DatagramSocket sendSocketFloor;

    /**
     * A DatagramSocket to receive DatagramPackets from the Floor subsystem.
     */
    private DatagramSocket receiveSocketFloor;

    /**
     * A List of HardwareDevices representing the floor events to handle.
     */
    private ArrayList<HardwareDevice> floorEventsToHandle;

    /**
     * A List of Elevators representing the elevators that are not currently running
     */
    private List<Elevator> availableElevators;

    /**
     * A List of Elevators representing the elevators that are currently running
     */
    private List<Elevator> busyElevators;

    /**
     * An InetAddress representing the address to communicate with the floor subsystem.
     */
    private InetAddress floorAddress = null;

    /**
     * An integer representing the port number to communicate with the floor subsystem.
     */
    private int floorPortNumber = 0;

    /**
     * A List of Threads representing the threads for the elevators.
     */
    private List<Thread> elevatorThreads;

    /**
     * Initializes a Scheduler.
     */
    public Scheduler(ArrayList<Integer> portNumbers) {
        Thread floor = new Thread(new Floor(this),"Floor");
        floor.start();

        int numElevators = portNumbers.size();
        availableElevators = new ArrayList<>();
        busyElevators = new ArrayList<>();
        elevatorThreads = new ArrayList<>();
        for (int i = 0; i < numElevators; i++) {
            String elevatorName = "Elevator" + (i + 1);
            Elevator elevator = new Elevator(this, portNumbers.get(i), elevatorName);
            Thread elevatorThread = new Thread(elevator, elevatorName);
            availableElevators.add(elevator);
            elevatorThreads.add(elevatorThread);
            elevatorThread.start();
        }

//        Elevator elevator1 = new Elevator(this, 70, "Elevator1");
//        Elevator elevator2 = new Elevator(this, 64, "Elevator2");
//        Elevator elevator3 = new Elevator(this, 67, "Elevator3");

//        Thread elevator1Thread = new Thread(elevator1, "Elevator1");
//        Thread elevator2Thread = new Thread(elevator2, "Elevator2");
//        Thread elevator3Thread = new Thread(elevator3, "Elevator3");

//        floor.start();
//        elevator1Thread.start();
//        elevator2Thread.start();
//        elevator3Thread.start();

        floorEventsToHandle = new ArrayList<>();
        numReqsHandled = 1;
        numReqs = 10000;

        states = new HashMap<>();
        addState("WaitingForFloorEvent", new WaitingForFloorEventState());
        addState("NotifyElevator", new NotifyElevatorState());
        addState("NotifyFloor", new NotifyFloorState());
        setState("WaitingForFloorEvent");

        try {
            sendSocketFloor = new DatagramSocket();
            receiveSocketFloor = new DatagramSocket(5000);
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se){
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Returns the current state of the Scheduler state machine.
     *
     * @return The current state of the Scheduler state machine.
     */
    public SchedulerState getCurrentState() {
        return currentState;
    }

    /**
     * Returns a HashMap of states in the Scheduler state machine.
     *
     * @return A HashMap of states in the Scheduler state machine.
     */
    public HashMap<String, SchedulerState> getStates() {
        return states;
    }

    /**
     * Sets current state of the Scheduler state machine.
     *
     * @param stateName A string representing the name of the state to set.
     */
    public void setState(String stateName) {
        currentState = states.get(stateName);
        currentState.displayState();
    }

    /**
     * Adds the given state to the Scheduler state machine.
     *
     * @param name A String representing the name of the state.
     * @param schedulerState A SchedulerState to be added to the Scheduler state machine.
     */
    public void addState(String name, SchedulerState schedulerState) {
        states.put(name, schedulerState);
    }

    /**
     * Receives the next floor event to be processed. Runs as long as there are more requests pending, it will wait
     * until there is an event added to the floor queue and no floor event is current being processed.
     *
     * @throws InterruptedException When a thread is interrupted while it is in a blocked state.
     */
    public synchronized void checkForFloorEvent() throws InterruptedException {
        // construct a DatagramPacket for receiving packets up to 100 bytes long
        byte[] floorData = new byte[100];
        DatagramPacket floorPacket = new DatagramPacket(floorData, floorData.length);

        // block until a DatagramPacket is received from receiveSocket
        System.out.println("[Scheduler] Waiting for packet from floor...");
        try {
            receiveSocketFloor.receive(floorPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // process the received DatagramPacket from the Floor subsystem
        String floorPacketString = new String(floorData, 0, floorPacket.getLength());
        System.out.println("[Scheduler] Received packet from floor containing: " + floorPacketString);
        HardwareDevice floorEvent = HardwareDevice.stringToHardwareDevice(floorPacketString);

        // add the floor event to the appropriate list of floor events to handle
        floorEventsToHandle.add(floorEvent);

        // construct acknowledgment data including the content of the received packet
        byte[] acknowledgmentData = ("ACK " + floorPacketString).getBytes();

        // get the address and the port number to communicate with floor when the floor event is done executing later
        InetAddress floorPacketAddress = floorPacket.getAddress();
        int floorPacketPort = floorPacket.getPort();
        floorPortNumber = floorPacketPort;
        floorAddress = floorPacketAddress;

        // create a DatagramPacket for the acknowledgment and send it
        sendPacketFloor = new DatagramPacket(acknowledgmentData, acknowledgmentData.length, floorPacketAddress,
                floorPacketPort);
        try {
            sendSocketFloor.send(sendPacketFloor);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("[Scheduler] Acknowledgment sent to floor!");
    }

    /**
     * Once the elevator subsystem finishes its task, the floor subsystem will be notified.
     * The number of requests handled will be incremented and the current floor event is cleared.
     */
    public synchronized void notifyFloorSubsystem(HardwareDevice hardwareDevice) {
        if (floorAddress != null && floorPortNumber != 0) { // this must be initialized before you use this method
            // construct message to Floor subsystem including the content of hardwareDevice
            String message = "[Scheduler] Floor event completed: " + hardwareDevice.toString();
            byte[] messageBytes = message.getBytes();

            // create a DatagramPacket for the message and send it
            sendPacketFloor = new DatagramPacket(messageBytes, messageBytes.length, floorAddress, floorPortNumber);
            try {
                sendSocketFloor.send(sendPacketFloor);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("[Scheduler] Message sent to floor containing: " + message);
            numReqsHandled++;
        }
    }

    /**
     * Adds the specified floor event into the floor queue.
     *
     * @param hardwareDevice A HardwareDevice representing the floor event.
     */
    public synchronized void addFloorEvent(HardwareDevice hardwareDevice) {
        floorEventsToHandle.add(hardwareDevice);
        notifyAll();
    }

    /**
     * Remove the specified floor event into the floor queue.
     *
     * @param hardwareDevice A HardwareDevice representing the floor event.
     */
    public synchronized void removeFloorEvent(HardwareDevice hardwareDevice) {
        floorEventsToHandle.remove(hardwareDevice);
        notifyAll();
    }

    /**
     * Constantly checks the elevator status, waiting for the elevator to complete its task. If the elevator is still
     * running and the number of requests handled is lower than the number of requests or the currentFloorEvent is null,
     * the thread should wait. Once the elevator has arrived, the floor subsystem should be notified.
     *
     * @param hardwareDevice The updated HardwareDevice.
     */
    public synchronized void checkElevatorStatus(HardwareDevice hardwareDevice) {
        System.out.println("[Scheduler]" + " Elevator has arrived at floor " + hardwareDevice.getCarButton() + ".");
        setState("NotifyFloor");
        currentState.handleRequest(this);
        notifyFloorSubsystem(hardwareDevice);
        notifyAll();
    }

    /**
     * Sets the number of requests.
     *
     * @param numReqs An integer representing the number of requests.
     */
    public void setNumReqs(int numReqs) {
        this.numReqs = numReqs;
    }

    /**
     * Returns an integer representing the number of requests.
     *
     * @return An integer representing the number of requests.
     */
    public int getNumReqs()  {
        return numReqs;
    }

    /**
     * Returns an integer representing the number of requests that have been handled.
     *
     * @return An integer representing the number of requests that have been handled.
     */
    public int getNumReqsHandled() {
        return numReqsHandled;
    }

    /**
     * Checks for the next floor event from the Floor subsystem.
     */
    @Override
    public void run() {
        while (numReqsHandled < numReqs) {
            currentState.handleRequest(this);
        }
    }

    /**
     * Sorting the elevators into lists depending on their running status
     */
    public void sortElevators() {
//        for (Elevator elevator : busyElevators){
//            if (elevator.getCurrentState() instanceof WaitingForElevatorRequestState){
//                availableElevators.add(elevator);
//                busyElevators.remove(elevator);
//            }
//        }
        Iterator<Elevator> iterator = busyElevators.iterator();
        while (iterator.hasNext()) {
            Elevator elevator = iterator.next();
            if (elevator.getCurrentState() instanceof WaitingForElevatorRequestState) {
                availableElevators.add(elevator);
                iterator.remove(); // Use Iterator's remove method
            }
        }
    }

    /**
     * Returns the list of the floor events to handle
     * @return The list of the floor events to handle
     */
    public ArrayList<HardwareDevice> getFloorEventsToHandle() {
        return floorEventsToHandle;
    }

    /**
     * Distributes the floor events to the closest available elevator
     */
    public void distributeFloorEvents() {
        int distance = 0;
        HardwareDevice floorEvent = floorEventsToHandle.removeFirst();
//        for (Elevator e : availableElevators) {
//            int elevatorDistance = Math.abs(e.getCurrentFloor() - floorEvent.getFloor());
//            if (elevatorDistance < distance) {
//                distance = elevatorDistance;
//            }
//            sendElevatorMessage(e, floorEvent);
//            break;
//        }
        Iterator<Elevator> iterator = availableElevators.iterator();
        while (iterator.hasNext()) {
            Elevator e = iterator.next();
            int elevatorDistance = Math.abs(e.getCurrentFloor() - floorEvent.getFloor());
            if (elevatorDistance < distance) {
                distance = elevatorDistance;
            }
            sendElevatorMessage(e, floorEvent);
            break;
        }
    }

    /**
     * Adds an elevator to the busy elevators list
     * @param elevator Elevator to get added to the busyElevators list
     */
    public void addBusyElevator(Elevator elevator) {
        busyElevators.add(elevator);
    }

    /**
     * Returns a list of busy elevators
     * @return The list of busy elevators
     */
    public List<Elevator> getBusyElevators() {
        return busyElevators;
    }


    /**
     * Sends a Floor Event to the Elevator
     * @param elevator Elevator that the floor event is going to be sent to
     * @param hardwareDevice Floor Event that is being sent
     */
    public void sendElevatorMessage(Elevator elevator, HardwareDevice hardwareDevice){

        byte[] data = hardwareDevice.toString().getBytes();
        try{
            sendPacketElevator = new DatagramPacket(data, data.length, InetAddress.getLocalHost(),elevator.getPort());
        } catch (UnknownHostException e){
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("[Scheduler] Sending floor event to "+ elevator.getName() + " containing: " +  new String(sendPacketElevator.getData(),0,sendPacketElevator.getLength()));

        try{
            // Sends packet to Server
            sendReceiveSocket.send(sendPacketElevator);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        addBusyElevator(elevator);
    }

    /**
     * Receives a message from an Elevator and sends an acknowledgement to the Elevator
     */
    public void receiveElevatorMessage() {
        //receive ack from elevator
        byte[] data = new byte[100];
        receivePacketElevator = new DatagramPacket(data, data.length);

        try {
            // Waits to receive a packet from the Server
            System.out.println("[Scheduler] Waiting for acknowledgment from Elevator...");
            sendReceiveSocket.receive(receivePacketElevator);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        String hdString = new String(data,0,receivePacketElevator.getLength());
        System.out.println("[Scheduler] Received acknowledgment from Elevator: " + hdString);

        //receive floor event from elevator
        data = new byte[100];
        receivePacketElevator = new DatagramPacket(data, data.length);

        try {
            // Waits to receive a packet from the Server
            System.out.println("[Scheduler] Waiting for floor event from Elevator...");
            sendReceiveSocket.receive(receivePacketElevator);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        hdString = new String(data,0,receivePacketElevator.getLength());
        System.out.println("[Scheduler] Received floor event from Elevator containing: \n" + hdString);

        // construct acknowledgment data including the content of the received packet
        byte[] acknowledgmentData = ("ACK " + hdString).getBytes();

        // create a DatagramPacket for the acknowledgment and send it
        sendPacketElevator = new DatagramPacket(acknowledgmentData, acknowledgmentData.length, receivePacketElevator.getAddress(),
                receivePacketElevator.getPort());
        try {
            sendReceiveSocket.send(sendPacketElevator);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("[Scheduler] Acknowledgment sent to Elevator!");

        sortElevators();
    }
}
