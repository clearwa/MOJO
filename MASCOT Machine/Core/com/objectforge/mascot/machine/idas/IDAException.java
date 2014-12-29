/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.idas;

/**
 * IDAs throw this exception to indicate problems
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class IDAException extends Exception {

    /**
     * Serialized version
     */
    private static final long serialVersionUID = 1L;

    /**
	 * Constructor for IDAException.
	 */
	public IDAException() {
		super();
	}

	/**
	 * Constructor for IDAException.
	 */
	public IDAException(String message) {
		super(message);
	}

//**java 1.4.1
//	/**
//	 * Constructor for IDAException.
//	 */
//	public IDAException(String message, Throwable cause) {
//		super(message, cause);
//	}
//
//	/**
//	 * Constructor for IDAException.
//	 */
//	public IDAException(Throwable cause) {
//		super(cause);
//	}
//
}
