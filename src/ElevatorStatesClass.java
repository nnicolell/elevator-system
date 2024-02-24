class DoorClosingState implements ElevatorState{

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        if (!request.getArrived()){
            context.setState("MovingBetweenFloors");
        }
        else{
            context.setState("NotifyScheduler");
        }
    }

    @Override
    public void displayState() {
        System.out.println("State: Doors Closing");
    }
}

class DoorOpeningState implements ElevatorState{

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorClosing");
    }
    @Override
    public void displayState() {
        System.out.println("State: Doors Opening");
    }
}

/**
 * This class represents the elevator once it has reached the desired floow
 */
class ReachedDestinationState implements ElevatorState{
    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorOpening");
    }

    @Override
    public void displayState() {
        System.out.println("Elevator has Reached Destination");
    }
}

class NotifySchedulerState implements ElevatorState{
    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("WaitingForElevatorRequest");
    }

    @Override
    public void displayState() {
        System.out.println("State: Notifying Scheduler");
    }
}

/**
 * This class represents the Elevator while it is waiting for a request
 */
class WaitingForElevatorRequestState implements ElevatorState{
    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorOpening");
    }

    @Override
    public void displayState() {
        System.out.println("State: Waiting For Elevator Request");
    }

}

/**
 * This class represents the elevator as it is moving from one floor to another
 */
class MovingBetweenFloorsState implements ElevatorState{
    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.printMovingMessage(request);
        context.setState("ReachedDestination");
    }

    @Override
    public void displayState() {
        System.out.println("State: Elevator is Moving Between Floors");
    }
}
