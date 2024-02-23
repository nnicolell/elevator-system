public class DoorOpeningState implements ElevatorState{

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("DoorClosing");
    }
    @Override
    public void displayState() {
        System.out.print("State: Doors Opening");
    }
}
