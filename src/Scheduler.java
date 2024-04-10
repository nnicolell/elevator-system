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
     * A SchedulerState representing the current state of the Scheduler state machine.
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
     * A List of Threads representing the threads for the elevators.
     */
    private final List<Thread> elevatorThreads;

    /**
     * A List of Elevators containing all elevators.
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
     * A boolean to signal that the elevator has arrived.
     */
    private boolean arrived;

    /**
     * A long representing the time, in nanoseconds, when a floor event is first sent to an Elevator.
     */
    private long startTime = -1;

    /**
     * A long representing the time, in nanoseconds, it takes to execute all the floor events.
     */
    private long endTime;

    /**
     * Initializes a Scheduler.
     *
     * @param portNumbers An ArrayList of Integers representing the port numbers each Elevator will receive UDP packets
     *                    on.
     */
    public Scheduler(ArrayList<Integer> portNumbers) {
        // start the Floor thread
        Thread floor = new Thread(new Floor(this),"Floor");
        floor.start();

        // start the Elevator threads
        int numElevators = portNumbers.size();
        availableElevators = new ArrayList<>();
        busyElevators = new ArrayList<>();
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

        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se){
            se.printStackTrace();
            System.exit(1);
        }

        // create a logger for Scheduler and FloorListener to log events on
        logger = new ElevatorSystemLogger("Scheduler");

        states = new HashMap<>();
        addState("WaitingForFloorEvent", new WaitingForFloorEvent());
        addState("NotifyElevator", new NotifyElevator());
        addState("NotifyFloor", new NotifyFloor());
        addState("SelectElevator", new SelectElevator());
        addState("WaitingForElevator", new WaitingForElevator());
        setState("WaitingForFloorEvent");
        arrived = false;
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
        while (floorEventsToHandle.size() <= numReqs) {
            logger.info("Received " + hardwareDevice + " from Floor.");
            floorEventsToHandle.add(hardwareDevice);
            logger.info("Sending ACK " + hardwareDevice + " to Floor.");
        }
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
        logger.info("Scheduler has executed all floor events.");
    }

    /**
     * Returns the list of the floor events to handle.
     *
     * @return The list of the floor events to handle.
     */
    public ArrayList<HardwareDevice> getFloorEventsToHandle() {
        return floorEventsToHandle;
    }

    /**
     * Distributes the floor events to the closest available elevator.
     */
    public synchronized void distributeFloorEvents() {
        int distance = 0;
        while (floorEventsToHandle.isEmpty() && numReqsHandled<=numReqs) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

        if (numReqsHandled<=numReqs) {
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
                    numReqsHandled++;
                    floorEvent.setElevator(e.getName());
                    sendElevatorFloorEvent(e, floorEvent);
                    break;
                }
            }
        }
        notifyAll();
    }

    /**
     * Adds the specified elevator to the list of busy elevators.
     *
     * @param elevator An Elevator to add to the list of busy elevators.
     */
    public void addBusyElevator(Elevator elevator) {
        busyElevators.add(elevator);
    }

    /**
     * Returns a List of Elevators representing a list of busy elevators.
     *
     * @return A List of Elevators representing a list of busy elevators.
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
        currentState.handleRequest(this);
        if (!hardwareDevice.getArrived()) {
            sendElevatorPacket(elevator, hardwareDevice.toString());

            // start the timer if its hasn't been started already
            if (startTime == -1) {
                startTime = System.nanoTime();
                logger.info("Timer has been started!");
            }

            currentState.handleRequest(this);
            receiveElevatorPacket();

            distributeFloorEvents();
            receiveElevatorFloorEvent();
        }
    }

    /**
     * Receives a message from an Elevator and sends an acknowledgement back.
     */
    private void receiveElevatorFloorEvent() {
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
            arrived = true;
            currentState.handleRequest(this);
            arrived = false;
            availableElevators.add(elevator);
            busyElevators.remove(elevator);
            distributeFloorEvents();
        }

        isFloorEventsComplete();
        endTime = System.nanoTime();
        logger.info("It took " + ((endTime - startTime) / 100000) + " ms to execute " + numReqs
                + " floor event(s).");
        notifyFloor(message);
    }

    /**
     * Checks if all the floor events received from the Floor subsystem is complete.
     */
    private void isFloorEventsComplete() {
        if (numReqsHandled == numReqs) {
            // stop the timer and log the time it takes, in milliseconds, to execute all the floor events
            endTime = System.nanoTime();
            logger.info("It took " + ((endTime - startTime) / 100000) + " ms to execute " + numReqs
                    + " floor event(s).");
            logger.info(numMovements + " movement(s) were completed.");
        }
    }

    /**
     * Notifies the Floor subsystem of a fulfilled floor event. Sends the String representation of the fulfilled floor
     * event to the Floor subsystem, and receives an acknowledgment back.
     *
     * @param fulfilledFloorEvent A String representing the fulfilled floor event to send to the Floor subsystem.
     */
    private void notifyFloor(String fulfilledFloorEvent) {
        // notify Floor that a floor event has been fulfilled
        byte[] sendBytes = fulfilledFloorEvent.getBytes();
        try {
            DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getLocalHost(),
                    5000);
            DatagramSocket sendSocket = new DatagramSocket();
            sendSocket.send(sendPacket);
            sendSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        currentState.handleRequest(this);
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

    /**
     * The specified Elevator has picked up the specified floor event to fulfill. Removes the specified floor event
     * from the list of floor events to handle.
     *
     * @param elevator An Elevator that has picked up a floor event.
     * @param hardwareDevice A HardwareDevice representing a floor event the specified Elevator picked up.
     */
    public void pickedUpFloorEvent(Elevator elevator, HardwareDevice hardwareDevice) {
        logger.info(elevator.getName() + " has picked up " + hardwareDevice.toString() + ".");
        floorEventsToHandle.remove(hardwareDevice);
    }

    /**
     * Returns the Elevator with the specified name.
     *
     * @param name A String representing the name of the Elevator.
     * @return An Elevator with the specified name. Null, if an Elevator with the specified name does not exist.
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
     * Returns the first available elevator.
     *
     * @return The first available elevator.
     */
    public Elevator getFirstAvailableElevator() {
        return availableElevators.get(0);
    }

    /**
     * Closes the sendReceiveSocket.
     */
    public void closeSendReceiveSocket() {
        sendReceiveSocket.close();
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
        availableElevators.remove(elevator);
        busyElevators.remove(elevator);

        numReqsHandled += numFloorEventsHandling;
        isFloorEventsComplete();

        for (Thread elevatorThread : elevatorThreads) {
            if (elevatorThread.getName().equals(name)) {
                logger.info("Shutting down " + name + ". It was handling " + numFloorEventsHandling
                        + " floor event(s).");
                elevatorThread.interrupt();
            }
        }

        notifyAll();
    }

    /**
     * Returns a List of Elevators representing all the elevators in the ElevatorSystem.
     *
     * @return A List of Elevators representing all the elevators in the ElevatorSystem.
     */
    public List<Elevator> getAllElevators() {
        return allElevators;
    }

    /**
     * Returns a boolean representing if an elevator has arrived at its destination floor or not.
     *
     * @return True, if an elevator has arrived at its destination floor. False, if not.
     */
    public boolean getArrived() {
        return arrived;
    }

}
