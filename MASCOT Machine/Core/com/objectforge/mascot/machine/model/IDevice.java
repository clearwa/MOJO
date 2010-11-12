/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.model;

import com.objectforge.mascot.machine.idas.IDAException;

/**
 * The interface that tags and defines a device
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */
public interface IDevice {
	public Object[] add(Object ref) throws IDAException;
	public Object[] open(Object ref) throws IDAException;
	public Object[] close(Object ref) throws IDAException;
	public Object[] status(Object ref)throws IDAException;

}
