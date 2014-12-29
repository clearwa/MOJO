/*
 * Created on 03-Apr-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.objectforge.mascot.examples.roots;

import java.util.Vector;

import com.objectforge.mascot.IDA.SPElement;
import com.objectforge.mascot.IDA.StatusPool;
import com.objectforge.mascot.IDA.telnet.TelnetIO;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * @author Allan Clearwaters
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IncarnatePhilosophers extends AbstractRoot {
	Subsystem philosophers;
	int[] modcounts = new int[10];
	int currentCount = 0;
	TelnetIO myIO;
	StatusPool pool;
	static Console console = new Console();
	public final static String CSI = "\033[";
	public final static String CLEAR = CSI + "2J";
	public final static String HOME = CSI + "0;0H";
	String sessionTitle = "Incarnated Diners";
	Object[] stat = null;
	int numphils;

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.IRoot#root(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		try {
			myIO = new TelnetIO(this);
			pool = (StatusPool) resolve("status-pool");

			myIO.println("Reading the Dining Philosophers ACP file");
			philosophers =
				console.form("Philosophers", getActivity().subsystem);
			pool.reset();

			write("status-pool", new Object[] { "run", new Boolean(true)});
			write(
				"status-pool",
				new Object[] { "session-title", sessionTitle });
			write("status-pool", new Object[] { "diners", null });
			write(
				"status-pool",
				new Object[] { "terminate", new Boolean(false)});

			console.start(philosophers);

			//Pick up the number of philosophers
			while (true) {
				stat = status("status-pool");
				if (stat[StatusPool.STAT_NUMPHILS] != null) {
					try {
						numphils =
							StatusPool.intValue(stat[StatusPool.STAT_NUMPHILS]);
						break;
					} catch (Exception e) {
						MascotDebug.println(9, "exception " + e);
					}
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
		} catch (MascotMachineException e) {
			return;
		}

		resumeRoot();
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
	 */
	public void resumeRoot() {

		myIO.charMode();
		try {
			Vector status;
			String[] states = new String[numphils];

			do {
				status = (Vector) pool.read(currentCount);
				currentCount = ((Integer) status.remove(0)).intValue();
				myIO.println(
					CLEAR
						+ HOME
						+ "The philosophers' table ("
						+ sessionTitle
						+ ")");
				for (int i = 0; i < status.size();) {
					SPElement element = (SPElement) status.remove(0);
					Object[] contents = (Object[]) element.contents;
					int pindex =
						(new Integer(((Integer) contents[Philosopher.PH_INDEX])
							.intValue()))
							.intValue()
							- 1;

					states[pindex] =
						"\t"
							+ contents[Philosopher.PH_ID]
							+ " is "
							+ contents[Philosopher.PH_MESSAGE];
					modcounts[pindex] = element.modCount;
				}
				for (int i = 0; i < numphils; i++)
					myIO.println(states[i]);
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
				stat = status("status-pool");
				if (StatusPool.booleanValue(stat[StatusPool.STAT_TERMINATE])) {
					break;
				}
			} while (monitor(null));
			syncDiners();
			philosophers.suicide();

		} catch (MascotMachineException e) {
		}
	}

	private void syncDiners() {
		try {
			StatusPool pool = (StatusPool) resolve("status-pool");
			Object[] stat = status("status-pool");
			Object mon = StatusPool.contents(stat[StatusPool.STAT_DINERS]);
			if (mon != null) {
				((DinersDisplay) StatusPool
					.contents(stat[StatusPool.STAT_DINERS]))
					.dispose();

				while (true) {
					pool.read();
					stat = status("status-pool");
					if (StatusPool.contents(stat[StatusPool.STAT_DINERS])
						!= mon) {
						break;
					}
				}
			}
		} catch (MascotMachineException e) {
		}

	}

	private boolean monitor(Subsystem aHost) {
		boolean retval = true;

		outerLoop : while (myIO.available() > 0) {
			switch (myIO.readln().charAt(0)) {
				case 's' :
					myIO.println("\n*** Subsystem suspended ***\n");
					if (aHost != null)
						aHost.subSuspend();
					while (myIO.readln().charAt(0) != 'r');
					myIO.println("\n*** Subsystem resumed ***\n");
					if (aHost != null)
						aHost.subResume();
					break outerLoop;

				case 't' :
					if (aHost != null)
						aHost.suicide();
					retval = false;
					break outerLoop;

				default :
					continue;
			}
		}
		return retval;
	}

}
