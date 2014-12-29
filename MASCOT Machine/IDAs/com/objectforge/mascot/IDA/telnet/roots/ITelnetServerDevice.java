/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet.roots;

import java.net.Socket;

/**
 * ITelnetServerDevice
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA.telnet.roots
 * Created on 28-Feb-2004 by @author Clearwa
*/
public interface ITelnetServerDevice {
    public Object getServerArgs();
    public void rootInit();
    public Socket socketAccept();
}
