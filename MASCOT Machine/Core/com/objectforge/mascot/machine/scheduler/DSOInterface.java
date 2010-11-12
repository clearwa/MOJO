/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.scheduler;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 * DeferredStimObjects implement this interface
 */
public interface DSOInterface {
	/**
	 * Method getNotify.
	 * Returns the notify object
	 */
	public SOInterface getNotify();
	
	/**
	 * Method getDeferred.
	 * Returns the deferred object
	 */
	public SOInterface getDeferred();
	
	/**
	 * Method isBlocked.
	 * Is this call supposed to block?
	 */
	public boolean isBlocked();
	
	
	/**
	 * Method getThread.
	 * Return the thread that created this object
	 */
	public Thread getThread();


}
