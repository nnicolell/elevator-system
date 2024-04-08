import java.time.LocalTime;

/**
 * A class to represent the necessary information to pass to the Scheduler.
 */
public class HardwareDevice {

    /**
     * A String representing the elevator executing the floor event.
     */
    private String elevator;

    /**
     * A LocalTime representing when a passenger requests an elevator.
     */
    private LocalTime time;

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
     * A Fault related to the floor event.
     */
    private final Fault fault;

    /**
     * True, if the Elevator that has arrived at its main floor event destination still has floor events to fulfill.
     * False, if not.
     */
    private boolean moreFloorEvents = false;

    /**
     * The number of passengers related to the floor event
     */
    private int numPassengers;

    /**
     * Initializes a HardwareDevice with a LocalTime representing when a passenger requests an elevator, an integer
     * representing the floor number a passenger requested an elevator at, a FloorButton representing whether the
     * passenger would like to move up or down, and an integer representing the floor number a passenger would like to
     * move to.
     *
     * @param elevator A String representing the elevator running the request.
     * @param time A LocalTime representing when a passenger requests an elevator.
     * @param floor An integer representing the floor number a passenger requested an elevator at.
     * @param floorButton A FloorButton representing whether the passenger would like to move up or down.
     * @param carButton An integer representing the floor number a passenger would like to move to.
     * @param fault A Fault related to the floor event.
     */
    public HardwareDevice (String elevator, LocalTime time, int floor, FloorButton floorButton, int carButton,int numPassengers, Fault fault) {
        this.elevator = elevator;
        this.time = time;
        this.floor = floor;
        this.floorButton = floorButton;
        this.carButton = carButton;
        this.numPassengers = numPassengers;
        this.fault = fault;
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
     * Returns an Elevator representing the elevator running the request.
     *
     * @return An Elevator representing the elevator running the request.
     */
    public String getElevator() {
        return elevator;
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
     * Returns an integer representing the floor number a passenger would like to move to.
     *
     * @return An integer representing the floor number a passenger would like to move to.
     */
    public int getNumPassengers() {
        return numPassengers;
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
     * Returns the fault related to the floor event.
     *
     * @return A fault related to the floor event.
     */
    public Fault getFault() { return fault; }

    /**
     * Sets the Elevator to have arrived at the floor number a passenger would like to move to.
     */
    public void setArrived() {
        this.arrived = true;
    }

    /**
     * Updates the Elevator time
     */
    public void setTime(LocalTime time) {
        this.time = time;
    }

    /**
     * Sets the Elevator to the specified elevator.
     *
     * @param name A String representing the name of the elevator.
     */
    public void setElevator(String name) {
        elevator = name;
    }

    /**
     * Sets if the Elevator that has arrived at its main floor event destination still has floor events to fulfill or
     * not.
     *
     * @param moreFloorEvents True, if the Elevator that has arrived at its main floor event destination still has floor
     *                        events to fulfill. False, if not.
     */
    public void setMoreFloorEvents(boolean moreFloorEvents) {
        this.moreFloorEvents = moreFloorEvents;
    }

    /**
     * Returns a boolean representing if the Elevator that has arrived at its main floor event destination still has
     * floor events to fulfill or not.
     *
     * @return True, if the Elevator that has arrived at its main floor event destination still has floor events to
     * fulfill. False, if not.
     */
    public boolean getMoreFloorEvents() {
        return moreFloorEvents;
    }

    /**
     * Returns a string representing the HardwareDevice.
     *
     * @return A String representing the HardwareDevice.
     */
    @Override
    public String toString() {
        return "{Elevator: " + elevator + ", Time: " + getTime() + ", Requested Floor: " + getFloor()
                + ", Direction: " + getFloorButton() + ", Car Button: " + getCarButton() + ", Number of Passengers: " + getNumPassengers() + ", Arrived: " + getArrived()
                + ", Fault: " + getFault().toString() + "}";
    }

    /**
     * Returns a HardwareDevice created from a string.
     *
     * @param hardwareDeviceString The string to be changed to a HardwareDevice
     * @return A HardwareDevice created from the parameter string.
     */
    public static HardwareDevice stringToHardwareDevice(String hardwareDeviceString) {
        String[] hdArray = hardwareDeviceString.substring(1, hardwareDeviceString.length() - 1).split(",");
        String[] hardwareDeviceStringArray = new String[hdArray.length];

        for (int i = 0; i < hdArray.length; i++) {
            String[] deviceArray = hdArray[i].split(": ");
            String value = deviceArray[1].trim();
            hardwareDeviceStringArray[i] = value;
        }

        String e = hardwareDeviceStringArray[0];
        LocalTime t = LocalTime.parse(hardwareDeviceStringArray[1]);
        int f = Integer.parseInt(hardwareDeviceStringArray[2]);
        FloorButton fb = hardwareDeviceStringArray[3].equalsIgnoreCase("up") ? FloorButton.UP : FloorButton.DOWN;
        int cb = Integer.parseInt(hardwareDeviceStringArray[4]);
        int np = Integer.parseInt(hardwareDeviceStringArray[5]);
        boolean a = hardwareDeviceStringArray[6].equalsIgnoreCase("true");
        Fault ft = Fault.stringToFault(hardwareDeviceStringArray[7]);
        HardwareDevice hardwareDevice = new HardwareDevice(e, t, f, fb, cb, np, ft);
        if (a) {
            hardwareDevice.setArrived();
        }
        return hardwareDevice;
    }

}
