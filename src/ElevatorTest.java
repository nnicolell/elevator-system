import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Elevator.
 */
class ElevatorTest {

    /**
     * Tests the initialization of Elevator.
     */
    @Test
    void testElevator() {
        Scheduler scheduler = new Scheduler();
        Elevator elevator = new Elevator(scheduler);
        assertEquals(scheduler, elevator.getScheduler());
    }

    /**
     * Tests the Elevator state machine.
     */
    @Test
    void testElevatorStateMachine() {
        Scheduler scheduler = new Scheduler();
        Elevator elevator = new Elevator(scheduler);
        HardwareDevice hardwareDevice = new HardwareDevice(LocalTime.parse("13:02:56.0"), 4, FloorButton.UP, 4);
        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequestState);
        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
        assertTrue(elevator.getCurrentState() instanceof DoorOpeningState);
        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
        assertTrue(elevator.getCurrentState() instanceof DoorClosingState);
        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
        assertTrue(elevator.getCurrentState() instanceof MovingBetweenFloorsState);
        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
        assertTrue(elevator.getCurrentState() instanceof ReachedDestinationState);
        hardwareDevice.setArrived();
        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
        assertTrue(elevator.getCurrentState() instanceof DoorOpeningState);
        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
        assertTrue(elevator.getCurrentState() instanceof DoorClosingState);
        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
        assertTrue(elevator.getCurrentState() instanceof NotifySchedulerState);
        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequestState);
    }

}