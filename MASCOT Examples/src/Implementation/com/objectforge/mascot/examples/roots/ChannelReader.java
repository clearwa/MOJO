/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.examples.roots;

import java.text.DateFormat;
import java.util.Date;

import com.objectforge.mascot.telnet.*;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;

/**
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
 */
public class ChannelReader extends AbstractRoot {

	public void mascotRoot(Activity activity, Object[] args) {
		super.printRoot();
		try {
			setCapacity("serial-cha1", 22);
		} catch (MascotMachineException e) {
			MascotDebug.println(9, "ChannelReader.read: " + e);
		}
		resumeRoot();
	}

	public void resumeRoot() {
		DateFormat df = DateFormat.getTimeInstance(DateFormat.LONG);
		Date startTime = new Date();

		while(args!=null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			try {
				int size = ((Integer) status("serial-cha1")[0]).intValue();
				write(
					"reader-channel",
					MascotCommands.CLEAR
						+ MascotCommands.HOME
						+ "ChannelReader: started "
						+ df.format(startTime));
				write("reader-channel", ", time now " + df.format(new Date()) + "\n");
				if (size < 1) {
					int chstat = ((Integer) status("serial-cha1")[1]).intValue();

					write("reader-channel", "\tChannel size 0 - status " + chstat + "\n");
					//					((SerialChannel)resolve( "serial-cha1" )).trace();
					continue;
				}
				for (int i = 0; i < size; i++) {

					Object contents = read("serial-cha1");
					int chstat = ((Integer) status("serial-cha1")[1]).intValue();
					write("reader-channel", "\t" + i + ":" + chstat + "\t" + contents + "\n");
				}
			} catch (MascotMachineException e) {
				MascotDebug.println(9, "ChannelReader.read: " + e);
			}
		}
	}
}
