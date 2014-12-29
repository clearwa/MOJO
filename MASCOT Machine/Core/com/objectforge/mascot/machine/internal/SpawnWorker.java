/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.internal;

import java.util.Vector;

import com.objectforge.mascot.machine.estore.*;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.MascotThread;

/**
 * IMascotTransaction
 * 
 * Project: MASCOT Examples
 * Package: com.objectforge.mascot.transaction
 * Created on 17-Oct-2003 by @author Clearwa
*/
public class SpawnWorker {
    private static long activityTag = 0;
    private static ControlQueue gate = new ControlQueue();
    
    //There is no public constructor
    private SpawnWorker(){
    }

    private class SpawnedRunner extends AbstractRoot {

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.roots.AbstractRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
         */
        public void mascotRoot(Activity activity, Object[] args) {
            ((ControlQueue) args[0]).cqStim();
            ((SpawnedRoot) args[1]).spawned(args[2]);
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
         */
        public void resumeRoot() {
            // Does Nothing

        }
    }
    
    public SpawnedRunner runnerFactory(){
        return this.new SpawnedRunner();
    }

    public static Object spawn(Subsystem subsystem, SpawnedRoot runner, Object theTransaction)
        throws MascotMachineException {
        ControlQueue myQ = new ControlQueue();
        Vector args = new Vector();
        Activity transAct;

        gate.cqJoin();
        try {
            args.add(myQ);
            args.add(runner);
            args.add(theTransaction);
            myQ.cqJoin();
            try {
                String myTag = "SPAWN:" + activityTag++;
                SpawnWorker myInstance = new SpawnWorker();

                IMascotReferences runnerFactory = EntityStore.mascotRepository().addActivityToWorker(
                    subsystem.getName(),
                    myInstance,
                    "runnerFactory",
                    args,
                    myTag);
                transAct =
                    (Activity) ((MascotThread) Thread.currentThread()).getActivity().getRoot().addWorker(
                        subsystem,
                        runnerFactory,
                        args,
                        myTag);
                transAct.actStart(myTag);
                myQ.cqWait();
            } finally {
                myQ.cqLeave();
            }
        } finally {
            gate.cqLeave();
        }
        return theTransaction;
    }

    //Helper methods
    public static ControlQueue spwanQue(Object[] args) {
        return (ControlQueue) args[0];
    }

    public static Object spawnTransaction(Object[] args) {
        return args[1];
    }
}
