# Experiments references
|   | configuration file            | node / station Scala file | "animal" Scala file  |
|---|-------------------------------|---------------------------|----------------------|
| 1 | wildlife-monitoring-mutable   | FeedbackMutableArea       | SmartCollarBehaviour |
| 2 | wildlife-monitoring-structure | WildLifeMonitoring        | AnimalBehaviour      |
# GUI Shapes explanation
## Station
They are fixed nodes. Here, the leader election happens. The circular area is an
approximation of the zone of leader influence.

<div style = "width : 70px">

![](assets/gui/area.png)
</div>

## Animal
Color information: each animal belongs to one group. The animals in the same group have the same color.
### Healthy
![](assets/gui/animal.png)
### In danger
![](./assets/gui/animal-danger.png)

## Mobile nodes
Color information: each mobile node belongs to a zone created by sparse choice (S). The color is the same as the leader.
### Healer
Since experiment #2, we include node capabilities. In this case, this node could heal an animal.
![](./assets/gui/healer.png)
### Explorer
![](./assets/gui/explorer.png)
# Experiment #2.1 (24-02-2021) wildlife monitoring: problem with #2
The plot in #2 was too noisy to make some conclusion. For this reason I try to run multiple run of this simulation.
Unfortunately, with 500 runs for each configuration, the average results converge.

**Plots**

![](./assets/result/24-02-21/200-iteration.png)

with 200 runs 

![](./assets/result/24-02-21/problem.png)

with 500 runs.

# Experiment #2 (22-02-2021) wildlife monitoring: mutable area formation based on entity capabilities.

## **Research question**: How local behaviour (autonomy) could infect the collective behaviour and structure?

The configuration is similar to #1. The main differences are:
1. there are two mobility node type, "healer" nodes (i.e. one with the capability to heal animals) and "explorer" nodes (i.e. one with the capability to explore an area). Currently, there are 50 healer nodes and 50 explorer nodes, in the next experiment the capabilities should be found at runtime.
2. the collective uses SCR-pattern, but the "area of influence" of the leader changes according to the node numbers inside the area (inspired by https://doi.org/10.1016/j.future.2020.07.032). More nodes are inside an area, and more the leader has lesser influence.

Conceptually, using mutable area formation, the data might spread rapidly and go only in the interested devices.
**Experiment limitations**
- the node capabilities are fixed.
- same as #1

**Screenshot**
![](./assets/result/22-02-21/snapshot.png)

**Plots**
Again, I plot only the danger count for each experiment.
I run 200 simulations (100 with fixed area zone and 100 with "mutable" area).
The mutable area seems to have better performance in terms heal animal, but perhaps a 
statistical test is needed.

![](./assets/result/22-02-21/result.png)

*The plot of leader tendency tests. Result from ./gradlew wildLifeMonitoring *

# Experiment #1 (31-01-2021) wildlife monitoring: rescue animals in danger. 

## **Research question**:  How different collectives affect the system? (Autonomy/structure relationship)

The environment is a flat square 2500 meters wides. The system has three nodes type:

1. "mobile nodes" (100 nodes) : capable to perceive and rescue animals in danger. 
   They explore the environment using "boids-like" dynamics. 
   They have a Bluetooth connection with a 200 meters range. 
   They send data sensed to a station and behave according 
   to station instructions.  The average velocity is 8.3 m/s.
2. station nodes (25 nodes): where the leader election happen. 
   They collect the data sensed by mobile nodes and then decide what animals should 
   be rescued. Currently, they choose the animal with the lowest id. 
   Conceptually, they are Lora gateways with 700 meters range.
3. "smart" collars (5 groups of 20 nodes): They send the id associated with an animal. 
   They have a Bluetooth connection with 200 meters range. The average animal velocity 
   is 8.3 m/s.

The network structure is inspired by this [paper](https://ieeexplore.ieee.org/abstract/document/8328721).
Mobiles drones are placed randomly. The stations are placed in a grid 5x5 covering the whole 
environment area.
The animals are divided into five groups placed, four in the square corners and one in the square 
centre.

The collective behaves following the SCR-pattern (leader election, data collection, 
information spreading). Here I try two movements configuration:
<ol type="a">
  <li> mobile nodes move according to the leader instruction but don't necessarily 
stay near it;</li>
  <li> after the leader election, mobile nodes tend to remain in the leader area.</li>
</ol>

Conceptually, b. could be more efficient than a. because nodes avoid wasting time to 
wander around. These two experiments could give us some insight for our research question.

**Synthetic Configuration**
- aggregate program frequency : 1 Hz
- aggregate program for mobile node: WildLifeMonitoring
- aggregate program for station: WildLifeMonitoring
- aggregate program for smart collar: AnimalBehaviour
- movement evaluation update : 10 Hz
- danger molecule change rate : 0.01 Hz
- simulation length : 300s
- environment size: 2500x2500 m
- mobile nodes count: 100
- animals nodes count: 100
- stations count : 25
- bluetooth range: 200 m
- lora range : 700 m
- average velocity : 8 m/s

**Experiment limitation**
- the "danger" sensing is very simplistic: "mobile nodes" check if the "danger" 
  molecule is set to true;
- the "rescue" action is also simplistic, it is instantaneous, and it changes *the* 
  *danger* molecule to false;
- station program evaluation rate is the same as mobile node: here I imagine a program 
  evaluation frequency lesser than others nodes;
- the collar evaluates an aggregate program for "broadcast" the danger status;
- the danger changing rate is surrealistic. 
- animals move like mobile nodes, namely simulating a flocking behavior. 
  Here we should use GPS track data for example. 
  I found [something](https://www.movebank.org/cms/movebank-main) but I can't export 
  data for Open Street Map. 

**Screenshot**
![](./assets/result/31-01-21/snapshot.png)

**Plots**
Currently, I plot only the danger count for each experiment. 
I run 20 simulations (10 with leader-tendency zone and 10 with fully-wander movement).
The leader tendency seems to bring a better system performance. 
In general, the structure has influenced the overall behavior.

![](./assets/result/31-01-21/leader/leader_01_.png)

*The plot of leader tendency tests. Result from ./gradlew wildLifeMonitoring*

![](./assets/result/31-01-21/noleader/noleader_01_.png)

*The plot of wander tendency tests. Result from ./gradlew wildLifeMonitoringNoLeader*
