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
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.samples.telnetj.Command;
import com.objectforge.mascot.samples.telnetj.TelnetConnection;
import com.objectforge.mascot.utility.MascotRuntimeException;

public abstract class TelnetSession extends AbstractRoot {
	//
	// Construction
	//
	public String sessionTitle = "--";
	
	public void mascotRoot( Activity activity, Object[] args ){
		Vector config = new Vector();
		
		try {
			config = (Vector)read( "config-channel" );
		} catch (MascotMachineException e) {
		}
		try {
			this.telnet = new TelnetConnection( (Socket)config.get(0) );
		} catch (IOException e) {
		}
		sessionTitle = (String)config.get(1);
		prompt = "MOJO " + sessionTitle + "> ";
		run();
	}
	
	public void resumeRoot(){
		run();
	}

	//
	// Attributes
	//

	public String prompt = "";
	public TelnetConnection telnet;

	//
	// Operations
	//

	public void execute(String commandline) throws IOException {
		// Extract first word in commandline (command)
		StringTokenizer t = new StringTokenizer(commandline, " ");
		String command = t.hasMoreTokens() ? t.nextToken() : commandline;

		// Find command
		Class commandClass = null;
		String c = command.toUpperCase();
		commandClass = (Class) commandMap.get(c);

		if (commandClass == null) {
			// Unknown command
			unknown(command, commandline);
		} else {
			try {
				Command newCommand = (Command) commandClass.newInstance();

				if (currentCommand != null) {
					currentCommand.kill();
				}

				currentCommand = newCommand;

				if (currentCommand != null) {
					currentCommand.activate(this, commandline);
				}
			} catch (InstantiationException x) {
				x.printStackTrace();
			} catch (IllegalAccessException x) {
				x.printStackTrace();
			}
		}
	}

	public void unknown(String command, String commandline) throws IOException {
	}

	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException x) {
		}
	}

	//
	// Runnable
	//

	public void run() {
		String commandline = "welcome";

		try {
			while (true) {
				try {
					execute(commandline);
				} catch (RuntimeException e) {
					break;
				}
				telnet.print(prompt);
				telnet.flush();
				commandline = telnet.readLine();
			}
		} catch (IOException x) {
		}

		try {
			telnet.close();
		} catch (IOException x) {
		}
		throw new MascotRuntimeException( "Session exits" );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	private Command currentCommand = null;
	protected Map commandMap = new HashMap();
}
