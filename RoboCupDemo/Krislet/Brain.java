//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.asSyntax.*;
import jason.asSemantics.Agent;
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.TransitionSystem;
import jason.infra.centralised.BaseCentralisedMAS;

class Brain extends AgArch implements SensorInput, Runnable
{
    //---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to krislet
    // - starts thread for this object
    public Brain(SendCommand krislet, String team, char side, int number, String playMode)
    {
		m_timeOver = false;
		m_krislet = krislet;
		m_memory = new Memory();
		//m_team = team;
		m_side = side;
		// m_number = number;
		m_playMode = playMode;
		
		if (BaseCentralisedMAS.getRunner() != null)
            BaseCentralisedMAS.getRunner().setupLogger();
		
		try {
			// set up the Jason agent(s)
			Agent ag = new Agent();         
			
			// TransitionSystem (the BDI Engine where the AgentSpeak Semantics is implemented)
			new TransitionSystem(ag, null, null, this);
			
			// player.asl is the file containing the code of the agent
			ag.initAg("player.asl");
	    } catch (Exception e) {
	        //logger.log(Level.SEVERE, "Init error", e);
	    }
		
		// Create a new thread
		myThread = new Thread(this, "player");
		
		// Start the thread
		myThread.start(); 
    }


    //---------------------------------------------------------------------------
    // This is main brain function used to make decisions
    // In each cycle, we decide which command to issue based on
    // current situation. The perceive and act methods below are
    // used for perception and action execution, respectively. 
    // The rules for doing so are:
    //
    //	1. If you don't know where is ball, then turn right and wait for new info
    //
    //	2. If ball is too far to kick it then
    //		2.1. If we are directed towards the ball, then go to the ball
    //		2.2. else turn to the ball
    //
    //	3. If we don't know where is opponent goal, then turn wait 
    //				and wait for new info
    //
    //	4. Kick ball
    //
    //	To ensure that we don't send commands too often after each cycle
    //	we wait one simulator steps. (This of course should be done better)

    // ***************  Improvements ******************
    // Always know where the goal is.
    // Move to a place on my side on a kick_off
    // ************************************************

    public void run()
    {
    	try {
	    	while( isRunning() )
	    	{
	    		// calls the Jason engine to perform one reasoning cycle
		        logger.fine("Reasoning....");
		        getTS().reasoningCycle();
		  
				if (getTS().canSleep()){
					sleep();
				}
	    	}
	    	
			m_krislet.bye();
    	}
    	catch (Exception e) {
			//logger.log(Level.SEVERE, "Run error", e);
		}
    	
    }
    
    	
    public String getAgName() {
    	return "player";
    }


 	//===========================================================================
    // Overridden methods of  AgArch class
    //---------------------------------------------------------------------------
	
	// this method just adds some perception for the agent
	@Override
    public List<Literal> perceive() {
		// change list p by adding/removing literals to
		// simulate faulty perception
        List<Literal> p = new ArrayList<Literal>();

        // generate percepts
    	if(Pattern.matches("^before_kick_off.*",m_playMode))
    	{
    		percept = Literal.parseLiteral("start");
    	}
    	else
    	{
	        object = m_memory.getObject("ball");
			
			if( object == null )
			{
				percept = Literal.parseLiteral("bloc(-1,-1)");
			}
			else if( object.m_distance > 1.0 )
			{	
				percept = Literal.parseLiteral("bloc("+object.m_distance+","+object.m_direction+")");
			}
			else 
			{
				// set opponent's goal
				if( m_side == 'l' )
					object = m_memory.getObject("goal r");
				else
					object = m_memory.getObject("goal l");
		
				if( object == null )
				{
					percept = Literal.parseLiteral("gloc(-1)");
				}
				else
				{
					percept = Literal.parseLiteral("gloc("+object.m_direction+")");
				}
			}
    	}
        
		p.add(percept);
        return p;
    }

    // this method get the agent actions
    @Override
    public void act(ActionExec action) 
    {
        //getTS().getLogger().info("Agent " + getAgName() + " is " + action.getActionTerm() + "ing");
        
        if( action.getActionTerm().equals(MOVING) )
        {
        	// first put it somewhere on my side
    	    m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );
        }
        else if( action.getActionTerm().equals(TURNING) ) 
        {
        	if( object == null )	//LOCATING the ball or goal
        	{
				// if you don't know where is ball or goal, then find it
				m_krislet.turn(40);    
				m_memory.waitForNewInfo();
        	}
        	else	//ORIENTING
        	{
            	// if ball is too far then turn to ball
    			m_krislet.turn(object.m_direction);
        	}
        }
        else if( action.getActionTerm().equals(DASHING) ) 	//APPROACHING the ball
        {
			// if we have correct direction then go to ball
			m_krislet.dash(10 * object.m_distance);
        }
        else /*if( action.getActionTerm().equals(KICKING) )*/	//KICKING the ball
        {
			m_krislet.kick(100, object.m_direction);
        }
        
        // set that the execution was ok
        action.setResult(true);
        actionExecuted(action);
    }

    @Override
    public boolean canSleep() {
        return true;
    }

    @Override
    public boolean isRunning() {
    	if( m_timeOver )
    	{
    		return false;
    	}
    	
        return true;
    }

    // a very simple implementation of sleep
    public void sleep() {
    	// sleep one step to ensure that we will not send
    	// two commands in one cycle.
    	try{
    		Thread.sleep(2*SoccerParams.simulator_step);
    	}catch(Exception e){}
    }

    // Not used methods
    // This simple agent does not need messages/control/...
    @Override
    public void sendMsg(jason.asSemantics.Message m) throws Exception {
    }

    @Override
    public void broadcast(jason.asSemantics.Message m) throws Exception {
    }

    @Override
    public void checkMail() {
    }			

    
    //===========================================================================
    // Here are supporting functions for implement logic
    //===========================================================================
    // Implementation of SensorInput Interface
    //---------------------------------------------------------------------------
    // This function sends see information
    public void see(VisualInfo info)
    {
    	m_memory.store(info);
    }


    //---------------------------------------------------------------------------
    // This function receives hear information from player
    public void hear(int time, int direction, String message)
    {
    }

    //---------------------------------------------------------------------------
    // This function receives hear information from referee
    public void hear(int time, String message)
    {						 
		if(message.compareTo("time_over") == 0)
		    m_timeOver = true;
		
		if(message.compareTo("before_kick_off") != 0)
			m_playMode = message;
    }


    //===========================================================================
    // Private members
    private SendCommand	     m_krislet;			// robot which is controlled by this brain
    private Memory			 m_memory;			// place where all information is stored
    private char			 m_side;
    volatile private boolean m_timeOver;
    private String           m_playMode;

    private Thread myThread = null;
	private ObjectInfo object;	// object of interest: ball and/or goal
    private Literal percept;
	
	private static Logger logger = Logger.getLogger(Brain.class.getName());

	private static final Term MOVING = Literal.parseLiteral("move");
	private static final Term TURNING = Literal.parseLiteral("turn");
	private static final Term DASHING = Literal.parseLiteral("dash");
	private static final Term KICKING = Literal.parseLiteral("kick");
}
