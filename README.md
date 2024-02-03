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

1. [Iteration 1](#Iteration-1)
   * [Responsibilities](#Responsibilities)
   * [Files](#Files)
   * [Setup and Usage](#Setup-and-Usage)

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
* [FloorTest.java]()
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
