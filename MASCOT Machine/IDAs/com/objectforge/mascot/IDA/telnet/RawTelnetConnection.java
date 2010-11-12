/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;

import com.objectforge.mascot.machine.scheduler.MascotThread;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * RawTelnetConnection
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA.telnet
 * Created on 26-Feb-2004 by @author Clearwa
*/
public class RawTelnetConnection extends AbstractTelnetConnection {

    /**
     * @param socket
     * @throws IOException
     */
    public RawTelnetConnection(Socket socket) throws IOException {
        super(socket);
//        in = new BufferedInputStream(socket.getInputStream(),8192);
//        out = new BufferedOutputStream(socket.getOutputStream());
    }

    /**
     * While not closing read the socket.  If the read times out check for thread death; if so 
     * exit the thread, if not reissue the read.
     */
    private int socketRead(byte[] buffer) {
        int count = 0;

        while (!closing) {
            try {
                count = in.read(buffer);

                if (count < 0) {
                    throw new EOFException("RawTelnetConnection<socketRead>: EOF");
                }
                return count;

            } catch (InterruptedIOException ioe) {
                if (((MascotThread) Thread.currentThread()).isDead()) {
                    throw new MascotRuntimeException("RawTelentConnection(socketRead): Thread is dead!!");
                }
                continue;
            } catch (IOException e1) {
                throw new MascotRuntimeException("RawTelnetConnection(socketRead): IOEX " + e1);
            }

        }
        throw new MascotRuntimeException("RawTelnetConnection(socketRead): exited from the bottom of the routine");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.IDA.telnet.AbstractTelnetConnection#readLine()
     */
    public Object readLine() throws IOException {
        byte[] buffer = new byte[10000];
        int count = socketRead(buffer);

        String retval = new String(buffer, 0, count, "US-ASCII");
        return retval;
    }
}
