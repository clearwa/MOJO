/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.IDA;

import java.util.Vector;

import com.objectforge.mascot.machine.idas.AbstractIDA;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.model.IChannel;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * The implementation of a serial channel.  This channel provides a generic mechanism
 * for reading/writing object references in FIFO order.  Reads and writes are blocking.
 * 
 * Since this is an IDA implementation only Mascot Machine synchronization primatives are
 * used.
  * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
*/
public class SerialChannel extends AbstractIDA implements IChannel {
    //The read and write control queues
    private ControlQueue readCQ = new ControlQueue();
    private ControlQueue writeCQ = new ControlQueue();
    private ControlQueue gateCQ = new ControlQueue();
    private ControlQueue termCQ = new ControlQueue();
    //The contents of the channel
    protected Vector channelContents = new Vector();
    //Default capacity of the channel
    protected volatile int capacity = 10;
    //Read and write counts
    private volatile int reads = 0;
    private volatile int writes = 0;
    boolean terminate;

    /**
     * Method createException.
     * Generate a new IDAException object and set its message string to reflect
     * message and errval.
     */
    private IDAException createException(String message, int errval) {
        return new IDAException(
            "SerialChannel"
                + ":"
                + Thread.currentThread().getName()
                + ":"
                + message
                + " - "
                + ControlQueue.errorString(errval));
    }

    /**
     * A method to remove an object from the content vector.  May be overridden by a subclass
     * to change channel behaviour.
     * @return
     */
    protected Object retrieveContents() {
        return channelContents.remove(0);
    }

    /**
     * Processing is:
     * 	join read CQ
     * 	<if nothing to read>
     * 		wait on the read CQ
     * 	remove the first object in the channel
     * 	leave read CQ
     * 	stim write CQ
     * 	return object
     */
    public Object read() throws IDAException {
        Object retval;
        int errval;

        try {
            if ((errval = readCQ.cqJoin()) == ControlQueue.CQ_ERROR)
                throw createException("readCQ:join", errval);
            while (channelContents.size() <= 0) {
                if ((errval = readCQ.cqWait()) != ControlQueue.CQ_OK)
                    throw createException("readCQ:wait - ", errval);
            }
            try {
                gateCQ.cqJoin();
                reads++;
                retval = retrieveContents();
            } finally {
                gateCQ.cqLeave();
            }
            if ((errval = readCQ.cqLeave()) != ControlQueue.CQ_OK)
                throw createException("readCQ:leave", errval);
            writeCQ.cqStim();
        } catch (MascotRuntimeException e) {
            throw e;
        }
        return retval;
    }

    /**
     * A method to add an object to the contents vector.  This method may be overridden to change
     * the add behaviour from simple fifo to something else.
     * @param contents
     */
    protected void addContents(Object contents) {
        channelContents.add(contents);
    }

    /**
     * Processing is:
     * 	join write CQ
     * 	<if the channel is full>
     * 		wait on the write CQ
     * 	add the object to the channel
     * 	leave write CQ
     * 	stim read CQ
     * 	return
     */
    public void write(Object contents) throws IDAException {
        int errval;

        try {
            if ((errval = writeCQ.cqJoin()) == ControlQueue.CQ_ERROR)
                throw createException("writeCQ:join", errval);
            while (channelContents.size() >= capacity) {
                if ((errval = writeCQ.cqWait()) != ControlQueue.CQ_OK)
                    throw createException("writeCQ:wait", errval);
            }
            try {
                gateCQ.cqJoin();
                writes++;
                addContents(contents);
            } finally {
                gateCQ.cqLeave();
            }
            errval = writeCQ.cqLeave();
            switch (errval) {
                case ControlQueue.CQ_OK :
                case ControlQueue.CQ_OWNED :
                    break;
                default :
                    throw createException("writeCQ:leave", errval);
            }
            readCQ.cqStim();
        } catch (MascotRuntimeException e) {
            throw e;
        }
    }

    /**
     * How many objects are currently in the channel and the size of the vector
     */
    public Object[] status() {
        Object[] retval = new Object[2];

        try {
            gateCQ.cqJoin();
            retval[1] = new Integer(writes - reads);
            retval[0] = new Integer(channelContents.size());
        } finally {
            gateCQ.cqLeave();
        }
        return retval;
    }

    /**
     * Method setCapacity.
     * Sets the limit on the size of the channel.  Decreasing capacity does not discard
     * objects that are already in the channel.
     */
    public void setCapacity(Integer capacity) {
        try {
            gateCQ.cqJoin();
            this.capacity = capacity.intValue();
        } finally {
            gateCQ.cqLeave();
        }
    }
    /**
     * @return
     */
    public boolean isTerminate() {
        boolean retval;

        termCQ.cqJoin();
        try {
            retval = terminate;
        } finally {
            termCQ.cqLeave();
        }
        return retval;
    }

    /**
     * @param b
     */
    public void setTerminate(boolean b) {
        termCQ.cqJoin();
        try {
            terminate = b;
        } finally {
            termCQ.cqLeave();
        }
    }

}