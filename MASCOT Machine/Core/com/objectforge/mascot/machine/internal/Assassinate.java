/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.internal;

import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.machine.scheduler.MascotThread;
import com.objectforge.mascot.utility.MascotDebug;

public class Assassinate extends AbstractRoot {
    /**
     * @param Subsystem
     */
    public Assassinate() {
        super();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.roots.AbstractRoot#root(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        TerminationState termState = (TerminationState) args[0];
        Subsystem mySub = (Subsystem) args[1];

        mySub.lockForAssassin(termState);

        //Terminate all contained activities
        Thread[] enumerated = new Thread[Thread.activeCount()];
        //			int count = Thread.currentThread().getThreadGroup().enumerate(enumerated, false);
        int count = subsystem.threadGroup.enumerate(enumerated, false);

        for (int i = 0; i < count; i++) {
            Thread toKill = enumerated[i];

            if (toKill != Thread.currentThread()) {
                if (toKill instanceof MascotThread) {
                    ((MascotThread) toKill).setDead(termState.getDead());
                    ((MascotThread) toKill).setSuspended(termState.getSuspend());
                    if (!termState.isResume()) {
                        toKill.interrupt();
                    }
                } else if (termState.isKill()) {
                    toKill.interrupt();
                }
            } else {
                MascotDebug.println(9, "Assassin marks itself as dead - " + toKill.getName());
                ((MascotThread) toKill).setDead(true);
                ((MascotThread) toKill).setSuspended(false);
            }
        }
        if (termState.isKill()) {
            mySub.waiters.stim();
            mySub.subDeallocate();
            if (mySub.container != null)
                mySub.container.removeSubsystem(mySub);
        } else if (termState.isResume()) {
            subsystem.suspendObject.stim();
        }
        mySub.reaper.setReap();
        mySub.assassinationSync.cqStim();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() {
        // Does nothing
    }
}