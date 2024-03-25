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
     * A HashMap of states in the Elevator state machine.
     */
    private final HashMap<String, ElevatorState> states;

    /**
     * The current state of the Elevator state machine.
     */
    private ElevatorState currentState;

    /**
     * An integer representing the number of passengers currently in the Elevator car.
     */
    private int numPassengers = 0;

    /**
     * A DatagramSocket to receive DatagramPackets.
     */
    private DatagramSocket receiveSocket = null;

    /**
     * An integer representing the current floor the Elevator is at.
     */
    private int currentFloor = 1;

    /**
     * An integer representing the port number to receive DatagramPackets on.
     */
    private final int port;

    /**
     * A String representing the name of the Elevator.
     */
    private final String name;

    /**
     * An ArrayList of HardwareDevices representing a list of floor events to complete.
     */
    private final ArrayList<HardwareDevice> floorEvents;

    /**
     * Initializes an Elevator.
     *
     * @param scheduler A Scheduler representing the elevator scheduler to receive and send events to.
     * @param port An integer representing the port number to receive DatagramPackets on.
     * @param name A String representing the name of the Elevator.
     */
    public Elevator(Scheduler scheduler, int port, String name) {
        this.scheduler = scheduler;
        this.floorEvents = new ArrayList<>();
        this.port = port;
        this.name = name;

        states = new HashMap<>();
        addState("WaitingForElevatorRequest", new WaitingForElevatorRequestState());
        addState("MovingBetweenFloors", new MovingBetweenFloorsState());
        addState("ReachedDestination", new ReachedDestinationState());
        addState("DoorClosing", new DoorClosingState());
        addState("DoorOpening", new DoorOpeningState());
        addState("NotifyScheduler", new NotifySchedulerState());
        setState("WaitingForElevatorRequest");

        try {
            receiveSocket = new DatagramSocket(port);
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }
    }

    /**
     * Receives an Elevator event from the Scheduler and executes it. Sends a message back to the Scheduler once it is
     * done executing the event.
     */
    @Override
    public void run() {
        while (scheduler.getNumReqsHandled() <= scheduler.getNumReqs()) {
            // receive the first elevator request
            byte[] data = new byte[150];
            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            try {
                System.out.println(name + " waiting...");
                receiveSocket.receive(receivePacket);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }

            System.out.println("[" + name + "] Received " + new String(receivePacket.getData(), 0, receivePacket.getLength()) + " from Scheduler.");
            HardwareDevice hardwareDevice = HardwareDevice.stringToHardwareDevice(new String(receivePacket.getData(),0,receivePacket.getLength()));
            floorEvents.add(hardwareDevice);
            sendPacket(("ACK " + hardwareDevice).getBytes(), receivePacket.getPort()); // send ACK to scheduler

            if (currentFloor != hardwareDevice.getFloor()) {
                String printString = String.format("[" + name + "] %s currently moving to floor %d.", this.name, hardwareDevice.getFloor());
                System.out.println(printString);
                FloorButton move = (currentFloor < hardwareDevice.getFloor()) ? FloorButton.UP : FloorButton.DOWN;
                moveBetweenFloors(hardwareDevice.getFloor(), move);
            }
            // need to change this
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors opening
            try {
                Thread.sleep(7030); // load time including doors opening and closing
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors closing

            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // moving

            moveBetweenFloors(hardwareDevice.getCarButton(), hardwareDevice.getFloorButton());

            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // reached destination

            hardwareDevice.setArrived();

            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors opening

            try {
                Thread.sleep(7680); // load time including doors opening and closing
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors closing
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // notify

            // send packet back
            sendPacket(hardwareDevice.toString().getBytes(), receivePacket.getPort());
            currentState.handleRequest(this, hardwareDevice);

            scheduler.checkElevatorStatus(hardwareDevice);

            System.out.println("[" + name + "] Received ACK from Scheduler.");
        }

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
     * Sends a DatagramPacket containing the specified array of bytes to the specified port number.
     *
     * @param dataSend An array of bytes representing the data to send.
     * @param receivePort An integer representing the port number to send the data to.
     */
    public void sendPacket(byte[] dataSend, int receivePort) {
        // create packet to send
        DatagramPacket sendPacket = null;
        try {
            sendPacket = new DatagramPacket(dataSend, dataSend.length, InetAddress.getLocalHost(), receivePort);
        } catch (UnknownHostException e) {
            System.err.println(e);
            System.exit(1);
        }

        try { // send the packet
            DatagramSocket sendSocket = new DatagramSocket();
            sendSocket.send(sendPacket);
        } catch (IOException se) {
            System.err.println(se);
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

//            HardwareDevice hardwareDeviceToDelete = null;
            ArrayList<HardwareDevice> floorEvent = scheduler.getFloorEventsToHandle();
            synchronized (floorEvent) {
                for (HardwareDevice hardwareDevice : floorEvent) {
                    if (hardwareDevice.getFloor() == currentFloor && hardwareDevice.getFloorButton() == button) {
                        floorEvents.add(hardwareDevice);
//                        hardwareDeviceToDelete = hardwareDevice;
                        System.out.println("[" + name + "] Picked up floor event " + hardwareDevice);
                    }
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

