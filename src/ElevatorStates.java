import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void displayState() {
        System.out.println("WaitingForElevatorRequest");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator doors are opening.
 */
class DoorsOpening implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        Timer faultTimer = new Timer();
        Timer timer = new Timer();
        AtomicInteger finished = new AtomicInteger(0);

        int sleepValue;
        if (mainFloorEvent.getFault().toString().equals("Door not closing")) {
            sleepValue = 8000;
            faultTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    context.setState("DoorsNotClosing"); // Assume a fault if doors don't close within 7.8 seconds
                    finished.set(1);
                    timer.cancel();
                }
            }, 7800);
        } else {
            sleepValue = 7680;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    context.setState("DoorsClosing");
                    finished.set(2);
                    faultTimer.cancel();
                }
            }, sleepValue);
        }

        // Check which timer finished first and cancel the other timer
        Timer finishTimer = new Timer();
        finishTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (finished.get() == 0) {
                    // Neither timer finished, do nothing
                } else if (finished.get() == 1) {
                    // faultTimer finished first, cancel timer
                    timer.cancel();
                } else if (finished.get() == 2) {
                    // timer finished first, cancel faultTimer
                    faultTimer.cancel();
                }
            }
        }, 7800);
    }
        public void displayState() {
        System.out.println("DoorsOpening");
    }

}

class DoorsNotClosing implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        context.forceCloseDoors();
        context.setState("DoorsClosing");
    }

    @Override
    public void displayState() {
        System.out.println("DoorsNotClosing");
    }
}

/**
 * This class represents a state in the Elevator state machine where the elevator doors are closing.
 */
class DoorsClosing implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        if (!mainFloorEvent.getArrived()) {
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
 * This class represents a state in the Elevator state machine where the elevator has reached the desired floor.
 */
class ReachedDestination implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        mainFloorEvent.setArrived();
        if (context.getFloorEventsSize() > 1) {
            // the Elevator has picked up passengers on its way to its initial destination, must notify the Scheduler
            // that we have dropped the initial passenger off before executing the other floor events
            context.setState("NotifyScheduler");
        } else {
            context.setState("DoorsOpening");
        }
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
    public void displayState() {
        System.out.println("NotifyingScheduler");
    }

}

/**
 * This class represents a state in the Elevator state machine where the elevator is moving from one floor to another.
 */
class MovingBetweenFloors implements ElevatorState {

    @Override
    public void handleRequest(Elevator context, HardwareDevice mainFloorEvent) {
        // determine if the Elevator car is currently at the floor it was requested on or not
        int currentFloor = context.getCurrentFloor();
        if (currentFloor == mainFloorEvent.getFloor()) {
            // Elevator car is currently on the floor it was requested on
            context.moveBetweenFloors(mainFloorEvent.getCarButton(), mainFloorEvent.getFloorButton());
            context.setState("ReachedDestination");
        } else {
            // Elevator car is not currently on the floor it was requested on
            FloorButton directionToMove = (currentFloor < mainFloorEvent.getFloor())
                    ? FloorButton.UP : FloorButton.DOWN;
            context.moveBetweenFloors(mainFloorEvent.getFloor(), directionToMove);
            context.setState("DoorsOpening");
        }
    }

    @Override
    public void displayState() {
        System.out.println("MovingBetweenFloors");
    }

}
