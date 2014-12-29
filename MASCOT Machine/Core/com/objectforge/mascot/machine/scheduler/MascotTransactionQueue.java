/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.scheduler;

import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * MascotTransactionQueue
 * This class extends the ControlQue to implement a transaction
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.scheduler
 * Created on 16-Apr-2004 by @author Clearwa
*/
public class MascotTransactionQueue extends ControlQueue {

    public Object doIt(IMascotTransaction runner, Object[] packet) throws MascotMachineException {
        //If I am already owner of the control queue then I do not wany to leave at on exit
        boolean doLeave = !isOwner();
        cqJoin();
        try {
            return runner.kernel(packet);
        } catch (Exception e) {
            if (e instanceof MascotMachineException) {
                throw (MascotMachineException) e;
            } else if (!(e instanceof RuntimeException)) {
                throw new MascotMachineException(e.toString());
            }
            throw (RuntimeException) e;
        } finally {
            if (doLeave) {
                cqLeave();
            }
        }
    }
}
