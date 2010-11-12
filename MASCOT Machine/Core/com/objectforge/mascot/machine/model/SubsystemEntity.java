/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.model;

import java.util.Map;

import com.objectforge.mascot.machine.estore.EInstance;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EsSubsystem;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * A subsystem entity holds the information necessary to instantiate a subsystem.  See MascotEntities
 * Note that subsystem entities are a degeneate case that have no implementation class.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */

public class SubsystemEntity extends MascotEntities implements MEDealloc {
	protected String subName;
	protected Object subObject;
	protected boolean subCloseOnExit;
    
    public class SubsystemRef extends MascotReferences{
        public Boolean closeOnExit;

        /**
         * @param name
         * @param resources
         */
        public SubsystemRef(String name, String reference, Map resources) {
            super(name, reference, resources);
         }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getReference()
         */
        public MascotEntities getReference() throws MascotMachineException {
            return SubsystemEntity.this;
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getIncarnation()
         */
        public EntityInstance getIncarnation() throws MascotMachineException {
            return SubsystemEntity.this.getCurrentIncarnation();
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getInstance()
         */
        public Object getInstance() throws MascotMachineException {
            return getIncarnation().getInstance();
        }
        
        /* This is the way to produces a subsystem instance
         */
         public Subsystem getInstance( Subsystem container, String name ) throws MascotMachineException{
             return (Subsystem) ((EsSubsystem)getIncarnation()).getInstance(container,name);
         }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.MascotReferences#getReftype()
         */
        public int getReftype() {
            return SUBSYSTEM_REF;
        }
        
        /**
         * @return
         */
        public boolean isCloseOnExit() throws MascotMachineException {
            if( closeOnExit == null ){
                throw new MascotMachineException( "SubsystemRef<isCloseOnExit>: closeOnExit is null.");
            }
            return closeOnExit.booleanValue();
        }

        /**
         * @param b
         */
        public void setCloseOnExit(boolean b) {
            closeOnExit = new Boolean(b);
        }

    }

	/**
	 * Constructor SubsystemEntity.
	 */
	public SubsystemEntity(String name, Object object, boolean closeOnExit) {
		super(name, null);
		//Note the creation parameters on the way by
		subName = name;
		subObject = object;
		subCloseOnExit = closeOnExit;
	}

	public void doDeallocate(Object anInstance) {
		incarnations.remove(currentIncarnation);
		currentIncarnation = null;
		subObject = null;
	}

	public SubsystemEntity(String name, Object object) {
		this(name, object, false);
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.MascotEntities#addIncarnation()
	 */
	protected EntityInstance addIncarnation() throws MascotMachineException {
		return addIncarnation( eiInstance() );
	}

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#eiInstance()
     */
    public EInstance eiInstance() {
        return new EsSubsystem(subName, (Class) subObject, subCloseOnExit, this);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#referenceFactory(java.lang.String, java.util.Map)
     */
    public IMascotReferences referenceFactory(String name, Map resources) {
        return new SubsystemRef( name, this.getName(), resources);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getType()
     */
    public String getType() {
        return "subsystem";
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getTypeID()
     */
    public int getTypeID() {
        return SUBSYS_TID;
    }

}

