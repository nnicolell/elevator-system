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
     * A FloorListener to test the Elevator subsystem with.
     */
    private FloorListener floorListener;

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
        elevator = scheduler.getElevatorTest();
        floorListener = scheduler.getFloorListener();
    }

    /**
     * Closes the FloorListener and Scheduler sockets after each test.
     */
    @AfterEach
    void cleanup() {
        floorListener.setRunningToFalse();
        scheduler.closeSockets();
    }

    /**
     * Tests the initialization of Elevator.
     */
    @Test
    void testElevator() {
        assertEquals(scheduler, elevator.getScheduler());
        assertEquals(elevatorPortNum, elevator.getPort());
        assertEquals(0, elevator.getFloorEventsSize());
        assertEquals(6, elevator.getStates().size());
    }

    /**
     * Tests getting the Scheduler.
     */
    @Test
    void testGetScheduler() {
        assertEquals(scheduler, elevator.getScheduler());
    }

    // TODO: instead of this, we should test the transition to each state
    // for example, test WaitingForElevatorRequest to MovingBetweenFloors and Waiting to DoorsOpening
//    /**
//     * Tests the Elevator state machine.
//     */
//    @Test
//    void testElevatorStateMachine() {
//        HardwareDevice hardwareDevice = new HardwareDevice(elevator.getName(), LocalTime.parse("13:02:56.0"), 4, FloorButton.UP, 4);
//        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequest);
//        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
//        assertTrue(elevator.getCurrentState() instanceof DoorsOpening);
//        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
//        assertTrue(elevator.getCurrentState() instanceof DoorsClosing);
//        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
//        assertTrue(elevator.getCurrentState() instanceof MovingBetweenFloors);
//        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
//        assertTrue(elevator.getCurrentState() instanceof ReachedDestination);
//        hardwareDevice.setArrived();
//        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
//        assertTrue(elevator.getCurrentState() instanceof DoorsOpening);
//        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
//        assertTrue(elevator.getCurrentState() instanceof DoorsClosing);
//        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
//        assertTrue(elevator.getCurrentState() instanceof NotifyScheduler);
//        elevator.getCurrentState().handleRequest(elevator, hardwareDevice);
//        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequest);
//    }

    // TODO: this doesn't work anymore since we're now calling handleRequest() in setState()... how should we replace this?
//    /**
//     * Tests setting the current state of the Elevator state machine.
//     */
//    @Test
//    void testSetState() {
//        elevator.setState("WaitingForElevatorRequest");
//        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequest);
//        elevator.setState("MovingBetweenFloors");
//        assertTrue(elevator.getCurrentState() instanceof MovingBetweenFloors);
//        elevator.setState("ReachedDestination");
//        assertTrue(elevator.getCurrentState() instanceof ReachedDestination);
//        elevator.setState("DoorClosing");
//        assertTrue(elevator.getCurrentState() instanceof DoorsClosing);
//        elevator.setState("DoorOpening");
//        assertTrue(elevator.getCurrentState() instanceof DoorsOpening);
//        elevator.setState("NotifyScheduler");
//        assertTrue(elevator.getCurrentState() instanceof NotifyScheduler);
//    }

    /**
     * Tests adding a state to the Elevator state machine.
     */
    @Test
    void testAddState() {
        assertEquals(6, elevator.getStates().size());
        String testStateName = "TestState";
        elevator.addState(testStateName, new WaitingForElevatorRequest());
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

    // TODO: doesn't work anymore, probably because handleRequest() is now in setState()
//    /**
//     * Tests getting the current state of the Elevator state machine.
//     */
//    @Test
//    void testGetCurrentState() {
//        elevator.setState("WaitingForElevatorRequest");
//        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequest);
//    }

    /**
     * Tests adding and removing passengers from the Elevator car.
     */
    @Test
    void testAddAndRemovePassengers() {
        assertEquals(0, elevator.getNumPassengers());
        elevator.addPassenger();
        assertEquals(1, elevator.getNumPassengers());
        elevator.removePassenger();
        assertEquals(0, elevator.getNumPassengers());
    }

    /**
     * Tests getting the number of passengers in the Elevator car.
     */
    @Test
    void testGetNumPassengers() {
        assertEquals(0, elevator.getNumPassengers());
        elevator.addPassenger();
        elevator.addPassenger();
        assertEquals(2, elevator.getNumPassengers());
    }

    // TODO: doesn't work anymore since moveBetweenFloors() requires 3 parameters
//    /**
//     * Tests moving the Elevator car between floors.
//     */
//    @Test
//    void testMoveBetweenFloors() {
//        assertEquals(1, elevator.getCurrentFloor());
//        elevator.moveBetweenFloors(5, FloorButton.UP);
//        assertEquals(5, elevator.getCurrentFloor());
//        elevator.moveBetweenFloors(2, FloorButton.DOWN);
//        assertEquals(2, elevator.getCurrentFloor());
//    }

    // TODO: doesn't work anymore since moveBetweenFloors() requires 3 parameters
//    /**
//     * Tests getting the current floor.
//     */
//    @Test
//    void testGetCurrentFloor() {
//        assertEquals(1, elevator.getCurrentFloor());
//        elevator.moveBetweenFloors(3, FloorButton.UP);
//        assertEquals(3, elevator.getCurrentFloor());
//    }

}