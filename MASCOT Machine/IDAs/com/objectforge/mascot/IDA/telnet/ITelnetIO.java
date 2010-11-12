/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet;

import com.objectforge.mascot.machine.model.IIDA;

/**
 * ITelnetIO
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA.telnet
 * Created on 26-Feb-2004 by @author Clearwa
*/
public interface ITelnetIO {
    public String readln();
    //Allow users to ask questions about the attached socket
    public String localHostName();
    public String remoteHostName();
    public String remoteHostAddress(); 
    public void print(String aLine);
    public void println(String aLine);
    public void control(Object payload);
    public IIDA getInput();
    public IIDA getOutput();
}
