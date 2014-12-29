/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.model;

import java.util.Map;

import com.objectforge.mascot.machine.estore.EInstance;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EsIDA;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;



/**
 * IDAEntities
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 29-Mar-2004 by @author Clearwa
*/
/**
 * An IDA entity encompasses both channels and pools and adds fields specific to these entities.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */

public abstract class IDAEntities extends MEInstanced{
	protected String type;
    
    class LocalIDARef extends IDARef {

        /**
         * @param name
         * @param resources
         */
        public LocalIDARef(String name, String reference, Map resources) {
            super(name, reference, resources);
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getReference()
         */
        public MascotEntities getReference() throws MascotMachineException {
            return IDAEntities.this;
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getIncarnation()
         */
        public EntityInstance getIncarnation() throws MascotMachineException {
            return IDAEntities.this.getCurrentIncarnation();
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getInstance()
         */
        public Object getInstance() throws MascotMachineException {
            return getIncarnation().getInstance();
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IDARef#getScope()
         */
        String getScope() {
            return "local";
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.MascotReferences#getReftype()
         */
        public int getReftype() {
            return LOCAL_IDA_REF;
        }
    }
	
	/**
	 */
	public IDAEntities(String name, Object root, String factoryMethod, String type) {
		super(name, root, factoryMethod);
        this.type = type;        
	}
	
	/**
	 * Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 */
	public void setType(String type) {
		this.type = type;
	}

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#eiInstance()
     */
    public EInstance eiInstance() {
        return new EsIDA(this);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#referenceFactory(java.lang.String, java.util.Map)
     */
    public IMascotReferences referenceFactory(String name, Map resources) {
        return new LocalIDARef( name, this.getName(), resources );
    }

}
