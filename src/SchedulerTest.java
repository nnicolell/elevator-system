import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Scheduler.
 */
class SchedulerTest {

    private Scheduler scheduler;
    private HardwareDevice hardwareDevice;

    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
        LocalTime localTime = LocalTime.parse("13:02:56.0");
        hardwareDevice = new HardwareDevice(localTime, 4, FloorButton.UP, 6);
    }

    /**
     * Tests the initialization of Scheduler.
     */
    @Test
    void testScheduler() {
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
        assertTrue(scheduler.getFloorQueue().isEmpty());

        LocalTime time = LocalTime.parse("14:05:15.0");
        hardwareDevice = new HardwareDevice(time,2, FloorButton.UP, 4);
        scheduler.addFloorEvent(hardwareDevice);

        assertEquals(hardwareDevice, scheduler.getFloorQueue().poll());
    }

    /**
     * Tests setting the number of requests.
     */
    @Test
    void testSetNumReqs() {
        assertEquals(10000, scheduler.getNumReqs());

        scheduler.setNumReqs(2);
        assertEquals(2, scheduler.getNumReqs());

        scheduler.setNumReqs(15);
        assertEquals(15, scheduler.getNumReqs());

        scheduler.setNumReqs(101);
        assertEquals(101, scheduler.getNumReqs());
    }

}