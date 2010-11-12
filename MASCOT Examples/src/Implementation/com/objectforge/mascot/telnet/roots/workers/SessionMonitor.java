/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/
package com.objectforge.mascot.telnet.roots.workers;

import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
 */
public class SessionMonitor extends AbstractRoot {
	static private int sessionCounter = 0;

	/* (non-Javadoc)
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		resumeRoot();
	}

	/* (non-Javadoc)
	 */
	public void resumeRoot() {
		MascotDebug.println( 9, "Session monitor starts");
			
		IIDA reader = (IIDA) getArgs()[1];
		Console console = new Console();
		try {
			while( true ){
				Object[] channels = (Object[]) reader.read();
				Subsystem mc = console.form("MascotConsole",getSubsystem());
					
				mc.addArgToSubsystem(0,channels[1]);
				mc.addArgToSubsystem(1,channels[0]);
					
				String tag = "Host" + ((Integer)channels[3]).toString() + ":" +
					sessionCounter++;
				mc.addArgToSubsystem(2,tag);
					
				console.start(mc);
			}
		} catch (IDAException e) {
			throw new MascotRuntimeException( "IncarnateSessions(resumeRoot): " + e);
		} catch (MascotMachineException e) {
			throw new MascotRuntimeException( "IncarnateSessions(resumeRoot): " + e);
		}
	}

}

