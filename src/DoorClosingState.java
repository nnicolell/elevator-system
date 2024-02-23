public class DoorClosingState implements ElevatorState{

    @Override
    public void handleRequest(Elevator context) {
        context.setState("Moving Between Floors");
    }
}
