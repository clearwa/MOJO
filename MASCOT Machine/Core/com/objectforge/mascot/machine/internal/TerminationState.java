/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.internal;

/*
* Project: MASCOT Machine
* Package: com.objectforge.mascot.machine.internal
* Created on 24-May-2003
*/
public class TerminationState {
	private boolean dead;
	private boolean suspend;
	
	/**
	 * @return
	 */
	public boolean getDead() {
		return dead;
	}

	/**
	 * @return
	 */
	public boolean getSuspend() {
		return suspend;
	}

	private TerminationState(){
		super();
	}

	public static TerminationState kill(){
		TerminationState retval = new TerminationState();
		retval.dead = true;
		retval.suspend = false;
		return retval;
	}

	public static TerminationState suspend(){
		TerminationState retval = new TerminationState();
		retval.dead = true;
		retval.suspend = true;
		return retval;
	}

	public static TerminationState resume(){
		TerminationState retval = new TerminationState();
		retval.dead = false;
		retval.suspend = false;
		return retval;
	}
	
	public boolean isKill(){
		return dead && !suspend;
	}
	
	public boolean isSuspend(){
		return dead && suspend;
	}
	
	public boolean isResume(){
		return !dead && !suspend;
	}
}
