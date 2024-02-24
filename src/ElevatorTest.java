import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Elevator.
 */
public class ElevatorTest {

    private Scheduler scheduler;
    private Elevator elevator;

    @BeforeEach
    void setup() {
        scheduler = new Scheduler();
        elevator = new Elevator(scheduler);
    }

    /**
     * Tests the initialization of Elevator.
     */
    @Test
    void testElevator() {
        assertEquals(scheduler, elevator.getScheduler());
        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequestState);
    }

    /**
     * Tests the Elevator state machine.
     */
    @Test
    void testElevatorStateMachine() {
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

    /**
     * Tests getting the scheduler.
     */
    @Test
    void testGetScheduler() {
        assertEquals(scheduler, elevator.getScheduler());
    }

    /**
     * Tests setting the current state of the Elevator state machine.
     */
    @Test
    void testSetState() {
        elevator.setState("WaitingForElevatorRequest");
        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequestState);
        elevator.setState("MovingBetweenFloors");
        assertTrue(elevator.getCurrentState() instanceof MovingBetweenFloorsState);
        elevator.setState("ReachedDestination");
        assertTrue(elevator.getCurrentState() instanceof ReachedDestinationState);
        elevator.setState("DoorClosing");
        assertTrue(elevator.getCurrentState() instanceof DoorClosingState);
        elevator.setState("DoorOpening");
        assertTrue(elevator.getCurrentState() instanceof DoorOpeningState);
        elevator.setState("NotifyScheduler");
        assertTrue(elevator.getCurrentState() instanceof NotifySchedulerState);
    }

    /**
     * Tests adding a state to the Elevator state machine.
     */
    @Test
    void testAddState() {
        String testStateName = "TestState";
        elevator.addState(testStateName, new WaitingForElevatorRequestState());
        HashMap<String, ElevatorState> states = elevator.getStates();
        assertEquals(7, states.size());
        assertTrue(states.containsKey(testStateName));
    }

    /**
     * Tests getting the states of the Elevator state machine.
     */
    @Test
    void testGetStates() {
        HashMap<String, ElevatorState> states = elevator.getStates();
        assertNotNull(states);
        assertEquals(6, states.size());
    }

}