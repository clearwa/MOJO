/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/
package com.objectforge.mascot.telnet.roots.workers;

import java.io.IOException;
import java.net.Socket;

import com.objectforge.mascot.IDA.telnet.TelnetConnection;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
 */
public class TelnetSocket extends AbstractRoot {

	/* (non-Javadoc)
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		Console console = new Console();
		
		Object[] channels = (Object[]) getSubsystem().getArgFromSubsystem(1);
		channels[0] = resolve("reader");
		channels[1] = resolve("writer");
		ControlQueue sync = (ControlQueue) getSubsystem().getArgFromSubsystem(2);
		sync.cqStim();

		TelnetConnection connection = null;
		try {
			connection = new TelnetConnection((Socket) channels[2]);
		} catch (IOException e) {
			throw new MascotRuntimeException("TelnetSocket(root): " + e);
		}

		Subsystem session = null;
		try {
			session = console.form("TelnetSession", getSubsystem());
		} catch (MascotMachineException e1) {
			throw new MascotRuntimeException("TelnetSocket(root): " + e1);
		}
		session.addArgToSubsystem(0, connection);
		console.start(session);
	}

	/* (non-Javadoc)
	 */
	public void resumeRoot() {
	}
}

