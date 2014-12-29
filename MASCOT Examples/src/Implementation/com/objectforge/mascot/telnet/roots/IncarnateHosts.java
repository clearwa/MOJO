/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/
package com.objectforge.mascot.telnet.roots;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
 */
public class IncarnateHosts extends AbstractRoot {

	/* (non-Javadoc)

	 * This code opens 3 new instances of the telnet device on ports 23, 17001, and 18001
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		try {
			add("telnet-device-23", "device-1");
			add("telnet-device-17001","device-2");
			add("telnet-device-18001","device-3");
		} catch (MascotMachineException e) {
			throw new MascotRuntimeException( "IncarnateHosts(root): " + e);
		}
		/*
		 * When an instance of the telnet device is running it has no idea of the port it's supposed
		 * to service.  Write a device control packet to start it listening on a particular port.
		 */
		try {
			((IIDA)resolve("device-1",Device.READER)).write(new DeviceControl(new Integer(23)));
			((IIDA)resolve("device-2",Device.READER)).write(new DeviceControl(new Integer(17001)));
			((IIDA)resolve("device-3",Device.READER)).write(new DeviceControl(new Integer(18001)));
		} catch (IDAException e1) {
			throw new MascotRuntimeException( "IncarnateHosts(root): " + e1);
		}
	}

	/* (non-Javadoc)
	 */
	public void resumeRoot() {

	}
}

