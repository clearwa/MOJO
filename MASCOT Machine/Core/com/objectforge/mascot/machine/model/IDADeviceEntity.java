/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


/*
 * Created on 28-Feb-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.objectforge.mascot.machine.model;

import java.util.Map;

import com.objectforge.mascot.machine.estore.EInstance;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EsDeviceIDARef;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public class IDADeviceEntity extends IDAEntities {
	private String rdevName;
	private String rdevRoot;
	class DeviceIDARef extends IDARef {

        /**
         * @param name
         * @param resources
         */
        public DeviceIDARef(String name, String reference, Map resources) {
            super(name, reference, resources);
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IDARef#getScope()
         */
        String getScope() {
            return "device";
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getReference()
         */
        public MascotEntities getReference() throws MascotMachineException{
            return IDADeviceEntity.this;
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getIncarnation()
         */
        public EntityInstance getIncarnation() throws MascotMachineException {
            return getReference().getCurrentIncarnation();
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getInstance()
         */
        public Object getInstance() throws MascotMachineException {
            return getIncarnation().getInstance();
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.MascotReferences#getReftype()
         */
        public int getReftype() {
            return DEVICE_IDA_REF;
        }
    }

	/**
	 * @param name
	 * @param root
	 * @param factoryMethod
	 * @param qualifiedName
	 */
	public IDADeviceEntity(String name, Object root, String factoryMethod, String type) {
		super(name, root, factoryMethod, type);
		rdevName = name;
		rdevRoot = (String)root;
	}

	/**
	 * @return
	 */
	public String getRdevName() {
		return rdevName;
	}

	/**
	 * @return
	 */
	public String getRdevRoot() {
		return rdevRoot;
	}

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#referenceFactory(java.lang.String, java.util.Map)
     */
    public IMascotReferences referenceFactory(String name, Map resources) {
        return new DeviceIDARef( name, this.getName(), resources);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#eiInstance()
     */
    public EInstance eiInstance() {
        return new EsDeviceIDARef(rdevName,null,this);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getType()
     * 
     * Don't change the type directly.  Override this method to give a distinctive string
     * representation
     */
    public String getType() {
        return "deviceRefIDA";
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getTypeID()
     */
    public int getTypeID() {
        return IDA_DEVICE_TID;
    }

}
