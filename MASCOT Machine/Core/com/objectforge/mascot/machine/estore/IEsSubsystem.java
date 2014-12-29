/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;

import java.util.Map;

import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.MascotReferences;

/**
 * IEsSubsystem
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.estore
 * Created on 02-Apr-2004 by @author Clearwa
*/
public interface IEsSubsystem {
    public abstract MascotReferences putReference(MascotReferences reference);
    /**
     * Get the subsystem roots
     * 
     * @return
     */
    public abstract Map getRoots();
    /**
     * Method toString.
     * Produce a formatted string that represents the  contents of an EsSubsystem object.  The indent is prepended 
     * to each output line.
     * 
     */
    public abstract String toString(final String indent);
    public abstract String toString();
    public abstract Subsystem getInstance(Subsystem container, String name);
    public abstract void addInstance(InstanceRecord ir);
    public abstract void removeInstance(Object instance);
    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.internal.EntityInstance#getInstance()
     */
    public abstract Object getInstance() throws MascotMachineException;
    public interface IInstallRecord{
        public boolean isInstalled();
        public MascotReferences getReference();
        public void setInstalled(boolean b);
    }
}