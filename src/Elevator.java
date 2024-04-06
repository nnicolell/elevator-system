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
     * An ElevatorSystemLogger to log events.
     */
    private final ElevatorSystemLogger logger;

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

        logger = new ElevatorSystemLogger(name);

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
        logger.info("State: " + currentState.displayState());
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
    private void sendPacketToScheduler(byte[] data) {
        try {
            logger.info("Sending " + new String(data, 0, data.length) + " to Scheduler.");
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
     * Receives a DatagramPacket from the Scheduler and returns a String representing the contents of the
     * DatagramPacket.
     *
     * @return A String representing the contents of the DatagramPacket received from the Scheduler.
     */
    private String receivePacketFromScheduler() {
        // receive a DatagramPacket from the Scheduler
        byte[] receiveData = new byte[200];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // save the Scheduler's address and port to communicate with it later
        schedulerAddress = receivePacket.getAddress();
        schedulerPort = receivePacket.getPort();

        // return the received message from the Scheduler
        return new String(receivePacket.getData(), 0, receivePacket.getLength());
    }

    /**
     * Receives a floor event from the Scheduler and processes it. Sends an acknowledgment message back to the
     * Scheduler.
     */
    public void getFloorEvent() {
        // receive a floor event from the Scheduler
        logger.info("Waiting for a floor event from Scheduler...");
        String floorEvent = receivePacketFromScheduler();
        logger.info("Received " + floorEvent + " from Scheduler.");

        // the floor event received from the Scheduler is the main floor event
        mainFloorEvent = HardwareDevice.stringToHardwareDevice(floorEvent);
        floorEvents.add(mainFloorEvent);

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
        logger.info("Currently at floor " + currentFloor + ", moving to floor " + floor + "...");
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
                        logger.severe("Stuck between floors. Shutting down...");
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

            ArrayList<HardwareDevice> floorEvent = scheduler.getFloorEventsToHandle();
            for (HardwareDevice hardwareDevice : floorEvent) {
                if (hardwareDevice.getFloor() == currentFloor && hardwareDevice.getFloorButton() == button) {
                    floorEvents.add(hardwareDevice);
                    logger.info("Picked up floor event " + hardwareDevice);

                    // FIXME: non-UDP way of implementing this...
                    // notify the Scheduler that we have picked up a floor event
                    scheduler.pickedUpFloorEvent(this, hardwareDevice);
                }
            }

            logger.info((currentFloor == floor? "Arrived" : "Currently") + " at floor " + currentFloor + ".");
        }

        if (!fault) { // transition to the next state if a fault does not occur
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
        // main floor event has been fulfilled
        HardwareDevice fulfilledFloorEvent = mainFloorEvent;
        floorEvents.remove(mainFloorEvent);
        mainFloorEvent = null;

        // determine if the Elevator has picked up passengers on its way to its main destination
        boolean moreEventsToFulfill = false;
        // if its has picked up passengers, it must continue executing the rest of the floor events
        if (floorEvents.size() > 0) {
            moreEventsToFulfill = true;
            // TODO: the main floor event that gets assigned should be the closest one to the currentFloor
            mainFloorEvent = floorEvents.get(0); // assign a new main floor event
        }

        // update the fulfilled floor event to allow the Scheduler to know if there's more floor events to be completed
        // or not
        fulfilledFloorEvent.setMoreFloorEvents(moreEventsToFulfill);

        // notify the Scheduler subsystem that the main floor event has been completed
        sendPacketToScheduler(fulfilledFloorEvent.toString().getBytes());

        // receive an acknowledgment from the Scheduler
        String acknowledgment = receivePacketFromScheduler();
        logger.info("Received " + acknowledgment + " from Scheduler.");

//        // mainFloorEvent has been fulfilled
//        floorEvents.remove(mainFloorEvent);
//        mainFloorEvent = null;
//
//        // the Elevator has picked up passengers on its way to its main destination, must continue executing the rest of
//        // the floor events
//        if (floorEvents.size() > 0) {
//            mainFloorEvent = floorEvents.get(0);
//            return true; // the Elevator has more floor events to execute
//        }
//
//        return false; // the Elevator currently has no more floor events to execute

        return moreEventsToFulfill;
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
            logger.warning("Forcing doors " + (forceOpen ? "open" : "closed") + "...");
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
