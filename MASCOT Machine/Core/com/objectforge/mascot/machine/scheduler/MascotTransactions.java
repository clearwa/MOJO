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
 * MascotTransactions
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.scheduler
 * Created on 16-Apr-2004 by @author Clearwa
*/
public interface MascotTransactions {
    public Object transaction( IMascotTransaction runner, Object[] packer) throws MascotMachineException;
}
