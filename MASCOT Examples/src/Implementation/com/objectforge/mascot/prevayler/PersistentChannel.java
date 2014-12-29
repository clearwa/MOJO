/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 *
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package com.objectforge.mascot.prevayler;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.FileManager;

import com.objectforge.mascot.machine.idas.AbstractIDA;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.CountingQI;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * PersistentChannel
 *
 * Project: MASCOT Examples
 * Package: com.objectforge.mascot.prevayler
 * Created on 08-Oct-2003 by @author Clearwa
 *
 * This is an implementation of a serial channel that has persistance.
*/
public class PersistentChannel extends AbstractIDA {
    private Prevayler pSystem;
    private PersistentList pList;
    protected String baseDir = null;
    CountingQI processQ = ControlQueue.CountingQFactory();
    ControlQueue readQ = new ControlQueue();
    CountingQI writeQ = ControlQueue.CountingQFactory();
    ControlQueue relayReadQ = new ControlQueue();
    CountingQI relayWriteQ = ControlQueue.CountingQFactory();
    private boolean initialized = false;

    public static final int PCINIT = 0;
    public static final int PCCREATE = 1;
    public static final int PCSNAPSHOT = 2;

    public PersistentChannel() {
        //Force a garbage collection to flush any instances that may be holding files open in
        //the base directory
        System.gc();
    }
    private void checkProcess() {
        if (!initialized) {
            processQ.join();
            processQ.waitQ();
            processQ.leave();
        }
        if (processQ.status()) {
            processQ.stim();
        }
    }

    boolean checkPayload( Object payload ){
        return payload != null;
    }

    //Reader, writer, and status for the relay side of the persistent channel

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IIDA#read()
     */
    public Object readRelay() throws IDAException {
        checkProcess();
        relayReadQ.cqJoin();
        //Stall until there is something to read
        while (true) {
            switch (((Integer) statusRelay()[0]).intValue()) {
                case 0 :
                    //There is nothing to read - wait for it to appear
                    relayReadQ.cqWait();
                    continue;
                case 1 :
                    //There is something to read but it has not triggered - sleep for a while
                    try {
                        Thread.sleep(1000); //check it every second
                    } catch (InterruptedException e1) {
                        //Ignore
                    }
                    continue;
                case 2 :
                    //There is something and it is available - read it
                    break;
                default : //What??
            }
            break;
        }
        relayWriteQ.join(); //Join the back of the que to let pending writers succeed

        Object retval = null;
        try {
            retval = pSystem.execute(PersistentList.readRelayQuery());
        } catch (Exception e) {
            throw (new IDAException("PersistentChannel:<read> - " + e));
        } finally {
            relayReadQ.cqLeave();
            relayWriteQ.leave();
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IIDA#write(java.lang.Object)
     */
    public void writeRelay(Object contents, int delay) throws IDAException {
        if( !checkPayload( contents )  ){
            return;
        }
        checkProcess();
        relayWriteQ.join();

        //Setup the time this thing is supposed to trigger
        try {
            Calendar trigger = Calendar.getInstance();
            trigger.add(Calendar.SECOND, delay);
            pSystem.execute(PersistentList.writeRelay(new Object[] { trigger.getTime(), contents }));
        } finally {
            relayWriteQ.leave();
            relayReadQ.cqStim();
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IIDA#status()
     */
    public Object[] statusRelay() {
        checkProcess();
        writeQ.join();
        try {
            return (Object[]) pSystem.execute(pList.relayStatusQuery());
        } catch (Exception e) {
            MascotDebug.println(0, "PersistenChannl:<status exception> -  " + e);
        } finally {
            writeQ.leave();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IIDA#read()
     */
    public Object read() throws IDAException {
        checkProcess();
        readQ.cqJoin();
        //Stall until there is something to read
        while (((Integer) status()[0]).intValue() < 1) {
            readQ.cqWait();
        }
        writeQ.join(); //Join the back of the que to let pending writers succeed

        Object retval = null;
        try {
            retval = pSystem.execute(PersistentList.readContainerQuery());
        } catch (Exception e) {
            throw (new IDAException("PersistentChannel:<read> - " + e));
        } finally {
            readQ.cqLeave();
            writeQ.leave();
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IIDA#write(java.lang.Object)
     */
    public void write(Object contents) throws IDAException {
        if( !checkPayload( contents )  ){
            return;
        }
        checkProcess();
        writeQ.join();
        pSystem.execute(PersistentList.writeContainer(contents));
        writeQ.leave();
        readQ.cqStim();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IIDA#status()
     */
    public Object[] status() {
        checkProcess();
        writeQ.join();
        try {
            return (Object[]) pSystem.execute(PersistentList.containerStatusQuery());
        } catch (Exception e) {
            MascotDebug.println(0, "PersistenChannl:<status exception> -  " + e);
        } finally {
            writeQ.leave();
        }
        return null;
    }

    /**
     * Discard the discard object
     * @param discard
     */
    public void discard(Object discard) {
        if( !checkPayload( discard )  ){
            return;
        }
        checkProcess();
        writeQ.join();
        pSystem.execute(PersistentList.discard(discard));
        writeQ.leave();
    }

    /**
     * Initialize the contents of the persistent system from the base directory
     * @param directory
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Prevayler pCreate(String directory) throws IOException, ClassNotFoundException {
        if (pSystem != null)
            return pSystem;
        if (directory != null && !initialized) {
            baseDir = directory;

            pList = new PersistentList();
            //Create and configure the factory
            PrevaylerFactory factory = new PrevaylerFactory();
            factory.configurePrevalentSystem(pList);
            factory.configurePrevalenceDirectory(directory);
            //I do not want to filter transactions
            factory.configureTransactionFiltering(false);
            //Create the sucker
            Prevayler mp = factory.create();
            //Now that the system is initialized, elide the discards with the the contents
            //so recovery is complete.
            mp.execute( PersistentList.elide() );
            //Since all of this happened behind the prevalent system's back crank out a snapshot
            mp.takeSnapshot();
            initialized = true;
            processQ.stim();
            readQ.cqStim();
            return mp;
        }
        return null;
    }

    public boolean control(int action) throws IOException, ClassNotFoundException {
        return control(action, baseDir);
    }

    /**
     * @param action
     * @return
     */
    public boolean control(int action, String directory) throws IOException, ClassNotFoundException {
        try {
            writeQ.join();
            switch (action) {
                case PCCREATE :
                    if (pSystem != null)
                        return true;

                    File workingDir = FileManager.produceDirectory(directory);
                    File[] files = workingDir.listFiles();
                    if (files != null) {
                        for (int i = 0; i < files.length; i++) {
                            files[i].delete();
                        }
                    }
                    //Fall through to PCINIT code
                case PCINIT :
                    pSystem = pCreate(directory);
                    return (pSystem != null);
                case PCSNAPSHOT :
                    if (pSystem == null)
                        break;
                    writeQ.join();
                    try {
                        pSystem.takeSnapshot();
                        return true;
                    } finally {
                        writeQ.leave();
                    }
                default :
                    break;
            }
        } finally {
            writeQ.leave();
        }
        return false;
    }

    /**
     * @return
     */
    public String getBaseDir() {
        return baseDir;
    }

}
