/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package mascot.test.transaction;

import junit.framework.TestCase;

import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.SpawnWorker;
import com.objectforge.mascot.machine.internal.SpawnedRoot;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * MascotTransactionTest
 * 
 * Project: MASCOT Examples
 * Package: mascot.test.transaction
 * Created on 17-Oct-2003 by @author Clearwa
*/
public class MascotTransactionTest extends TestCase {

    /**
     * Constructor for transactionTest.
     * @param arg0
     */
    public MascotTransactionTest(String arg0) {
        super(arg0);
    }

    private class Trans1 implements SpawnedRoot {
        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.internal.SpawnedRoot#spawned(java.lang.Object[])
         */
        public void spawned(Object args) {
            System.out.println("Transaction 1 runs");            
        }
    }

    private class Trans2 implements SpawnedRoot {

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.internal.SpawnedRoot#spawned(java.lang.Object[])
         */
        public void spawned(Object args) {
            System.out.println("Transaction 2 runs");            
        }
    }

    public SpawnedRoot T1Factory() {
        return this.new Trans1();
    }

    public SpawnedRoot T2Factory() {
        return this.new Trans2();
    }

    public void testTransaction() {
        try {
            SpawnWorker.spawn(EntityStore.getGlobalSubsystem(), this.T1Factory(), null);
            SpawnWorker.spawn(EntityStore.getGlobalSubsystem(), this.T2Factory(), null);
            System.out.println("Transaction Test exits");
        } catch (MascotMachineException e) {
            e.printStackTrace();
        }
        return;
    }

}
