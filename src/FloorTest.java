import java.io.IOException;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Floor
 */
public class FloorTest {

    private List<String> lines;

    /**
     * Setup before each method to read from test input file
     */
    @BeforeEach
    void setup() {
        try {
            lines = Files.readAllLines(Paths.get("test_input.txt"));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Generates a random integer
     * @return Integer
     */
    private int generateRandomInt() {
        Random random = new Random();
        return random.nextInt(9999 - 1) + 1;
    }

    /**
     * Tests the creation of HardwareDevice from a given string
     */
    @Test
    void testCreateHardwareDevice() {
        ArrayList<Integer> elevatorPortNumbers = new ArrayList<>();
        elevatorPortNumbers.add(70);
        elevatorPortNumbers.add(64);
        elevatorPortNumbers.add(67);
        int x = generateRandomInt();
        Scheduler scheduler = new Scheduler(elevatorPortNumbers, x);
        Floor floor = new Floor(scheduler);
        HardwareDevice hardwareDevice = floor.createHardwareDevice(lines.get(0).split(" "));
        LocalTime localTime = LocalTime.parse("13:02:56.0");
        HardwareDevice expectedHardwareDevice = new HardwareDevice("Elevator1", localTime, 4,
                FloorButton.UP, 6, 3, Fault.NO_FAULT);

        assertEquals(hardwareDevice.getTime(), expectedHardwareDevice.getTime());
        assertEquals(hardwareDevice.getFloor(), expectedHardwareDevice.getFloor());
        assertEquals(hardwareDevice.getFloorButton(), expectedHardwareDevice.getFloorButton());
        assertEquals(hardwareDevice.getArrived(), expectedHardwareDevice.getArrived());
        assertEquals(hardwareDevice.getCarButton(), expectedHardwareDevice.getCarButton());
    }

    /**
     * Tests if the correct number of lines are read from the test_input file
     */
    @Test
    void testReadCorrectNumberOfLines() {
        assertEquals(5, lines.size());
    }

}
