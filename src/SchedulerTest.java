import java.time.LocalTime;
import java.util.*;
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
     * An Elevator to test with.
     */
    private Elevator elevator;

    /**
     * Instantiates Scheduler and HardwareDevice.
     */
    @BeforeEach
    void setUp() {
        ArrayList<Integer> elevatorPortNumbers = new ArrayList<>();
        int x = generateRandomInt();
        elevatorPortNumbers.add(x);
        scheduler = new Scheduler(elevatorPortNumbers);
        elevator = scheduler.getFirstAvailableElevator();
        hardwareDevice = new HardwareDevice("E1",LocalTime.parse("13:02:56.0"), 4, FloorButton.UP,
                6, 1, Fault.NO_FAULT);
    }

    /**
     * Generates a random integer between 0 and 9999.
     *
     * @return A random integer.
     */
    private int generateRandomInt() {
        Random random = new Random();
        return random.nextInt(9999 - 1) + 1;
    }

    /**
     * Closes the sockets after each test.
     */
    @AfterEach
    void cleanup() {
        scheduler.closeSendReceiveSocket();
    }

    /**
     * Tests the initialization of Scheduler.
     */
    @Test
    void testScheduler() {
        assertEquals(0, scheduler.getNumReqsHandled());
        assertEquals(48, scheduler.getNumReqs());
        assertEquals(5, scheduler.getStates().size());
        assertInstanceOf(WaitingForFloorEvent.class, scheduler.getCurrentState());
    }

    /**
     * Tests getting the current state of the Scheduler state machine.
     */
    @Test
    void testGetCurrentState() {
        assertInstanceOf(WaitingForFloorEvent.class, scheduler.getCurrentState());
        scheduler.setState("NotifyElevator");
        assertInstanceOf(NotifyElevator.class, scheduler.getCurrentState());
    }

    /**
     * Tests getting the states of the Scheduler state machine.
     */
    @Test
    void testGetStates() {
        HashMap<String, SchedulerState> states = scheduler.getStates();
        assertNotNull(states);
        assertEquals(5, states.size());
    }

    /**
     * Tests setting the state of the Scheduler state machine.
     */
    @Test
    void testSetState() {
        assertInstanceOf(WaitingForFloorEvent.class, scheduler.getCurrentState());
        scheduler.setState("NotifyFloor");
        assertInstanceOf(NotifyFloor.class, scheduler.getCurrentState());
        scheduler.setState("NotifyElevator");
        assertInstanceOf(NotifyElevator.class, scheduler.getCurrentState());
    }

    /**
     * Tests adding a state to the Scheduler state machine.
     */
    @Test
    void testAddState() {
        String testStateName = "TestState";
        assertEquals(5, scheduler.getStates().size());
        scheduler.addState(testStateName, new WaitingForFloorEvent());
        HashMap<String, SchedulerState> states = scheduler.getStates();
        assertEquals(6, states.size());
        assertTrue(states.containsKey(testStateName));
    }

    /**
     * Tests adding a floor event to the queue.
     */
    @Test
    void testAddFloorEvent() {
        hardwareDevice = new HardwareDevice("Elevator1", LocalTime.parse("14:05:15.0"),2,
                FloorButton.UP, 4, 2, Fault.NO_FAULT);
        scheduler.addFloorEvent(hardwareDevice);
    }

    /**
     * Tests setting the number of requests.
     */
    @Test
    void testSetAndGetNumReqs() {
        assertEquals(48, scheduler.getNumReqs());

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
        assertEquals(0, scheduler.getNumReqsHandled());
    }

    /**
     * Tests adding an elevator to the busy elevators and getting the list of elevators
     */
    @Test
    void testAddingAndGettingBusyElevator() {
        scheduler.addBusyElevator(elevator);
        List<Elevator> busy = new ArrayList<>();
        busy.add(elevator);
        assertEquals(busy, scheduler.getBusyElevators());
    }

    /**
     * Tests getting the list of floor events to handle
     */
    @Test
    void testGetFloorEventsToHandle() {
        hardwareDevice = new HardwareDevice("E1", LocalTime.parse("14:05:15.0"),2, FloorButton.UP,
                4, 3, Fault.NO_FAULT);
        scheduler.addFloorEvent(hardwareDevice);
        ArrayList<HardwareDevice> floorEvents = new ArrayList<>();
        floorEvents.add(hardwareDevice);
        assertEquals(floorEvents.get(0), scheduler.getFloorEventsToHandle().get(0));
    }

}