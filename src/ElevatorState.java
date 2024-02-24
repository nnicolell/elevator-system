/**
 * An interface to represent the states in the Elevator state machine.
 */
public interface ElevatorState {

        /**
         * Handles the event based on the specified request.
         *
         * @param context An Elevator representing the context of the state machine.
         * @param request A HardwareDevice representing the request to complete.
         */
        void handleRequest(Elevator context, HardwareDevice request);

        /**
         * Displays the current state information.
         */
        void displayState();

}
