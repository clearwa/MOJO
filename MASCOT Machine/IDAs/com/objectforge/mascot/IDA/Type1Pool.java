/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

/*
 * Created on 20-Feb-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.objectforge.mascot.IDA;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.objectforge.mascot.machine.idas.AbstractIDA;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.CountingQI;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * Abstract implemention for Device and Status pools
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public abstract class Type1Pool extends AbstractIDA {
    /**
    	* This is a local class that extends SPElement for the StatusPool object.
    */
    protected HashMap status = new HashMap();
    volatile int writeCount = 0;
    CountingQI readCQ = ControlQueue.CountingQFactory();
    CountingQI writeCQ = ControlQueue.CountingQFactory();
    CountingQI gate = ControlQueue.CountingQFactory();

    protected String[] statusKeys;
    protected List statusKeyList;

    /**
     * Method getContents.
     * Return the contents of the pool, ie. all objects whose key is not
     * a status key.  Note this is guarded by the write control queue so that
     * the snapshot will be consistent.  The argument snapshot controls whether
     * an entry for the total number of writes is prepended to the return vector.  
     * The objects in the return vector are clones of the originals.
     */
    protected void getContents(Vector retval, boolean snapshot) {
        writeCQ.que().cqJoin();
        try {
            if (snapshot)
                retval.add(new Integer(writeCount));

            for (Iterator i = status.keySet().iterator(); i.hasNext();) {
                Object theKey = i.next();

                if (statusKeyList == null || !statusKeyList.contains(theKey)) {
                    SPElement value = (SPElement) status.get(theKey);
                    SPElement element = (SPElement) value.clone();

                    retval.add(element);
                }
            }
        } finally {
            writeCQ.que().cqLeave();
        }
    }

    /**
     * Return just the contents - no count prepended
     */
    public Object read() {
        return read(false); //Read without the count
    }

    /**
     * Method read.
     * Read the contents.  Prepending count is dependent on snapshot
     */
    private Object read(boolean snapshot) {
        Vector retval = new Vector();
        readCQ.join(); //Join the read queue - non-blocking
        try {
            readCQ.waitQ(); //Wait for a stim
            while (writeCQ.status()) {
                readCQ.waitQ(); //Don't read while there are writers pending
            }
            getContents(retval, snapshot); //Get the contents
        } finally {
            if (readCQ.leave())
                readCQ.stim(); //release the next reader on the way out
        }
        return retval;
    }

    /**
     * Method read.
     * Read immediately if writeCount exceeds the passed readCount, otherwise
     * wait for a write to occur.
     */
    public Object read(int readCount) {
        Vector retval = new Vector();

        if (writeCount > readCount) {
            getContents(retval, true);
            return retval;
        } else {
            while (writeCount <= readCount) {
                gate.join();
                try {
                    gate.waitQ();
                } finally {
                    if (gate.leave()) {
                        gate.stim();
                    }
                }
            }
            return read(true);
        }
    }

    /**
     * Write an object to/modify an object in the status pool.  
     */
    public void write(Object contents) {
        Object[] content = (Object[]) contents;
        SPElement element;

        writeCQ.join(); //Join the write queue - non-blocking

        //If the key is not already there then create it and add it.
        try {
            if (!status.containsKey(content[0]) || status.get(content[0]) == null) {
                element = new SPElement();
                status.put(content[0], element);
            }

            //Get the element and modify it
            element = (SPElement) status.get(content[0]);
            element.modCount++;
            writeCount++;
            element.contents = content;
        } finally {
            writeCQ.leave(); //Leave the write queue
            readCQ.stim(); //Stim the read queue on the way out
            gate.stim();
        }
    }

    /**
     * Get the status area.  Initialization and reset ensure that the status keys
     * exist with null values.
     */
    public Object[] status() {
        if (statusKeys == null) {
            return new Object[0];
        }
        Object[] retval = new Object[statusKeys.length];

        writeCQ.que().cqJoin(); //Synchronize on write
        try {
            for (int i = 0; i < statusKeys.length; i++) {
                retval[i] = status.get(statusKeys[i]);
                if (retval[i] == null)
                    retval[i] = new SPElement();
            }
        } finally {
            writeCQ.que().cqLeave();
        }
        return retval;
    }

    /**
     * Method reset.
     * Reset the pool.  Contents remain as they were; counts and queues are
     * reset.
     */
    public void reset() {
        readCQ.que().cqJoin();
        writeCQ.que().cqJoin();
        try {
            status = new HashMap();
            writeCount = 0;
            readCQ.init();
            writeCQ.init();
            if (statusKeyList != null) {
                for (Iterator i = statusKeyList.iterator(); i.hasNext();)
                    status.put((String) i.next(), null);
            }
        } finally {
            writeCQ.que().cqLeave();
            readCQ.que().cqLeave();
        }
    }

    //Convenience routines to manipulate the SPElement objects returned by this pool
    private static boolean canDecode(Object element) {
        return element instanceof SPElement && ((SPElement) element).contents != null;
    }

    /**
     * Method booleanValue.
     */
    public static boolean booleanValue(Object element) throws MascotRuntimeException {
        if (canDecode(element))
            return ((Boolean) ((Object[]) ((SPElement) element).contents)[1]).booleanValue();
        throw new MascotRuntimeException("SPE.booleanValue: Cannot decode");
    }

    /**
     * Method intValue.
     * @throws MascotRuntimeException
     */
    public static int intValue(Object element) throws MascotRuntimeException {
        if (canDecode(element))
            return ((Integer) ((Object[]) ((SPElement) element).contents)[1]).intValue();
        throw new MascotRuntimeException("SPE.intValue: Cannot decode");
    }

    /**
     * Method stringValue.
     */
    public static String stringValue(Object element) {
        if (canDecode(element))
            return (String) ((Object[]) ((SPElement) element).contents)[1];
        return null;
    }

    /**
     * Method contents.
     */
    public static Object contents(Object element) {
        if (canDecode(element))
            return ((Object[]) ((SPElement) element).contents)[1];
        return null;
    }

    /**
     * Method tag.
     */
    public static Object tag(Object element) {
        if (canDecode(element))
            return ((Object[]) ((SPElement) element).contents)[0];
        return null;
    }
}
