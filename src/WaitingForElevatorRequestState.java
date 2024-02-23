/**
 * This class represents the Elevator while it is waiting for a request
 */
public class WaitingForElevatorRequestState implements ElevatorState{
    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorOpening");
    }

    @Override
    public void displayState() {
        System.out.print("State: Waiting For Elevator Request");
    }

}
