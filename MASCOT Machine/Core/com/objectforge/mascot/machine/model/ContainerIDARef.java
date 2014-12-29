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
 * ContainerIDARef
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 31-Mar-2004 by @author Clearwa
*/
public class ContainerIDARef extends IDARef {

    /**
     * @param name
     * @param resources
     */
    public ContainerIDARef(String name, String reference, Map resources) {
        super(name, reference, resources);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IDARef#getScope()
     */
    String getScope() {
        return "container";
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getReference()
     */
    public MascotEntities getReference() throws MascotMachineException{
        // There is not reference
        throw new MascotMachineException("ContainerIDARef<getReference>: Containers have no reference");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getIncarnation()
     */
    public EntityInstance getIncarnation() throws MascotMachineException {
        throw new MascotMachineException("ContainerIDARef<getIncarnation>: Containers do not have incarnations");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getInstance()
     */
    public Object getInstance() throws MascotMachineException {
        throw new MascotMachineException("ContainerIDARef<getInstance>: Containers do not have instances");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotReferences#getReftype()
     */
    public int getReftype() {
        return CONTAINER_IDA_REF;
    }

}
