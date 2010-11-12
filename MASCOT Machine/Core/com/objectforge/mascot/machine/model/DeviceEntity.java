/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.model;

import java.util.Map;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.estore.EInstance;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EsDevice;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * A device entity holds the information necessary to instantiate a device.  See MascotEntities
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */

public class DeviceEntity extends MascotEntities {
	/**
	 */
	private String devName;
	private String devHandler;
    private Device deviceInstance;
    
    public class DeviceRef extends MascotReferences {

        /**
         * @param name
         * @param resources
         */
        public DeviceRef(String name, String reference, Map resources) {
            super(name, reference, resources);
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getReference()
         */
        public MascotEntities getReference() throws MascotMachineException {
            return DeviceEntity.this;
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getIncarnation()
         */
        public EntityInstance getIncarnation() throws MascotMachineException {
            return DeviceEntity.this.getCurrentIncarnation();
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getInstance()
         */
        public Object getInstance() throws MascotMachineException {
            if( DeviceEntity.this.deviceInstance == null ){
                DeviceEntity.this.deviceInstance = (Device) getIncarnation().getInstance();
            }
            return DeviceEntity.this.deviceInstance;
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.MascotReferences#getReftype()
         */
        public int getReftype() {
            return DEVICE_REF;
        }
    }

	/**
	 * @return
	 */
	public String getDevHandler() {
		return devHandler;
	}

	/**
	 * @return
	 */
	public String getDevName() {
		return devName;
	}

	public DeviceEntity(String name, String handler) {
		super(name, handler);
		devName = name;
		devHandler = handler;
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.MascotEntities#addIncarnation()
	 */
	protected EntityInstance addIncarnation() throws MascotMachineException {
		return (EntityInstance) new EsDevice(devName, null, this);
	}

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#referenceFactory(java.lang.String, java.util.Map)
     */
    public IMascotReferences referenceFactory(String name, Map resources) {
        return new DeviceRef( name, this.getName(), resources );
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#eiInstance()
     */
    public EInstance eiInstance() {
        return new EsDevice(this);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getType()
     */
    public String getType() {
        return "device";
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getTypeID()
     */
    public int getTypeID() {
        return DEVICE_TID;
    }

}
