package com.objectforge.mascot.samples;

import java.awt.Window;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.Vector;

import com.objectforge.mascot.IDA.SPElement;
import com.objectforge.mascot.IDA.StatusPool;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.samples.telnetj.NullCommand;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * @author Clearwa
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MascotCommands extends NullCommand {
	TelnetSession cli;
	static Console console = new Console();
	public final static String CSI = "\033[";
	public final static String CLEAR = CSI + "2J";
	public final static String HOME = CSI + "0;0H";

	public void activate(TelnetSession cli, String commandline) throws IOException {
		this.cli = cli;

		if (commandline.equals("welcome")) {
			cli.telnet.println(
				"MOJO-01beta Copyright - The Object Forge, 2002 (www.object-forge.com)\n");
			help();
			cli.telnet.println("\nThe MOJO Telnet Server bids you enter");
		} else if (commandline.equals("timers")) {
			timers();
		} else if (commandline.equals("exit")) {
			throw new MascotRuntimeException("Telnet session exit");
		} else if (commandline.equals("diners")) {
			philosophers();
		} else if (commandline.equals("snapshot")) {
			snapshot();
		} else if (commandline.equals("help")) {
			help();
		}
	}

	public void help() {
		for (int i = 0; i < CommandHelp.help.length; i++) {
			try {
				cli.telnet.println(CommandHelp.help[i]);
			} catch (IOException e) {
			}
		}
	}

	public void philosophers() {
		try {
			Subsystem philosophers;
			PropertyResourceBundle bundle = null;
			EntityStore es;
			int[] modcounts = new int[10];
			Object[] stat = null;
			int currentCount = 0;
			StatusPool pool = (StatusPool) cli.resolve("status-pool");

			try {
				bundle =
					(PropertyResourceBundle) PropertyResourceBundle.getBundle("mascot_machine");
			} catch (RuntimeException e) {
				MascotDebug.println(9,"Philosophers: No property file\n\t" + e);
				return; 
			}
			cli.telnet.println("Reading the Dining Philosophers ACP file");
			es = console.enroll(bundle.getString("mascot.machine.diners"));
			EntityStore.replace(es);
			philosophers = console.form("Philosophers", cli.getActivity().subsystem);
			pool.reset();

			cli.write("status-pool", new Object[] { "run", new Boolean(true)});
			cli.write("status-pool", new Object[] { "session-title", cli.sessionTitle });
			cli.write("status-pool", new Object[] { "diners", null });
			cli.write("status-pool", new Object[] { "terminate", new Boolean(false)});

			console.start(philosophers);

			//Pick up the number of philosophers
			int numphils;
			while (true) {
				stat = cli.status("status-pool");
				if (stat[StatusPool.STAT_NUMPHILS] != null) {
					try {
						numphils = StatusPool.intValue(stat[StatusPool.STAT_NUMPHILS]);
						break;
					} catch (Exception e) {
						MascotDebug.println(9,"exception " + e);
					}
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}

			Vector status;
			String[] states = new String[numphils];
			do {
				status = (Vector) pool.read(currentCount);
//				MascotDebug.println(9, "Console gets read");
				currentCount = ((Integer) status.remove(0)).intValue();
				cli.telnet.println(
					CLEAR + HOME + "The philosophers' table (" + cli.sessionTitle + ")");
				for (int i = 0; i < status.size();) {
					SPElement element = (SPElement) status.remove(0);
					Object[] contents = (Object[]) element.contents;
					int pindex =
						(new Integer(((Integer) contents[Philosopher.PH_INDEX]).intValue()))
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
					cli.telnet.println(states[i]);
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
				stat = cli.status("status-pool");
				if (StatusPool.booleanValue(stat[StatusPool.STAT_TERMINATE])) {
					philosophers.suicide();
					break;
				}
			} while (monitor(philosophers));
			if (StatusPool.contents(stat[StatusPool.STAT_DINERS]) != null)
				 ((Window) StatusPool.contents(stat[StatusPool.STAT_DINERS])).dispose();
		} catch (MascotMachineException e) {
		} catch (IOException e) {
		}
	}

	public void timers() {
		try {
			Subsystem aHost = console.form("timer-test", cli.getActivity().subsystem);
			aHost.addArgToSubsystem(0, cli.getActivity().resolve("reader-channel"));
			aHost.addArgToSubsystem(1, cli.getActivity().resolve("timer-config"));
			for (int i = 0; i < 6; i++) {
				cli.write("timer-config", cli.sessionTitle);
			}
			console.start(aHost);
			//Transfer lines
			do {
				String content = (String) cli.read("reader-channel");

				cli.telnet.print(content);
				cli.telnet.flush();
			} while (monitor(aHost));
		} catch (MascotMachineException e) {
		} catch (IOException e) {
		}
		try {
			cli.telnet.println("\n*** Timer subsystem terminated ***\n");
		} catch (IOException e1) {
		}
	}

	private boolean monitor(Subsystem aHost) {
		boolean retval = true;

		try {
			outerLoop : while (cli.telnet.available() > 0) {
				switch (cli.telnet.nextChar()) {
					case 's' :
						cli.telnet.println("\n*** Subsystem suspended ***\n");
						if (aHost != null)
							aHost.subSuspend();
						while (cli.telnet.nextChar() != 'r');
						cli.telnet.println("\n*** Subsystem resumed ***\n");
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
		} catch (IOException e) {
		}
		return retval;
	}

	public void snapshot() {
		Vector snapshot = null;

		do {
			try {
				Thread.sleep(5000);
//				snapshot = ControlQueue.SOsnapshot();

				cli.telnet.print(CLEAR + HOME + snapshot.remove(0));
				int size = snapshot.size();
				cli.telnet.println(", size is " + size);
				for (int i = 0; i < size; i++) {
					cli.telnet.print((String) snapshot.remove(0));
					if (i != 0 && (i % 4) == 3)
						cli.telnet.println("");
				}
				cli.telnet.println("");
			} catch (InterruptedException e) {
			} catch (IOException e) {
			}
		} while (monitor(null));
	}

}
