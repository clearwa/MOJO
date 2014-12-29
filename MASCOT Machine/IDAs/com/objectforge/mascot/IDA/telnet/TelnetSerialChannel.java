/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet;

import java.net.Socket;

import com.objectforge.mascot.IDA.SerialChannel;

/*
* Project: MASCOT Machine
* Package: com.objectforge.mascot.IDA.telnet
* Created on 12-May-2003
*/
public class TelnetSerialChannel extends SerialChannel {
	private Socket socket;
    private ITelnetConnection connection;
	
	public TelnetSerialChannel(){
		super();
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
     * @param connection
     */
    public void setConnection(ITelnetConnection connection) {
        this.connection = connection;        
    }

    /**
     * @return
     */
    public ITelnetConnection getConnection() {
        return connection;
    }

}
