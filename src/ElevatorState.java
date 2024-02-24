public interface ElevatorState {

        void handleRequest(Elevator context, HardwareDevice request);

        void displayState();
}
