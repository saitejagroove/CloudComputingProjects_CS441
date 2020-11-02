# Saiteja Karnati HW2 - CS441

Cloud Homework 2 - Map Reduce on DBLP.xml using hadoop, AWS EMR

Index:
1) Intro
2) Installation and setup
3) Creating Jar
4) Horton Deployment
5) Map/Reduce tasks
6) AWS EMR deployment


## Intro

In this HW2, we try to explore Map reduce tasks in our local Hadoop framework, Horton sandbox and AWS EMR.

##Installation and Setup

Install and setup Hadoop using link https://kontext.tech/column/hadoop/377/latest-hadoop-321-installation-on-windows-10-step-by-step-guide for windows and add Winutils and other files missing in bin. 
This helps working with hadoop in your local machine.


Getting the Jar file: Run sbt clean compile assembly in your project folder and the assembled jar package should be found in the location mentioned in the build file. For me, it is -> Target/Scala2.13/

Next, Install VMware and import the Horton sandbox ovf file with hadoop environment preinstalled. Copy the Jar, wget the data and then you can start the jobs. 
Copy jar (You can use maria_dev as well)
scp -P 2222 dblp.xml root@sandbox-hdp.hortonworks.com:/root 			
Copy data (You can wget from link it as well)
scp -P 2222 dblp.xml root@sandbox-hdp.hortonworks.com:/root
SSH into VM Sandbox:
ssh -p 2222 -l root sandbox-hdp.hortonworks.com

Run:
hdfs dfs -mkdir input_dir
hdfs dfs -put dblp.xml input_dir/ 

Copy output to local machine
hdfs dfs -get output_dir output
exit
scp -P 2222 root@sandbox-hdp.hortonworks.com:/root/output /output
You can also deploy your work in AWS EMR clusters. My youtube demo : https://youtu.be/N3diGXkFrm8 

##Creating Jar:
 
Navigate to the project, in terminal, test using:
sbt clean compile test 

run using:
sbt clean compile run

Get jar package assembled using in the location mentioned:
sbt clean compile assembly

We will be using this Jar in variour remote locations.

## Map Reduce jobs:
InputFormatter-> Custom class splits xml data.


## Running in Horton Sandbox:
Next, Install VMware and import the Horton sandbox ovf file with hadoop environment preinstalled. Copy the Jar, wget the data and then you can start the jobs. 
Copy jar (You can use maria_dev as well)
scp -P 2222 dblp.xml root@sandbox-hdp.hortonworks.com:/root 			
Copy data (You can wget from link it as well)
scp -P 2222 dblp.xml root@sandbox-hdp.hortonworks.com:/root
SSH into VM Sandbox:
ssh -p 2222 -l root sandbox-hdp.hortonworks.com

Run:
hdfs dfs -mkdir input_dir
hdfs dfs -put dblp.xml input_dir/ 

Copy output to local machine
hdfs dfs -get output_dir output
exit
scp -P 2222 root@sandbox-hdp.hortonworks.com:/root/output /output

explore the results in your output dir

## Running in AWS EMR (https://youtu.be/N3diGXkFrm8 )

Create Cluster and associated s3 bucket for storage.

SSH into the Cluster( putty in my case)
wget <datazip>
gunzip <datazip>
hdfs dfs -mkdir input_dir
hdfs dfs -put dblp.xml input_dir/

Now back to console,
Upload the Jar is s3 bucker

Edit security inbound rules to access 8088 port to monitor progress

Add a new step - Custom Jar -> locate our jar


## Map/Reduce jobs

As part of this homework a total of 6 map/reduce jobs were created.
Those are listed below by job name, the relevant classes have intuitive matching names. The association between the classes involved and each job is clearly listed in the MapReduceDriver class. 

* AuthorsbyVenue: This job bins all the authors by venue. The output tuples are of form (venue-Text, Author-Text, numberofpub-int) in a sorted manner.
* Single Author publications: This Job bins all the publications with single author(authors.size==1) by venue. The output tuples are of form (venue- Text, Publication1-text,pub2- text)	
* NumberAuthors: This job bins the publications by number of authors. The output tuples have the following format: (bin, number of pub). (e.g. (2-3, 10))
* AuthorVenue: This job bins the publications by number of authors, venue (article, phdthesis, etc..) and year of publication. Tuples have a composite key, while their value is the number of publications that fit into that category.
* AuthorScore: This job maps each author to their authorship score. The output tuples have the following format: (AuthorName, AuthorshipScore)

