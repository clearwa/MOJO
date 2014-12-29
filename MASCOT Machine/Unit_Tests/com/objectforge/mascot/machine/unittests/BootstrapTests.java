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

/**
 * BootstrapTests
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests
 * Created on 13-Apr-2004 by @author Clearwa
*/
public class BootstrapTests extends TestCase {

    private Console console = new Console();

    /**
     * Constructor for BootstrapTests.
     * @param arg0
     */
    public BootstrapTests(String arg0) {
        super(arg0);
    }
    
    public void testTelnet() throws MascotMachineException{
        //Clear the respository and print it again  
        TestEntityStore.init();
        Utilities.stall( "Start the reaper...");
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
        EntityStore telnet = console.enroll( "boot.macp");
        Utilities.printIt( telnet );
        Utilities.stall( "Start the telnet server...");
        EntityStore.merge( telnet );
        Utilities.stall( "The telnet server should be up and running...");
    }

}
