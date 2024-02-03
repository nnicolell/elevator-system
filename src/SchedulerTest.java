import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Scheduler.
 */
class SchedulerTest {

    /**
     * Tests the initialization of Scheduler.
     */
    @Test
    void testScheduler() {
        Scheduler scheduler = new Scheduler();
        assertTrue(scheduler.getFloorQueue().isEmpty());
        assertNull(scheduler.getCurrentFloorEvent());
        assertEquals(1, scheduler.getNumReqsHandled());
        assertEquals(10000, scheduler.getNumReqs());
    }

    /**
     * Tests adding a floor event to the queue.
     */
    @Test
    void testAddFloorEvent() {
        Scheduler scheduler = new Scheduler();
        assertTrue(scheduler.getFloorQueue().isEmpty());

        LocalTime time = LocalTime.parse("14:05:15.0");
        HardwareDevice hardwareDevice = new HardwareDevice(time,2, FloorButton.UP, 4);
        scheduler.addFloorEvent(hardwareDevice);

        assertEquals(hardwareDevice, scheduler.getFloorQueue().poll());
    }

    /**
     * Tests setting the number of requests.
     */
    @Test
    void testSetNumReqs() {
        Scheduler scheduler = new Scheduler();
        assertEquals(10000, scheduler.getNumReqs());

        scheduler.setNumReqs(2);
        assertEquals(2, scheduler.getNumReqs());

        scheduler.setNumReqs(15);
        assertEquals(15, scheduler.getNumReqs());

        scheduler.setNumReqs(101);
        assertEquals(101, scheduler.getNumReqs());
    }

}