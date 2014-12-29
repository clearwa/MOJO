/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.model;

/**
 * The interface that tags and defines an activity.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */
public interface IActivity {
	/**
	 * Method actStart.
	 * Start am activity
	 */
	public void actStart(String activityName);
	/**
	 * Method actSuspend.
	 * Suspend an activity
	 */
	public void actSuspend();
	/**
	 * Method actResume.
	 * Resume a suspended activity
	 */
	public void actResume();
	/**
	 * Method actTerminate.
	 * Terminate an activity
	 */
	public void actTerminate();
	/**
	 * Method resolve.
	 * Resolve the IDA reference supplied in connectionRef
	 */
	public Object resolve( Object connectionRef );

	/**
	 * Resolve to a channel inside a deviceRef
	 */
	public Object resolve(Object deviceRef, String connectionRef);
}
