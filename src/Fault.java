public enum Fault {

    NO_FAULT("No fault"),
    ELEVATOR_STUCK("Elevator stuck between floors"),
    ARRIVAL_SENSOR_FAILED("Arrival sensor failed"),
    DOORS_NOT_OPENING("Doors not opening"),
    DOORS_NOT_CLOSING("Doors not closing");

    private final String faultType;

    Fault(String faultType) {
        this.faultType = faultType;
    }

    @Override
    public String toString() {
        return faultType;
    }

    public static Fault stringToFault(String s) {
        return switch (s) {
            case "Elevator stuck between floors" -> ELEVATOR_STUCK;
            case "Arrival sensor failed" -> ARRIVAL_SENSOR_FAILED;
            case "Doors not opening" -> DOORS_NOT_OPENING;
            case "Doors not closing" -> DOORS_NOT_CLOSING;
            default -> NO_FAULT;
        };
    }
}
