/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.telnet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import com.objectforge.mascot.IDA.SPElement;
import com.objectforge.mascot.IDA.StatusPool;
import com.objectforge.mascot.examples.roots.DinersDisplay;
import com.objectforge.mascot.examples.roots.Philosopher;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.GlobalSubsystem;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.telnet.roots.TelnetSession;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;
import com.objectforge.mascot.utility.MascotUtilities;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
  */
public class MascotCommands extends NullCommand {
    static Console console = new Console();
    public final static String CSI = "\033[";
    public final static String CLEAR = CSI + "2J";
    public final static String HOME = CSI + "0;0H";
    protected TelnetSession cli;

    private void syncDiners() {
        try {
            StatusPool pool = (StatusPool) cli.resolve("status-pool");
            Object[] stat = cli.status("status-pool");
            Object mon = StatusPool.contents(stat[StatusPool.STAT_DINERS]);
            if (mon != null) {
                ((DinersDisplay) StatusPool.contents(stat[StatusPool.STAT_DINERS])).dispose();

                while (true) {
                    pool.read();
                    stat = cli.status("status-pool");
                    if (StatusPool.contents(stat[StatusPool.STAT_DINERS]) != mon) {
                        break;
                    }
                }
            }
        } catch (MascotMachineException e) {
        }

    }

    public void activate(TelnetSession cli, String theCommandline) {
        this.cli = cli;
        String commandline = theCommandline.toLowerCase().trim();

        if (commandline.equals("welcome")) {
            cli.io.println(EntityStore.Banner());
            help();
            cli.io.println("\nThe MOJO Telnet Server bids you enter");
        } else if (commandline.equals("timers")) {
            timers();
        } else if (commandline.equals("exit")) {
            cli.io.control("exit");
            throw new MascotRuntimeException("Telnet session exit");
        } else if (commandline.equals("diners")) {
            philosophers();
        } else if (commandline.equals("snapshot")) {
            snapshot();
        } else if (commandline.equals("help")) {
            help();
        } else if (commandline.equals("finalize")) {
            Runtime.getRuntime().runFinalization();
        } else if (commandline.equals("kill")) {
            EntityStore.getGlobalSubsystem().suicide();
        } else if (commandline.startsWith("load")) {
            try {
                load(theCommandline);
            } catch (MascotMachineException e) {
                return;
            }
        } else if (commandline.startsWith("start")) {
            try {
                startsub(theCommandline);
            } catch (MascotMachineException e) {
                return;
            }
        } else if (commandline.startsWith("subsystems")) {
            subsTree();
        } else if (commandline.startsWith("debug")) {
            StringTokenizer tokens = new StringTokenizer(commandline);
            tokens.nextToken(); //dump the "debug" string
            try {
                MascotDebug.debug = (new Integer(tokens.nextToken())).intValue();
                cli.io.println("    debug level set to " + MascotDebug.debug);
            } catch (NumberFormatException e) {
                cli.io.println("    Bad number format");
                return;
            }
        } else if (commandline.startsWith("cstart")) {
            try {
                startInContainer( commandline );
            } catch (MascotMachineException e) {
            }
        } else if (commandline.startsWith("run")) {
            StringTokenizer tokens = new StringTokenizer(commandline);
            String command = tokens.nextToken(); //dump the comman token

            String filename = "";
            String subname = "";
            if (tokens.countTokens() >= 1) {
                StringTokenizer names =
                    new StringTokenizer(theCommandline.substring(command.length()).trim(), ",");
                switch (names.countTokens()) {
                    case 0 :
                        break;

                    default :
                        filename = names.nextToken().trim();
                        if (names.countTokens() > 0)
                            subname = names.nextToken().trim();
                        break;
                }
            }
            try {
                load(command + " " + filename);
                startsub(command + " " + subname);
            } catch (MascotMachineException e) {
                return;
            }
        }
    }

    /**
     * Walk and print the global subsystem tree
     */
    private void subsTree() {
        GlobalSubsystem global = EntityStore.getGlobalSubsystem();
        StringWriter buffer = new StringWriter();
        PrintWriter printer = new PrintWriter(buffer);

        Console.treeWalk(global, "", printer);
        cli.io.print(buffer.toString());
        buffer = new StringWriter();
        printer = new PrintWriter(buffer);
        cli.io.print(buffer.toString());
    }

    protected void enrollBundle(String key) {
        EntityStore es;

        try {
            es = console.enroll( MascotUtilities.getMascotResource(key) );
        } catch (RuntimeException e) {
            MascotDebug.println(9, "enrollBundle: No property entry for key " + key + "\n\t" + e);
            return;
        }
        try {
            EntityStore.replace(es);
        } catch (MascotMachineException e1) {
            throw new MascotRuntimeException( e1.getMessage() );
        }
    }

    public void help() {
        for (int i = 0; i < CommandHelp.help.length; i++) {
            cli.io.println(CommandHelp.help[i]);
        }
    }

    public void philosophers() {
        try {
            Subsystem philosophers;

            int[] modcounts = new int[10];
            Object[] stat = null;
            int currentCount = 0;

            cli.io.charMode();
            StatusPool pool = (StatusPool) cli.resolve("status-pool");

            cli.io.println("Reading the Dining Philosophers ACP file");
            enrollBundle("mascot.machine.diners");
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
                        MascotDebug.println(9, "exception " + e);
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
                currentCount = ((Integer) status.remove(0)).intValue();
                cli.io.println(CLEAR + HOME + "The philosophers' table (" + cli.sessionTitle + ")");
                for (int i = 0; i < status.size();) {
                    SPElement element = (SPElement) status.remove(0);
                    Object[] contents = (Object[]) element.contents;
                    int pindex =
                        (new Integer(((Integer) contents[Philosopher.PH_INDEX]).intValue())).intValue() - 1;

                    states[pindex] =
                        "\t" + contents[Philosopher.PH_ID] + " is " + contents[Philosopher.PH_MESSAGE];
                    modcounts[pindex] = element.modCount;
                }
                for (int i = 0; i < numphils; i++)
                    cli.io.println(states[i]);
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                stat = cli.status("status-pool");
                if (StatusPool.booleanValue(stat[StatusPool.STAT_TERMINATE])) {
                    break;
                }
            } while (monitor(null));
            syncDiners();
            philosophers.suicide();
        } catch (MascotMachineException e) {
        }
    }

    private boolean timerLoaded = false;

    public void timers() {
        if (!timerLoaded) {
            timerLoaded = true;
            enrollBundle("mascot.machine.timers");
        }
        cli.io.charMode();

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

                cli.io.print(content);
            } while (monitor(aHost));
        } catch (MascotMachineException e) {
        }

        cli.io.println("\n*** Timer subsystem terminated ***\n");
    }

    private String getInput(String prompt, String commandline) {
        StringTokenizer tokens = new StringTokenizer(commandline);

        if (tokens.countTokens() < 2) {
            cli.io.lineMode();
            cli.io.print(prompt + "> ");
            return cli.io.readln().trim();
        } else {
            return commandline.substring(tokens.nextToken().length()).trim();
        }
    }

    public void load(String commandline) throws MascotMachineException {
        //Try to load the SET file
        String filename = getInput("\tPlease enter a filename", commandline).trim();
        filename = MascotUtilities.canonicalFilename(filename);

        EntityStore estores = console.enroll(filename);

        if (estores != null ) {
            EntityStore.replace(estores);
        } else {
            cli.io.println("\tUnable to load \"" + filename + "\"");
            throw new MascotMachineException("Cannot load");
        }
    }

    void startsub(String commandline) throws MascotMachineException {
        String response = getInput("\tPlease enter the name of the subsystem to start", commandline);

        //Form the subsystem
        Subsystem subsys;
        try {
            subsys = EntityStore.formSubsystem(response, cli.getActivity().subsystem);
        } catch (Exception e) {
            cli.io.println("\tCannot start \"" + response + "\"");
            throw new MascotMachineException("Cannot start");
        }

        subsys.addArgToSubsystem(0, cli.io.getInput());
        subsys.addArgToSubsystem(1, cli.io.getOutput());
        subsys.subStart();
        subsys.waitForMe();
    }

    void startInContainer(String commandline) throws MascotMachineException {
        String response = getInput("\n  Enter the container (return for global)", "");
        Subsystem container = EntityStore.getGlobalSubsystem();

        if (!(response == "")) {
            List subsystems = Console.findSubsystems(response);
            container = null;
            for (Iterator i = subsystems.iterator(); i.hasNext();) {
                Subsystem current = (Subsystem) i.next();
                if (current.getName().startsWith(response)) {
                    container = current;
                    break;
                }
            }
        }
        if (container == null) {
            cli.io.println("\tCannot find container - quit!");
            return;
        }
        response = getInput("  Enter the subsystem to start", "");
        Subsystem subsys;
        try {
            subsys = EntityStore.formSubsystem(response, container);
            // Make sure this subsystem does not hang around when it's done.
            subsys.setCloseOnEmpty( true );
            subsys.subStart();
        } catch (Exception e) {
            // On exception simply quit
        }
    }

    private boolean monitor(Subsystem aHost) {
        boolean retval = true;

        outerLoop : while (cli.io.available() > 0) {
            switch (cli.io.readln().charAt(0)) {
                case 's' :
                    cli.io.println("\n*** Subsystem suspended ***\n");
                    if (aHost != null)
                        aHost.subSuspend();
                    while (cli.io.readln().charAt(0) != 'r');
                    cli.io.println("\n*** Subsystem resumed ***\n");
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

    public void snapshot() {

        cli.io.charMode();
        String snapshot = EntityStore.mascotRepository().toString();

        cli.io.print(CLEAR + HOME);
        cli.io.println(snapshot);
    }

}
