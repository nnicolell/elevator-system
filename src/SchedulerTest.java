import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Scheduler.
 */
class SchedulerTest {

    /**
     * A Scheduler to test with.
     */
    private Scheduler scheduler;

    /**
     * A HardwareDevice to test with.
     */
    private HardwareDevice hardwareDevice;

    /**
     * Instantiates Scheduler and HardwareDevice.
     */
    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
        hardwareDevice = new HardwareDevice(LocalTime.parse("13:02:56.0"), 4, FloorButton.UP, 6);
    }

    /**
     * Tests the initialization of Scheduler.
     */
    @Test
    void testScheduler() {
        assertEquals(1, scheduler.getNumReqsHandled());
        assertEquals(10000, scheduler.getNumReqs());
        assertEquals(3, scheduler.getStates().size());
        assertInstanceOf(WaitingForFloorEventState.class, scheduler.getCurrentState());
    }

    /**
     * Tests getting the current state of the Scheduler state machine.
     */
    @Test
    void testGetCurrentState() {
        assertInstanceOf(WaitingForFloorEventState.class, scheduler.getCurrentState());
        scheduler.setState("NotifyElevator");
        assertInstanceOf(NotifyElevatorState.class, scheduler.getCurrentState());
    }

    /**
     * Tests getting the states of the Scheduler state machine.
     */
    @Test
    void testGetStates() {
        HashMap<String, SchedulerState> states = scheduler.getStates();
        assertNotNull(states);
        assertEquals(3, states.size());
    }

    /**
     * Tests setting the state of the Scheduler state machine.
     */
    @Test
    void testSetState() {
        assertInstanceOf(WaitingForFloorEventState.class, scheduler.getCurrentState());
        scheduler.setState("NotifyFloor");
        assertInstanceOf(NotifyFloorState.class, scheduler.getCurrentState());
        scheduler.setState("NotifyElevator");
        assertInstanceOf(NotifyElevatorState.class, scheduler.getCurrentState());
    }

    /**
     * Tests adding a state to the Scheduler state machine.
     */
    @Test
    void testAddState() {
        String testStateName = "TestState";
        assertEquals(3, scheduler.getStates().size());
        scheduler.addState(testStateName, new WaitingForFloorEventState());
        HashMap<String, SchedulerState> states = scheduler.getStates();
        assertEquals(4, states.size());
        assertTrue(states.containsKey(testStateName));
    }

    /**
     * Tests adding a floor event to the queue.
     */
    @Test
    void testAddFloorEvent() {
        hardwareDevice = new HardwareDevice(LocalTime.parse("14:05:15.0"),2, FloorButton.UP, 4);
        scheduler.addFloorEvent(hardwareDevice);
    }

    /**
     * Tests setting the number of requests.
     */
    @Test
    void testSetAndGetNumReqs() {
        assertEquals(10000, scheduler.getNumReqs());

        scheduler.setNumReqs(2);
        assertEquals(2, scheduler.getNumReqs());

        scheduler.setNumReqs(15);
        assertEquals(15, scheduler.getNumReqs());

        scheduler.setNumReqs(101);
        assertEquals(101, scheduler.getNumReqs());
    }

    /**
     * Tests getting an integer representing the number of requests that have been handled.
     */
    @Test
    void testGetNumReqsHandled() {
        assertEquals(1, scheduler.getNumReqsHandled());
    }

    /**
     * Tests adding an elevator to the busy elevators and getting the list of elevators
     */
    @Test
    void testAddingAndGettingBusyElevator() {
        Elevator elevator = new Elevator(scheduler, 66, "Elevator");
        scheduler.addBusyElevator(elevator);
        List<Elevator> busy = new ArrayList<>();
        busy.add(elevator);
        assertEquals(busy, scheduler.getBusyElevator());
    }


    /**
     * Tests getting the list of floor events to handle
     */
    @Test
    void testGetFloorEventsToHandle() {
        hardwareDevice = new HardwareDevice(LocalTime.parse("14:05:15.0"),2, FloorButton.UP, 4);
        scheduler.addFloorEvent(hardwareDevice);
        List<HardwareDevice> floorEvents = new ArrayList<>();
        floorEvents.add(hardwareDevice);
        assertEquals(floorEvents, scheduler.getFloorEventsToHandle());
    }

}