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
 * EchoHandlerRoot
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests
 * Created on 07-Apr-2004 by @author Clearwa
*/
public class EchoHandlerRoot extends AbstractRoot {

    /**
     * 
     */
    public EchoHandlerRoot() {
        super();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        try {
            write("writer", "This is a line from the echo handler ");
            resumeRoot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() throws Exception {
        while (true) {
            write("writer", (Object) read("reader"));
        }
    }
}
