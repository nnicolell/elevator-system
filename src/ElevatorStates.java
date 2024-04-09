/**
 * This class represents a state in the Elevator state machine where the elevator is waiting for a floor request from
 * the scheduler.
 */
class WaitingForElevatorRequest implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
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
    public String displayState() {
        return "WaitingForElevatorRequest";
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator doors are opening.
 */
class DoorsOpening implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        boolean fault = mainFloorEvent.getFault().toString().equals("Doors not opening");
        // if there's a fault transition to DoorsNotOpening, if not transition to DoorsClosing
        context.openOrCloseDoors(fault, "DoorsNotOpening", "DoorsClosing");
    }

    @Override
    public String displayState() {
        return "DoorsOpening";
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator doors are not opening properly.
 */
class DoorsNotOpening implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        context.forceOpenOrCloseDoors(true); // force open the elevator car doors
        context.setState("DoorsClosing");
    }

    @Override
    public String displayState() {
        return "DoorsNotOpening";
    }
}

/**
 * This class represents a state in the Elevator state machine where the elevator doors are closing.
 */
class DoorsClosing implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        boolean fault = mainFloorEvent.getFault().toString().equals("Doors not closing");
        // if there's a fault transition to DoorsNotClosing, if not transition to MovingBetweenFloors or NotifyScheduler
        // depending on if the elevator has arrived at mainFloorEvent.carButton floor
        context.openOrCloseDoors(fault, "DoorsNotClosing",
                !mainFloorEvent.getArrived() ? "MovingBetweenFloors" : "NotifyScheduler");
    }

    @Override
    public String displayState() {
        return "DoorsClosing";
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator doors are not closing properly.
 */
class DoorsNotClosing implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        context.forceOpenOrCloseDoors(false); // force close the elevator car doors
        if (!mainFloorEvent.getArrived()) {
            // has not arrived at the carButton floor, continue travelling to the carButton floor
            context.setState("MovingBetweenFloors");
        } else {
            // has arrived at the carButton floor, notify Scheduler that the elevator has fulfilled mainFloorEvent
            context.setState("NotifyScheduler");
        }
    }

    @Override
    public String displayState() {
        return "DoorsNotClosing";
    }
}

/**
 * This class represents a state in the Elevator state machine where the elevator has reached the desired floor.
 */
class ReachedDestination implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        mainFloorEvent.setArrived();
        if (context.getFloorEventsSize() > 1) {
            // the Elevator has picked up passengers on its way to its initial destination, must notify the Scheduler
            // that we have dropped the initial passenger off before executing the other floor events
            context.getView().updateElevator(context);
            context.setState("NotifyScheduler");
        } else {
            context.setState("DoorsOpening");
        }
    }

    @Override
    public String displayState() {
        return "ReachedDestination";
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator has notified the scheduler that it has
 * reached the desired floor.
 */
class NotifyScheduler implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        // notify the Scheduler that the request has been completed and check if the Elevator has picked up passengers
        // on its way to its initial destination
        if (context.moreFloorEventsToFulfill()) {
            context.setState("MovingBetweenFloors"); // continue executing the rest of the floor events
        } else {
            context.setState("WaitingForElevatorRequest"); // wait for a new floor event from Scheduler
        }
    }

    @Override
    public String displayState() {
        return "NotifyingScheduler";
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator is moving from one floor to another.
 */
class MovingBetweenFloors implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        // determine if a fault should occur or not
        boolean fault = mainFloorEvent.getFault().toString().equals("Elevator stuck between floors");

        // determine if the Elevator car is currently at the floor it was requested on or not
        int currentFloor = context.getCurrentFloor();
        if (currentFloor == mainFloorEvent.getFloor()) {
            // Elevator car is currently on the floor it was requested on
            context.moveBetweenFloors(fault, "ReachedDestination", mainFloorEvent.getCarButton(),
                    mainFloorEvent.getFloorButton());
        } else {
            // Elevator car is not currently on the floor it was requested on
            FloorButton directionToMove = (currentFloor < mainFloorEvent.getFloor())
                    ? FloorButton.UP : FloorButton.DOWN;
            context.getView().updateFloor(context);
            context.moveBetweenFloors(fault, "DoorsOpening", mainFloorEvent.getFloor(), directionToMove);
        }
    }

    @Override
    public String displayState() {
        return "MovingBetweenFloors";
    }

}
