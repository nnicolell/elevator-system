/**
 * This class represents the Elevator while it is waiting for a request
 */
public class WaitingForElevatorRequestState implements ElevatorState{
    @Override
    public void handleRequest(Elevator context) {
        context.setState("Doors Opening");
    }

//    @Override
//    public void receivedRequest(Elevator context, HardwareDevice request) {
//        context.setState("Doors Opening");
//        System.out.print("Received request " + request.toString() + "request is being processed now");
//    }
//
//    @Override
//    public void notifyScheduler(Elevator context){
//        System.out.print("Elevator is still waiting for a request");
//    }
//
//    @Override
//    public void displayState() {
//        System.out.print("State: Waiting For Elevator Request");
//    }
//
//    @Override
//    public void waitingForRequest() {
//    }
//
//    @Override
//    public void floorReached(Elevator context) {
//    }
//    @Override
//    public void carStopped(Elevator context) {
//    }
//
//    @Override
//    public void doorOpening(Elevator context){}
//
//    @Override
//    public void doorClosing(Elevator context){}
}
