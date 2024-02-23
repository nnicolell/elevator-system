public interface ElevatorState {

        void handleRequest(Elevator context, HardwareDevice request);

//        void waitingForRequest();
//
//        void receivedRequest(Elevator context, HardwareDevice request);
//
//        void notifyScheduler(Elevator context);
//
//        void floorReached(Elevator context);
//        void doorOpening(Elevator context);
//        void doorClosing(Elevator context);
//        /**
//         * Displays the current state information.
//         */
        void displayState();
//
//        void carStopped(Elevator context);
}
