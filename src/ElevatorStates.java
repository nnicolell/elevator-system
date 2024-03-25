/**
 * This class represents a state in the Elevator state machine where the elevator doors are closing.
 */
class DoorsClosing implements ElevatorState {

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
        System.out.println("DoorsClosing");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator doors are opening.
 */
class DoorsOpening implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        try {
            Thread.sleep(7680); // load time including doors opening and closing
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        context.setState("DoorsClosing");
    }

    @Override
    public void displayState() {
        System.out.println("DoorsOpening");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator has reached the desired floor.
 */
class ReachedDestination implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        request.setArrived();
        context.setState("DoorsOpening");
    }

    @Override
    public void displayState() {
        System.out.println("ReachedDestination");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator has notified the scheduler that it has
 * reached the desired floor.
 */
class NotifyScheduler implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.getScheduler().checkElevatorStatus(request);
        context.setState("WaitingForElevatorRequest");
    }

    @Override
    public void displayState() {
        System.out.println("NotifyingScheduler");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator is waiting for a floor request from
 * the scheduler.
 */
class WaitingForElevatorRequest implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorsOpening");
    }

    @Override
    public void displayState() {
        System.out.println("WaitingForElevatorRequest");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator is moving from one floor to another.
 */
class MovingBetweenFloors implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.moveBetweenFloors(request.getCarButton(), request.getFloorButton());
        context.setState("ReachedDestination");
    }

    @Override
    public void displayState() {
        System.out.println("MovingBetweenFloors");
    }

}
