/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet;

import java.io.IOException;

import com.objectforge.mascot.IDA.SerialChannel;
import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.model.IRoot;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * RawTelnetIO
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA.telnet
 * Created on 26-Feb-2004 by @author Clearwa
*/
public class RawTelnetIO extends AbstractTelnetIO {
    int readerIndex = 0;
    String lines;
    String lastToken = "";

    /**
     * @param root
     * @param input
     * @param output
     */
    public RawTelnetIO(IRoot root, IIDA input, IIDA output) {
        super(root, input, output);
    }

    /**
     * @param root
     */
    public RawTelnetIO(IRoot root) {
        super(root);
    }

    private void fillReader() throws IOException, IDAException {
        if (!((SerialChannel) input).isTerminate()) {
            Object newval = input.read();
            if (newval instanceof DeviceControl) {
                throw new MascotRuntimeException("RawTelnetIO: termination request via Device Control");
            }
            readerIndex = 0;
            lines = (String)newval;
            return;
        }
        throw new MascotRuntimeException("RawTelnetIO<readln>: termination request from the channel");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.IDA.telnet.AbstractTelnetIO#readln()
     */
    public String readln() {
        String retval = "";
        int nextIndex = 0;

        try {
            while (true) {
                if( lines == null || ( nextIndex = lines.indexOf('\n',readerIndex))<0 ){
                    retval += (lines == null || readerIndex>lines.length())?"":lines.substring(readerIndex);
                    fillReader();
                    continue;
                }
                retval += lines.substring( readerIndex, ++nextIndex);
                readerIndex = nextIndex;
                return retval;
            }
        } catch (IOException e) {
            throw new MascotRuntimeException("RawTelnetIO<readln>: IOException " + e);
        } catch (IDAException e) {
            throw new MascotRuntimeException("RawTelnetIO<readln>: IDAException " + e);
        } catch (MascotRuntimeException e) {
            throw e;
        } catch (RuntimeException e1) {
            throw new MascotRuntimeException("RawTelnetIO<readln>: RuntimeException " + e1);
        }
    }
}
