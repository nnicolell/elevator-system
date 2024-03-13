import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * A Scheduler to handle communication between the Elevator and Floor.
 */
public class Scheduler implements Runnable {

    /**
     * A Queue of HardwareDevices representing the floor events.
     */
    private final Queue<HardwareDevice> floorQueue;

    /**
     * A HardwareDevice representing the current floor event that is being handled.
     */
    private HardwareDevice currentFloorEvent;

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
    private DatagramPacket sendPacketElevator, receivePacketElevator;
    private DatagramSocket sendReceiveSocket;

    /**
     * A DatagramSocket to receive DatagramPackets from the Floor subsystem.
     */
    private DatagramSocket receiveSocket;

    /**
     * A List of HardwareDevices representing the floor events to handle.
     */
    private List<HardwareDevice> floorEventsToHandle;

    /**
     * Initializes a Scheduler.
     */
    public Scheduler() {
        floorQueue = new ArrayDeque<>(); // TODO: we don't need this anymore now that we have floorEventsToHandle
        floorEventsToHandle = new ArrayList<>();
        currentFloorEvent = null;
        numReqsHandled = 1;
        numReqs = 10000;

        states = new HashMap<>();
        addState("WaitingForFloorEvent", new WaitingForFloorEventState());
        addState("NotifyElevator", new NotifyElevatorState());
        addState("NotifyFloor", new NotifyFloorState());
        setState("WaitingForFloorEvent");

        try {
            receiveSocket = new DatagramSocket(23);
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
        // TODO: if we're using UDP, do we need all this wait() stuff??? also secured by the state machine...
//        // wait while the queue is empty or the elevator is already running, and the number of requests handled is less
//        // than the total number of requests or the elevator is not running
//        while ((floorQueue.isEmpty() || currentFloorEvent != null)
//                && (numReqsHandled <= numReqs || currentFloorEvent == null)) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }

        // construct a DatagramPacket for receiving packets up to 100 bytes long
        byte[] floorData = new byte[100];
        DatagramPacket floorPacket = new DatagramPacket(floorData, floorData.length);

        // block until a DatagramPacket is received from receiveSocket
        System.out.println("[Scheduler] Waiting for packet from floor...");
        try {
            receiveSocket.receive(floorPacket);
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

        // TODO: no longer need a currentFloorEvent since multiple elevators are running at the same time
        currentFloorEvent = floorQueue.poll(); // currentFloorEvent is taken from the beginning of floorQueue
        System.out.println("[Scheduler] Received floor request: " + currentFloorEvent + ".");
        notifyAll();
    }

    /**
     * Once the elevator subsystem finishes its task, the floor subsystem will be notified.
     * The number of requests handled will be incremented and the current floor event is cleared.
     */
    public synchronized void notifyFloorSubsystem() {
        System.out.println("[Scheduler] Floor Request: " + currentFloorEvent + " has been completed.");
        currentFloorEvent = null;
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
        while (!hardwareDevice.getArrived() && (numReqsHandled <= numReqs || currentFloorEvent == null)) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[Scheduler]" + " Elevator has arrived at floor " + hardwareDevice.getCarButton() + ".");
        setState("NotifyFloor");
        currentState.handleRequest(this);
        notifyFloorSubsystem();
        notifyAll();
    }

    /**
     * Returns the currentFloorEvent to the Elevator if it is not null. If it is null, the thread should wait.
     *
     * @return A HardwareDevice representing the current floor event.
     */
    public synchronized HardwareDevice getElevatorRequest() {
        while (currentFloorEvent == null) {
            try {
                wait();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        notifyAll();
        return currentFloorEvent;
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
     * Returns a Queue of HardwareDevices representing the floor events.
     *
     * @return A Queue of HardwareDevices representing the floor events.
     */
    public Queue<HardwareDevice> getFloorQueue() {
        return floorQueue;
    }

    /**
     * Returns a HardwareDevice representing the current floor event that is being handled.
     *
     * @return A HardwareDevice representing the current floor event that is being handled.
     */
    public HardwareDevice getCurrentFloorEvent() {
        return currentFloorEvent;
    }

//    public void sendElevatorMessage(HardwareDevice hardwareDevice){
//
//        try{
//            sendPacketElevator = new DatagramPacket(receivePacketClient.getData(), receivePacketClient.getLength(), InetAddress.getLocalHost(),69);
//        } catch (UnknownHostException e){
//            e.printStackTrace();
//            System.exit(1);
//        }
//        System.out.println("SCHEDULER: SENDING PACKET TO ELEVATOR");
//        System.out.println("----------------------------------------------------");
//        System.out.println("PACKET:");
//        //formattedByteRequest = formatRequest.formatByte(sendPacket);
//        //System.out.println("Byte: " + Arrays.toString(formattedByteRequest));
//        //System.out.println("String: " + formatRequest.formatString(formattedByteRequest) + "\n");
//        try{
//            // Sends packet to Server
//            sendReceiveSocket.send(sendPacketElevator);
//        } catch (IOException e){
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }
//
//    public void getElevatorMessage(){
//        //byte dataServer[] = new byte[4];
//        receivePacketElevator = new DatagramPacket(dataServer, dataServer.length);
//
//        try {
//            // Waits to receive a packet from the Server
//            System.out.println("SCHEDULER: WAITING FOR PACKET FROM ELEVATOR");
//            System.out.println("----------------------------------------------------");
//            sendReceiveSocket.receive(receivePacketElevator);
//        } catch (IOException e){
//            e.printStackTrace();
//            System.exit(1);
//        }
//        System.out.println("SCHEDULER: PACKET RECEIVED FROM ELEVATOR");
//        System.out.println("PACKET:");
//        // formattedByteRequest = formatRequest.formatByte(receivePacketServer);
//        // System.out.println("Byte: " + Arrays.toString(formattedByteRequest));
//        // System.out.println("String: " + formatRequest.formatString(formattedByteRequest) + "\n");
//    }

}
