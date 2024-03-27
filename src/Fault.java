public enum Fault {
    NO_FAULT("No Fault"),
    ELEVATOR_STUCK("Elevator stuck between floors"),
    ARRIVAL_SENSOR_FAILED("Arrival sensor failed"),
    DOOR_NOT_OPENING("Door not opening"),
    DOOR_NOT_CLOSING("Door not closing");

    private final String faultType;

    Fault(String faultType) {
        this.faultType = faultType;
    }

    @Override
    public String toString() {
        return faultType;
    }

    public static Fault whichFault(String s) {
        return switch (s) {
            case "Elevator stuck between floors" -> ELEVATOR_STUCK;
            case "Arrival sensor failed" -> ARRIVAL_SENSOR_FAILED;
            case "Door not opening" -> DOOR_NOT_OPENING;
            case "Door not closing" -> DOOR_NOT_CLOSING;
            default -> NO_FAULT;
        };
    }
}
