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
        // TODO: must update mainFloorEvent and floorEvents
        // mainFloorEvent = null;
        // remove mainFloorEvent from floorEvents
        // if floorEvents is not empty, set a floor event to be the mainFloorEvent and continue running
        // if floorEvents is empty, doors will open and the scheduler will be notified
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
        // TODO: this is redundant, find a way to have scheduler call checkElevatorStatus on itself
        context.getScheduler().checkElevatorStatus(request);
        context.sendPacketToScheduler(request.toString().getBytes());
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
        Scheduler scheduler = context.getScheduler();
        if (scheduler.getNumReqsHandled() <= scheduler.getNumReqs()) {
            context.getFloorEvent(); // get a floor event from the Scheduler

            // determine if the Elevator car is currently at the floor it was requested on or not
            if (context.getCurrentFloor() == context.getMainFloorEvent().getFloor()) {
                // Elevator car is currently on the floor it was requested on, open the doors
                context.setState("DoorsOpening");
            } else {
                // Elevator car is not currently on the floor it was requested on, move to the floor it was requested on
                context.setState("MovingBetweenFloors");
            }
        }
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
        // determine if the Elevator car is currently at the floor it was requested on or not
        int currentFloor = context.getCurrentFloor();
        if (currentFloor == request.getFloor()) {
            // Elevator car is currently on the floor it was requested on
            context.moveBetweenFloors(request.getCarButton(), request.getFloorButton());
            context.setState("ReachedDestination");
        } else {
            // Elevator car is not currently on the floor it was requested on
            FloorButton directionToMove = (currentFloor < request.getFloor()) ? FloorButton.UP : FloorButton.DOWN;
            context.moveBetweenFloors(request.getFloor(), directionToMove);
            context.setState("DoorsOpening");
        }
    }

    @Override
    public void displayState() {
        System.out.println("MovingBetweenFloors");
    }

}
