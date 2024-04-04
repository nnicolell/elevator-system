import javax.swing.*;
import java.awt.*;
public class ElevatorSystemUI extends JFrame{

    private int width, height;
    private int numOfElevators;
    private JLabel[][] grid;
    private JLabel[] elevators;
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

        elevators = new JLabel[numOfElevators];
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
            JLabel e = new JLabel("Elevator " + (i+1));
            // FIXME: THIS IS HOW YOU ADD A NEW LINE IN JLABEL:
            e.setText("<html>" + e.getText()+"<br/>Direction: "+ "<br/>Current Floor: "+ "<br/>Arrived: "+ "</html>");
            e.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            e.setOpaque(true);
            elevatorsCloseUp.add(e);
            elevators[i] = e;
        }
    }

    public static void main(String[] args) {
        new ElevatorSystemUI(5);
    }

}
