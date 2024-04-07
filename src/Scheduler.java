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
     * An integer representing the number of requests that have been received from the Floor subsystem.
     */
    private int numReqsReceived = 0;

    /**
     * The current state of the Scheduler state machine.
     */
    private SchedulerState currentState;

    /**
     * A HashMap of states in the Scheduler state machine.
     */
    private final HashMap<String, SchedulerState> states;

    /**
     * A DatagramSocket to send and receive DatagramPackets from the Elevator subsystem.
     */
    private DatagramSocket sendReceiveSocket;

    /**
     * A List of HardwareDevices representing the floor events to handle.
     */
    private final ArrayList<HardwareDevice> floorEventsToHandle;

    /**
     * A List of Elevators representing the elevators that are not currently running
     */
    private final List<Elevator> availableElevators;

    /**
     * A List of Elevators representing the elevators that are currently running
     */
    private final List<Elevator> busyElevators;

    /**
     * The FloorListener for the Scheduler.
     */
    private final FloorListener floorListener;

    /**
     * A List of Threads representing the threads for the elevators.
     */
    private final List<Thread> elevatorThreads;

    /**
     * A List containing the failed elevators.
     */
    private final List<Elevator> failedElevators;

    /**
     * A List containing all elevators.
     */
    private final List<Elevator> allElevators;

    /**
     * An ElevatorSystemLogger to log events.
     */
    private final ElevatorSystemLogger logger;

    /**
     * An integer to represent the number of movements the elevators have made.
     */
    private int numMovements = 0;

    /**
     * Initializes a Scheduler.
     */
    public Scheduler(ArrayList<Integer> portNumbers, int portFloor) {
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

        try {
            sendReceiveSocket = new DatagramSocket();
            sendReceiveSocket.setSoTimeout(78000);
        } catch (SocketException se){
            se.printStackTrace();
            System.exit(1);
        }

        // create a logger for Scheduler and FloorListener to log events on
        logger = new ElevatorSystemLogger("Scheduler");

        // start the FloorListener thread
        floorListener = new FloorListener(this, portFloor, logger);
        Thread floorListenerThread = new Thread(floorListener);
        floorListenerThread.start();

        states = new HashMap<>();
        addState("WaitingForFloorEvent", new WaitingForFloorEvent());
        addState("NotifyElevator", new NotifyElevator());
        addState("NotifyFloor", new NotifyFloor());
        addState("SelectElevator", new SelectElevator());
        setState("WaitingForFloorEvent");
    }

    public int getNumReqsReceived() {
        return numReqsReceived;
    }

    public void incrementNumReqsReceived() {
        numReqsReceived++;
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
     * Adds the specified floor event into the floor queue.
     *
     * @param hardwareDevice A HardwareDevice representing the floor event.
     */
    public synchronized void addFloorEvent(HardwareDevice hardwareDevice) {
        floorEventsToHandle.add(hardwareDevice);
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
            HardwareDevice floorEvent = floorEventsToHandle.get(0);
            if (e != null && !e.isMaxCapacity()) { //if the elevator is not full
                int elevatorDistance = Math.abs(e.getCurrentFloor() - floorEvent.getFloor());
                if (elevatorDistance < distance) {
                    distance = elevatorDistance;
                }
                floorEventsToHandle.remove(0);
                addBusyElevator(e);
                iterator.remove();
                floorEvent.setElevator(e.getName());
                sendElevatorFloorEvent(e, floorEvent);
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
     * Receives a DatagramPacket from an Elevator. Returns a String representing the message from the Elevator.
     *
     * @return A String representing the message from the Elevator.
     */
    private String receiveElevatorPacket() {
        byte[] receiveBytes = new byte[250];
        DatagramPacket receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);
        try {
            sendReceiveSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // process the received message from the Elevator
        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
        String elevatorName = HardwareDevice.stringToHardwareDevice(message).getElevator();
        logger.info("Received " + message + " from " + elevatorName + ".");
        return message;
    }

    /**
     * Sends a DatagramPacket to the specified elevator containing the specified message.
     *
     * @param elevator An Elevator to send the specified message to.
     * @param message A String representing the message to send to the specified Elevator.
     */
    private void sendElevatorPacket(Elevator elevator, String message) {
        byte[] messageBytes = message.getBytes();
        try {
            DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length,
                    InetAddress.getLocalHost(), elevator.getPort());
            sendReceiveSocket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Sending " + message + " to " + elevator.getName() + ".");
    }

    /**
     * Sends the specified floor event to the specified elevator.
     *
     * @param elevator An Elevator that the floor event is going to be sent to.
     * @param hardwareDevice A HardwareDevice representing a floor event.
     */
    private void sendElevatorFloorEvent(Elevator elevator, HardwareDevice hardwareDevice) {
        // send the floor event to the elevator and receive an acknowledgment
        sendElevatorPacket(elevator, hardwareDevice.toString());
        receiveElevatorPacket();

        distributeFloorEvents();
        setState("NotifyElevator");
    }

    /**
     * Receives a message from an Elevator and sends an acknowledgement back.
     */
    public void receiveElevatorFloorEvent() {
        distributeFloorEvents();

        // receive a completed floor event from an elevator and send an acknowledgment back
        String message = receiveElevatorPacket();
        HardwareDevice fulfilledFloorEvent = HardwareDevice.stringToHardwareDevice(message);
        Elevator elevator = getElevator(fulfilledFloorEvent.getElevator());
        sendElevatorPacket(elevator, "ACK " + message);

        // if the Elevator has no more floor events to complete, then the movement is complete and the elevator is
        // available
        if (!fulfilledFloorEvent.getMoreFloorEvents()) {
            numMovements++;
            logger.info(elevator.getName() + " has completed a movement. numMovements: " + numMovements + ".");
            availableElevators.add(elevator);
            busyElevators.remove(elevator);
            distributeFloorEvents();
        }

        numReqsHandled++;

        notifyFloor(message);
    }

    private void notifyFloor(String fulfilledFloorEvent) {
        // notify Floor that a floor event has been fulfilled
        byte[] sendBytes = fulfilledFloorEvent.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length, floorListener.getFloorAddress(),
                floorListener.getFloorPort());
        try {
            DatagramSocket sendSocket = new DatagramSocket();
            sendSocket.send(sendPacket);
            sendSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        logger.info("Sending " + fulfilledFloorEvent + " to Floor.");

        // receive an acknowledgment from the Floor
        byte[] ackBytes = new byte[200];
        DatagramPacket receivePacket = new DatagramPacket(ackBytes, ackBytes.length);
        try {
            sendReceiveSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        logger.info("Received " + new String(receivePacket.getData(), 0, receivePacket.getLength())
                + " from Floor.");
    }

    // FIXME: non-UDP way of implementing this...
    /**
     * The specified Elevator has picked up the specified floor event to fulfill. Removes the specified floor event
     * from the list of floor events to handle.
     *
     * @param elevator An Elevator that has picked up a floor event.
     * @param hardwareDevice A HardwareDevice representing a floor event the specified Elevator picked up.
     */
    public synchronized void pickedUpFloorEvent(Elevator elevator, HardwareDevice hardwareDevice) {
        logger.info(elevator.getName() + " has picked up " + hardwareDevice.toString() + ".");
        floorEventsToHandle.remove(hardwareDevice);
        notifyAll();
    }

    /**
     * Get the Elevator object based on the name
     * @param name Name of the Elevator
     * @return Elevator Object
     */
    public Elevator getElevator(String name) {
        for (Elevator elevator : allElevators) {
            if (elevator.getName().equals(name)) {
                return elevator;
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
     * Closes the sendReceiveSocket.
     */
    public void closeSendReceiveSocket() {
        sendReceiveSocket.close();
    }

    /**
     * Returns the floor listener.
     *
     * @return The floor listener.
     */
    public FloorListener getFloorListener() {
        return floorListener;
    }

    /**
     * Kills the specified elevator thread.
     *
     * @param name A String representing the name of the elevator thread to be killed.
     * @param numFloorEventsHandling An integer representing the number of floor events the Elevator was handling at the
     *                               time it was killed.
     */
    public synchronized void killElevatorThread(String name, int numFloorEventsHandling) {
        Elevator elevator = getElevator(name);
        failedElevators.add(elevator);
        availableElevators.remove(elevator);
        busyElevators.remove(elevator);

        numReqsHandled += numFloorEventsHandling;

        for (Thread elevatorThread : elevatorThreads) {
            if (elevatorThread.getName().equals(name)) {
                logger.info("Shutting down " + name + ". It was handling " + numFloorEventsHandling
                        + " floor event(s).");
                elevatorThread.interrupt();
            }
        }

        notifyAll();
    }

    public List<Elevator> getAllElevators() {
        return allElevators;
    }
}
