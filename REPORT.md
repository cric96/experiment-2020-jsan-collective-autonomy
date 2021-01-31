# Experiment #1 (31-01-2021) wildlife monitoring: rescue animals in danger. 

## **Research question**:  How different collectives affect the system? (Autonomy/structure relationship)

The environment is a flat square 2500 meters wides. The system has three nodes type:

1. "mobile node" (100 nodes) : capable to perceive and rescue animals in danger. They explore the environment using "boids-like" dynamics. They have a Bluetooth connection with a 200 meters range. They send data sensed to the station and behave according to station instructions.  The average velocity is 8.3 m/s
2. station none (20 nodes): where the leader election happen. They collect the data and then decide what animals should be rescued. Currently, it chooses the animals with the lowest id. It is a Lora gateway with 700 meter range.
3. collar attaches to an animal (5 groups of 20 nodes): It sends the id associated with the animal. It has a Bluetooth connection with 200 meters range. The average velocity is 8.3 m/s

Mobiles drones are placed randomly. The stations are placed in a grid.

The collective behaves following the SCR-pattern (leader election, data collection, information spreading). Here I try two movements configuration:
<ol type="a">
  <li> mobile nodes move according to the leader instruction but don't necessarily stay near it;</li>
  <li> after the leader election, mobile nodes tend to remain in the leader area.</li>
</ol>

Conceptually, b. could be must efficient  because nodes avoid wasting time to wander around.

**Experiment limitation**

- the "danger" sensing is very simplistic: "mobile nodes" check if the "danger" molecule is set to true;
- the "rescue" action is also simplistic, it is instantaneous and change *the* *danger* molecule to false ;
- station program evaluation rate is the same as mobile node: here I imagine a program evaluation frequency  lesser than others nodes;
- the collar evaluates an aggregate program for "broadcast" the danger status;
- the danger changing rate is surrealistic. 
- animals move like mobile nodes, namely simulating a flocking behavior. Here we should found a GPS track data for example. I found [something](https://www.movebank.org/cms/movebank-main) but I can't export data to Open Street Map. 

**Screenshot**

**TODO** 

**Plots**

Currently, I plot only the danger count for each experiment. I run 10 simulations (5 with leader-tendency zone and 5 with fully-wander movement). The leader tendency seems to bring a better system performance. In general, the structure has influenced the overall behavior.

![](C:\Users\gianluca.aguzzi\Desktop\mdpi-jsan\assets\result\31-01-21\leader\leader_01_.png)

*The plot of leader tendency tests.*

![](C:\Users\gianluca.aguzzi\Desktop\mdpi-jsan\assets\result\31-01-21\noleader\noleader_01_.png)

*The plot of wander tendency tests*