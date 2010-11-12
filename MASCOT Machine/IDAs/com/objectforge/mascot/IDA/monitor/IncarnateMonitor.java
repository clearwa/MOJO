/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/
package com.objectforge.mascot.IDA.monitor;

import com.objectforge.mascot.IDA.telnet.TelnetIO;
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
 * @version $Id$, $Name: 1.2 $
 */
public class IncarnateMonitor extends AbstractRoot {
    Integer port = new Integer(1110);    //The default value

	/* (non-Javadoc)
	
	 * This code opens 1 new instances of the monitor device on port 1110
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		try {
			add("monitor-1110", "monitor");
		} catch (MascotMachineException e) {
			throw new MascotRuntimeException("IncarnateHosts(root): " + e);
		}
		/*
		 * When an instance of the monitor device is running it has no idea of the port it's supposed
		 * to service.  Write a device control packet to start it listening on a particular port.
		 */
		IIDA writer;
        if( getSubsystem().getResource("monitor-port") != null ){
            port = (Integer) getSubsystem().getResource("monitor-port");
        }

		try {
			writer = (IIDA) resolve("monitor", Device.READER);
			writer.write(new DeviceControl( port ));
		} catch (IDAException e1) {
			throw new MascotRuntimeException("IncarnateHosts(root): " + e1);
		}

		TelnetIO io = new TelnetIO(this, null, writer);

		io.println("This is the Incanation Subsystem");
		io.println("This is another line");
	}

	/* (non-Javadoc)
	 */
	public void resumeRoot() {
	}
}
