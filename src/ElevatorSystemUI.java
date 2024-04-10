import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that creates a user interface for the elevator system.
 */
public class ElevatorSystemUI extends JFrame implements ElevatorSystemView {

    /**
     * Integers representing the number of elevators and floors for the ElevatorSystem.
     */
    private final int numElevators, numFloors;

    /**
     * A 2D array of JLabels representing the grid for the elevators and floors.
     */
    private final JLabel[][] grid;

    /**
     * An ArrayList of JLabels of running elevators.
     */
    private final ArrayList<JLabel> elevators;

    /**
     * A JPanel that contains the floors and the elevators.
     */
    private final JPanel buildingFloors;

    /**
     * A JPanel that contains a more details about the elevator cars.
     */
    private final JPanel elevatorsCloseUp;

    /**
     * A DefaultListModel of HardwareDevices for the request log.
     */
    private final DefaultListModel<HardwareDevice> listRequest;

    /**
     * Initializes an ElevatorSystemUI.
     *
     * @param numElevators An integer representing the number of elevators.
     * @param numFloors  An integer representing the number of floors.
     * @param elevatorList A List of Elevators to run.
     */
    public ElevatorSystemUI(int numElevators, int numFloors, List<Elevator> elevatorList) {
        super("Elevator System");
        this.numElevators = numElevators;
        this.numFloors = numFloors;

        elevators = new ArrayList<>();

        setElevators(); // sets the JLabels of the elevators

        // adds views to the elevators
        for (Elevator e : elevatorList) {
            e.setView(this);
        }

        // sets the width and height of the UI
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        // panel for the building floors
        buildingFloors = new JPanel();
        buildingFloors.setLayout(new GridLayout(numFloors+1, numElevators));
        buildingFloors.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // grid of the floors
        grid = new JLabel[numElevators][numFloors];
        addFloors();

        // panel for the elevators and its closeups
        JPanel elevatorsPanel = new JPanel();
        elevatorsPanel.setLayout(new GridLayout(2, 1));

        elevatorsCloseUp = new JPanel();
        elevatorsCloseUp.setLayout(new GridLayout(1, numElevators));

        addElevators();
        elevatorsPanel.add(elevatorsCloseUp);
        listRequest = new DefaultListModel<>();

        JList requestsLog = new JList<>(listRequest);
        JScrollPane scrollPaneRequest = new JScrollPane(requestsLog);
        scrollPaneRequest.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPaneRequest.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        elevatorsPanel.add(scrollPaneRequest);
        elevatorsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel("ELEVATOR SYSTEM");
        titleLabel.setFont(new Font("Verdana", Font.PLAIN, 30));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        this.add(titleLabel, BorderLayout.NORTH);
        this.add(buildingFloors, BorderLayout.WEST);
        this.add(elevatorsPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(width /2, height /2);
        this.setResizable(true);
        this.setVisible(true);
    }

    /**
     * Sets the elevator JLabels.
     */
    private void setElevators() {
        for (int i = 0; i < numElevators; i++) {
            String name = "Elevator" + (i+1);
            JLabel e = new JLabel(name);
            e.setName(name);
            elevators.add(e);
        }
    }

    /**
     * Adds the floors and elevators grid to the buildingFloors JPanel.
     */
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
    }

    /**
     * Adds a close up of each elevator.
     */
    private void addElevators() {
        for (int i = 0; i < numElevators; i++) {
            JLabel e = elevators.get(i);
            e.setText("<html>" + e.getName() +"<br/>Direction: "+ "<br/>Destination Floor: "+ "</html>");
            e.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            e.setOpaque(true);
            elevatorsCloseUp.add(e);
        }
    }

    /**
     * Updates the close up of the specified elevator.
     *
     * @param elevator An Elevator representing the elevator that has to be updated.
     */
    @Override
    public void updateElevator(Elevator elevator) {
        for (JLabel jLabel : elevators) {
            if (jLabel.getName().equals(elevator.getName())) {
                HardwareDevice floorEvent = elevator.getMainFloorEvent();
                if (floorEvent != null) {
                    jLabel.setText("<html>" + jLabel.getName() + "<br/>Direction: " + floorEvent.getFloorButton() +
                            "<br/>Destination Floor: " + floorEvent.getCarButton() + "</html>");
                }
            }
        }
    }

    /**
     * Updates the floor of the elevator to be filled in with the required colour.
     *
     * @param elevator An Elevator representing the elevator that has to be updated.
     */
    @Override
    public void updateFloor(Elevator elevator) {
        for (int i = 0; i < elevators.size(); i++) {
            if (elevators.get(i).getName().equals(elevator.getName())) {
                if (elevator.isTransientFault()) {
                    grid[i][elevator.getCurrentFloor() - 1].setBackground(Color.YELLOW);
                } else if (elevator.isHardFault()) {
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

    /**
     * Adds a request to the JList.
     *
     * @param request A HardwareDevice representing the new request.
     */
    @Override
    public void addRequests(HardwareDevice request) {
            listRequest.addElement(request);
    }

}