import java.time.LocalTime;

/**
 * A class to represent the necessary information to pass to the Scheduler.
 */
public class HardwareDevice {

    /**
     * A LocalTime representing when a passenger requests an elevator.
     */
    private final LocalTime time;

    /**
     * An integer representing the floor number a passenger requested an elevator at.
     */
    private final int floor;

    /**
     * A FloorButton representing whether the passenger would like to move up or down.
     */
    private final FloorButton floorButton;

    /**
     * An integer representing the floor number a passenger would like to move to.
     */
    private final int carButton;

    /**
     * True if the elevator has arrived at the floor number a passenger would like to move to. False if not.
     */
    private boolean arrived = false;

    /**
     * Initializes a HardwareDevice with a LocalTime representing when a passenger requests an elevator, an integer
     * representing the floor number a passenger requested an elevator at, a FloorButton representing whether the
     * passenger would like to move up or down, and an integer representing the floor number a passenger would like to
     * move to.
     *
     * @param time A LocalTime representing when a passenger requests an elevator.
     * @param floor An integer representing the floor number a passenger requested an elevator at.
     * @param floorButton A FloorButton representing whether the passenger would like to move up or down.
     * @param carButton An integer representing the floor number a passenger would like to move to.
     */
    public HardwareDevice (LocalTime time, int floor, FloorButton floorButton, int carButton) {
        this.time = time;
        this.floor = floor;
        this.floorButton = floorButton;
        this.carButton = carButton;
    }

    /**
     * Returns a LocalTime representing when a passenger requests an elevator.
     *
     * @return A LocalTime representing when a passenger requests an elevator.
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * Returns an integer representing the floor number a passenger requested an elevator at.
     *
     * @return An integer representing the floor number a passenger requested an elevator at.
     */
    public int getFloor() {
        return floor;
    }

    /**
     * Returns a FloorButton representing whether the passenger would like to move up or down.
     *
     * @return A FloorButton representing whether the passenger would like to move up or down.
     */
    public FloorButton getFloorButton() {
        return floorButton;
    }

    /**
     * Returns an integer representing the floor number a passenger would like to move to.
     *
     * @return An integer representing the floor number a passenger would like to move to.
     */
    public int getCarButton() {
        return carButton;
    }

    /**
     * Returns True if the elevator has arrived at the floor number a passenger would like to move to. False if not.
     *
     * @return True if the elevator has arrived at the floor number a passenger would like to move to. False if not.
     */
    public boolean getArrived() {
        return arrived;
    }

    /**
     * Sets the Elevator to have arrived at the floor number a passenger would like to move to.
     */
    public void setArrived() {
        this.arrived = true;
    }

}
