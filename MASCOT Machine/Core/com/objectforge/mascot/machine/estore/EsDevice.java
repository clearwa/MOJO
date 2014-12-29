/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.internal.IEIAccess;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.DeviceEntity;
import com.objectforge.mascot.machine.model.MascotEntities;


/**
 * An extension to EInstance to hold devices
 */
public class EsDevice extends EInstance {
    /**
     * @param entity
     */
    public EsDevice(MascotEntities entity) {
        super(entity);
    }

	/**
	 * @param name
	 * @param aClass
	 * @param entity
	 */
	public EsDevice(String name, Class aClass, MascotEntities entity) {
		super(name, aClass, entity);
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.internal.EntityInstance#getInstance()
	 */
	public Object getInstance() throws MascotMachineException {
		Device myDevice = new Device( ((DeviceEntity)parentEntity).getDevName(), ((DeviceEntity)parentEntity).getDevHandler());
		allocateInstance(myDevice);
		((IEIAccess)myDevice).setEInstance(this);
		return myDevice;
	}
	
}