/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;

import com.objectforge.mascot.machine.idas.DeviceRefIDA;
import com.objectforge.mascot.machine.internal.IEIAccess;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IDADeviceEntity;
import com.objectforge.mascot.machine.model.MascotEntities;

/*
* Project: MASCOT Machine
* Package: com.objectforge.mascot.machine.internal
* Created on 31-May-2003
*/
public class EsDeviceIDARef extends EsIDA {

    /**
     * @param name
     * @param aClass
     * @param entity
     */
    public EsDeviceIDARef(String name, Class aClass, MascotEntities entity) {
        super(name, aClass, entity);
    }

    /**
     * @param entity
     */
    public EsDeviceIDARef(MascotEntities entity) {
        super(entity);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.internal.EntityInstance#getInstance()
     */
    public Object getInstance() throws MascotMachineException {
        DeviceRefIDA myRef =
            new DeviceRefIDA(
                ((IDADeviceEntity) parentEntity).getRdevName(),
                ((IDADeviceEntity) parentEntity).getRdevRoot());
        allocateInstance(myRef);
        ((IEIAccess) myRef).setEInstance(this);
        return myRef;
    }

}
