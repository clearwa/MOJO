/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA;

import com.objectforge.mascot.machine.idas.AbstractIDA;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.CountingQI;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * TokenPool
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA
 * Created on 27-Feb-2004 by @author Clearwa
*/
public class TokenPool extends AbstractIDA {
    CountingQI readers = ControlQueue.CountingQFactory();
    ControlQueue gate = new ControlQueue();
    volatile int maxTokens = 5; //The default value
    volatile int tokens;

    public TokenPool() {
        tokens = maxTokens;
    }

    private int setTokenValue(boolean increment) {
        int retval = 0;

        try {
            gate.cqJoin();
            if (increment && (tokens <= maxTokens)) {
                retval = ++tokens;
            } else if (tokens > 0) {
                retval = tokens--;
            }
            return retval;
        } finally {
            MascotDebug.println(9, "Tokens = " + tokens);
            gate.cqLeave();
        }
    }

    private Object readToken(boolean direction) throws IDAException {
        int retval = -1;

        try {
            while (true) {
                retval = setTokenValue(direction);
                if (direction) {
                    //Wirtes always succeed
                    readers.stim();
                    break;
                } else if (retval > 0) {
                    //For a read, succeed when I get a return > 0
                    break;
                } else {
                    //Suspend the caller until a token is available
                    readers.join();
                    readers.waitQ();
                    if (readers.leave()) {
                        readers.stim();
                    }
                }
            }
            return new Integer(retval);
        } finally {
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IIDA#read()
     */
    public Object read() throws IDAException {
        return readToken(false);
    }

    public Object write() throws IDAException {
        return readToken(true);
    }

    public void setCapacity(int limit) {
        gate.cqJoin();
        //Change the capacity - first note how many are already checked out.  It may not be possible to 
        //reduce the capcity depending on how many are gone already.  The capacity cannot be less than the 
        //number of tokens that are already allocated.
        int checkedOut = maxTokens - tokens;
        int newMax = (limit > checkedOut) ? limit : checkedOut;

        tokens = newMax - checkedOut;
        maxTokens = newMax;
        gate.cqLeave();
    }

}
