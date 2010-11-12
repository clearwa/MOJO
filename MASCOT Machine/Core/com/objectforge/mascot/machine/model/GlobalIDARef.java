/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.model;

import java.util.Map;

import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * GlobalIDARef
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 31-Mar-2004 by @author Clearwa
*/
public class GlobalIDARef extends IDARef {

    /**
     * @param name
     * @param resources
     */
    public GlobalIDARef(String name, String reference, Map resources) {
        super(name, reference, resources);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IDARef#getScope()
     */
    String getScope() {
        return "global";
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getReference()
     */
    public MascotEntities getReference() throws MascotMachineException{
        // There is not reference
        throw new MascotMachineException("GlobalIDARef<getReference>: Global has no reference");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getIncarnation()
     */
    public EntityInstance getIncarnation() throws MascotMachineException {
        throw new MascotMachineException("GlobalIDARef<getIncarnation>: Global does not have incarnations");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getInstance()
     */
    public Object getInstance() throws MascotMachineException {
        throw new MascotMachineException("GlobalIDARef<getInstance>: Global does not have instances");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotReferences#getReftype()
     */
    public int getReftype() {
        return GLOBAL_IDA_REF;
    }

}
