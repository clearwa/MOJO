/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.telnet;

/**
 *
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
 */
public class CommandHelp {
	public static final String help[] =
		{
			"MOJO Commands are:\n",
			"    help - this text.",
			"    timers - run the timers subsystem.",
			"    diners - run the dining philosophers subsystem.",
			"    snapshot - print the current state of the Entity Store.",
                        "    subsystems - print a representation of the currently running subsystems.",
			"    load <filename> - enroll filename, ie. load it into the MASCOT Machine's repository.",
			"        If  filename is null you will be asked for one.  Filenames must be complete",
			"        (either relative to the mascotmachine.jar directory or absolute) and",
			"        include an extension if there is one.",
			"    start <subsystem name> - start a subsystem previously enrolled in the repository.",
			"        If no name is given you will be asked for one.  Note that names are case sensitive.",
			"    run <filename>,<subsystem name> - load and start.  If the names are not givien",
			"        you will be asked.",
			"    exit - shut the telenet session.",
			"    kill - kill off the MASCOT Machine.",
			"\nCommands are case sensitive; terminal emulation is ANSI/vt220/vt100. The",
			"terminal should be set into CRLF mode (at least for the MS implementation).",
			"In other cases, experiment until you get a sensible screen.",
			"\nFor diners and timers:",
			"    t - terminates the subsystem and returns to the command line",
			"    s - suspends(pauses) the subsystem",
			"    r - resumes a suspended subsystem"
        };

}
