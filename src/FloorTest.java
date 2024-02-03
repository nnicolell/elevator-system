import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Floor
 */
public class FloorTest {
    /**
     * Tests the creation of HardwareDevice from a given string
     */
    @Test
    void testCreateHardwareDevice() {
        Scheduler scheduler = new Scheduler();
        Floor floor = new Floor(scheduler);
        HardwareDevice hardwareDevice = floor.createHardwareDevice("13:02:56.0 4 Up 6".split(" "));
        LocalTime localTime = LocalTime.parse("13:02:56.0");
        HardwareDevice expectedHardwareDevice = new HardwareDevice(localTime, 4, FloorButton.UP, 6);

        assertEquals(hardwareDevice.getTime(), expectedHardwareDevice.getTime());
        assertEquals(hardwareDevice.getFloor(), expectedHardwareDevice.getFloor());
        assertEquals(hardwareDevice.getFloorButton(), expectedHardwareDevice.getFloorButton());
        assertEquals(hardwareDevice.getArrived(), expectedHardwareDevice.getArrived());
        assertEquals(hardwareDevice.getCarButton(), expectedHardwareDevice.getCarButton());

    }
}
