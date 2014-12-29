/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.model;

import com.objectforge.mascot.machine.idas.IDAException;

/**
 * Root interface for IDAs, ie. channels and pools.  AbstractIDA provides the
 * base implementation of this interface.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */
public interface IIDA {
	/**
	 * Method read.
	 * Read an IDA or device
	 * @throws IDAException
	 */
	public Object read() throws IDAException;
	/**
	 * Method write.
	 * Write an IDA or device
	 * @throws IDAException
	 */
	public void write(Object contents) throws IDAException;
	/**
	 * Method status.
	 * Return an array that contains information about an IDA or device.
	 */
	public Object[] status();
}
