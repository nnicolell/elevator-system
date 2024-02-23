/**
 * This class represents the elevator once it has reached the desired floow
 */
public class ReachedDestinationState implements ElevatorState{
    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorOpening");
    }

    @Override
    public void displayState() {
        System.out.println("Elevator has Reached Destination");
    }
}
