/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.idas;

import com.objectforge.mascot.machine.model.IPool;

/**
 * Degeneratie implemntatin of a pool
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class PoolStub extends AbstractIDA implements IPool {

	/**
	 */
	public Object read(Object idaRef) {
		return null;
	}

	/**
	 */
	public Object write(Object idaRef, Object contents) {
		return null;
	}

	/**
	 */
	public void status(Object idaRef, Object statusBundle) {
	}

}
