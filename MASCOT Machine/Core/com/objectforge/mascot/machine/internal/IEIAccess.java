/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.internal;

import com.objectforge.mascot.machine.estore.EntityInstance;

/*
* Project: MASCOT Machine
* Package: com.objectforge.mascot.machine.internal
* Created on 31-May-2003
*/
public interface IEIAccess {
	/**
	 * Deliver the EntityInstance that create this instance
	* Project: MASCOT Machine
	* Package: com.objectforge.mascot.machine.internal
	* Created on 31-May-2003
	 */
	public EntityInstance getEInstance();
	/**
	 * Set the EntityInstance that created this instance
	* Project: MASCOT Machine
	* Package: com.objectforge.mascot.machine.internal
	* Created on 31-May-2003
	 */
	public void setEInstance(EntityInstance anInstance);
}
