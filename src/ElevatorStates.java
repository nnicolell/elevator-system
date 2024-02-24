/**
 * This class represents a state in the Elevator state machine where the elevator doors are closing.
 */
class DoorClosingState implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        if (!request.getArrived()) {
            context.setState("MovingBetweenFloors");
        } else {
            context.setState("NotifyScheduler");
        }
    }

    @Override
    public void displayState() {
        System.out.println("[ElevatorState] Doors Closing");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator doors are opening.
 */
class DoorOpeningState implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorClosing");
    }

    @Override
    public void displayState() {
        System.out.println("[ElevatorState] Doors Opening");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator has reached the desired floor.
 */
class ReachedDestinationState implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorOpening");
    }

    @Override
    public void displayState() {
        System.out.println("[ElevatorState] Reached Destination");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator has notified the scheduler that it has
 * reached the desired floor.
 */
class NotifySchedulerState implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("WaitingForElevatorRequest");
    }

    @Override
    public void displayState() {
        System.out.println("[ElevatorState] Notifying Scheduler");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator is waiting for a floor request from
 * the scheduler.
 */
class WaitingForElevatorRequestState implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorOpening");
    }

    @Override
    public void displayState() {
        System.out.println("[ElevatorState] Waiting For Elevator Request");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator is moving from one floor to another.
 */
class MovingBetweenFloorsState implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("ReachedDestination");
    }

    @Override
    public void displayState() {
        System.out.println("[ElevatorState] Elevator is Moving Between Floors");
    }

}
