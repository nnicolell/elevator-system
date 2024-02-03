import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Floor
 */
public class FloorTest {

    private List<String> lines;
    @BeforeEach
    void setup() {
        try {
            lines = Files.readAllLines(Paths.get("test_input.txt"));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Tests the creation of HardwareDevice from a given string
     */
    @Test
    void testCreateHardwareDevice() {
        Scheduler scheduler = new Scheduler();
        Floor floor = new Floor(scheduler);
        HardwareDevice hardwareDevice = floor.createHardwareDevice(lines.getFirst().split(" "));
        LocalTime localTime = LocalTime.parse("13:02:56.0");
        HardwareDevice expectedHardwareDevice = new HardwareDevice(localTime, 4, FloorButton.UP, 6);

        assertEquals(hardwareDevice.getTime(), expectedHardwareDevice.getTime());
        assertEquals(hardwareDevice.getFloor(), expectedHardwareDevice.getFloor());
        assertEquals(hardwareDevice.getFloorButton(), expectedHardwareDevice.getFloorButton());
        assertEquals(hardwareDevice.getArrived(), expectedHardwareDevice.getArrived());
        assertEquals(hardwareDevice.getCarButton(), expectedHardwareDevice.getCarButton());

    }

    @Test
    void testReadCorrectNumberOfLines() {
        assertEquals(lines.size(), 4);
    }
}
