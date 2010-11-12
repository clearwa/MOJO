/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.internal;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.ActivityEntity;
import com.objectforge.mascot.machine.model.IRoot;
import com.objectforge.mascot.machine.model.SETEntity;
import com.objectforge.mascot.machine.model.SETReference;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * @author Clearwa
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Reaper extends AbstractRoot {

    /**
     * 
     */
    public Reaper() {
        super();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    /**
     * The Reaper is a background activity that looks at the ActivityEntitys in the worker SET to see if they are
     * running, aka. alive.  If not then it reaps these entries.
     */
    public void mascotRoot(Activity activity, Object[] args) {
        int iteration = 0;

        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            } //Do this once per second

            iteration++;
            try {
                SETEntity wd = (SETEntity) EntityStore.mascotRepository().getSetDescriptors().get("worker");
                List roots = new ArrayList(wd.getSetContents());

                MascotDebug.println(15, "\n&&&&&&&&&&&&&&&&&&&&&&");
                for (Iterator i = roots.iterator(); i.hasNext();) {
                    boolean doReap = false;
                    SETReference setRef = (SETReference) i.next();
                    if(!(setRef.lookup() instanceof ActivityEntity) ){
                        continue;
                    }
                    ActivityEntity act = (ActivityEntity) setRef.lookup();

                    MascotDebug.print(
                        15,
                        ("Activity: "
                            + act.getUniqueName()
                            + ((act.isReap()) ? " is " : " is not ")
                            + "marked for reaping"));
                    if (!act.isReap()) {
                        //If not marked to reap then ignore
                        MascotDebug.println(15, "");
                        continue;
                    }
                    if (act.getCurrentIncarnation() == null) {
                        MascotDebug.println(15, ", incarnation is null");
                        doReap = true;
                    } else {
                        Hashtable incarnation = act.getCurrentIncarnation().getInstances();
                        if (!(incarnation.size() > 0)) {
                            MascotDebug.println(15, ", has no instances");
                            doReap = true;
                        } else {
                            MascotDebug.println(15, "");
                            for (Iterator inst = incarnation.keySet().iterator(); inst.hasNext();) {
                                IRoot eiroot = (IRoot) inst.next();
                                MascotDebug.println(15, "  Instance: " + eiroot.getClass().getName());
                            }
                        }
                    }
                    if (doReap) {
                        MascotDebug.println(
                            6,
                            "    ---- Reaping on iteration " + iteration + ", activity: " + act);
                        wd.removeMember(setRef);
                    }
                }
            } catch (MascotMachineException e1) {
                // On any problem simply bag it - probably should comment?
            }

        }

    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() {
        // do nothing

    }

}
