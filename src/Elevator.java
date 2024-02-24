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

    private HashMap<String, ElevatorState> states;
    private ElevatorState currentState;

    /**
     * Initializes an Elevator with a Scheduler representing the elevator scheduler to receive and send events to.
     *
     * @param scheduler A Scheduler representing the elevator scheduler to receive and send events to.
     */
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
        states = new HashMap<>();
        addState("WaitingForElevatorRequest", new WaitingForElevatorRequestState());
        addState("MovingBetweenFloors", new MovingBetweenFloorsState());
        addState("ReachedDestination", new ReachedDestinationState());
        addState("DoorClosing", new DoorClosingState());
        addState("DoorOpening", new DoorOpeningState());
        addState("NotifyScheduler", new NotifySchedulerState());

        setState("WaitingForElevatorRequest");
    }

    /**
     * Prints a message describing which floor the Elevator is moving to, and if the Elevator is going up or down.
     *
     * @param hardwareDevice A HardwareDevice representing the current event.
     */
    public void printMovingMessage(HardwareDevice hardwareDevice) {
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
            HardwareDevice hardwareDevice = scheduler.getElevatorRequest();
            currentState.handleRequest(this,hardwareDevice);
            currentState.displayState(); // doors opening
            currentState.handleRequest(this,hardwareDevice);
            currentState.displayState(); // doors closing
            currentState.handleRequest(this,hardwareDevice);
            currentState.displayState(); // moving
//            printMovingMessage(hardwareDevice);
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            currentState.handleRequest(this,hardwareDevice);
            currentState.displayState(); // reached destination

//            hardwareDevice.setArrived();

            currentState.handleRequest(this,hardwareDevice);
            currentState.displayState(); // doors opening
            currentState.handleRequest(this,hardwareDevice);
            currentState.displayState(); // doors closing
            currentState.handleRequest(this,hardwareDevice);
            currentState.displayState(); // notify

            hardwareDevice.setArrived();
            scheduler.checkElevatorStatus(hardwareDevice);

            currentState.handleRequest(this,hardwareDevice);
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

    public void setState(String stateName){
        currentState = states.get(stateName);
    }

    public void addState(String name, ElevatorState state){
        states.put(name, state);
    }

}

