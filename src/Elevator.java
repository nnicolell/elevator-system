import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * An Elevator to represent the elevator car moving up or down floors.
 */
public class Elevator implements Runnable {

    /**
     * A Scheduler representing the elevator scheduler to receive and send events to.
     */
    private final Scheduler scheduler;

    /**
     * An integer representing the port number to receive DatagramPackets from the Scheduler on.
     */
    private final int port;

    /**
     * A String representing the name of the Elevator.
     */
    private final String name;

    /**
     * A HashMap representing the states in the Elevator state machine.
     */
    private final HashMap<String, ElevatorState> states;

    /**
     * An ArrayList of HardwareDevices representing a list of floor events to complete.
     */
    private final ArrayList<HardwareDevice> floorEvents;

    private HardwareDevice mainFloorEvent;

    /**
     * The current state of the Elevator state machine.
     */
    private ElevatorState currentState;

    /**
     * A DatagramSocket to receive DatagramPackets from the Scheduler.
     */
    private DatagramSocket receiveSocket;

    private InetAddress schedulerAddress;

    private int schedulerPort = 0;

    /**
     * An integer representing the current floor the Elevator is at.
     */
    private int currentFloor = 1;

    /**
     * An integer representing the number of passengers currently in the Elevator car.
     */
    private int numPassengers = 0;

    /**
     * Initializes an Elevator.
     *
     * @param scheduler A Scheduler representing the elevator scheduler to receive and send events to.
     * @param port An integer representing the port number to receive DatagramPackets from the Scheduler on.
     * @param name A String representing the name of the Elevator.
     */
    public Elevator(Scheduler scheduler, int port, String name) {
        this.scheduler = scheduler;
        this.port = port;
        this.name = name;

        floorEvents = new ArrayList<>(); // initialize the ArrayList of floor events

        states = new HashMap<>(); // initialize the Elevator state machine
        addState("WaitingForElevatorRequest", new WaitingForElevatorRequest());
        addState("MovingBetweenFloors", new MovingBetweenFloors());
        addState("ReachedDestination", new ReachedDestination());
        addState("DoorsClosing", new DoorsClosing());
        addState("DoorsOpening", new DoorsOpening());
        addState("NotifyScheduler", new NotifyScheduler());
        setState("WaitingForElevatorRequest");

        try {
            receiveSocket = new DatagramSocket(port);
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }
    }

    /**
     * Receives a floor event from the Scheduler and processes it. Sends an acknowledgment message back to the
     * Scheduler.
     */
    public void getFloorEvent() {
        // receive a floor event from the Scheduler
        byte[] floorEventData = new byte[150];
        DatagramPacket receivePacket = new DatagramPacket(floorEventData, floorEventData.length);
        try {
            System.out.println("[" + name + "] Waiting for a floor event from Scheduler...");
            receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // process the received floor event
        String floorEvent = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println("[" + name + "] Received floor event " + floorEvent + " from Scheduler.");
        mainFloorEvent = HardwareDevice.stringToHardwareDevice(floorEvent);
        floorEvents.add(mainFloorEvent);

        // save the Scheduler's address and port to communicate with it later
        schedulerAddress = receivePacket.getAddress();
        schedulerPort = receivePacket.getPort();

        sendPacketToScheduler(("ACK " + mainFloorEvent).getBytes()); // send an acknowledgment packet to the Scheduler
    }

    /**
     * Receives an Elevator event from the Scheduler and executes it. Sends a message back to the Scheduler once it is
     * done executing the event.
     */
    @Override
    public void run() {
        // run the Elevator thread while it still has requests to handle
        while (scheduler.getNumReqsHandled() <= scheduler.getNumReqs()) {
            handleRequest(mainFloorEvent); // in WaitingForElevatorRequest, gets a floor event from the Scheduler

            // move the elevator car to the floor the request was made on
            if (currentFloor != mainFloorEvent.getFloor()) {
                // TODO: need to display states when moving between floors here
                System.out.println("[" + name + "] Currently moving to floor " + mainFloorEvent.getFloor() + ".");
                FloorButton move = (currentFloor < mainFloorEvent.getFloor()) ? FloorButton.UP : FloorButton.DOWN;
                moveBetweenFloors(mainFloorEvent.getFloor(), move);
            }

            handleRequest(mainFloorEvent); // transitions to DoorOpening
            handleRequest(mainFloorEvent); // transitions to DoorClosing
            handleRequest(mainFloorEvent); // transitions to MovingBetweenFloors
            handleRequest(mainFloorEvent); // transitions to ReachedDestination
            handleRequest(mainFloorEvent); // transitions to DoorOpening
            handleRequest(mainFloorEvent); // transitions to DoorClosing
            handleRequest(mainFloorEvent); // transitions to NotifyScheduler

            // TODO: find some way to add this into the NotifyScheduler handleRequest method
            // originally it was scheduler.checkElevatorStatus(hardwareDevice) then sendPacketToScheduler()
            // but this is redundant and scheduler should call checkElevatorStatus on itself
            sendPacketToScheduler(mainFloorEvent.toString().getBytes());

            handleRequest(mainFloorEvent); // transitions to WaitingForElevatorRequest

            // TODO: print out what is received from the scheduler
            System.out.println("[" + name + "] Received ACK from Scheduler.");
        }
        receiveSocket.close();
    }

    /**
     * Handles an event in the Elevator state machine with the specified HardwareDevice. Prints the current state.
     *
     * @param hardwareDevice A HardwareDevice representing the floor event the Elevator is executing.
     */
    private void handleRequest(HardwareDevice hardwareDevice) {
        currentState.handleRequest(this, hardwareDevice);
        System.out.print("[" + name + "] State: ");
        currentState.displayState();
    }

    /**
     * Returns a Scheduler representing the elevator scheduler to receive and send events to.
     *
     * @return A Scheduler representing the elevator scheduler to receive and send events to.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Sets the current state of the Elevator state machine.
     *
     * @param stateName A string representing the name of the state to set.
     */
    public void setState(String stateName) {
        currentState = states.get(stateName);
    }

    /**
     * Adds the given state to the Elevator state machine.
     *
     * @param name A String representing the name of the state.
     * @param elevatorState An ElevatorState to be added to the Elevator state machine.
     */
    public void addState(String name, ElevatorState elevatorState) {
        states.put(name, elevatorState);
    }

    /**
     * Returns the current state of the Elevator state machine.
     *
     * @return The current state of the Elevator state machine.
     */
    public ElevatorState getCurrentState() { return currentState; }

    /**
     * Returns a HashMap of states in the Elevator state machine.
     *
     * @return A HashMap of states in the Elevator state machine.
     */
    public HashMap<String, ElevatorState> getStates() {
        return states;
    }

    /**
     * Returns the current number of passengers in the Elevator.
     *
     * @return An integer representing the current number of passengers in the Elevator.
     */
    public int getNumPassengers() {
        return numPassengers;
    }

    /**
     * Increment the number of passengers by 1.
     */
    public void addPassenger() {
        numPassengers++;
    }

    /**
     * Decrement the number of passengers by 1.
     */
    public void removePassenger() {
        numPassengers--;
    }

    /**
     * Sends a DatagramPacket to the Scheduler containing the specified array of bytes.
     *
     * @param data An array of bytes representing the data to send.
     */
    public void sendPacketToScheduler(byte[] data) {
        try {
            System.out.println("[" + name + "] Sending " + new String(data, 0, data.length) + " to Scheduler.");
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, schedulerAddress, schedulerPort);
            DatagramSocket sendSocket = new DatagramSocket();
            sendSocket.send(sendPacket);
            sendSocket.close();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Returns an integer representing the floor number the Elevator is currently on.
     *
     * @return An integer representing the floor number the Elevator is currently on.
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    /**
     * Returns an integer representing the port number the Elevator receives DatagramPackets on.
     *
     * @return An integer representing the port number the Elevator receives DatagramPackets on.
     */
    public int getPort() {
        return port;
    }

    /**
     * Moves the Elevator between floors.
     *
     * @param floor An integer representing the floor the Elevator needs to move to.
     * @param button A FloorButton representing the direction the Elevator needs to move.
     */
    public void moveBetweenFloors(int floor, FloorButton button) {
        int delta = Math.abs(floor - currentFloor); // number of floors to move
        for (int i = 0; i < delta; i++) {
            try {
                Thread.sleep(9280);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (button == FloorButton.UP) {
                currentFloor++;
            } else {
                currentFloor--;
            }

            // TODO: need to notify the Scheduler subsystem of what hardwareDevice is being picked up...?
//            HardwareDevice hardwareDeviceToDelete = null;
            ArrayList<HardwareDevice> floorEvent = scheduler.getFloorEventsToHandle();
            for (HardwareDevice hardwareDevice : floorEvent) {
                if (hardwareDevice.getFloor() == currentFloor && hardwareDevice.getFloorButton() == button) {
                    floorEvents.add(hardwareDevice);
//                        hardwareDeviceToDelete = hardwareDevice;
                    System.out.println("[" + name + "] Picked up floor event " + hardwareDevice);
                }
            }
            System.out.println("[" + name + "] Currently at floor " + currentFloor);
        }
    }

    /**
     * Returns a String representing the name of the Elevator.
     *
     * @return A String representing the name of the Elevator.
     */
    public String getName() {
        return name;
    }

}

