/**
 * An enumerator to represent elevator faults.
 */
public enum Fault {

    NO_FAULT("No fault"),
    ELEVATOR_STUCK("Elevator stuck between floors"),
    DOORS_NOT_OPENING("Doors not opening"),
    DOORS_NOT_CLOSING("Doors not closing");

    /**
     * A String representing the fault.
     */
    private final String faultType;

    /**
     * Initializes a Fault.
     *
     * @param faultType A String representing the fault.
     */
    Fault(String faultType) {
        this.faultType = faultType;
    }

    /**
     * Returns a String representing the fault.
     *
     * @return A String representing the fault.
     */
    @Override
    public String toString() {
        return faultType;
    }

    /**
     * Returns a Fault representing the specified string.
     *
     * @param faultType A String representing the fault.
     * @return A Fault representing the specified string.
     */
    public static Fault stringToFault(String faultType) {
        return switch (faultType) {
            case "Elevator stuck between floors" -> ELEVATOR_STUCK;
            case "Doors not opening" -> DOORS_NOT_OPENING;
            case "Doors not closing" -> DOORS_NOT_CLOSING;
            default -> NO_FAULT;
        };
    }

}
