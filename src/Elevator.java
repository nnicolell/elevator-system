import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
     * True, if request should be handled when state is being set. False, if not.
     */
    private boolean handleRequestInSetState = true;

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
        addState("DoorsNotOpening", new DoorsNotOpening());
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
        if (handleRequestInSetState) {
            currentState.handleRequest(this, mainFloorEvent);
        }
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
     * @param fault True, if a fault should occur. False, if not.
     * @param state A String representing the state the Elevator state machine should transition to after the elevator
     *              car has finished moving between floors.
     * @param floor An integer representing the floor the Elevator needs to move to.
     * @param button A FloorButton representing the direction the Elevator needs to move.
     */
    public void moveBetweenFloors(boolean fault, String state, int floor, FloorButton button) {
        int delta = Math.abs(floor - currentFloor); // number of floors to move
        System.out.println("[" + name + "] Currently at floor " + currentFloor + ", moving to floor " + floor + "...");
        for (int i = 0; i < delta; i++) {
            // handles the case where an ELEVATOR_STUCK fault occurs
            Timer faultTimer = new Timer();
            Timer timer = new Timer();
            AtomicInteger finished = new AtomicInteger(0);

            if (fault) {
                faultTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finished.set(1);
                        timer.cancel();
                        System.out.println("[" + name + "] Stuck between floors. Shutting down...");
                        currentState = null; // shut down the elevator
                        scheduler.killElevatorThread(name);
                    }
                }, 11000); // assume a fault if elevator doesn't arrive within 11 seconds
            } else {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finished.set(2);
                        faultTimer.cancel();
                    }
                }, 9280); // time it takes to move from one floor to the next
            }

            // check if timer has finished and cancel the faultTimer
            Timer finishTimer = new Timer();
            finishTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (finished.get() == 2) {
                        faultTimer.cancel();
                    }
                }
            }, 9280);

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

        if (!fault) { // transition to the next state if a fault does not occur,f
            setState(state);
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
     * Sets a timer to handle a fault in the case where a door does not open or close.
     *
     * @param fault True, if a fault should occur. False, if not.
     * @param faultState A String representing the state the Elevator state machine should transition to if a fault
     *                   occurs.
     * @param normalState A String representing the state the Elevator state machine should transition to if a fault
     *                    does not occur.
     */
    public void openOrCloseDoors(boolean fault, String faultState, String normalState) {
        Timer faultTimer = new Timer();
        Timer timer = new Timer();
        AtomicInteger finished = new AtomicInteger(0);

        if (fault) {
            faultTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    setState(faultState); // assume a fault if doors don't open/close within 7.8 seconds
                    finished.set(1);
                    timer.cancel();
                }
            }, 7800);
        } else {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    setState(normalState);
                    finished.set(2);
                    faultTimer.cancel();
                }
            }, 7680);
        }

        // check which timer finished first and cancel the other timer
        Timer finishTimer = new Timer();
        finishTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (finished.get() == 1) { // faultTimer finished first, cancel timer
                    timer.cancel();
                } else if (finished.get() == 2) { // timer finished first, cancel faultTimer
                    faultTimer.cancel();
                }
            }
        }, 7800);
    }

    /**
     * Forces the Elevator car doors to close.
     */
    public void forceOpenOrCloseDoors(boolean forceOpen) {
        try {
            System.out.println("[" + name + "] Forcing doors " + (forceOpen ? "open" : "close") + "...");
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

    /**
     * Sets a HardwareDevice to the mainFloorEvent.
     */
    public void setMainFloorEvent(HardwareDevice hardwareDevice) {
        mainFloorEvent = hardwareDevice;
    }

    /**
     * Sets handleRequestInSetState to the specified boolean.
     *
     * @param handleRequestInSetState True, if request should be handled when state is being set. False, if not.
     */
    public void setHandleRequestInSetState(boolean handleRequestInSetState) {
        this.handleRequestInSetState = handleRequestInSetState;
    }

    /**
     * Returns true, if request should be handled when state is being set. False, if not.
     *
     * @return True, if request should be handled when state is being set. False, if not.
     */
    public boolean getHandleRequestInSetState() {
        return handleRequestInSetState;
    }
}
