/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.IDA.telnet.roots;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;

import com.objectforge.mascot.IDA.telnet.ITelnetConnection;
import com.objectforge.mascot.IDA.telnet.TelnetConnection;
import com.objectforge.mascot.IDA.telnet.TelnetConstants;
import com.objectforge.mascot.IDA.telnet.TelnetSerialChannel;
import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.MascotThread;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;
import com.objectforge.mascot.utility.MascotUtilities;

/**
 * TelnetServerDevice is the handler for a telnet device.  This is a device that implements a telnet host; instances of 
 * this handler are craated when an instance of the telnet device is spawned.
  * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
*/
public class TelnetServerDevice extends AbstractRoot {
    IMascotReferences socketFactory;
    protected boolean suppressNegotiation;
    Vector pool = new Vector(10);
    Console console = new Console();
    protected ServerSocket serverSocket = null;
    protected Socket socket;
    DeviceControl config;
    Hashtable sockets = new Hashtable();
    static int counter = 0;
    private String targetSession;
    protected int port;

    /**
     * TelnetIncarnateSession is a subsystem with not activites but 2 locally defined serial channels.  TelnetServerDevice
     * forms incarnations of this subsystem and then inserts a worker activity in to it with this class as 
     * root.  The net result is that when this code runs it is inside a TelnetIncarnatinSubsystem.
     */
    public class TelnetSocketPrivate extends AbstractRoot {

        /* (non-Javadoc)
         * The root does the work of firing of a TelnetSession subsystem.  It first resolves references to the
         * locally defined reader and writer channels and stuffs these into the channels array (passed at index 1
         * of the subsystem arguments).  It next attempts to create an instance of TelnetConnection to handle 
         * input and output on the Socket (passed at index 2 of the channels array).  If the connection is 
         * successfully created then the subsystem TelnetSession is formed, connection added as a subsystem
         * argument, and the it is started.
         * 
         * TelnetSession defines 2 channels with container scope; these resolve to the reader and writer 
         * local to this instance of TelnetIncarnateSession.  TelnetServerDevice also passes these references (via
         * the channels array that was filled in earlier in the processing) to the companion IncarnateSessions
         * activity.  IncarnateSession unpacks these references and adds them as arguments to an incarnation
         * of the MascotConsole subsystem.  The result of all of this is that the channels locally defined
         * in TelnetIncarnateSessions are the reader and writer connections between the TelnetSession incarnated
         * here and the MascotConsole incarnated by IncarnateSessions activity.
         */
        public void mascotRoot(Activity activity, Object[] args) {
            Console console = new Console();

            /*
             * Get the channels array and resolve the reader and writer referneces.
             */
            Object[] channels = (Object[]) getSubsystem().getArgFromSubsystem(1);
            channels[0] = resolve("reader");
            channels[1] = resolve("writer");
            /*
             * With the resolution complete the TelnetServerDevice thread can be released.
             */
            ControlQueue sync = (ControlQueue) getSubsystem().getArgFromSubsystem(2);
            sync.cqStim();

            /*
             * Create the TelnetConnection.  This should always succeed since this activity does
             * not run unless the socket is valid.
             */
            ITelnetConnection connection = null;
            try {
                connection = getConnection((Socket) channels[2]);
            } catch (IOException e) {
                throw new MascotRuntimeException("TelnetSocket(root): " + e);
            }

            //Set the socket value on the reader and writer channels if need be
            if (channels[0] instanceof TelnetSerialChannel) {
                ((TelnetSerialChannel) channels[0]).setSocket((Socket) channels[2]);
                ((TelnetSerialChannel) channels[0]).setConnection(connection);
            }
            if (channels[1] instanceof TelnetSerialChannel) {
                ((TelnetSerialChannel) channels[1]).setSocket((Socket) channels[2]);
                ((TelnetSerialChannel) channels[0]).setConnection(connection);
            }

            /*
             * Incarnate a TelnetSession subsystem, add connection as an argument, and start it.  This
             * fires up a subsystem with SessionReader and SessionWriter activities.
             */
            Subsystem session = null;
            try {
                session = console.form(targetSession, getSubsystem());
            } catch (MascotMachineException e1) {
                throw new MascotRuntimeException("TelnetSocket(root): " + e1);
            }
            session.addArgToSubsystem(0, connection);
            if ( TelnetServerDevice.this instanceof ITelnetServerDevice) {
                session.addArgToSubsystem(1, ((ITelnetServerDevice)TelnetServerDevice.this).getServerArgs());
            }
            console.start(session);
        }

        /* (non-Javadoc)
         */
        public void resumeRoot() {
        }
    }

    protected ITelnetConnection getConnection(Socket socket) throws IOException {
        return new TelnetConnection(socket);
    }

    /**
     * Creating an instance of TelnetServerDevice adds a worker definition to the working
     * subsystem
     */
    public TelnetServerDevice() {
        targetSession = "TelnetSession";
    }

    /**
     * Create an instance of MonitorSocketPrivate
     */
    public TelnetSocketPrivate TelnetSocketFactory() {
        return this.new TelnetSocketPrivate();
    }

    protected void rootInit() {
        try {
            socketFactory = EntityStore.mascotRepository().addActivityToWorker(
                "TelnetIncarnateSession",
                this,
                "TelnetSocketFactory",
                null);
        } catch (MascotMachineException e) {
            MascotUtilities.throwMRE(
                "TelnetServerDevice(TelnetServerDevice<Cannot add worker Activity>: " + e);
        }
        super.printRoot();
        if (args.length > TelnetConstants.SESSION_ARG)
            targetSession = (args[TelnetConstants.DEVICE_ARG]!=null)?(String) args[TelnetConstants.SESSION_ARG]:targetSession;
        /*
         * A newly created handler needs to be told which socket to listen on.  It reads this
         * information from the its reader channel.
         */
        try {
            while (true) {
                Object myobj;

                myobj = (Object) read("reader");
                if (myobj instanceof DeviceControl) {
                    port = ((Integer) ((DeviceControl) myobj).payload).intValue();
                    config = (DeviceControl) myobj;
                    break;
                }
            }

        } catch (MascotMachineException e) {
            throw new MascotRuntimeException("TelnetServerDevice<read>: " + e);
        }
    }

    /* (non-Javadoc)
     */
    public void mascotRoot(Activity activity, Object[] args) {
        rootInit();
        resumeRoot();
    }

    protected Socket socketAccept() {
        Socket mySocket = null;

        while (true) {
            try { // Accept connections
                mySocket = serverSocket.accept();
                break;
            } catch (InterruptedIOException ioe) {
                /*
                 * The socket accept has timed out.  Check whether this thread has been marked as
                 * dead and if so exit.
                 */
                if (((MascotThread) Thread.currentThread()).isDead()) {
                    throw new MascotRuntimeException("TelnetServerDevice<accept>: " + ioe);
                }
                continue;
            } catch (Exception x) {
                throw new MascotRuntimeException("TelnetServerDevice<accept>: " + x);
            }
        }
        return mySocket;
    }

    /* (non-Javadoc)
     * Create a server socket and monitor it for connections.
     */
    public void resumeRoot() {
        boolean socketAlive = false;

        /*
         * Now that I know the socket try to create it.  Note that if this fails then some
         * tidyup needs to occur.  In particular, the session monitor instance spawned as companion
         * to the handler will need to be told to die.
         */
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(500);
            socketAlive = true;
        } catch (IOException x) {
            //			throw new MascotRuntimeException(
            //				"TelnetServerDevice<serverSocket>: " + x);
        }
        MascotDebug.println(0, "TelnetServerDevice started on port " + port);

        /*
         * If the socket is alive then loop listening for connection requests.  If it isn't alive then
         * then a degenerate version of the dance occurs so that the session monitor companion can be
         * closed.
         */
        for (int i = 0; true; i++) {

            socket = socketAccept();
            if (socket == null) {
                break;
            }

            /*
             * At this point the socket may or may not be alive.  If it is then finish the incarnation
             * process.
             */
            Object[] sessionChannels = new Object[5];

            if (socketAlive) {
                Subsystem session = null;
                ControlQueue sync = new ControlQueue();
                /*
                 * Fill in slots in the session channels array.  Indicies 0 and 1 will be filled
                 * with resolved reader and writer referneces by the worker.
                 */
                sessionChannels[2] = socket;
                sessionChannels[3] = new Integer(port);
                if (this instanceof ITelnetServerDevice) {
                    sessionChannels[4] = ((ITelnetServerDevice) this).getServerArgs();
                }
                try {
                    /*
                     * The TelnetIncarnateSession subsystem (refer to global.acp) has no activtities
                     * defined and 2 local serial channels, "reader" and "writer".  The processing
                     * here uses it as a container in which to run the worker activity.  Form it now.
                     */
                    session = console.form("TelnetIncarnateSession", getSubsystem());
                    /*
                     * Add arguments to the subsystem
                     */
                    session.addArgToSubsystem(0, socket);
                    session.addArgToSubsystem(1, sessionChannels);
                    session.addArgToSubsystem(2, sync);
                    session.addArgToSubsystem(3, new Integer(port));
                    addWorker(session, socketFactory, null);
                } catch (MascotMachineException e1) {
                    throw new MascotRuntimeException("TelnetServerDevice(resumeRoot): " + e1);
                }
                /*
                 * Keep track of the stared sessions.
                 */
                sockets.put(socket, new Object());
                /*
                 * Start the subsystem and wait until it completes
                 */
                sync.cqJoin();
                console.start(session);
                sync.cqWait();
                sync.cqLeave();
            }

            /*
             * The sessionChannels have now been filled (or not, as the case may be).  If the socket
             * is alive then write this object to the handler's writer channel.  The IncarnateSessions
             * companion activity will be listening; if the socket is alive then pass the contents of 
             * the channel array, otherwise send a DeivceControl packet to say I'm dead and you should be
             * as well.
             */
            try {
                if (socketAlive) {
                    write("writer", sessionChannels);
                } else {
                    //Tell the other side to exit
                    write("writer", new DeviceControl(new Boolean(false)));
                    throw new MascotRuntimeException("TelnetServerDevice<serverSocket>: Server socket is dead");
                }
            } catch (MascotMachineException e2) {
                throw new MascotRuntimeException("TelnetServerDevice(resumeRoot): " + e2);
            }
        }

    }
}
