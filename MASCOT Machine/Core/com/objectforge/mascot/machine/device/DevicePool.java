/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


/*
 * Created on 25-Feb-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.objectforge.mascot.machine.device;

import java.util.Vector;

import com.objectforge.mascot.IDA.Type1Pool;
import com.objectforge.mascot.machine.scheduler.ControlQueue;

/**
 * DevicePools store information for devices.  This is an internal class.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public class DevicePool extends Type1Pool {
	ControlQueue lock = new ControlQueue();
	/**
	 * Read the contents.  Prepending count is dependent on snapshot
	 */
	public Object snapshot() {
		Vector retval = new Vector();
		getContents(retval, false); //Get the contents
		return retval;
	}

	/**
	 * 
	 */
	public void lock() {
		lock.cqJoin();
	}

	public void unlock(){
		lock.cqLeave();
	}
	
	public static DevicePool devicePoolFactory(){
		return new DevicePool();
	}
}
