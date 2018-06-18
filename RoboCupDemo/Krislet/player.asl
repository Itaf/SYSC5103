// Agent player in project RoboCup.mas2j

//bloc: location of ball in terms of distance, D, and direction (angle), A
//gloc: location of opponent's goal in terms of direction (angle), A

/* Initial beliefs and rules */
//none

/* Initial goals */
!start.

/* Plans */
+!start : true <- move.

/* ball is NEITHER in FRONT nor at FEET, LOCATE ball */
+bloc(D,A) : D == -1 & A == -1
	<- !locating(ball).	

+!locating(ball) : bloc(D,A) & D == -1 & A == -1 
	<- turn;
	   !locating(ball).	
	
+!locating(ball) : true 
	<- true.	


/* ball in FRONT, ORIENT to ball then APPROACH it */
+bloc(D,A) : D > 1 & not(A == 0)	//undirected to ball
	<- !orienting(ball).	
	   
+!orienting(ball) : bloc(D,A) & D > 1 & not(A == 0) 
	<- turn;
	   !orienting(ball).	
	
+!orienting(ball) : true 
	<- true.	
	
+bloc(D,A) : D > 1 & A == 0			//directed to ball
	<- !approaching(ball).	

+!approaching(ball) : bloc(D,A) & D > 1 & A == 0
	<- dash;
	   !approaching(ball).
	
+!approaching(ball) : true 
	<- true.				


/* ball at FEET, LOCATE opponent's goal then KICK ball */
+gloc(A) : A == -1			//undirected to goal
	<- !locating(goal).

+!locating(goal) : gloc(A) & A == -1
	<- turn;
	   !locating(goal).
	
+!locating(goal) : true 
	<- true.			
	
+gloc(A) : not(A == -1)		//directed to goal
	<- !kicking(ball).	
	   
+!kicking(ball) : gloc(A) & not(A == -1) 
	<- kick;
	   !kicking(ball).
	
+!kicking(ball) : true 
	<- true.	
