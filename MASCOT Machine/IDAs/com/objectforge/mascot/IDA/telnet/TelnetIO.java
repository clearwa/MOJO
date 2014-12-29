/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.IDA.telnet;

import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.model.IRoot;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public class TelnetIO extends AbstractTelnetIO {
    /**
     * 
     */
    private boolean linemode = true;
    private TelnetConnection connection;

    public TelnetIO(IRoot root) {
        super(root);
    }

    public TelnetIO(IRoot root, IIDA input, IIDA output) {
        super(root, input, output);
        if (this.output instanceof TelnetSerialChannel) {
            //Note that input and output should ba attached to the same socket
            connection = (TelnetConnection) ((TelnetSerialChannel) this.output).getConnection();
        }
    }

    public String readln() {
        try {
            Object ret = input.read();

            if (ret instanceof DeviceControl) {
                throw new MascotRuntimeException("TelnetIO: termination request");
            }

            if (ret instanceof Character) {
                return ((Character) ret).toString();
            }
            return (String) ret;
        } catch (IDAException e) {
            throw new MascotRuntimeException("TelnetIO(readln):" + e);
        }
    }

    private void sendMode(boolean mode) {
        try {
            output.write(new DeviceControl(new Boolean(mode)));
        } catch (IDAException e) {
            throw new MascotRuntimeException("TelnetIO(sendMode):" + e);
        }
    }

    public void lineMode() {
        linemode = true;
        sendMode(linemode);
    }

    public void charMode() {
        linemode = false;
        sendMode(linemode);
    }

    public int available() {
        Object[] status = input.status();

        return ((Integer) status[0]).intValue();
    }

    /**
     * @return
     */
    public TelnetConnection getConnection() {
        return connection;
    }

}
