import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

}