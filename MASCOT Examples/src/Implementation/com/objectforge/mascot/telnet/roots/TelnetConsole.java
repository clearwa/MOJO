/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.telnet.roots;

import com.objectforge.mascot.telnet.MascotCommands;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
 */
public class TelnetConsole extends TelnetSession {

    public TelnetConsole() {
        super();
        commandMap.put("WELCOME", MascotCommands.class);
        commandMap.put("TIMERS", MascotCommands.class);
        commandMap.put("EXIT", MascotCommands.class);
        commandMap.put("DINERS", MascotCommands.class);
        commandMap.put("SNAPSHOT", MascotCommands.class);
        commandMap.put("HELP", MascotCommands.class);
        commandMap.put("KILL", MascotCommands.class);
        commandMap.put("LOAD", MascotCommands.class);
        commandMap.put("START", MascotCommands.class);
        commandMap.put("RUN", MascotCommands.class);
        commandMap.put("FINALIZE", MascotCommands.class);
        commandMap.put("SUBSYSTEMS", MascotCommands.class);
        commandMap.put("DEBUG", MascotCommands.class);
        commandMap.put("CSTART", MascotCommands.class);
        prompt = "mascot> ";
    }

    public void run() {
        super.run();
    }

}
