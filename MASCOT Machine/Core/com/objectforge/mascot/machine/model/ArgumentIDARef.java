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
 * ArgumentIDARef
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 02-Apr-2004 by @author Clearwa
*/
public class ArgumentIDARef extends IDARef {

    /**
     * @param name
     * @param resources
     */
    public ArgumentIDARef(String name, String reference, Map resources) {
        super(name, reference, resources);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IDARef#getScope()
     */
    String getScope() {
        return "argument";
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getReference()
     */
    public MascotEntities getReference() throws MascotMachineException{
        // There is not reference
        throw new MascotMachineException("ArgumentIDARef<getReference>: Arguments have no reference");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getIncarnation()
     */
    public EntityInstance getIncarnation() throws MascotMachineException {
        throw new MascotMachineException("ArgumentIDARef<getIncarnation>: Arguments do not have incarnations");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getInstance()
     */
    public Object getInstance() throws MascotMachineException {
        throw new MascotMachineException("ArgumentIDARef<getInstance>: Arguments do not have instances");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotReferences#getReftype()
     */
    public int getReftype() {
        return ARGUMENT_IDA_REF;
    }

}
