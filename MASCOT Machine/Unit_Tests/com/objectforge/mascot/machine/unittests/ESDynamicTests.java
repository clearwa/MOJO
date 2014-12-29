/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.unittests;

import java.util.Vector;

import junit.framework.TestCase;

import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.estore.TestEntityStore;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Reaper;
import com.objectforge.mascot.machine.internal.WorkerDelegate;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * ESDynamicTests
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests
 * Created on 10-Apr-2004 by @author Clearwa
*/
public class ESDynamicTests extends TestCase {

    /**
     * Constructor for ESDynamicTests.
     * @param arg0
     */
    public ESDynamicTests(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        MascotDebug.setDebug(9);

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //Test statics
    Console console = new Console();

    /*
     * If the creation tests succeed then read a SETs document to test the device
     * stuff
     */
    public void testSET() throws InterruptedException {
        //Clear the respository and print it again  
        TestEntityStore.init();
        Utilities.printIt(EntityStore.mascotRepository().toString());
        Utilities.stall("The repository should be in its init state...");

        try {
            Utilities.printIt("Load echo-test.macp-1...");
            EntityStore.merge(console.enroll("echo-test-1.macp"));
            Utilities.stall("Check the output, the echo-test subsystem should have run...");
        } catch (MascotMachineException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test the reaper
     * 
     * @throws MascotMachineException
    */
    public void testReaper() throws MascotMachineException {
        Utilities.printIt(EntityStore.mascotRepository().toString());
        EntityStore baseES = EntityStore.mascotRepository();
        IMascotReferences reaperActivity =
            baseES.addActivityToWorker("global", Reaper.class.getName(), null, null);
        Vector reapers = new Vector();
        Activity reap =
            (Activity) WorkerDelegate.addWorker(
                EntityStore.getGlobalSubsystem(),
                reaperActivity,
                null,
                null,
                reapers);
        reapers.removeAllElements();
        reap.actStart("Reaper");
        System.gc();
        Utilities.stall("Running the reaper - will take up to 5 seconds for the first pass...");

        Utilities.printIt(EntityStore.mascotRepository().toString());
        Utilities.stall("The repository state after reaper run...");
    }

    /*
     * Test includes and argument.  Note the respository is not reset so all the
     * previous entities are defined, in particular the echo device.
     */
     public void testIncludes() throws MascotMachineException{
         MascotDebug.setDebug(9);
         Utilities.printIt("Load echo-test-2.macp...");
         EntityStore.merge(console.enroll("echo-test-2.macp"));
         Utilities.stall("Check the output, the echo-test-2 subsystem should have run...");
         Utilities.printIt(EntityStore.mascotRepository().toString());
         Utilities.stall("The repository state after echo-test-2 run...");
     }

}
