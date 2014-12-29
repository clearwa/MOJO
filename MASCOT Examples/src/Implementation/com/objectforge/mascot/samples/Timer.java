package com.objectforge.mascot.samples;

import java.text.DateFormat;
import java.util.Date;

import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
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
public class Timer extends AbstractRoot {
	DateFormat df = DateFormat.getTimeInstance(DateFormat.LONG);
	String title = "--";

	public void mascotRoot(Activity activity, Object[] args) {
		super.printRoot();
		try {
			title = (String) read("timer-config");
		} catch (MascotMachineException e) {
		}
		resumeRoot();
	}

	public void resumeRoot() {
		for (int count = 0;; count++) {
			try {
				write(
					"serial-cha1",
					title + " " + args[0] + ", count " + count + ", time " + df.format(new Date()));
				Thread.sleep(((Integer) args[1]).intValue());
			} catch (MascotMachineException e) {
				MascotDebug.println(9,"Timer: " + e);
			} catch (InterruptedException e) {
			}
		}
	}

}
