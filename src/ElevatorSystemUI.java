import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ElevatorSystemUI extends JFrame implements ElevatorSystemView {

    private int width, height;
    private int numOfElevators;
    private JLabel[][] grid;
    private ArrayList<JLabel> elevators;
    private final int NUM_FLOORS = 22;
    private JPanel buildingFloors;
    private JPanel elevatorsCloseUp;
    private JPanel elevatorsPanel;
    private JList requestsLog;

    public ElevatorSystemUI(int numOfElevators) {
        super("Elevator System");
        this.numOfElevators = numOfElevators;
        //this.setLayout(new GridLayout(1, 2));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int) screenSize.getWidth();
        height = (int) screenSize.getHeight();

        // Panel for the building floors
        buildingFloors = new JPanel();
        buildingFloors.setLayout(new GridLayout(NUM_FLOORS, numOfElevators));

        // Grid of the floors
        grid = new JLabel[numOfElevators][NUM_FLOORS];
        addFloors();

        // Panel for the elevators and its closeups
        elevatorsPanel = new JPanel();
        elevatorsPanel.setLayout(new GridLayout(2, 1));

        elevatorsCloseUp = new JPanel();
        elevatorsCloseUp.setLayout(new GridLayout(1, numOfElevators));

        elevators = new ArrayList<>();
        addElevators();
        elevatorsPanel.add(elevatorsCloseUp);
        requestsLog = new JList();
        elevatorsPanel.add(requestsLog);

        JLabel titleLabel = new JLabel("ELEVATOR SYSTEM");
        titleLabel.setFont(new Font("Verdana", Font.PLAIN, 30));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        this.add(titleLabel, BorderLayout.NORTH);
        this.add(buildingFloors, BorderLayout.WEST);
        this.add(elevatorsPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(width/2,height/2);
        this.setResizable(true);
        this.setVisible(true);
    }

    private void addFloors() {
        for (int i = NUM_FLOORS; i > 0; i--) {
            JLabel floor = new JLabel("Floor " + i);
            buildingFloors.add(floor);

            for (int j = numOfElevators; j > 0; j--) {
                JLabel e = new JLabel("       ");
                e.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                e.setOpaque(true);
                buildingFloors.add(e);
                grid[j-1][i-1] = e;
            }
        }
    }

    private void addElevators() {
        for (int i = 0; i < numOfElevators; i++) {
            String name = "Elevator " + (i+1);
            JLabel e = new JLabel(name);
            e.setName(name);
            // FIXME: THIS IS HOW YOU ADD A NEW LINE IN JLABEL:
            e.setText("<html>" + name +"<br/>Direction: "+ "<br/>Destination Floor: "+ "<br/>Arrived: "+ "</html>");
            e.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            e.setOpaque(true);
            elevatorsCloseUp.add(e);
            elevators.add(e);
        }
    }

//    public void updateElevator(List elevator) {
////        for (JLabel e : elevators){
////            if (e.getName() == elevator.getName()){
////                HardwareDevice floorEvent = elevator.getMainFloorEvent();
////                e.setText("<html>" + e.getName() +"<br/>Direction: " + floorEvent.getFloorButton() +
////                        "<br/>Destination Floor: " + floorEvent.getCarButton() + "<br/>Arrived: " + floorEvent.getArrived() + "</html>");
////            }
////        }
//        for (int i = 0; i < numOfElevators; i++){
////            if (e.getName() == elevator.getName()){
////                HardwareDevice floorEvent = elevator.getMainFloorEvent();
////                e.setText("<html>" + e.getName() +"<br/>Direction: " + floorEvent.getFloorButton() +
////                        "<br/>Destination Floor: " + floorEvent.getCarButton() + "<br/>Arrived: " + floorEvent.getArrived() + "</html>");
////            }
//
//        }
//    }

    @Override
    public void updateElevator(List<Elevator> elevator) {
        for (int i = 0; i < elevator.size(); i++) {
            if (elevators.get(i).getName().equals(elevator.get(i).getName())) {
                HardwareDevice floorEvent = elevator.get(i).getMainFloorEvent();
                JLabel e = elevators.get(i);
                e.setText("<html>" + e.getName() +"<br/>Direction: " + floorEvent.getFloorButton() +
                        "<br/>Destination Floor: " + floorEvent.getCarButton() + "<br/>Arrived: " + floorEvent.getArrived() + "</html>");
            }
        }
    }

    public void updateFloor(Elevator elevator, int index) {
        grid[index][elevator.getMainFloorEvent().getFloor()].setBackground(Color.CYAN);
    }

    public static void main(String[] args) {
        new ElevatorSystemUI(5);
    }

}
