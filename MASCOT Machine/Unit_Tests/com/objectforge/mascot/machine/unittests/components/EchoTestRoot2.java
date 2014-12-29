/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.unittests.components;

import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.roots.AbstractRoot;

/**
 * EchoTestRoot2
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests.components
 * Created on 13-Apr-2004 by @author Clearwa
*/
public class EchoTestRoot2 extends AbstractRoot {
    IIDA reader;
    IIDA writer;

    /**
     * 
     */
    public EchoTestRoot2() {
        super();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        try {
            reader = (IIDA) getSubsystem().getArgFromSubsystem(0);
            writer = (IIDA) getSubsystem().getArgFromSubsystem(1);
            resumeRoot();
            EntityStore.getGlobalSubsystem().subTerminate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() throws Exception {
        System.out.println( reader.read());
        writer.write( "This is a test from " + this.getClass().getName());
        System.out.println( reader.read() );
    }

}
