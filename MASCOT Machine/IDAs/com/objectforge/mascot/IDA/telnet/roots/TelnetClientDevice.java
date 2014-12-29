/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet.roots;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.objectforge.mascot.IDA.telnet.ClientDescriptor;
import com.objectforge.mascot.IDA.telnet.TelnetConnection;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * TelnetClientDevice
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA.telnet.roots
 * Created on 07-Dec-2003 by @author Clearwa
*/
public class TelnetClientDevice extends AbstractRoot {

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        resumeRoot();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() {
        ClientDescriptor descriptor = null;
        Object result = null;

        while (true) {
            try {
                result = read("reader");
            } catch (MascotMachineException e) {
                MascotDebug.println(9, "TelnetClientDevice<resumeRoot>: error reading channel - " + e);
            }
            if (result instanceof ClientDescriptor) {
                descriptor = (ClientDescriptor) result;
                //Open a socket for the client ala the contents of ClientDexcriptor
                try {
                    descriptor.setSocket(new Socket(descriptor.getAddress(), descriptor.getPort()));
                    //Create the new TelnetConnection - supress negotiation is true
                    descriptor.setConnection(new TelnetConnection(descriptor.getSocket(),true));
                } catch (UnknownHostException e1) {
                    MascotDebug.println(0, "TelnetClientDevice<resumeRoot>: Unknown host - " + e1);
                    descriptor.setException(e1);
                } catch (IOException e1) {
                    MascotDebug.println(5, "TelnetClientDevice<resumeRoot>: IO exception - " + e1);
                    descriptor.setException(e1);
                } finally {
                    //Tell the client that the socket is filled in
                    descriptor.getQue().cqStim();
                }
            }
        }
    }

}
