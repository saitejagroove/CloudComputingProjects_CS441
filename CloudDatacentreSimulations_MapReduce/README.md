Sai Teja Karnati - Cloud HW1:

Cloud Homework 1- Cloudsim simulations of various models

I will explore different simulations with different parameter. Some use Timeshared Policy while others use Space shared policy. We also use Mapper and reducer cloudlets using custom Cloudlet,Broker, Host classes extending from default Cloudsim classes.

I used SCALA lang for Simulations, some Java for custom extended classes and the project is built in SBT(Compatible with Intellij).

###Installation instructions:
Steps to run simulation using Intellij

1. Open IntellJ IDEA with scala plugin.
2. Open the downloaded and extracted project
3. Use SBT configurations to work with this project when asked.
4. Build the project from the top option.
4. Go to SRC->SCALA->(open any of the Multiple simulations) and run it

###Installation and running simulation instructions:
Steps:
Open project using Intellij.
Build the project with SBT Configurations
Once the project is build, you can go to terminal and enter the following commands:
"sbt clean compile test" to runs tests 
"sbt clean comile run" and choose the respective simulatioin class to run from options

You can do this in commandline without opening in Intellij as well.


###Simulations

The following Simulations are provided:

- CsimTimeSharing: Simulation with Time Sharing.
- CsimNoTimeSharing: Simulation without Time Sharing.
- CsimDataSharing: Simulation with  Space Sharing.
- CsimNoDataSharing: Simulation without Space Sharing.
- CsimLowRes: Simulation with Data Locality policy, Space Sharing and reduced number of VMs, Hosts.
- Simulation2blow: Simulation *without* Data Locality policy, Space Sharing and reduced number of VMs, Hosts.

#### Tests

The following test classes are provided:

- Simulation1Test
- MyBrokerTest

Checks nulls, corrent num of hosts, recursive calls, num of vms.

### Configuration parameters
3 diff config files, 1 for Timesharing, 1 for Space Sharing and 1 for Low resources
Datacenters = 3 in all the config files,
Hosts = 20,20,10  
Hosts = 5,5,10 
cost = 3.0
costPerMem = 0.05
costPerStorage = 0.1

explore the Config files in resources for more details.

###3 Map/Reduce architecture

We explore Map/reducec archihtecture and the manage the cloudlets

Key details:
- Chunks of equal size 300mb
- 2-layer architecture (mapper + reducer layer)
- Dynamic number of reducers (80:40)

Data chunks of 300 mb->Mapper-> Reducer.
Reducer waits for atleast 2 mappers to finish before starting.


### Analysis of results


5 main simulations- TimeShared,No Timeshared, SpaceShared, No Space shared and both shared with low resources, Hybrid (Still in progress)

#### Metric: cost
TimeShared Sim: 472.2 units for 3 datacenter (20,20,10) and 80 Mapper,40 reducers - used around 30 reducers only
No TimeShared Sim: 576.6 units for 3 datacenter (20,20,10) and 80 Mapper,40 reducers - used around 30 reducers only
SpaceShared Sim: 430.2 units for 3 datacenter (20,20,10) and 80 Mapper,40 reducers - used around 20 reducers only
No SpaceShared Sim: 540.6 units for 3 datacenter (20,20,10) and 80 Mapper,40 reducers - used around 20 reducers only
TimeShared and Space Shared Sim: 431.2 units for 3 datacenter (20,20,10) and 80 Mapper,40 reducers - used around 40 reducers only
TimeShared Sim: 472.2 units for 3 datacenter (20,20,10) and 80 Mapper,40 reducers - used around 30 reducers only


The generated cloudlet schedule along with each individual cost is reported in the respective .csv files. (TimeShared.csv,NoTimeShared.csv, SpaceShared.csv, NoSpaceShared.csv,BothShared.csv)

###### SpaceShared vs TimeShared

We notice that  that costs in the TimeShared environment are higher than in the SpaceShared one; this is due to the fact that whenever there are not enough VMs to run all the cloudlets independently, the TimeShared policy allocates cloudlets on the same VM and runs than concurrently, therefore yelding to a higher cost.
We can think of it as if the computational resources (i.e. CPUs) are used by the two cloudlets alternatively. (shared CPUs) Using both reduces the costs doesn't significantly mainly due to other parameters

##### Hybrid model 
Still working on  the Hybrid model and allocations.


