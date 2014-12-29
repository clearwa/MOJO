/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.unittests.components;

import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.roots.AbstractRoot;

/**
 * TestRootSimple
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests.components
 * Created on 05-Apr-2004 by @author Clearwa
*/
public class TestRootSimple extends AbstractRoot {

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        System.out.println("Simple test root");

    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() throws Exception {
        // Do nothing

    }

}
