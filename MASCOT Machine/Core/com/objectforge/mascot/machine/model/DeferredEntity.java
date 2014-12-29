/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.model;

import java.util.Map;

import com.objectforge.mascot.machine.estore.EInstance;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * DeferredEntity
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 09-Apr-2004 by @author Clearwa
*/
public class DeferredEntity extends MascotEntities {

    /**
     * @param name
     * @param className
     */
    public DeferredEntity(String name, String className) {
        super(name, className);
    }

    /**
     * 
     */
    public DeferredEntity() {
        super();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#addIncarnation()
     */
    protected EntityInstance addIncarnation() throws MascotMachineException {
        return null;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#referenceFactory(java.lang.String, java.util.Map)
     */
    public IMascotReferences referenceFactory(String name, Map resources) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#eiInstance()
     */
    public EInstance eiInstance() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getType()
     */
    public String getType() {
        return "deferred";
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getTypeID()
     */
    public int getTypeID() {
        return -1;
    }

}
