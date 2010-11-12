/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.scheduler;


/**
 * StimObjects implement this interface
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public interface SOInterface {
	/**
	 * Method soRelease.
	 * Release all of the threads wainting on this instance
	 */
	public void soRelease();

	/**
	 * Method waitForStim.
	 * If no pending stim then block the calling thread until an soRelease call.
	 */
	public void waitForRelease();
	
	/**
	 * Method soStim.
	 * Stim all of the currently blocked threads
	 */
	public void soStim();
	
	/**
	 * Method waitForStim.
	 * If there is not pending stim on this instance then block until an soStim call
	 */
	public void waitForStim();
	
	/**
	 * Method getCount.
	 * Get number of events in an instance
	 */
	public int getCount();

	/**
	 * Method hasEvents.
	 * Are there events on the event queue
	 */
	public boolean hasEvents();

	/**
	 * Method getEvent.
	 * Get an event at index in the event vector.  If no event
	 * then returns null
	 */
	public CQEvent getEvent(int index);

	/**
	 * Method removeEvent.
	 * Like getEvent but the event is removed from the vector
	 */
	public CQEvent removeEvent(int index);
	
	/**
	 * Method removeEvent.
	 * Remove entry event from the instance if it is still there.
	 */
	public void removeEvent( CQEvent event);

	/**
	 * Method addEvent.
	 * Add an event
	 */
	public void addEvent(CQEvent event);

}
