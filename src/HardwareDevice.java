import java.time.LocalTime;

/**
 * A record to represent the necessary information to pass to the Scheduler.
 *
 * The following accessor methods can be used:
 *      time() - Returns a LocalTime representing when a passenger requests an elevator.
 *      floor() - Returns an integer representing the floor number a passenger requested an elevator at.
 *      floorButton() - Returns a FloorButton representing whether the passenger would like to move up or down.
 *      carButton() - Returns an integer representing the floor number a passenger would like to move to.
 *
 * @param time A LocalTime representing when a passenger requests an elevator.
 * @param floor An integer representing the floor number a passenger requested an elevator at.
 * @param floorButton A FloorButton representing whether the passenger would like to move up or down.
 * @param carButton An integer representing the floor number a passenger would like to move to.
 */
public record HardwareDevice(LocalTime time, int floor, FloorButton floorButton, int carButton) { }
