import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ElevatorSystemUI extends JFrame implements ElevatorSystemView {

    private int width, height;
    private int numElevators, numFloors;
    private JLabel[][] grid;
    private ArrayList<JLabel> elevators;
    private JPanel buildingFloors;
    private JPanel elevatorsCloseUp;
    private JPanel elevatorsPanel;
    private JPanel floorsPanel;
    private JList requestsLog;
    private List<Elevator> elevatorList;
    private ElevatorSystemController esController;

    public ElevatorSystemUI(int numElevators, int numFloors, List<Elevator> elevatorList) {
        super("Elevator System");
        this.numElevators = numElevators;
        this.numFloors = numFloors;
        this.elevatorList = elevatorList;
        elevators = new ArrayList<>();
        setElevators();

        esController = new ElevatorSystemController(this, elevatorList);
        for (Elevator e : elevatorList) {
            e.setView(this);
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int) screenSize.getWidth();
        height = (int) screenSize.getHeight();

        // Panel for the building floors
        buildingFloors = new JPanel();
        buildingFloors.setLayout(new GridLayout(numFloors+1, numElevators));
        buildingFloors.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Grid of the floors
        grid = new JLabel[numElevators][numFloors];
        addFloors();

        // Panel for the elevators and its closeups
        elevatorsPanel = new JPanel();
        elevatorsPanel.setLayout(new GridLayout(2, 1));

        elevatorsCloseUp = new JPanel();
        elevatorsCloseUp.setLayout(new GridLayout(1, numElevators));

        addElevators();
        elevatorsPanel.add(elevatorsCloseUp);
        requestsLog = new JList();
        elevatorsPanel.add(requestsLog);
        elevatorsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

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

    private void setElevators() {
        for (int i = 0; i < numElevators; i++) {
            String name = "Elevator" + (i+1);
            JLabel e = new JLabel(name);
            e.setName(name);
            elevators.add(e);
        }
    }

    private void addFloors() {
        for (int i = numFloors; i > 0; i--) {
            JLabel floor = new JLabel("Floor " + i);
            buildingFloors.add(floor);

            for (int j = 0; j < numElevators; j++) {
                JLabel e = new JLabel();
                e.setName("");
                e.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                e.setBackground(Color.WHITE);
                e.setOpaque(true);
                buildingFloors.add(e);
                grid[j][i-1] = e;
            }
        }
        buildingFloors.add(new JLabel("Elevator"));
        for (int i = 0; i < numElevators; i++) {
            buildingFloors.add(new JLabel(""+ (i+1)));
        }
        //buildingFloors.add()
    }

    private void addElevators() {
        for (int i = 0; i < numElevators; i++) {
            JLabel e = elevators.get(i);
            e.setText("<html>" + e.getName() +"<br/>Direction: "+ "<br/>Destination Floor: "+ "<br/>Arrived: "+ "</html>");
            e.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            e.setOpaque(true);
            elevatorsCloseUp.add(e);
        }
    }

    @Override
    public void updateElevator(Elevator elevator) {
        for (int i = 0; i < elevators.size(); i++) {
            if (elevators.get(i).getName().equals(elevator.getName())) {
                HardwareDevice floorEvent = elevator.getMainFloorEvent();
                JLabel e = elevators.get(i);
                e.setText("<html>" + e.getName() +"<br/>Direction: " + floorEvent.getFloorButton() +
                        "<br/>Destination Floor: " + floorEvent.getCarButton() + "<br/>Arrived: " + floorEvent.getArrived() + "</html>");
            }
        }
    }

    @Override
    public void updateFloor(Elevator elevator) {
        for (int i = 0; i < elevators.size(); i++) {
            if (elevators.get(i).getName().equals(elevator.getName())) {
                if (elevator.isTransientFault() == true) {
                    grid[i][elevator.getCurrentFloor() - 1].setBackground(Color.YELLOW);
                } else if (elevator.isHardFault() == true) {
                    grid[i][elevator.getCurrentFloor() - 1].setBackground(Color.RED);
                } else {
                    grid[i][elevator.getCurrentFloor() - 1].setBackground(Color.GREEN);
                    for (int j = 0; j < numFloors; j++) {
                        grid[i][j].setBackground(Color.WHITE);
                    }
                    grid[i][elevator.getCurrentFloor() - 1].setBackground(Color.GREEN);
                }
            }
        }
    }

    @Override
    public void updateFaults(Elevator elevator) {
        for (int i = 0; i < elevators.size(); i++) {
            if (elevators.get(i).getName().equals(elevator.getName())) {
                if (elevator.getCurrentState() == null){
                    grid[i][elevator.getCurrentFloor()-1].setBackground(Color.RED);
                }
                else if ((elevator.getCurrentState() instanceof DoorsNotClosing) || elevator.getCurrentState() instanceof DoorsNotOpening){
                    grid[i][elevator.getCurrentFloor()-1].setBackground(Color.YELLOW);
                }
                else {
                    grid[i][elevator.getCurrentFloor()-1].setBackground(Color.GREEN);
                }
            }
        }
    }
}