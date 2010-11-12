/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.IDA.monitor;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.objectforge.mascot.IDA.telnet.TelnetConnection;
import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IIDA;
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
public class MonitorDevice extends AbstractRoot {
	private IMascotReferences writerFactory;
    private IMascotReferences readerFactory;
    protected boolean suppressNegotiation;
	Console console = new Console();
	ServerSocket serverSocket = null;
	Socket socket;
	DeviceControl config;
	static int counter = 0;
	TelnetConnection monitorConnection;
	ControlQueue connSync = new ControlQueue();
	ControlQueue startSync = new ControlQueue();
	IIDA reader;

	private int port;

	public class MonitorReaderPrivate extends AbstractRoot {

		/* (non-Javadoc)
		 */
		public void mascotRoot(Activity activity, Object[] args) {
			resumeRoot();
		}

		/* (non-Javadoc)
		 */
		public void resumeRoot() {
			MascotDebug.println(9, "Monitor reader is running");
			monitorConnection.setLinemode(false);
			while (true) {
				try {
					if (((Character) monitorConnection.readLine()).charValue() == 3) {
						break;
					}
				} catch (MascotRuntimeException mre) {
					break;
				} catch (IOException e) {
					break;
				}
			}
			try {
				reader.write(new DeviceControl("exit"));
			} catch (IDAException e) {
			}
			monitorConnection.setClosing();
		}
	}

	public class MonitorWriterPrivate extends AbstractRoot {

		/* (non-Javadoc)
		 */
		public void mascotRoot(Activity activity, Object[] args) {
			reader = (IIDA) resolve("reader");
			resumeRoot();
			dropConnection();
		}

		/* (non-Javadoc)
		 */
		public void resumeRoot() {
			MascotDebug.println(9, "Monitor writer is running");
			try {
				monitorConnection.println("Hello from the MOJO Monitor Device!!");
			} catch (IOException e) {
				// Don't Care			
			}
			while (true) {
				try {
					Object line = reader.read();

					if (line instanceof String) {
						monitorConnection.print((String) line);
						monitorConnection.flush();
					} else
						break; //Device control means quit
				} catch (IDAException e1) {
					break;
				} catch (IOException e1) {
					break;
				}
			}
			monitorConnection.setClosing();
		}
	}

	/**
	 * Creating an instance of TelnetServerDevice adds a worker definition to the working
	 * subsystem
	 */
	public MonitorDevice() {
	}

	/**
	 * Create an instance of MonitorReaderPrivate
	 */
	public MonitorReaderPrivate MonitorReaderFactory() {
		return this.new MonitorReaderPrivate();
	}

	/**
	 * Create an instance of MonitorWriterPrivate
	 */
	public MonitorWriterPrivate MonitorWriterFactory() {
		return this.new MonitorWriterPrivate();
	}

	/* (non-Javadoc)
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		super.printRoot();

		try {
			readerFactory = EntityStore.mascotRepository().addActivityToWorker(
				getSubsystem().getName(),
				this,
				"MonitorReaderFactory",
				null,
				"reader");
			writerFactory = EntityStore.mascotRepository().addActivityToWorker(
				getSubsystem().getName(),
				this,
				"MonitorWriterFactory",
				null,
				"writer");
		} catch (MascotMachineException e) {
			MascotUtilities.throwMRE("MonitorDevice(MonitorDevice<Cannot add worker activities>: " + e);
		}
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

		resumeRoot();
	}

	boolean checkConnection(TelnetConnection newConnection) {
		boolean retval = false;

		connSync.cqJoin();
		if (monitorConnection == null) {
			monitorConnection = newConnection;
			retval = true;
		}
		connSync.cqLeave();
		return retval;
	}

	void dropConnection() {
		if (monitorConnection != null) {
			connSync.cqJoin();
			try {
				monitorConnection.close();
			} catch (IOException e) {
				// Don't Care
			}
			monitorConnection = null;
			connSync.cqLeave();
		}

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
		while (true) {
			try {
				serverSocket = new ServerSocket(port);
				serverSocket.setSoTimeout(500);
				socketAlive = true;
			} catch (InterruptedIOException e2) {
				continue;
			} catch (IOException e2) {
				throw new MascotRuntimeException("MonitorDevice<serversocker>: " + e2);
			}
			break;
		}

		MascotDebug.println(0, "MonitorDevice started on port " + port);

		/*
		 * If the socket is alive then loop listening for connection requests.  If it isn't alive then
		 * then a degenerate version of the dance occurs so that the session monitor companion can be
		 * closed.
		 */
		for (int i = 0; true; i++) {
			try { // Accept connections
				if (socketAlive) {
					socket = serverSocket.accept();
				}
				//			} catch (SocketTimeoutException ste) {		//**java 1.4.1
			} catch (InterruptedIOException ioe) { //**java 1.3.1
				/*
				 * The socket accept has timed out.  Check whether this thread has been marked as
				 * dead and if so exit.
				 */
				if (((MascotThread) Thread.currentThread()).isDead()) {
					throw new MascotRuntimeException("MonitorDevice<accept>: Thread is dead!!");
				}
				continue;
			} catch (Exception x) {
				throw new MascotRuntimeException("MonitorDevice<accept>: " + x);
			}

			/*
			 * I've accepted a connection but it may be the case that someone already has the monitor.
			 * Assuming the server socket is still alive then decide what to do next.
			 */
			if (socketAlive) {
				TelnetConnection newConnection;

				//Creating a new TelnetConnection also handles any negotiation with the client
				try {
					newConnection = new TelnetConnection(socket);
				} catch (IOException e) {
					throw new MascotRuntimeException("MonitorDevice<create connection>: " + e);
				}

				if (checkConnection(newConnection)) {
					Activity reader = null;
					Activity writer = null;

					try {
						reader =
							(Activity) addWorker(
								getSubsystem(),
								readerFactory,
								null,
								"reader");
						writer =
							(Activity) addWorker(
								getSubsystem(),
								writerFactory,
								null,
								"writer");
					} catch (MascotMachineException e1) {
						throw new MascotRuntimeException("MonitorDevice<add workers>: " + e1);
					}

					/*
					 * Start the activity in my subsystem.
					 */
					reader.actStart("MonitorReader:" + counter++);
					writer.actStart("MonitorWriter:" + counter++);
				} else {
					//The device is not available, tell the client and then throw the bum out
					try {
						newConnection.println("The momitor is allocated!!");
						newConnection.println("Please try again later - MOJO:)");
						newConnection.close();
					} catch (IOException e1) {
						//I don't care.
					}
				}
			}
		}
	}
}
