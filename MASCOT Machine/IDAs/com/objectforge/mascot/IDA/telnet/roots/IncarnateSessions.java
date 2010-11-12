/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.IDA.telnet.roots;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import com.objectforge.mascot.IDA.SPElement;
import com.objectforge.mascot.IDA.telnet.TelnetConstants;
import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;
import com.objectforge.mascot.utility.MascotUtilities;

/**
 * IncarnateSessions is the root of a companion activity for TelnetHosts.  This class monitors the device
 * pool for new instances.  When it finds one it spawns a worker activity to listen for accepted socket 
 * messages from the hanlder.  In essence the handler delegates the work of opening a session to this
 * activity once it has accepted the telnet connectin on a particular server socket.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class IncarnateSessions extends AbstractRoot {
    protected IMascotReferences monitorFactory;
    protected String targetDevice;
    protected Hashtable hosts = new Hashtable();
    protected static int counter = 0;
    protected static int sessionCounter = 0;
    protected Console console = new Console();
    protected static boolean workerInit = false;
    protected String targetSession;

    /**
     * IncarnateSession spawns activities with this class as the root.  It listens for delegated session
     * requests from the telnet-device handler and sp
     */
    public class SessionMonitorPrivate extends AbstractRoot {

        /* (non-Javadoc)
         */
        public void mascotRoot(Activity activity, Object[] args) {
            resumeRoot();
        }

        /* (non-Javadoc)
         */
        public void resumeRoot() {
            MascotDebug.println(9, "Telnet session monitor starts");

            /*
             * Cast the reference to the channel I want to read.  The root "reads" from the handler's
             * "writer" channel.  Obtuse I know but that's the way it is; reader and writer are defined
             * from the handler's point of view.
             */
            IIDA reader = (IIDA) getArgs()[1];
            try {
                while (true) {
                    Object packet = reader.read();
                    if (packet instanceof DeviceControl) {
                        throw new MascotRuntimeException("SessionMonitorPrivate(resumeRoot): handler has died");
                    }
                    Object[] channels = (Object[]) packet;
                    Subsystem mc = console.form(targetSession, getSubsystem());

                    mc.addArgToSubsystem(0, channels[1]);
                    mc.addArgToSubsystem(1, channels[0]);

                    String tag = "Host" + ((Integer) channels[3]).toString() + ":" + sessionCounter++;
                    mc.addArgToSubsystem(2, tag);
                    //Add in any extra args
                    mc.addArgToSubsystem(3,channels[4]);

                    console.start(mc);
                }
            } catch (IDAException e) {
                throw new MascotRuntimeException("IncarnateSessions(resumeRoot): " + e);
            } catch (MascotMachineException e) {
                throw new MascotRuntimeException("IncarnateSessions(resumeRoot): " + e);
            }
        }

    }
    /**
     * Creating an IncarnateSessions instance adds the definition of a worker activity to the
     * worker subsystem entity store
     */
    public IncarnateSessions() {
        super();
        targetSession = "MascotConsole";
        targetDevice = "telnet-device";
    }

    /**
     * Create an instance of SessionMonitorPrivate for this instance of IncarnateSessions
     */
    public SessionMonitorPrivate SessionMonitorFactory() {
        return this.new SessionMonitorPrivate();
    }

    /* (non-Javadoc)
     */
    public void mascotRoot(Activity activity, Object[] args) {
        try {
            monitorFactory = EntityStore.mascotRepository().addActivityToWorker(
                getSubsystem().getName(),
                this,
                "SessionMonitorFactory",
                null);
        } catch (MascotMachineException e) {
            MascotUtilities.throwMRE("Device(Device<Cannot add worker Activity>: " + e);
        }
        if (args.length > TelnetConstants.DEVICE_ARG) {
            targetDevice = (args[TelnetConstants.DEVICE_ARG]!=null)?(String) args[TelnetConstants.DEVICE_ARG]:targetDevice;
            targetSession = (args[TelnetConstants.SESSION_ARG]!=null)?(String) args[TelnetConstants.SESSION_ARG]:targetSession;
        }
        resumeRoot();
    }

    /* (non-Javadoc)
     * This method loops continuously monitoring the contents of the telnet-device pool.  When a new instance
     * appears in the pool it spawns a SessionMonitorPrivate activity to listen for delegated session
     * requests from the handler.
     */
    public void resumeRoot() {

        while (true) {
            /*
             * Read the pool.
             */
            Object[] elements = null;
            try {
                elements = ((Vector) read(Device.makePoolName(targetDevice))).toArray();
            } catch (MascotMachineException e1) {
                MascotDebug.println(0,"IncarnateSessions.resumeRoot: Error reading device pool\n\t" + e1);
            }

            /*
             * Thumb through the contents of the read return to check for new instances.
             */
            for (int i = 0; i < elements.length; i++) {
                Object[] contents = (Object[]) ((SPElement) elements[i]).contents;
                /*
                 * If I find a new instance then spawn a listener
                 */
                if (!hosts.containsKey(contents[Device.DEVICE_INSTANCE])
                    && contents[Device.DEVICE_CHANNS] != null) {

                    Activity act = null;
                    try {
                        act =
                            (Activity) addWorker(getSubsystem(),
                                monitorFactory,
                                new Vector(Arrays.asList((Object[]) contents[Device.DEVICE_CHANNS])));
                    } catch (MascotMachineException e) {
                        MascotDebug.println(0,"IncarnateSessions.resumeRoot: Error adding worker\n\t" + e);
                    }
                    /*
                     * Start the activity in my subsystem.
                     */
                    act.actStart("SessionMonitor:" + counter++);
                    /*
                     * Take note of that I have already processed this device instance.  File the
                     * contents away.  Note that if the contents change in the pool then this
                     * object will be out of date - not a problem now but I just need to remember!
                     */
                    hosts.put(contents[Device.DEVICE_INSTANCE], contents);
                }

            }
        }
    }
}
