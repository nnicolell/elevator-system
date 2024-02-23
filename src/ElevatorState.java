public interface ElevatorState {
        void waitingForRequest();

        void receivedRequest(Elevator context, HardwareDevice request);

        void notifyScheduler(Elevator context);

        void floorReached(Elevator context);
        /**
         * Displays the current state information.
         */
        void displayState();

        void carStopped(Elevator context);
}
