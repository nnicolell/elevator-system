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
     * A DatagramSocket to send DatagramPackets to the Floor subsystem.
     */
    private DatagramSocket sendSocketFloor;

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
     * The floor listener for the Scheduler
     */
    private FloorListener floorListener;

    /**
     * A List of Threads representing the threads for the elevators.
     */
    private List<Thread> elevatorThreads;

    /**
     * A List containing the failed elevators
     */
    private List<Elevator> failedElevators;

    /**
     * A List containing all elevators
     */
    private List<Elevator> allElevators;

    /**
     * An ElevatorSystemLogger to log events.
     */
    private final ElevatorSystemLogger logger;

    /**
     * Initializes a Scheduler.
     */
    public Scheduler(ArrayList<Integer> portNumbers, int portFloor) {
        logger = new ElevatorSystemLogger("Scheduler");

        // start the Floor thread
        Thread floor = new Thread(new Floor(this),"Floor");
        floor.start();

        // start the Elevator threads
        int numElevators = portNumbers.size();
        availableElevators = new ArrayList<>();
        busyElevators = new ArrayList<>();
        failedElevators = new ArrayList<>();
        elevatorThreads = new ArrayList<>();
        allElevators = new ArrayList<>();
        for (int i = 0; i < numElevators; i++) {
            String elevatorName = "Elevator" + (i + 1);
            Elevator elevator = new Elevator(this, portNumbers.get(i), elevatorName);
            Thread elevatorThread = new Thread(elevator, elevatorName);
            availableElevators.add(elevator);
            elevatorThreads.add(elevatorThread);
            allElevators.add(elevator);
            elevatorThread.start();
        }

        floorEventsToHandle = new ArrayList<>();
        numReqsHandled = 1;
        numReqs = 10000;

        states = new HashMap<>();
        addState("WaitingForFloorEvent", new WaitingForFloorEvent());
        addState("NotifyElevator", new NotifyElevator());
        addState("NotifyFloor", new NotifyFloor());
        addState("SelectElevator", new SelectElevator());
        setState("WaitingForFloorEvent");

        try {
            sendSocketFloor = new DatagramSocket();
            sendReceiveSocket = new DatagramSocket();
            sendReceiveSocket.setSoTimeout(78000);
        } catch (SocketException se){
            se.printStackTrace();
            System.exit(1);
        }

        floorListener = new FloorListener(this, portFloor);
        Thread floorListenerThread = new Thread(floorListener);
        floorListenerThread.start();
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
        logger.info("State: " + currentState.displayState());
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
     * Once the elevator subsystem finishes its task, the floor subsystem will be notified.
     * The number of requests handled will be incremented and the current floor event is cleared.
     */
    public synchronized void notifyFloorSubsystem(HardwareDevice hardwareDevice) {
        // construct message to Floor subsystem including the content of hardwareDevice
        String message = "Floor event completed: " + hardwareDevice.toString();

        try {
            sendSocketFloor.send(floorListener.getSendPacketFloor());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Message sent to Floor: " + message);
        numReqsHandled++;
        notifyAll();
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
     * Constantly checks the elevator status, waiting for the elevator to complete its task. If the elevator is still
     * running and the number of requests handled is lower than the number of requests or the currentFloorEvent is null,
     * the thread should wait. Once the elevator has arrived, the floor subsystem should be notified.
     *
     * @param hardwareDevice The updated HardwareDevice.
     */
    public synchronized void checkElevatorStatus(HardwareDevice hardwareDevice) {
        logger.info(hardwareDevice.getElevator() +" has arrived at floor " + hardwareDevice.getCarButton() + ".");
        setState("SelectElevator");
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
     * Returns the list of the floor events to handle
     * @return The list of the floor events to handle
     */
    public ArrayList<HardwareDevice> getFloorEventsToHandle() {
        return floorEventsToHandle;
    }

    /**
     * Distributes the floor events to the closest available elevator
     */
    public synchronized void distributeFloorEvents() {
        int distance = 0;
        while (floorEventsToHandle.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        Iterator<Elevator> iterator = availableElevators.iterator();
        while (iterator.hasNext()) {
            Elevator e = iterator.next();
            HardwareDevice floorEvent = floorEventsToHandle.remove(0);
            if (e != null) {
                int elevatorDistance = Math.abs(e.getCurrentFloor() - floorEvent.getFloor());
                if (elevatorDistance < distance) {
                    distance = elevatorDistance;
                }
                addBusyElevator(e);
                iterator.remove();
                floorEvent.setElevator(e.getName());
                sendElevatorMessage(e, floorEvent);
                break;
            }
        }
        notifyAll();
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
        setState("NotifyElevator");
        byte[] data = hardwareDevice.toString().getBytes();
        try{
            sendPacketElevator = new DatagramPacket(data, data.length, InetAddress.getLocalHost(),elevator.getPort());
        } catch (UnknownHostException e){
            e.printStackTrace();
            System.exit(1);
        }
        logger.info("Sending floor event to " + elevator.getName() + " containing: " + new String(sendPacketElevator.getData(),0,sendPacketElevator.getLength()));

        try{
            // sends packet to Elevator
            sendReceiveSocket.send(sendPacketElevator);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        distributeFloorEvents();
    }

    /**
     * Receives a message from an Elevator and sends an acknowledgement to the Elevator
     */
    public void receiveElevatorMessage() {
        distributeFloorEvents();
        // receive ack from elevator
        byte[] data = new byte[200];
        receivePacketElevator = new DatagramPacket(data, data.length);

        try {
            // Waits to receive a packet from the Server
            logger.info("Waiting for acknowledgment from Elevator...");
            sendReceiveSocket.receive(receivePacketElevator);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        String hdString = new String(data,0,receivePacketElevator.getLength());
        logger.info("Received acknowledgment from Elevator: " + hdString);

        //receive floor event from elevator
        data = new byte[200];
        receivePacketElevator = new DatagramPacket(data, data.length);

        try {
            // Waits to receive a packet from the Server
            logger.info("Waiting for floor event from Elevator...");
            sendReceiveSocket.receive(receivePacketElevator);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        hdString = new String(data,0,receivePacketElevator.getLength());
        logger.info("Received floor event from Elevator containing: " + hdString);

        HardwareDevice hardwareDevice = HardwareDevice.stringToHardwareDevice(hdString);
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

        logger.info("Acknowledgment sent to Elevator!");

        availableElevators.add(getElevator(hardwareDevice.getElevator()));
        busyElevators.remove(getElevator(hardwareDevice.getElevator()));
        distributeFloorEvents();
    }

    /**
     * Get the Elevator object based on the name
     * @param name Name of the Elevator
     * @return Elevator Object
     */
    private Elevator getElevator(String name) {
        for (Elevator e : allElevators){
            if (e.getName().equals(name)){
                return e;
            }
        }
        return null;
    }

    /**
     * Gets the first elevator available.
     * @return Elevator
     */
    public Elevator getElevatorTest() {
        return availableElevators.get(0);
    }

    /**
     * Closes the sockets.
     */
    public void closeSockets() {
        sendReceiveSocket.close();
        sendSocketFloor.close();
    }

    /**
     * Gets the floor listener
     * @return The floor listener
     */
    public FloorListener getFloorListener() {
        return floorListener;
    }

    /**
     * Kills the specified elevator thread
     * @param name The name of the elevator thread to be killed
     */
    public void killElevatorThread(String name){
        failedElevators.add(getElevator(name));
        availableElevators.remove(getElevator(name));
        busyElevators.remove(getElevator(name));

        for (Thread t : elevatorThreads){
            if (t.getName().equals(name)){
                logger.info("Shutting down " + name);
                t.interrupt();
            }
        }
    }
}
