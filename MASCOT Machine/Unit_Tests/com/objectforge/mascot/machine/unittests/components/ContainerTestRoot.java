/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.unittests.components;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.machine.unittests.Utilities;

/**
 * ContainerTestRoot
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests.components
 * Created on 12-Apr-2004 by @author Clearwa
*/
public class ContainerTestRoot extends AbstractRoot {
    String[] reskeys = new String[] { "arg1", "arg2", "contained" };
    Console console = new Console();

    /**
     * 
     */
    public ContainerTestRoot() {
        super();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.roots.AbstractRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        //Print the pass arguments
        for (int i = 0; i < args.length; i++) {
            Utilities.printIt("Argument - index: " + i + ", value:" + args[i]);
        }
        //Print the subsystem resources
        for (int i = 0; i < reskeys.length; i++) {
            Utilities.printIt(
                "Resource - index: " + reskeys[i] + ", value:" + getSubsystem().getResource(reskeys[i]));
        }
        //Open the device
        try {
            open("echo", "echo-2");
            Subsystem contained =
                console.form((String) getSubsystem().getResource("contained"), getSubsystem());
            contained.addArgToSubsystem(0, resolve("echo-2", Device.WRITER));
            contained.addArgToSubsystem(1, resolve("echo-2", Device.READER));
            contained.subStart();
        } catch (MascotMachineException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() throws Exception {
        // Do nothing

    }

}
