/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.model.IRoot;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * AbstractTelnetIO
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA.telnet
 * Created on 26-Feb-2004 by @author Clearwa
*/
public abstract class AbstractTelnetIO implements ITelnetIO {
    protected IIDA input;
    protected IIDA output;
    protected Socket socket;
    
    public abstract String readln();
    
    public AbstractTelnetIO(IRoot root) {
        this(root,(IIDA)root.resolve("input"),(IIDA)root.resolve("output"));
    }
    
    public AbstractTelnetIO(IRoot root,IIDA input, IIDA output){
        this.input = input;
        this.output = output;
        if (this.output instanceof TelnetSerialChannel) {
            //Note that input and output should ba attached to the same socket
            socket = ((TelnetSerialChannel) this.output).getSocket();
        }
    }

    //Allow users to ask questions about the attached socket
    public String localHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return ("UnknownLocalHost");
        }
    }

    public String remoteHostName() {
        if (socket == null) {
            return ("NullSocketHost");
        }
        return (socket.getInetAddress().getHostName());
    }

    public String remoteHostAddress() {
        if (socket == null) {
            return ("NullSocketAddress");
        }
        return (socket.getInetAddress().getHostAddress());
    }

    public void print(String aLine) {
        try {
            output.write(aLine);
        } catch (IDAException e) {
            throw new MascotRuntimeException("TelnetIO(print):" + e);
        }
    }

    public void println(String aLine) {
        print(aLine + "\r\n");
    }

    public void control(Object payload) {
        DeviceControl control = new DeviceControl(payload);
        try {
            output.write(control);
        } catch (IDAException e) {
            throw new MascotRuntimeException("TelnetIO(control):" + e);
        }
    }

    /**
     * @return
     */
    public IIDA getInput() {
        return input;
    }

    /**
     * @return
     */
    public IIDA getOutput() {
        return output;
    }
}
