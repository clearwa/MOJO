/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.internal;

import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.roots.AbstractRoot;

/**
 * BootTester
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.internal
 * Created on 18-Apr-2004 by @author Clearwa
*/
public class BootTester extends AbstractRoot {
    int counter = 1;

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.roots.AbstractRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        while (true) {
            EntityStore.mascotRepository().toString();
            System.out.print(".");
            if( (++counter%100)==0){
                System.out.println( counter );
            }
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() throws Exception {
    }
}

