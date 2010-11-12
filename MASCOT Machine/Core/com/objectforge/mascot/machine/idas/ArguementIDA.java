/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.idas;

/**
 * When forming a subsystem, the Mascot Machine treats arguments to an activity as
 * an IDA.  This class implements this entity.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class ArguementIDA extends AbstractIDA {
	protected int index;  //The index in the activity args list
	
	public ArguementIDA( String aString ){
		index = (new Integer( aString )).intValue();
	}
	
	/**
	 * Returns the index.
	 */
	public int getIndex() {
		return index;
	}

}
