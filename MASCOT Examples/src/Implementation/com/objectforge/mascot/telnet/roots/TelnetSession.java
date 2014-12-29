/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.telnet.roots;

/**
* <br><br><center><table border="1" width="80%"><hr>
* <strong><a href="http://www.amherst.edu/~tliron/telnetj">telnetj</a></strong>
* <p>
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
* @author Allan Clearwaters
* @version $Id$, $Name: 1.3 $
**/

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.objectforge.mascot.IDA.telnet.*;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.telnet.*;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;

public abstract class TelnetSession extends AbstractRoot {
	//
	// Construction
	//
	public String sessionTitle = "--";
	public TelnetIO io;

	public void mascotRoot(Activity activity, Object[] args) {
		io = new TelnetIO( this );

		sessionTitle = (String)getSubsystem().getArgFromSubsystem(2);
		prompt = "MOJO " + sessionTitle + "> ";
		run();
	}

	public void resumeRoot() {
		run();
	}

	//
	// Attributes
	//

	public String prompt = "";

	//
	// Operations
	//

	public void execute(String commandline) {
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
				MascotDebug.println(9, "TelnetSession(execute): InstantiationException");
			} catch (IllegalAccessException x) {
				MascotDebug.println(9, "TelnetSession(execute): IllegalAccessException");
			}
		}
	}

	public void unknown(String command, String commandline) {
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

			while (true) {
				try {
					execute(commandline);
				} catch (RuntimeException e) {
					break;
				}
				io.lineMode();
				io.print(prompt);
				commandline = io.readln();
			}
		if(getSubsystem()!=null){
			getSubsystem().suicide();
		}
		throw new MascotRuntimeException("Session exits");
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	private Command currentCommand = null;
	protected Map commandMap = new HashMap();
}
