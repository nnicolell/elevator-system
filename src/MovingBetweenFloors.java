/**
 * This class represents the elevator as it is moving from one floor to another
 */
public class MovingBetweenFloors implements ElevatorState{

    @Override
    public void receivedRequest(Elevator context, HardwareDevice request) {
        System.out.print("Request "+ request + "has been added to the queue");
    }

    @Override
    public void notifyScheduler(Elevator context) {
        System.out.println("Elevator is still moving");
    }

    @Override
    public void floorReached(Elevator context) {
        System.out.println("The elevator has reached the floor");
        context.setState("Reached Destination");
    }

    @Override
    public void displayState() {
        System.out.println("Elevator is Moving Between Floors");
    }

    @Override
    public void carStopped(Elevator context) {
    }
    @Override
    public void waitingForRequest() {

    }
}
