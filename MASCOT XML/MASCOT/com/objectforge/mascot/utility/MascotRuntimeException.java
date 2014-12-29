/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
*/


package com.objectforge.mascot.utility;

/**
 * A runtime exception for the Mascot Machine.  These are thrown to indicate
 * thread dead and aid termination processing.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 */
public class MascotRuntimeException extends RuntimeException {
    /**
     * Serialized version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for MascotRuntimeException.
     */
    public MascotRuntimeException() {
        super();
    }

    /**
     * Constructor for MascotRuntimeException.
     */
    public MascotRuntimeException(String arg0) {
        super(arg0);
    }

//**java 1.4.1
//  /**
//   * Constructor for MascotRuntimeException.
//   */
//  public MascotRuntimeException(String arg0, Throwable arg1) {
//      super(arg0, arg1);
//  }
//
//  /**
//   * Constructor for MascotRuntimeException.
//   */
//  public MascotRuntimeException(Throwable arg0) {
//      super(arg0);
//  }
//
}
