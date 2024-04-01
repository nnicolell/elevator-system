# Elevator System

Design and implement an elevator control system and simulator.

The system will consist of an elevator controller, a simulator for the elevator cars, and a simulator for the floors.

<!-- Authors -->
## Authors
* [Alexander Hum](https://github.com/alexhum)
* [Emily Tang](https://github.com/emilyxtang)
* [Nicole Lim](https://github.com/nnicolell)
* [Nivetha Sivasaravanan](https://github.com/nive024)
* [Rimsha Atif](https://github.com/rimshaatif)

## Table of Contents

* [Iteration 4](#Iteration-4)
  * [Responsibilities](#Responsibilities)
  * [Files](#Files)
  * [Setup and Usage](#Setup-and-Usage)
* [Iteration 3](#Iteration-3)
  * [Responsibilities](#Responsibilities)
  * [Files](#Files)
  * [Setup and Usage](#Setup-and-Usage)
* [Iteration 2](#Iteration-2)
  * [Responsibilities](#Responsibilities)
  * [Files](#Files)
  * [Setup and Usage](#Setup-and-Usage)
* [Iteration 1](#Iteration-1)
  * [Responsibilities](#Responsibilities)
  * [Files](#Files)
  * [Setup and Usage](#Setup-and-Usage)

<!-- Iteration 4 -->
## Iteration 4

<!-- Set-up Instructions -->
### Setup and Usage
Open the folder 'L1G6_milestone_4' in IntelliJ and run the main method in [ElevatorSystem.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorSystem.java).
JDK version 21.0.1 was used to develop the code.

<!-- Files -->
### Files
* [Elevator.java](https://github.com/nnicolell/elevator-system/blob/master/src/Elevator.java)
  * The Elevator class represents the elevator car moving up or down floors.
* [ElevatorSystem.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorSystem.java)
  * The ElevatorSystem class contains the main method and is used to test the ElevatorSystem.
* [ElevatorStates.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorStates.java)
  * The ElevatorStates class contains classes representing the different states for the Elevator, these classes implement the ElevatorStates interface.
* [ElevatorState.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorState.java)
  * This interface provides methods to handle the events that cause state transitions for the Elevator.
* [Floor.java](https://github.com/nnicolell/elevator-system/blob/master/src/Floor.java)
  * The Floor class represents the floor where the requests happen.
* [FloorButton.java](https://github.com/nnicolell/elevator-system/blob/master/src/FloorButton.java)
  * The FloorButton enumerator represents whether a passenger would like to move up or down.
* [HardwareDevice.java](https://github.com/nnicolell/elevator-system/blob/master/src/HardwareDevice.java)
  * The HardwareDevice class represents the necessary information to pass to the Scheduler.
* [Scheduler.java](https://github.com/nnicolell/elevator-system/blob/master/src/Scheduler.java)
  * The Scheduler class handles the messaging between the elevator and floor and schedules the requests.
* [SchedulerStates.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerStates.java)
  * The SchedulerStates class represents the different states in the Scheduler machine; the class implements the SchedulerState interface.
* [SchedulerState.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerState.java)
  * The SchedulerStates interface provides methods to handle state transitions for the Scheduler.
* [FloorListener.java](https://github.com/nnicolell/elevator-system/blob/master/src/FloorListener.java)
  * The FloorListener class contains methods to constantly be listening to the Floor for any new events.

#### Test Files
* [ElevatorTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorTest.java)
* [FloorTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/FloorTest.java)
* [HardwareDeviceTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/HardwareDeviceTest.java)
* [SchedulerTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerTest.java)

<!-- Responsibilities -->
### Responsibilities
| Person                   |                                      Responsibilities                                                     |         
| ------------------------ |:---------------------------------------------------------------------------------------------------------:|
| Alexander Hum            |            Scheduler.java, SchedulerTest, UML Class Diagram, Timing Diagrams, README.md                   |
| Emily Tang               |      Elevator.java, ElevatorStates.java, ElevatorTest.java, UML Class Diagram, README.md                  | 
| Nicole Lim               |      Scheduler.java, SchedulerTest, UML Class Diagram, Timing Diagrams, README.md                         |
| Nivetha Sivasaravanan    |            Floor.java, HardwareDevice.java, ElevatorTest, UML Class Diagram, README.md                    |
| Rimsha Atif              |             Elevator.java, ElevatorTest,  UML Class Diagram, Timing Diagrams, README.md                   |


<!-- Iteration 3 -->
## Iteration 3

<!-- Set-up Instructions -->
### Setup and Usage
Open the folder 'L1G6_milestone_3' in IntelliJ and run the main method in [ElevatorSystem.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorSystem.java).
JDK version 21.0.1 was used to develop the code.

<!-- Files -->
### Files
* [Elevator.java](https://github.com/nnicolell/elevator-system/blob/master/src/Elevator.java)
  * The Elevator class represents the elevator car moving up or down floors.
* [ElevatorSystem.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorSystem.java)
  * The ElevatorSystem class contains the main method and is used to test the ElevatorSystem.
* [ElevatorStates.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorStates.java)
  * The ElevatorStates class contains classes representing the different states for the Elevator, these classes implement the ElevatorStates interface.
* [ElevatorState.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorState.java)
  * This interface provides methods to handle the events that cause state transitions for the Elevator.
* [Floor.java](https://github.com/nnicolell/elevator-system/blob/master/src/Floor.java)
  * The Floor class represents the floor where the requests happen.
* [FloorButton.java](https://github.com/nnicolell/elevator-system/blob/master/src/FloorButton.java)
  * The FloorButton enumerator represents whether a passenger would like to move up or down.
* [HardwareDevice.java](https://github.com/nnicolell/elevator-system/blob/master/src/HardwareDevice.java)
  * The HardwareDevice class represents the necessary information to pass to the Scheduler.
* [Scheduler.java](https://github.com/nnicolell/elevator-system/blob/master/src/Scheduler.java)
  * The Scheduler class handles the messaging between the elevator and floor and schedules the requests.
* [SchedulerStates.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerStates.java)
  * The SchedulerStates class represents the different states in the Scheduler machine; the class implements the SchedulerState interface.
* [SchedulerState.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerState.java)
  * The SchedulerStates interface provides methods to handle state transitions for the Scheduler.


#### Test Files
* [ElevatorTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorTest.java)
* [FloorTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/FloorTest.java)
* [HardwareDeviceTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/HardwareDeviceTest.java)
* [SchedulerTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerTest.java)

<!-- Responsibilities -->
### Responsibilities
| Person                   |                                      Responsibilities                                       |         
| ------------------------ |:-------------------------------------------------------------------------------------------:|
| Alexander Hum            |            Floor.java,UML Class Diagram, README.md                                          |
| Emily Tang               |      Scheduler.java, SchedulerTest README.me                                                | 
| Nicole Lim               |      Scheduler.java, SchedulerTest, README.md                                               |
| Nivetha Sivasaravanan    |            Elevator.java, ElevatorTest, README.md                                           |
| Rimsha Atif              |             Floor.java, ElevatorTest,UML Sequence Diagram, README.md                        |



<!-- Iteration 2 -->
## Iteration 2

<!-- Set-up Instructions -->
### Setup and Usage
Open the folder 'L1G6_milestone_2' in IntelliJ and run the main method in [ElevatorSystem.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorSystem.java).
JDK version 21.0.1 was used to develop the code.

<!-- Files -->
### Files
* [Elevator.java](https://github.com/nnicolell/elevator-system/blob/master/src/Elevator.java)
  * The Elevator class represents the elevator car moving up or down floors.
* [ElevatorSystem.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorSystem.java)
  * The ElevatorSystem class contains the main method and is used to test the ElevatorSystem.
* [ElevatorStates.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorStates.java)
  * The ElevatorStates class contains classes representing the different states for the Elevator, these classes implement the ElevatorStates interface.
* [ElevatorState.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorState.java)
  * This interface provides methods to handle the events that cause state transitions for the Elevator.
* [Floor.java](https://github.com/nnicolell/elevator-system/blob/master/src/Floor.java)
  * The Floor class represents the floor where the requests happen.
* [FloorButton.java](https://github.com/nnicolell/elevator-system/blob/master/src/FloorButton.java)
  * The FloorButton enumerator represents whether a passenger would like to move up or down.
* [HardwareDevice.java](https://github.com/nnicolell/elevator-system/blob/master/src/HardwareDevice.java)
  * The HardwareDevice class represents the necessary information to pass to the Scheduler.
* [Scheduler.java](https://github.com/nnicolell/elevator-system/blob/master/src/Scheduler.java)
  * The Scheduler class handles the messaging between the elevator and floor and schedules the requests.
* [SchedulerStates.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerStates.java)
  * The SchedulerStates class represents the different states in the Scheduler machine; the class implements the SchedulerState interface.
* [SchedulerState.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerState.java)
  * The SchedulerStates interface provides methods to handle state transitions for the Scheduler.


#### Test Files
* [ElevatorTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorTest.java)
* [FloorTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/FloorTest.java)
* [HardwareDeviceTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/HardwareDeviceTest.java)
* [SchedulerTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerTest.java)

<!-- Responsibilities -->
### Responsibilities
| Person                   |                                      Responsibilities                                       |         
| ------------------------ |:-------------------------------------------------------------------------------------------:|
| Alexander Hum            |            Scheduler.java, SchdulerTest.java, Scheduler State Diagram, README.md            |
| Emily Tang               |       ElevatorTest.java, SchedulerTest.java, UML Class + Sequence Diagram, README.md        | 
| Nicole Lim               |             Elevator.java, ElevatorTest.java, Elevator State Diagram, README.md             |
| Nivetha Sivasaravanan    | Scheduler.java, SchdulerTest.java, Scheduler State Diagram, UML Sequence Diagram, README.md |
| Rimsha Atif              |             Elevator.java, ElevatorTest.java, Elevator State Diagram, README.md             |


<!-- Iteration 1 -->
## Iteration 1

<!-- Set-up Instructions -->
### Setup and Usage

Open the folder 'L1G6_milestone_1' in IntelliJ and run the main method in [ElevatorSystem.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorSystem.java).
JDK version 21.0.1 was used to develop the code.

<!-- Files -->
### Files
* [Elevator.java](https://github.com/nnicolell/elevator-system/blob/master/src/Elevator.java)
  * The Elevator class represents the elevator car moving up or down floors.
* [ElevatorSystem.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorSystem.java)
  * The ElevatorSystem class contains the main method and is used to test the ElevatorSystem.
* [Floor.java](https://github.com/nnicolell/elevator-system/blob/master/src/Floor.java)
  * The Floor class represents the floor where the requests happen.
* [FloorButton.java](https://github.com/nnicolell/elevator-system/blob/master/src/FloorButton.java)
  * The FloorButton enumerator represents whether a passenger would like to move up or down.
* [HardwareDevice.java](https://github.com/nnicolell/elevator-system/blob/master/src/HardwareDevice.java)
  * The HardwareDevice class represents the necessary information to pass to the Scheduler.
* [Scheduler.java](https://github.com/nnicolell/elevator-system/blob/master/src/Scheduler.java)
  * The Scheduler class handles the messaging between the elevator and floor and schedules the requests.

#### Test Files
* [ElevatorTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/ElevatorTest.java)
* [FloorTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/FloorTest.java)
* [HardwareDeviceTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/HardwareDeviceTest.java)
* [SchedulerTest.java](https://github.com/nnicolell/elevator-system/blob/master/src/SchedulerTest.java)

<!-- Responsibilities -->
### Responsibilities
| Person                   |                                        Responsibilities                                        |         
| ------------------------ |:----------------------------------------------------------------------------------------------:|
| Alexander Hum            |                 Elevator.java, ElevatorTest.java, UML Class Diagram, README.md                 |
| Emily Tang               | HardwareDevice.java, HardwareDeviceTest.java, FloorButton.java, ElevatorSystem.java, README.md | 
| Nicole Lim               |              Scheduler.java, SchedulerTest.java, UML Sequence Diagram, README.md               |
| Nivetha Sivasaravanan    |                             Floor.java, FloorTest.java, README.md                              |
| Rimsha Atif              |              Scheduler.java, SchedulerTest.java, UML Sequence Diagram, README.md               |
