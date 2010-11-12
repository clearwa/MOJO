package com.objectforge.mascot.samples;

import java.util.StringTokenizer;
import java.util.Vector;

import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * @author Clearwa
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class IncarnateHosts extends AbstractRoot {
	/**
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		Console console = new Console();

		//The arguments are subsystem names
		for (int i = 0; i < args.length; i++) {
			StringTokenizer substrings =
				new StringTokenizer((String) args[i], ",");
			Vector config = new Vector();
			String subsysName = null;

			for (int j = 0; substrings.hasMoreTokens(); j++) {
				switch (j) {
					case 0 :
						//This is the name of subsystem to start
						subsysName = substrings.nextToken();
						break;

					case 1 :
						//This is the port number
						config.add(0, new Integer(substrings.nextToken()));

					default :
						break;
				}
			}
			try {
				Subsystem aHost = console.form(subsysName, activity.subsystem);
				aHost.addArgToSubsystem(0, resolve("config-channel"));
				write("config-channel", config);
				console.start(aHost);
			} catch (MascotMachineException e) {
				MascotDebug.println(9,"Incarnate Hosts: " + e);
			}
		}
	}

	public void resumeRoot() {
	}
}
