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
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.roots.AbstractRoot;

/**
 * EchoTestRoot1
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests.components
 * Created on 07-Apr-2004 by @author Clearwa
*/
public class EchoTestRoot1 extends AbstractRoot {

    /**
     * 
     */
    public EchoTestRoot1() {
        super();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        try {
            open( "echo","stammer");
            resumeRoot();
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
        //Read the announcement form the device
        IIDA rchan = (IIDA) resolve("stammer",Device.WRITER);
        IIDA wchan = (IIDA) resolve("stammer",Device.READER);
        System.out.println( rchan.read());
        wchan.write( "This is a test from " + this.getClass().getName());
        System.out.println( rchan.read() );
    }

}
