/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;


/**
 *
 * Hashtables cannot hold null entries.  This is a singleton class that is used to mark null entries in
 * this class's Hashtables.
 */
class NullTableEntry {

	/**
	 * @param EntityStore
	 */
	public NullTableEntry() {
		super();
	}
}