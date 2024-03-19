import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Elevator.
 */
public class ElevatorTest {

    /**
     * A Scheduler to test with.
     */
    private Scheduler scheduler;
    /**
     * A Elevator to test with.
     */
    private Elevator elevator;
    private FloorListener floorListener;

    /**
     * A random to test with.
     */
    private final Random random = new Random();

    /**
     * Instantiates Scheduler and HardwareDevice.
     */
    @BeforeEach
    void setup() {
        ArrayList<Integer> elevatorPortNumbers = new ArrayList<>();
        int x = generateRandomInt();
        elevatorPortNumbers.add(x);
        scheduler = new Scheduler(elevatorPortNumbers);
        elevator = scheduler.getElevatorTest();
        floorListener = scheduler.getFloorListener();
    }
    /**
     * Closes the sockets after each test
     */
    @AfterEach
    void cleanup() {
        scheduler.closeSockets();
    }

    /**
     * Generates a random integer
     * @return Integer
     */
    private int generateRandomInt() {
        return random.nextInt(9999 - 1) + 1;
    }


    /**
     * Tests the initialization of Elevator.
     */
    @Test
    void testElevator() {
        assertEquals(scheduler, elevator.getScheduler());
        assertEquals(6, elevator.getStates().size());
        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequestState);
    }


    @Test
    void testGetScheduler() {
        assertEquals(scheduler, elevator.getScheduler());
    }
    /**
     * Tests the Elevator state machine.
     */
    @Test
    void testElevatorStateMachine() {
        HardwareDevice hardwareDevice = new HardwareDevice(elevator.getName(), LocalTime.parse("13:02:56.0"), 4, FloorButton.UP, 4);
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
        assertEquals(6, elevator.getStates().size());
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

    /**
     * Tests getting the current state of the Elevator state machine.
     */
    @Test
    void testGetCurrentState() {
        assertTrue(elevator.getCurrentState() instanceof WaitingForElevatorRequestState);
        elevator.setState("MovingBetweenFloors");
        assertTrue(elevator.getCurrentState() instanceof MovingBetweenFloorsState);
    }

    /**
     * Test for loading and unloading passengers from the car
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
     * Test for retrieving the number of passengers in the car
     */
    @Test
    void testGetNumPassengers() {
        assertEquals(0, elevator.getNumPassengers());
        elevator.addPassenger();
        elevator.addPassenger();
        assertEquals(2, elevator.getNumPassengers());
    }

    /**
     * Test for elevator moving between floors
     */
    @Test
    void testMoveBetweenFloors() {
        LocalTime l = LocalTime.parse("13:02:56.0");
        HardwareDevice hd = new HardwareDevice(elevator.getName(), l, 3, FloorButton.UP, 5);

        assertEquals(1, elevator.getCurrentFloor());
        elevator.moveBetweenFloors(5, FloorButton.UP);
        assertEquals(5, elevator.getCurrentFloor());
        elevator.moveBetweenFloors(2, FloorButton.DOWN);
        assertEquals(2, elevator.getCurrentFloor());
    }

    /**
     * Test to retrieve the current floor
     */
    @Test
    void testGetCurrentFloor(){
        elevator.moveBetweenFloors(3, FloorButton.UP);
        assertEquals(3, elevator.getCurrentFloor());
    }


}