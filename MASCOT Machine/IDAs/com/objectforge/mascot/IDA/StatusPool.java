/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.IDA;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * 
 * Status Pool implementation - for the Phiosophers problem.  This pool exhibits write
 * before read behaviour.  The pool has 2 sections destinguished by whether a key lies 
 * within those defined by the statusKeys vector.  At the moment this is static - there
 * is no way to dynmaically change what is or is not a status key.  Any other keys in the
 * pool are deemed to be content; these are dynamic.
 * 
 * Since this is an implementation of an IDA it uses only the Mascot Machine synchronization
 * primatives.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public class StatusPool extends Type1Pool {

	//Static constants used to identify status entryies in the pool.
	public final static int STAT_NUMPHILS = 0; //Integer()
	public final static int STAT_RUN = 1; //Boolean()
	public final static int STAT_TIME = 2; //Integer()
	public final static int STAT_SESSION_TITLE = 3; //String()
	public final static int STAT_DINERS = 4; //java.awt.Window()
	public final static int STAT_TERMINATE = 5; //Boolean()

	private final static String[] statusKeys =
		{ "numphils", "run", "time", "session-title", "diners", "terminate" };
	private static List statusKeyList;
	static {
		statusKeyList = Arrays.asList(statusKeys);
	}

	public StatusPool() {
		for (Iterator i = statusKeyList.iterator(); i.hasNext();)
			status.put(i.next(), null);
		super.statusKeys = statusKeys;
		super.statusKeyList = statusKeyList;
	}

}
