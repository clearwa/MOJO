/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package mascot.test.persistentchannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import junit.framework.TestCase;

import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.WorkerDelegate;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.MascotThread;
import com.objectforge.mascot.prevayler.PersistentChannel;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * PersistentThreadingTest
 * 
 * Project: MASCOT Examples
 * Package: mascot.test.persistentchannel
 * Created on 13-Oct-2003 by @author Clearwa
*/
public class PersistentThreadingTest extends TestCase implements Cloneable {
    private boolean runReader = true;
    private boolean runWriter = true;
    private boolean runRelay = true;
    private static Object[] testStrings =
        { "Test string 1", "Test string 2", "Test string 3", "Test string 4", "Test string 5", "temp" };
    volatile PersistentChannel myChan;
    ControlQueue syncQ = new ControlQueue();
    private boolean discard = true;
    int creation = PersistentChannel.PCCREATE;
    private boolean unorderedCompare = false;
    private boolean releaseOnRead = false;

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void compareVals(String[] listVals) {
        ArrayList valsList = new ArrayList(Arrays.asList(listVals));
        for (int i = 0; i < testStrings.length; i++) {
            if (unorderedCompare) {
                assertTrue("Unordered Compare results", valsList.contains(testStrings[i]));
                valsList.remove(testStrings[i]);
            } else {
                assertEquals("Ordered Compare results", testStrings[i], listVals[i]);
            }
        }
    }

    class LocalThread1 implements Runnable {

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            for (int i = 0; i < testStrings.length; i++) {
                try {
                    myChan.write(testStrings[i]);
                } catch (Exception e) {
                    PersistentThreadingTest.fail("Writer fails - " + e);
                }
            }
        }
    }

    class LocalThread2 implements Runnable {

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            Object[] listVals = new String[testStrings.length];

            try {
                for (int i = 0; i < testStrings.length; i++) {
                    try {
                        listVals[i] = (Object) myChan.read();
                        MascotDebug.println(0, "Channel: Index: " + i + ", Value: " + listVals[i]);
                        myChan.writeRelay(listVals[i], 3 + i);
                    } catch (Exception e) {
                        PersistentThreadingTest.fail("Reader fails - " + e);
                    }
                }
                compareVals((String[]) listVals);
            } finally {
                if (releaseOnRead) {
                    syncQ.cqStim();
                }
            }
        }
    }

    class LocalThread3 implements Runnable {

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            Object[] listVals = new String[testStrings.length];

            try {
                for (int i = 0; i < testStrings.length; i++) {
                    try {
                        listVals[i] = (Object) myChan.readRelay();
                        MascotDebug.println(0, "Relay: Index: " + i + ", Value: " + listVals[i]);
                        if (discard) {
                            myChan.discard(listVals[i]);
                        }
                    } catch (Exception e) {
                        PersistentThreadingTest.fail("Relay Reader fails - " + e);
                    }
                }
                compareVals((String[]) listVals);
                int status = ((Integer) myChan.statusRelay()[0]).intValue();
                if (discard) {
                    assertEquals("Relay is not empty", status, 0);
                }
                MascotDebug.println(0, "Mascot threads read/write test passes");
            } finally {
                syncQ.cqStim();
            }
        }
    }

    class WriteRoot extends AbstractRoot {

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.roots.AbstractRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
         */
        public void mascotRoot(Activity activity, Object[] args) {
            (new LocalThread1()).run();
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
         */
        public void resumeRoot() {
            //Does nothing
        }
    }

    class ReadRoot extends AbstractRoot {

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.roots.AbstractRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
         */
        public void mascotRoot(Activity activity, Object[] args) {
            (new LocalThread2()).run();
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
         */
        public void resumeRoot() {
            //Does nothing            
        }
    }

    class RelayRoot extends AbstractRoot {

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.roots.AbstractRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
         */
        public void mascotRoot(Activity activity, Object[] args) {
            (new LocalThread3()).run();
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
         */
        public void resumeRoot() {
            //Does nothing            
        }
    }

    public AbstractRoot WriteRootFactory() {
        return this.new WriteRoot();
    }

    public AbstractRoot ReadRootFactory() {
        return this.new ReadRoot();
    }

    public AbstractRoot RelayRootFactory() {
        return this.new RelayRoot();
    }

    /**
     * Constructor for PersistentThreadingTest.
     * @param arg0
     */
    public PersistentThreadingTest(String arg0) {
        super(arg0);
    }

    public void testInit() throws IOException {
        //Make sure any persistent information has gone away
        System.out.println("\n++Test init");
        PersistentTestUtils.channelDirDelete("pc_testdir");
    }

    public void testAsMascotThreads() {
        System.out.println("\n++Test as Mascot Threads");
        MascotThread writeThread = new MascotThread(new LocalThread1());
        MascotThread readThread = new MascotThread(new LocalThread2());
        MascotThread relayThread = new MascotThread(new LocalThread3());

        myChan = new PersistentChannel();
        writeThread.setName("PCWriter");
        writeThread.start();
        readThread.setName("PCReader");
        readThread.start();
        relayThread.setName("PCRelay");
        relayThread.start();
        assertTrue(
            "Mascot Threads create",
            PersistentTestUtils.channelControl(myChan, PersistentChannel.PCCREATE, "pc_testdir"));
        try {
            writeThread.join();
            readThread.join();
            relayThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runAsMascotActivities() {
        EntityStore es = EntityStore.mascotRepository(); //Create a new global subsystem

        Activity readerAct = null;
        Activity writerAct = null;
        Activity relayAct = null;

        myChan = new PersistentChannel();
        syncQ.cqJoin();
        try {
            if (runReader) {
                readerAct =
                    (Activity) WorkerDelegate.addWorker(
                        EntityStore.getGlobalSubsystem(),
                        es.addActivityToWorker("global", this, "ReadRootFactory", null),
                        null,
                        null);
            }
            if (runRelay) {
                relayAct =
                    (Activity) WorkerDelegate.addWorker(
                        EntityStore.getGlobalSubsystem(),
                        es.addActivityToWorker("global", this, "RelayRootFactory", null),
                        null,
                        null);
            }
            if (runWriter) {
                writerAct =
                    (Activity) WorkerDelegate.addWorker(
                        EntityStore.getGlobalSubsystem(),
                        es.addActivityToWorker("global", this, "WriteRootFactory", null),
                        null,
                        null);
            }
            if (writerAct != null) {
                writerAct.actStart("Test Writer");
            }
            if (readerAct != null) {
                readerAct.actStart("Test Reader");
            }
            if (relayAct != null) {
                relayAct.actStart("Test Relay");
            }
        } catch (MascotMachineException e) {
            fail("Failed to create and lauch MASCOT Activities");
        }
        assertTrue(
            "Mascot Threads create",
            PersistentTestUtils.channelControl(myChan, creation, "pc_testdir"));
        syncQ.cqWait();
    }

    public void testAsMascotActivities() {
        System.out.println("\n++Test as Mascot Activities");
        discard = true;
        runReader = true;
        runWriter = true;
        runRelay = true;
        runAsMascotActivities();
    }

    /**
     * Run the activities again, this time with discard false.  This means that
     * the channel should remember the inserted values on restart.
     *
     */
    public void testPersistence() {
        System.out.println("\n++Persistence Test");
        discard = false;
        runReader = true;
        runWriter = true;
        runRelay = true;
        runAsMascotActivities();
    }

    /**
     * Upon initialization, the elide should put values back into the 
     * read side.
     *
     */
    public void testElide() {
        System.out.println("\n++Elide test");
        discard = true;
        runReader = true;
        runWriter = false;
        runRelay = true;
        creation = PersistentChannel.PCINIT;
        unorderedCompare = true;
        runAsMascotActivities();
    }

    public void testRelayRetention() {
        System.out.println("\n++Relay Retention test");
        discard = false;
        runReader = true;
        runWriter = true;
        runRelay = false;
        creation = PersistentChannel.PCCREATE;
        unorderedCompare = false;
        releaseOnRead = true;
        runAsMascotActivities();
    }

    public void testRelay() {
        System.out.println("\n++Relay Read test");
        discard = false;
        runReader = false;
        runWriter = false;
        runRelay = true;
        creation = PersistentChannel.PCINIT;
        unorderedCompare = false;
        runAsMascotActivities();
    }
    
    public void testAgain(){
        System.out.println("\nOne final cycle");
        creation = PersistentChannel.PCINIT;
        testPersistence();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        myChan = null;
        System.gc();
        if (discard) {
            PersistentTestUtils.channelDirDelete("pc_testdir");
        }
    }

}
