public class DoorClosingState implements ElevatorState{

    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        if (!request.getArrived()){
            context.setState("MovingBetweenFloors");
        }
        else{
            context.setState("NotifyScheduler");
        }
    }

    @Override
    public void displayState() {
        System.out.print("State: Doors Closing");
    }
}
