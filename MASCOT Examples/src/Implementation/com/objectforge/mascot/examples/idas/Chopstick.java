/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.examples.idas;
import com.objectforge.mascot.machine.idas.AbstractIDA;
import com.objectforge.mascot.machine.scheduler.ControlQueue;

/**
 * 
 * A channel (possibly pool depending on your interpreation) that holds a 
 * ControlQueue.  Reading delivers a reference to the CQ, writing leaves it.
 * Used by the Philosophers activity to implement sharing chopsticks.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
 */
public class Chopstick extends AbstractIDA {
	ControlQueue resource = new ControlQueue();
	
	public Object read(){
		return resource;
	}
	
	public void write( Object contents ){
		resource.cqLeave();
		return;
	}

}
