/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package com.objectforge.mascot.transaction;

import com.objectforge.mascot.IDA.SerialChannel;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.SpawnWorker;
import com.objectforge.mascot.machine.internal.SpawnedRoot;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.roots.AbstractRoot;

/**
 * SimpleRoot
 * 
 * Project: MASCOT Examples
 * Package: com.objectforge.mascot.transaction
 * Created on 17-Oct-2003 by @author Clearwa
*/
public class SimpleRoot extends AbstractRoot {
    private SerialChannel localChan = new SerialChannel();
       
    private class Trans1 implements SpawnedRoot {

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.internal.SpawnedRoot#spawned(java.lang.Object[])
         */
        public void spawned(Object args) {
            System.out.println( args);
            try {
                localChan.write("++ Transaction 1 finishes");
            } catch (IDAException e) {
                e.printStackTrace();
            }
        }
    }

    private class Trans2 implements SpawnedRoot {

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.internal.SpawnedRoot#spawned(java.lang.Object[])
         */
        public void spawned(Object args) {
            System.out.println( args );
            try {
                localChan.write("++ Transaction 2 finishes");
            } catch (IDAException e) {
                 e.printStackTrace();
            }
        }
    }

    public SpawnedRoot T1Factory() {
        return this.new Trans1();
    }

    public SpawnedRoot T2Factory() {
        return this.new Trans2();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        try {
            SpawnWorker.spawn(activity.getSubsystem(), this.T1Factory(), "Transaction 1 runs");
            SpawnWorker.spawn(activity.getSubsystem(), this.T2Factory(), "Transaction 2 runs");
            for(int i=0;i<2;){
                Object result = localChan.read();
                
                if( result instanceof String ){
                    System.out.println( result );
                    i++;
                    continue;
                }
                break;
            }
            System.out.println("Transaction Test exits");
        } catch (MascotMachineException e) {
            e.printStackTrace();
        } catch (IDAException e) {
            e.printStackTrace();
        }
        return;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() {
        //Does nothing
    }

}
