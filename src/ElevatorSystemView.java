import java.util.List;

public interface ElevatorSystemView {
    void updateElevator(List<Elevator> elevator);
    void updateFloor(Elevator elevator, int index);
}