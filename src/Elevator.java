import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

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
     * Name of the Elevator
     */
    private String name;

    /**
     * List of events to do
     */
    private ArrayList<HardwareDevice> listEvents;


    /**
     * Initializes an Elevator with a Scheduler representing the elevator scheduler to receive and send events to.
     *
     * @param scheduler A Scheduler representing the elevator scheduler to receive and send events to.
     */
    public Elevator(Scheduler scheduler, int port, String name) {
        this.scheduler = scheduler;
        this.listEvents = new ArrayList<>();
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
            byte[] data = new byte[150];
            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            try {
                System.out.println(name + " waiting...");
                receiveSocket.receive(receivePacket);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }

            System.out.println("[Elevator] " + name + " received " + new String(receivePacket.getData(), 0, receivePacket.getLength()) + " from Scheduler.");
            HardwareDevice hardwareDevice = HardwareDevice.stringToHardwareDevice(new String(receivePacket.getData(),0,receivePacket.getLength()));
            listEvents.add(hardwareDevice);
            //send ack to scheduler
            sendPacket(("ACK " + hardwareDevice).getBytes(), receivePacket.getPort());

            if (currentFloor != hardwareDevice.getFloor()) {
                String printString = String.format("[Elevator] %s currently moving to floor %d.", this.name, hardwareDevice.getFloor());
                System.out.println(printString);
                FloorButton move = (currentFloor < hardwareDevice.getFloor()) ? FloorButton.UP : FloorButton.DOWN;
                moveBetweenFloors(hardwareDevice.getFloor(), move);
            }
            //need to change this
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors opening
            try {
                Thread.sleep(7030); //load time including doors opening and closing
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
                Thread.sleep(7680); //load time including doors opening and closing
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // doors closing
            currentState.handleRequest(this, hardwareDevice);
            currentState.displayState(); // notify

            //send packet back
            sendPacket(hardwareDevice.toString().getBytes(), receivePacket.getPort());
            currentState.handleRequest(this, hardwareDevice);

            scheduler.checkElevatorStatus(hardwareDevice);

            System.out.println("[Elevator] Received ack from Scheduler.");


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

    public void moveBetweenFloors(int floor, FloorButton button) {
        int delta = Math.abs(floor - currentFloor); //number of floors to move
        for (int i = 0; i < delta; i++) {
            try {
//                Thread.sleep(9280); //move between floors
                Thread.sleep(9280);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (button == FloorButton.UP) {
                currentFloor++;
            } else {
                currentFloor--;
            }

            HardwareDevice hardwareDeviceToDelete = null;
            ArrayList<HardwareDevice> floorEvent = scheduler.getFloorEventsToHandle();
            synchronized (floorEvent) {
                Iterator<HardwareDevice> iterator = floorEvent.iterator();
                while (iterator.hasNext()) {
                    HardwareDevice hardwareDevice = iterator.next();
                    if (hardwareDevice.getFloor() == currentFloor && hardwareDevice.getFloorButton() == button) {
                        listEvents.add(hardwareDevice);
//                        hardwareDeviceToDelete = hardwareDevice;
                        System.out.println("[Elevator] Picked up floor event " + hardwareDevice);
                    }
                }
            }
            System.out.println(getName() + " Currently at floor " + currentFloor);
        }
    }

    public String getName(){
        return name;
    }


}

