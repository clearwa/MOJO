/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet;

import java.net.Socket;

import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.scheduler.ControlQueue;

/**
 * ClientDescriptor
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA.telnet
 * Created on 07-Dec-2003 by @author Clearwa
*/
public class ClientDescriptor extends DeviceControl {
    private int port;
    private String address;
    private Socket socket;
    private ControlQueue que = new ControlQueue();
    private TelnetConnection connection;
    private Exception exception;

    /**
     * @param payload
     */
    public ClientDescriptor(Object payload) {
        super(payload);
    }
    
    public ClientDescriptor(String address, int port){
        super(null);
        this.port = port;
        this.address = address;
    }

    /**
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * @return
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @param socket
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * @return
     */
    public ControlQueue getQue() {
        return que;
    }

    /**
     * @return
     */
    public TelnetConnection getConnection() {
        return connection;
    }

    /**
     * @param connection
     */
    public void setConnection(TelnetConnection connection) {
        this.connection = connection;
    }

    /**
     * @return
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @param exception
     */
    public void setException(Exception exception) {
        //The act of setting an exception clears the descriptor
        address = null;
        socket = null;
        connection = null;
        this.exception = exception;
    }
    
    public boolean hasException(){
        return exception != null;
    }

}
