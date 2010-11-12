/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.internal;

import com.objectforge.mascot.machine.estore.*;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
//import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotUtilities;

/**
 * @author Clearwa
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Bootstrap extends AbstractRoot {
    /**
     * 
     */
    public Bootstrap() {
        super();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.roots.AbstractRoot#root(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        EntityStore es;
        Console console = new Console();

        EntityStore.MascotBundle = MascotUtilities.getMascotBundle();
        MascotDebug.println(9, EntityStore.Banner());
        String debug = MascotUtilities.getMascotResource("mascot.machine.debug");
        if (debug == null) {
            MascotDebug.setDebug(0);
        } else {
            try {
                MascotDebug.setDebug((new Integer(debug)).intValue());
            } catch (NumberFormatException e1) {
                MascotDebug.setDebug(0);
            }
        }
//        //Councurrency tester
//        try {
//            EntityStore baseES = EntityStore.mascotRepository();
//            IMascotReferences bootActivity =
//                baseES.addActivityToWorker("tester", BootTester.class.getName(), null, null);
//            Activity boot =
//                (Activity) WorkerDelegate.addWorker(
//                    EntityStore.getGlobalSubsystem(),
//                    bootActivity,
//                    null,
//                    null);
//            boot.actStart("Tester");
//        } catch (MascotMachineException e1) {
//            e1.printStackTrace();
//        }
        es = console.enroll(MascotUtilities.getMascotResource("mascot.machine.boot"));
        try {
            EntityStore.merge(es);
        } catch (MascotMachineException e) {
            e.printStackTrace();
        }
        MascotDebug.println(9, "\n\nAfter boot" + EntityStore.storeToString());
//        EntityStore.mascotRepository().toString();
        Console.getReaper().setReap();
        MascotDebug.println(9, "Console exits");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() {
        // Do nothihng			
    }

}
