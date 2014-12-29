package com.objectforge.mascot.samples;

/**
 * @author Clearwa
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TelnetConsole extends TelnetSession {

	public TelnetConsole(){
		super();
		commandMap.put("WELCOME",MascotCommands.class);
		commandMap.put( "TIMERS",MascotCommands.class);
		commandMap.put( "EXIT",MascotCommands.class);
		commandMap.put( "DINERS",MascotCommands.class);
		commandMap.put( "SNAPSHOT",MascotCommands.class);
		commandMap.put( "HELP",MascotCommands.class);
		prompt = "mascot> ";
	}
	
	public void run(){
		super.run();
	}

}
