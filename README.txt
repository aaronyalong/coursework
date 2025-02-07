Aaron Yalong (ayalong@uci.edu) 16865957
In my folder there is a list of files:
input.txt (with test cases, can be edited for testing)
out (folder created when I changed the name of my folder)
output.txt (output produced)
iml file
src (source files explained below)

In my source I have 5 .java files and the main executable file is named Shell.java. My other classes are ProcessCB, ReadyList, Resource, and ResourceCB which are all necessary to execute my shell.java. 

The IDE I used was IntelliJ. I imported my project using IntelliJ with JDK 22 version. To run it, to the Shell.Java file (which contains main class) and build->compile from there. it should produce an output.txt which is identical to the one already in this zipped folder. You do not need to use command lines, instead you can edit the input.txt file if you want to add test cases. Doing so will change the output.txt after compilation.


Project Specifications: Data Structures and Functions

	•	Design and implement the extended version of the manager, including:
	•	PCB, RCB, and RL data structures
	•	functions create(), destroy(), request(), release(), timeout(), scheduler(), init(), init_default()
	•	Design and implement the shell (see command language and output specifications)
	•	Instantiate manager to include the following at start-up:
	•	A process descriptor array PCB[16]
	•	A resource descriptor array RCB[4] with multiunit resources
	•	The inventory of each resource is as specified in the init command
	•	A multi-level RL based on the parameters specified in the init command
	•	The init() function should always perform the following tasks:
	•	Erase all previous contents of the data structures PCB, RCB, RL
	•	Set/reset the number of levels in the RL to the value specified in the init command
	•	Set/reset the inventory of the resources to the value specified in the init command
	•	Create a single running process at PCB[0] with priority 0
	•	Enter the process into the RL at the lowest-priority level 0