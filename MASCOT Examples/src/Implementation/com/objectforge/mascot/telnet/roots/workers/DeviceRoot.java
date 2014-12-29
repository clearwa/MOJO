/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


/*
 * Created on 06-Mar-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.objectforge.mascot.telnet.roots.workers;

import java.util.Vector;

import com.objectforge.mascot.IDA.SPElement;
import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.device.DevicePool;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
 */
public class DeviceRoot extends AbstractRoot {
	private Device device;
	volatile DevicePool devicePool;
	volatile Object deviceInstanceName;

		
	public DeviceRoot(){
		super();
	}

	/* (non-Javadoc)
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		MascotDebug.println(9, "Device Activity starts");
		
		//unpack the arguments
        try {
            device = (Device)args[0];
            devicePool = (DevicePool)args[2];
            deviceInstanceName = args[3];
            
            Object[] channels = { resolve("reader"), resolve("writer")};
            
            devicePool.lock();
            Object[] poolContents = ((Vector) devicePool.snapshot()).toArray();
            Device.PoolContents pc = device.new PoolContents();
            int index = pc.doFind(deviceInstanceName, poolContents);
            
            if (((Object[]) ((SPElement) poolContents[index]).contents)[Device.DEVICE_CHANNS] == null) {
            	Object[] newContents = { deviceInstanceName, channels, new Integer(0)};
            	devicePool.write(newContents);
            }
            devicePool.unlock();
        } catch (RuntimeException e) {
            MascotDebug.println(5,"DeviceRoot.mascotRoot: RuntimeException: " + e);
            e.printStackTrace();
        }
	}

	/* (non-Javadoc)
	 */
	public void resumeRoot() {

	}
}

