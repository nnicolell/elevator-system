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

    /**
     * Initializes a Scheduler.
     */
    public Scheduler() {
        floorQueue = new ArrayDeque<>();
        currentFloorEvent = null;
        numReqsHandled = 1;
        numReqs = 10000;

        states = new HashMap<>();
        addState("NotifyElevator", new NotifyElevatorState());
        addState("WaitingForFloorEvent", new WaitingForFloorEventState());
        addState("NotifyFloor", new NotifyFloorState());
        setState("WaitingForFloorEvent");
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
        while ((floorQueue.isEmpty() || currentFloorEvent != null)
                && (numReqsHandled <= numReqs || currentFloorEvent == null)) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        currentFloorEvent = floorQueue.poll();
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
        floorQueue.add(hardwareDevice);
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

}
