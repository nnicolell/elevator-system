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
    private int elevatorPortNum;

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
        elevatorPortNum = generateRandomInt();
        elevatorPortNumbers.add(elevatorPortNum);
        scheduler = new Scheduler(elevatorPortNumbers, generateRandomInt());
        elevator = scheduler.getFirstAvailableElevator();
//        floorListener = scheduler.getFloorListener();
    }

    /**
     * Closes the FloorListener and Scheduler sockets after each test.
     */
    @AfterEach
    void cleanup() {
        //floorListener.setRunningToFalse();
        scheduler.closeSendReceiveSocket();
    }

    /**
     * Tests the initialization of Elevator.
     */
    @Test
    void testElevator() {
        assertEquals(scheduler, elevator.getScheduler());
        assertEquals(elevatorPortNum, elevator.getPort());
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
        elevator.addPassenger(1);
        assertEquals(1, elevator.getNumPassengers());
        elevator.removePassenger(1);
        assertEquals(0, elevator.getNumPassengers());
    }

    /**
     * Tests getting the number of passengers in the Elevator car.
     */
    @Test
    void testGetNumPassengers() {
        assertEquals(0, elevator.getNumPassengers());
        elevator.addPassenger(2);
        elevator.addPassenger(3);
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
     * Tests the if and set transient fault
     */
    @Test
    void testIsAndSetTransientFault() {
        elevator.setTransientFault(true);
        assertEquals(true, elevator.isTransientFault());
        elevator.setTransientFault(false);
        assertEquals(false, elevator.isTransientFault());
    }

    /**
     * Tests the if and set hard fault
     */
    @Test
    void testIsAndSetHardFault() {
        assertEquals(false, elevator.isHardFault());
        elevator.setHardFault(true);
        assertEquals(true, elevator.isHardFault());
        elevator.setHardFault(false);
        assertEquals(false, elevator.isHardFault());
    }

    /**
     * Tests the if and get the capacity of passengers
     */
    @Test
    void testIsAndGetMaxCapacity() {
        assertEquals(false, elevator.isMaxCapacity());
        elevator.setMaxCapacity(5);
        assertEquals(5, elevator.getMaxCapacity());
    }
}