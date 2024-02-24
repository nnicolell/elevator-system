/**
 * Interface representing elevator state transition
 */
public interface ElevatorState {

        /**
         * handle events for elevator
         * @param context stores the current state of the elevator
         * @param request the hardware device representing the event
         */
        void handleRequest(Elevator context, HardwareDevice request);

        void displayState();
}
