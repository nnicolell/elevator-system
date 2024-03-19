import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the HardwareDevice.
 */
class HardwareDeviceTest {

    /**
     * Tests the initialization of HardwareDevice.
     */
    @Test
    void testHardwareDevice() {
        LocalTime time = LocalTime.parse("14:05:15.0");
        HardwareDevice hardwareDevice = new HardwareDevice("Elevator1", time,2, FloorButton.UP, 4);
        assertEquals("Elevator1", hardwareDevice.getElevator());
        assertEquals(time, hardwareDevice.getTime());
        assertEquals(2, hardwareDevice.getFloor());
        assertEquals(FloorButton.UP, hardwareDevice.getFloorButton());
        assertEquals(4, hardwareDevice.getCarButton());
        assertFalse(hardwareDevice.getArrived());
    }

    /**
     * Tests setting the Elevator to have arrived at the floor number a passenger would like to move to.
     */
    @Test
    void testSetArrived() {
        LocalTime time = LocalTime.parse("22:48:59.2");
        HardwareDevice hardwareDevice = new HardwareDevice("Elevator1", time,6, FloorButton.DOWN, 1);
        assertFalse(hardwareDevice.getArrived());
        hardwareDevice.setArrived();
        assertTrue(hardwareDevice.getArrived());
    }

    /**
     * Tests the string representation of a HardwareDevice.
     */
    @Test
    void testToString() {
        LocalTime time = LocalTime.parse("13:14:15.6");
        HardwareDevice hardwareDevice = new HardwareDevice("Elevator1", time,1, FloorButton.UP, 2);
        assertEquals("{ Time: 13:14:15.600, Requested Floor: 1, Direction: UP, Car Button: 2 }",
                hardwareDevice.toString());
    }

    /**
     * Tests the conversion between a string to a HardwareDevice.
     */
    @Test
    void testStringToHardwareDevice() {
        LocalTime time = LocalTime.parse("13:14:15.6");
        HardwareDevice hardwareDevice = new HardwareDevice("Elevator1", time,1, FloorButton.UP, 2);
        hardwareDevice.setArrived();
        String hardwareDeviceString = hardwareDevice.toString();
        assertEquals(hardwareDevice.toString(),
                hardwareDevice.stringToHardwareDevice(hardwareDeviceString).toString());
    }

}