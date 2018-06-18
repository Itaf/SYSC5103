To run the project on Windows:- 

1) Download the "RoboCupProject(Itaf+Motasem).zip"

2) Extract the files

3) Start the server
	- Go into “rcssserver-14.0.3-win”
	- Double click on “rcssserver.exe”

4) Start the monitor 
	- Go into “rcssmonitor-14.1.0-win”
	- Double click on “rcssmonitor.exe”

5) REPEAT 5 times:
	- Open Command Prompt
	- Change the current working directory:
		cd ../RoboCupProject(Itaf+Motasem)/RoboCupDemo/Krislet

	- Set the path to Java's JDK
		For example, set PATH="C:\Program Files\Java\jdk1.8.0_152\bin"

	- Set the path to the JASON classes and Krislet classes
		set CLASSPATH=../RoboCupProject(Itaf+Motasem)/jason-2.2a/lib/jason.jar;../RoboCupProject(Itaf+Motasem)/RoboCupDemo/Krislet;

	- Compile Krislet
		javac Krislet.java

	- Run an instance of the soccer-playing agent
		- Type the following line 5 times:
			java Krislet -team <team_name>

		- Try this for example: 
			java Krislet -team Carleton


Full Example:
I:
cd I:\ITAF\UNIVERSITY\SYSC\5103\Project\RoboCupDemo\Krislet
set PATH="C:\Program Files\Java\jdk1.8.0_152\bin"
set CLASSPATH=I:\ITAF\UNIVERSITY\SYSC\5103\Project\RoboCupDemo\Krislet;I:\ITAF\UNIVERSITY\SYSC\5103\Project\jason-2.2a\libs\jason-2.2.jar;
javac Krislet.java
java Krislet -team Carleton
ping localhost