import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Thread.sleep;

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
     * The current number of passengers in the elevator car
     */
    private int numPassengers;

    /**
     * Socket on which to send and receive
     */
    private DatagramSocket receiveSocket = null;

    /**
     * Current floor that the elevator is at
     */
    private int currentFloor;

    /**
     * Port to receive on
     */
    private int port;

    /**
     * Initializes an Elevator with a Scheduler representing the elevator scheduler to receive and send events to.
     *
     * @param scheduler A Scheduler representing the elevator scheduler to receive and send events to.
     */
    public Elevator(Scheduler scheduler, int port) {
        this.scheduler = scheduler;
        this.port = port;
        states = new HashMap<>();
        addState("WaitingForElevatorRequest", new WaitingForElevatorRequestState());
        addState("MovingBetweenFloors", new MovingBetweenFloorsState());
        addState("ReachedDestination", new ReachedDestinationState());
        addState("DoorClosing", new DoorClosingState());
        addState("DoorOpening", new DoorOpeningState());
        addState("NotifyScheduler", new NotifySchedulerState());
        setState("WaitingForElevatorRequest");

        numPassengers = 0;
        currentFloor = 1;

        try {
            receiveSocket = new DatagramSocket(port);
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }

    }

    /**
     * Prints a message describing which floor the Elevator is moving to, and if the Elevator is going up or down.
     *
     * @param hardwareDevice A HardwareDevice representing the current event.
     */
    private void printMovingMessage(HardwareDevice hardwareDevice) {
        System.out.println("[" + Thread.currentThread().getName() + "] Elevator Car currently moving "
                + hardwareDevice.getFloorButton() + " to floor " + hardwareDevice.getCarButton() + "...");
    }

    /**
     * Receives an Elevator event from the Scheduler and executes it. Sends a message back to the Scheduler once it is
     * done executing the event.
     */
    @Override
    public void run() {
        while (scheduler.getNumReqsHandled() <= scheduler.getNumReqs()) {
            //receive the first elevator request
            byte[] data = new byte[100];
            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            try {
                System.out.println("Waiting...");
                receiveSocket.receive(receivePacket);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }
            //need to change this
            HardwareDevice hardwareDevice = scheduler.getElevatorRequest();
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors opening
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors closing
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // moving
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // reached destination


            //do i need to add socket stuff here?
            hardwareDevice.setArrived();

            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors opening
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors closing
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // notify

            //send packet back
            sendPacket("hardwareDevice".getBytes(), receivePacket.getPort());

            scheduler.checkElevatorStatus(hardwareDevice);

            currentState.handleRequest(this, hardwareDevice);
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
     * Get the current number of passengers in the elevator
     * @return current number of passengers in the elevator
     */
    public int getNumPassengers() {
        return numPassengers;
    }

    /**
     * Increment the number of passengers by 1
     */
    public void addPassenger() {
        numPassengers++;
    }

    /**
     * Decrement the number of passengers by 1
     */
    public void removePassenger() {
        numPassengers--;
    }

    public void sendPacket(byte[] dataSend, int receivePort) {
        //create packet to send
        DatagramPacket sendPacket = null;
        try {
            sendPacket = new DatagramPacket(dataSend, dataSend.length, InetAddress.getLocalHost(), receivePort);
        } catch (UnknownHostException e) {
            System.err.println(e);
            System.exit(1);
        }

        System.out.println("Server sending to Host:");
        System.out.println(new String(sendPacket.getData(),0, sendPacket.getLength()));
        byte[] receivePacketShortened = new byte[sendPacket.getLength()];
        System.arraycopy(sendPacket.getData(), 0, receivePacketShortened, 0, sendPacket.getLength());
        System.out.println(Arrays.toString(receivePacketShortened));

        DatagramSocket sendSocket = null;
        try {
            sendSocket = new DatagramSocket();
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }

        //send request
        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getPort() {
        return port;
    }


}

