/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.test.roots;

import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;

/*
* Project: MASCOT Machine
* Package: com.objectforge.mascot.test.roots
* Created on 25-May-2003
*/
public class TestSubsystems extends AbstractRoot {
	Console console = new Console();
	static int created = 0;
	static int finalized = 0;

	public class Final {
		int number;

		public Final(int number) {
			super();
			this.number = number;
			created++;
		}

		/* (non-Javadoc)
		* @see java.lang.Object#finalize()
		*/
		protected void finalize() throws Throwable {
			super.finalize();
			finalized++;
			System.out.println("+++ final " + number + " finalized, " + finalized + "/" + created + "= +++");
		}

	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.roots.AbstractRoot#root(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		int numberOfSubs = ((Integer) args[0]).intValue();
		
		//Print out the state of estore
		System.out.println(EntityStore.mascotRepository().toString());

		// Generate subsystems and fire them off
		try {
			for (int i = 0; i < numberOfSubs; i++) {
				Subsystem sub = console.form("simple");
				System.out.println("Starting subsystem incarnation #" + i);
				sub.addArgToSubsystem(0, new Integer(i));
				sub.subStart();
			}
		} catch (MascotMachineException e) {
			e.printStackTrace();
		}
		System.out.println("TestSubsystems exits");
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
	 */
	public void resumeRoot() {
		// Nothing to do

	}

}
