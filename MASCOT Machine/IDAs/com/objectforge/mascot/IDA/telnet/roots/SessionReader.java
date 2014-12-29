/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.IDA.telnet.roots;

import java.io.IOException;

import com.objectforge.mascot.IDA.SerialChannel;
import com.objectforge.mascot.IDA.telnet.ITelnetConnection;
import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * A SessionReader activity reads from the reader channel and writes to a TelnetConnection instance.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class SessionReader extends AbstractRoot {
    private ITelnetConnection mySocket;

    /* (non-Javadoc)
     */
    public void mascotRoot(Activity activity, Object[] args) {
        /*
         * Unpack the TelnetConnection instance.
         */
        mySocket = (ITelnetConnection) getSubsystem().getArgFromSubsystem(0);
        resumeRoot();
    }

    /* (non-Javadoc)
     */
    public void resumeRoot() {
        Object line = null;
        boolean exitFlag = false;

        try {
            while (args != null) {
                /*
                 * Read from the reader channel.
                 */
                try {
                    line = read("reader");
                } catch (MascotMachineException e) {
                    MascotDebug.println(9, "SessionReader(resumeRoot): " + e);
                    break;
                }
                /*
                 * Now that I have something decide what to do with it.  If it is an instance of
                 * String then send it out.  If it is an instance of DeviceControl process it.
                 */
                if (line instanceof String) {
                    mySocket.print((String) line);
                    mySocket.flush();
                } else if (line instanceof DeviceControl) {
                    Object payload = ((DeviceControl) line).payload;

                    /*
                     * A Boolean payload means that the console wants to change the line mode.  If line
                     * mode is true then input is returned a line at a time; otherwise it's a character
                     * at a time.
                     */
                    if (payload instanceof Boolean) {
                        mySocket.setLinemode(((Boolean) payload).booleanValue());
                    }
                    /*
                     * If the payload is a string then my controller is trying to tell me something.  At
                     * the moment only "exit" is valie which means quit.
                     */
                    else if (payload instanceof String) {
                        if ((exitFlag = ((String) payload).equals("exit")))
                            break;
                    }
                } else {
                    System.out.println("Unknown payload" + line);
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.println("From SessionReader");
            e.printStackTrace();
        }
        /*
         * Close the socket and clear the reader channel.  Wait for notification that the other
         * side has died before exiting.
         */
        try {
            mySocket.setClosing();
            mySocket.close();

            /*
             * Tell the associated MascotConsole to exit as well.
             */
            try {
                write("writer", new DeviceControl("exit"));
                ((SerialChannel)resolve("writer")).setTerminate(true);
            } catch (MascotMachineException e2) {
                MascotDebug.println(9, "SessionWriter(resumeRoot<closing console>): " + e2);
            }
            while (!exitFlag) {
                Object payload = read("reader");
                exitFlag = (payload instanceof DeviceControl);
            }
        } catch (MascotMachineException e) {
            throw new MascotRuntimeException("SessionReader(resumeroot): " + e.getMessage());
        } catch (IOException e) {
            throw new MascotRuntimeException("SessionReader(resumeroot): " + e.getMessage());
        } finally {
        }
        MascotDebug.println(9, "Reader exits");
        subsystem.getContainer().suicide();
    }

}
