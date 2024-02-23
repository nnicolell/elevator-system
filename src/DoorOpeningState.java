public class DoorOpeningState implements ElevatorState{

    @Override
    public void handleRequest(Elevator context) {
        context.setState("Doors Closing");
    }
}
