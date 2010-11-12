/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.scheduler;
import java.util.Vector;

/**
 * Implenmentaion of SOInterface
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */

//An object to stim remotely
public class StimObject implements SOInterface {

	/**
	 * A StimObject has 1 field:<br>
	 * 
	 * Vector events<br>
	 * A queue of CQEvents that have been posted against an instance.
	 */
	private Vector events = new Vector(); //The events vector

	//A counting que 
	private CountingQI waiters = ControlQueue.CountingQFactory();

	/**
	 * Stim the wainting que to start cascade.
	 */
	public void soRelease() {
		waiters.stim();
	}

	/**
	 * Release all threads waiting on the CountingQ
	 */
	public void waitForRelease() {
		waiters.join();
		waiters.waitQ();
		waiters.leave();

		if (waiters.status())
			waiters.stim();
	}
	
	/**
	 */
	public void soStim(){
	}
	
	/**
	 */
	public void waitForStim(){
	}
	
	/**************************************************************************
	 * Convenience routinges for manipulating the event vector
	 **************************************************************************/
	
	/**
	 * Returns true if there are any events in the events vector, otherwise false.
	 */
	public synchronized boolean hasEvents() {
		return events.size() > 0;
	}

	/**
	 */
	public int getCount() {
		return events.size();
	}

	/**
	 * Return the event at index in the events vector.
	 * Safe access to the events vector.  Returns null if the index is out of bounds.
	 */
	public synchronized CQEvent getEvent(int index) {
		int size = events.size();
		if (size == 0)
			return null;
		return (size > index) ? (CQEvent) events.elementAt(index) : null;
	}

	/**
	 * Like getEvent but the event at index is removed form the vector if it exists.  Otherwise
	 * returns null.
	 */
	public synchronized CQEvent removeEvent(int index) {
		int size = events.size();

		if (size == 0 || size<=index )
			return null;
			
		CQEvent retval = (CQEvent) events.elementAt( index );
		events.removeElementAt(index);
		return retval;
	}

	public synchronized void removeEvent(CQEvent event) {
		events.remove(event);
	}

	/**
	 * Add an event to the event vector
	 */
	public synchronized void addEvent(CQEvent event) {
		events.add(event);
	}


}
