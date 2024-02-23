/**
 * This class represents the elevator as it is moving from one floor to another
 */
public class MovingBetweenFloorsState implements ElevatorState{
    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("ReachedDestination");
    }

    @Override
    public void displayState() {
        System.out.println("State: Elevator is Moving Between Floors");
    }
}
