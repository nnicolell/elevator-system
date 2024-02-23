/**
 * This class represents the elevator once it has reached the desired floow
 */
public class ReachedDestination implements ElevatorState{
    @Override
    public void waitingForRequest() {

    }

    @Override
    public void receivedRequest(Elevator context, HardwareDevice request) {
        System.out.print("Request "+ request + "has been added to the queue");
    }

    @Override
    public void notifyScheduler(Elevator context) {

    }

    @Override
    public void floorReached(Elevator context) {

    }

    @Override
    public void displayState() {
        System.out.println("Elevator has Reached Destination");
    }

    @Override
    public void carStopped(Elevator context) {
        System.out.println("The cart has fully stopped");
        context.setState("Doors Opening");
    }
}
