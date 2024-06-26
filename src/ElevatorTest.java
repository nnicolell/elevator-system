import java.time.LocalTime;
import java.util.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Elevator subsystem.
 */
public class ElevatorTest {

    /**
     * A Scheduler to test the Elevator subsystem with.
     */
     private Scheduler scheduler;

    /**
     * An Elevator to test the Elevator subsystem with.
     */
    private Elevator elevator;

    /**
     * An integer representing the port number the Elevator receives DatagramPackets on.
     */
    private int elevatorPort;

    /**
     * A Random object to test the Elevator subsystem with.
     */
    private final Random random = new Random();

    /**
     * Returns a random integer between 0 and 9999.
     *
     * @return A random integer between 0 and 9999.
     */
    private int generateRandomInt() {
        return random.nextInt(9999 - 1) + 1;
    }

    /**
     * Instantiates a Scheduler, Elevator, and a FloorListener before each test.
     */
    @BeforeEach
    void setup() {
        ArrayList<Integer> elevatorPortNumbers = new ArrayList<>();
        elevatorPort = generateRandomInt();
        elevatorPortNumbers.add(elevatorPort);
        scheduler = new Scheduler(elevatorPortNumbers);
        elevator = scheduler.getFirstAvailableElevator();
    }

    /**
     * Closes the FloorListener and Scheduler sockets after each test.
     */
    @AfterEach
    void cleanup() {
        scheduler.closeSendReceiveSocket();
    }

    /**
     * Tests the initialization of Elevator.
     */
    @Test
    void testElevator() {
        assertEquals(scheduler, elevator.getScheduler());
        assertEquals(elevatorPort, elevator.getPort());
        assertEquals(0, elevator.getFloorEventsSize());
        assertEquals(8, elevator.getStates().size());
    }

    /**
     * Tests getting the Scheduler.
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
        elevator.setHandleRequestInSetState(false);
        elevator.setState("WaitingForElevatorRequest");
        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequest);
        elevator.setState("MovingBetweenFloors");
        assertTrue(elevator.getCurrentState() instanceof MovingBetweenFloors);
        elevator.setState("ReachedDestination");
        assertTrue(elevator.getCurrentState() instanceof ReachedDestination);
        elevator.setState("DoorsClosing");
        assertTrue(elevator.getCurrentState() instanceof DoorsClosing);
        elevator.setState("DoorsOpening");
        assertTrue(elevator.getCurrentState() instanceof DoorsOpening);
        elevator.setState("NotifyScheduler");
        assertTrue(elevator.getCurrentState() instanceof NotifyScheduler);
    }

    /**
     * Tests adding a state to the Elevator state machine.
     */
    @Test
    void testAddState() {
        assertEquals(8, elevator.getStates().size());
        String testStateName = "TestState";
        elevator.addState(testStateName, new WaitingForElevatorRequest());
        HashMap<String, ElevatorState> states = elevator.getStates();
        assertEquals(9, states.size());
        assertTrue(states.containsKey(testStateName));
    }

    /**
     * Tests getting the states of the Elevator state machine.
     */
    @Test
    void testGetStates() {
        HashMap<String, ElevatorState> states = elevator.getStates();
        assertNotNull(states);
        assertEquals(8, states.size());
    }

    /**
     * Tests getting the current state of the Elevator state machine.
     */
    @Test
    void testGetCurrentState() {
        elevator.setHandleRequestInSetState(false);
        elevator.setState("WaitingForElevatorRequest");
        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequest);
    }

    /**
     * Tests adding and removing passengers from the Elevator car.
     */
    @Test
    void testAddAndRemovePassengers() {
        assertEquals(0, elevator.getNumPassengers());
        elevator.addPassengers(1);
        assertEquals(1, elevator.getNumPassengers());
        elevator.removePassengers(1);
        assertEquals(0, elevator.getNumPassengers());
    }

    /**
     * Tests getting the number of passengers in the Elevator car.
     */
    @Test
    void testGetNumPassengers() {
        assertEquals(0, elevator.getNumPassengers());
        elevator.addPassengers(2);
        elevator.addPassengers(3);
        assertEquals(5, elevator.getNumPassengers());
    }

    /**
     * Tests getting the current floor.
     */
    @Test
    void testGetCurrentFloor() {
        HardwareDevice hardwareDevice = new HardwareDevice(elevator.getName(), LocalTime.parse("13:02:56.0"),
                3, FloorButton.UP, 4,2, Fault.NO_FAULT);
        elevator.setMainFloorEvent(hardwareDevice);
        elevator.setHandleRequestInSetState(false);
        assertEquals(1, elevator.getCurrentFloor());
    }

    /**
     * Tests setting the handleRequestInSetState variable in Elevator.
     */
    @Test
    void testSetAndGetHandleRequestInState() {
        assertTrue(elevator.getHandleRequestInSetState());
        elevator.setHandleRequestInSetState(false);
        assertFalse(elevator.getHandleRequestInSetState());
    }

    /**
     * Tests getting the number of passengers.
     */
    @Test
    void testSetAndGetMainFloorEvent() {
        HardwareDevice hardwareDevice = new HardwareDevice(elevator.getName(), LocalTime.parse("13:02:56.0"),
                3, FloorButton.UP, 4, 1,Fault.NO_FAULT);
        elevator.setMainFloorEvent(hardwareDevice);
        assertEquals(hardwareDevice, elevator.getMainFloorEvent());
    }

    /**
     * Tests isTransientFault() and setTransientFault().
     */
    @Test
    void testIsAndSetTransientFault() {
        elevator.setTransientFault(true);
        assertTrue(elevator.isTransientFault());
        elevator.setTransientFault(false);
        assertFalse(elevator.isTransientFault());
    }

    /**
     * Tests ifHardFault() and setHardFault().
     */
    @Test
    void testIsAndSetHardFault() {
        assertFalse(elevator.isHardFault());
        elevator.setHardFault(true);
        assertTrue(elevator.isHardFault());
        elevator.setHardFault(false);
        assertFalse(elevator.isHardFault());
    }

    /**
     * Tests isMaxCapacity() and setMaxCapacity().
     */
    @Test
    void testIsAndSetMaxCapacity() {
        assertFalse(elevator.isMaxCapacity());
        elevator.setMaxCapacity(5);
        assertEquals(5, elevator.getMaxCapacity());
    }

}