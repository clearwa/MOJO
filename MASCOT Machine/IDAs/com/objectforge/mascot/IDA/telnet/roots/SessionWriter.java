/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.IDA.telnet.roots;

import com.objectforge.mascot.IDA.SerialChannel;
import com.objectforge.mascot.IDA.TokenPool;
import com.objectforge.mascot.IDA.telnet.ITelnetConnection;
import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * Instances of SessionWriter read from TelnetConnection and write to writer
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class SessionWriter extends AbstractRoot {
	ITelnetConnection myConnection;

	/* (non-Javadoc)
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		myConnection = (ITelnetConnection) getSubsystem().getArgFromSubsystem(0);
		resumeRoot();
	}

	/* (non-Javadoc)
	 */
	public void resumeRoot() {
		boolean error = false;

		try {
			/*
			 * Under normal circumstances read from the connection and write to writer.
			 */
			while (args!=null) {
				Object toSend = myConnection.readLine();

				try {
					write("writer", toSend);
				} catch (MascotMachineException e) {
					MascotDebug.println(9, "SessionWriter(resumeRoot): " + e);
					error = true;
					break;
				}
			}
		} catch (Exception e) {
			MascotDebug.println(9, "SessionWriter(resumeRoot): " + e);
			error = true;
//            e.printStackTrace();
		}

        if( resolve("token-pool") != null ){
            try {
                ((TokenPool)resolve("token-pool")).write();
            } catch (IDAException e1) {
            }
        }
		/*
		 * On an error or when connection is closing tell everyone that I'm on the way down.
		 */
		if (!myConnection.isClosing() || error) {
			/*
			 * Tell the reader side I'm dying.
			 */
			try {
                ((SerialChannel)resolve("reader")).setTerminate(true);
				write("reader", new DeviceControl("exit"));
			} catch (MascotMachineException e1) {
				MascotDebug.println(9, "SessionWriter(resumeRoot<closing reader>): " + e1);
			}
		}
	}
}

