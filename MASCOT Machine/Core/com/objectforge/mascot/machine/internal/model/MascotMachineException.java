/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.internal.model;

/**
 * A general purpose exception used by the Mascot Machine and ACP editor.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class MascotMachineException extends Exception {
    /**
     * Serialized version
     */
    private static final long serialVersionUID = 1L;

    public MascotMachineException(){
		super();
	}
	
	public MascotMachineException( String message ){
		super( message );
	}

//**java 1.4.1
//	/**
//	 * Constructor MascotMachineException.
//	 */
//	public MascotMachineException(String message, Exception e) {
//		super( message,e );
//	}
//
	
}
