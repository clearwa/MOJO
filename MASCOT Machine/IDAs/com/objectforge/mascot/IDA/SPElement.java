/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.IDA;


/**
 * Status Pool Elements - the objects that held in a StatusPool.  These are
 * clonable.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class SPElement implements Cloneable {
	public int modCount;
	public Object contents;

	public SPElement() {
		modCount = 0;
		contents = null;
	}

	/**
	 * Method clone.
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
	
}
