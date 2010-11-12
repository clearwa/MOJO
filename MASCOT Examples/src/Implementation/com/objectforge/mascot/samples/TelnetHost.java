package com.objectforge.mascot.samples;

/**
* <br><br><center><table border="1" width="80%"><hr>
* <strong><a href="http://www.amherst.edu/~tliron/telnetj">telnetj</a></strong>
* <p>
* Copyright (C) 2001 by Tal Liron
* <p>
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public License
* as published by the Free Software Foundation; either version 2.1
* of the License, or (at your option) any later version.
* <p>
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* <a href="http://www.gnu.org/copyleft/lesser.html">GNU Lesser General Public License</a>
* for more details.
* <p>
* You should have received a copy of the <a href="http://www.gnu.org/copyleft/lesser.html">
* GNU Lesser General Public License</a> along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
* <hr></table></center>
**/

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;

public class TelnetHost extends AbstractRoot {
	Vector pool = new Vector(10);
	Console console = new Console();
	ServerSocket serverSocket = null;
	Socket socket;
	Vector config = new Vector();

	private Subsystem createSession() {
		Subsystem aHost;
		try {
			aHost = console.form("TelnetSession", activity.subsystem);
			aHost.addArgToSubsystem(0, resolve("config-channel"));
			return aHost;
		} catch (MascotMachineException e) {
		}
		return null;
	}

	public void createPool() {
		for (int i = 0; i < 5; i++) {
			pool.add(createSession());
		}
	}

	public Subsystem getSession() {
		for (int i = 0; i < pool.size(); i++) {
			Subsystem test = (Subsystem) pool.get(i);
			if (!test.isAlive()) {
				return test;
			}
		}
		return createSession();
	}

	public void mascotRoot(Activity activity, Object[] args) {
		super.printRoot();
		try {
			config = (Vector) read("config-channel");
		} catch (MascotMachineException e) {
		}
		port = ((Integer) config.remove(0)).intValue();

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException x) {
			x.printStackTrace();
			return;
		}
		MascotDebug.println(9,"telnetj host started on port " + port);

		//Create a pool of subsystem threads
		createPool();
		resumeRoot();
	}

	public void resumeRoot() {
		for (int i = 0; true; i++) {
			try { // Accept connections
				socket = serverSocket.accept();
				config = new Vector();
				config.add(0, socket);
				config.add(1, "Host" + port + ":" + i);
				try {
					write("config-channel", config);
					console.start(getSession());
				} catch (MascotMachineException e) {
				} //				interfaceFactory.connect(new TelnetConnection(socket));
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	} ///////////////////////////////////////////////////////////////////////////////////////
	// Private
	private int port;
}
