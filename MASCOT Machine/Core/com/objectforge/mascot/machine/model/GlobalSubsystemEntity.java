/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.model;

import java.util.Map;
import java.util.Map.Entry;

import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EsGlobalSubsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/*
* Project: MASCOT Machine
* Package: com.objectforge.mascot.machine.model
* Created on 29-May-2003
*/
public class GlobalSubsystemEntity extends SubsystemEntity {

    /**
     * @param name
     * @param object
     */
    public GlobalSubsystemEntity(String name, Object object) {
        super(name, object);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#addIncarnation()
     */
    protected EntityInstance addIncarnation() throws MascotMachineException {
        return addIncarnation(new EsGlobalSubsystem(subName, (Class) subObject, subCloseOnExit));
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#merge(com.objectforge.mascot.machine.model.MascotEntities, boolean)
     */
    public MascotEntities merge(MascotEntities source, boolean replace) throws MascotMachineException {
        return this;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#referenceFactory(java.lang.String, java.util.Map)
     */
    public IMascotReferences referenceFactory(String name, Map resources) {
        return super.referenceFactory(name, resources);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#merge(java.util.Map.Entry, boolean)
     */
    public void merge(Entry entry, boolean replace) throws MascotMachineException {
        ((EsGlobalSubsystem) getCurrentIncarnation())
            .merge((EsGlobalSubsystem) ((GlobalSubsystemEntity) entry.getValue())
            .getCurrentIncarnation(),
            replace);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getType()
     */
    public String getType() {
        return "globalSubsystem";
    }

}
