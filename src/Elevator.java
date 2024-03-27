import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * An Elevator to represent an elevator car moving up or down floors.
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

    /**
     * A HardwareDevice representing the floor event the Scheduler assigned to the Elevator.
     *
     * It does not represent the floor events that were picked up while executing the floor event the Scheduler assigned
     * to the Elevator.
     */
    private HardwareDevice mainFloorEvent = null;

    /**
     * The current state of the Elevator state machine.
     */
    private ElevatorState currentState;

    /**
     * A DatagramSocket to receive DatagramPackets from the Scheduler.
     */
    private DatagramSocket receiveSocket;

    /**
     * An InetAddress representing the address to send DatagramPackets to the Scheduler.
     */
    private InetAddress schedulerAddress;

    /**
     * An integer representing the port number to send DatagramPackets to the Scheduler.
     *
     * If 0, the Elevator has not received a DatagramPacket from the Scheduler yet.
     */
    private int schedulerPort = 0;

    /**
     * An integer representing the current floor the Elevator is at.
     */
    private int currentFloor = 1;

    /**
     * An integer representing the number of passengers currently in the Elevator car.
     */
    private int numPassengers = 0; // TODO: implement numPassengers

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

        try {
            receiveSocket = new DatagramSocket(port);
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }

        states = new HashMap<>(); // initialize the Elevator state machine
        addState("WaitingForElevatorRequest", new WaitingForElevatorRequest());
        addState("MovingBetweenFloors", new MovingBetweenFloors());
        addState("ReachedDestination", new ReachedDestination());
        addState("DoorsClosing", new DoorsClosing());
        addState("DoorsNotClosing", new DoorsNotClosing());
        addState("DoorsOpening", new DoorsOpening());
        addState("NotifyScheduler", new NotifyScheduler());
    }

    /**
     * Sets and displays the current state of the Elevator state machine. Executes the actions of the current state.
     *
     * @param stateName A string representing the name of the state to set.
     */
    public void setState(String stateName) {
        currentState = states.get(stateName);
        System.out.print("[" + name + "] State: ");
        currentState.displayState();
        currentState.handleRequest(this, mainFloorEvent);
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
     * Sets the state of the Elevator state machine to WaitingForElevatorRequest.
     */
    @Override
    public void run() {
        setState("WaitingForElevatorRequest");
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
     * Moves the Elevator between floors.
     *
     * @param floor An integer representing the floor the Elevator needs to move to.
     * @param button A FloorButton representing the direction the Elevator needs to move.
     */
    public void moveBetweenFloors(int floor, FloorButton button) {
        int delta = Math.abs(floor - currentFloor); // number of floors to move
        System.out.println("[" + name + "] Currently at floor " + currentFloor + ", moving to floor " + floor + "...");
        for (int i = 0; i < delta; i++) {
            try {
                Thread.sleep(9280); // time it takes to move between a floor
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
     * Notifies the Scheduler that the mainFloorEvent has been fulfilled. Determines if there are more floorEvents to be
     * fulfilled in the current run.
     *
     * @return True, if there are more floor events to be fulfilled in the current run. False, if not.
     */
    public boolean moreFloorEventsToFulfill() {
        // TODO: this is redundant, find a way to have scheduler call checkElevatorStatus on itself
        scheduler.checkElevatorStatus(mainFloorEvent);
        sendPacketToScheduler(mainFloorEvent.toString().getBytes());
        // TODO: must receive an acknowledgment from Scheduler

        // mainFloorEvent has been fulfilled
        floorEvents.remove(mainFloorEvent);
        mainFloorEvent = null;

        // the Elevator has picked up passengers on its way to its main destination, must continue executing the rest of
        // the floor events
        if (floorEvents.size() > 0) {
            mainFloorEvent = floorEvents.get(0);
            return true; // the Elevator has more floor events to execute
        }

        return false; // the Elevator currently has no more floor events to execute
    }

    /**
     * Forces the Elevator car doors to close.
     */
    public void forceCloseDoors() {
        try {
            System.out.println("[" + name + "] Forcing doors closed...");
            Thread.sleep(7680); // load time including doors opening and closing
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Increment the number of passengers in the Elevator car by 1.
     */
    public void addPassenger() {
        numPassengers++;
    }

    /**
     * Decrement the number of passengers in the Elevator car by 1.
     */
    public void removePassenger() {
        numPassengers--;
    }

    /**
     * Returns an integer representing the number of floor events the Elevator has to fulfill.
     *
     * @return An integer representing the number of floor events the Elevator has to fulfill.
     */
    public int getFloorEventsSize() {
        return floorEvents.size();
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
     * Returns a Scheduler representing the Scheduler to receive and send events to.
     *
     * @return A Scheduler representing the Scheduler to receive and send events to.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Returns the current number of passengers in the Elevator car.
     *
     * @return An integer representing the current number of passengers in the Elevator car.
     */
    public int getNumPassengers() {
        return numPassengers;
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
     * Returns a String representing the name of the Elevator.
     *
     * @return A String representing the name of the Elevator.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a HardwareDevice representing the floor event the Scheduler assigned to the Elevator.
     *
     * @return A HardwareDevice representing the floor event the Scheduler assigned to the Elevator.
     */
    public HardwareDevice getMainFloorEvent() { return mainFloorEvent; }

}
