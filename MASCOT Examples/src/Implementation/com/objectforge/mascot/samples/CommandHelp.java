package com.objectforge.mascot.samples;

/**
 * @author Clearwa
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CommandHelp {
	public static final String help[] =
		{"MOJO Commands are:",
			"\thelp - this text",
			"\ttimers - run the timers subsystem",
			"\tdiners - run the dining philosophers subsystem",
			"\texit - shut the telenet session",
			"Commands are case sensitive; terminal emulation is ANSI/vt220/vt100. The",
			"terminal should be set into CRLF mode (at least for the MS implementation).",
			"In other cases, experiment until you get a sensible screen.",
			"For diners and timers:",
			"\tt - terminates the subsystem and returns to the command line",
			"\ts - suspends(pauses) the subsystem",
			"\tr - resumes a suspended subsystem",
			"\nMOJO is work in progress and comes with the usual health warnings.  Users",
			"run MOJO at thier own risk; Object Forge accepts no responsibility for any",
			"damage, loss, or inconvenience arising either directly or indirectly from the",
			"use of this software.  Please email comments/queries to allan@object-forge.com",
			"or visit our site at www.object-forge.com."
			
		};

}
