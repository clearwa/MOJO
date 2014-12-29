/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet;

import java.io.IOException;

/**
 * ITelnetConnection
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA.telnet
 * Created on 26-Feb-2004 by @author Clearwa
*/
public interface ITelnetConnection {
    public Object readLine() throws IOException;    
    public void flush() throws IOException;    
    public void send(char c) throws IOException ;
    public void print(char c) throws IOException ;
    public void println() throws IOException ;
    public void print(String s) throws IOException ;
    public void println(String s) throws IOException ;
    public void setLinemode( boolean mode);
    public void close() throws IOException;
    public void setClosing();
    public boolean isClosing();
}
