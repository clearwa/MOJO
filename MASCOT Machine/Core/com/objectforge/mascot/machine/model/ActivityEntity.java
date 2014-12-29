/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.model;

import java.util.Map;

import com.objectforge.mascot.machine.estore.EInstance;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EsActivity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * An activity entity holds the information necessary to instantiate an activity.  See MascotEntities
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class ActivityEntity extends MEInstanced {
    public class ActivityRef extends MascotReferences {

        /**
         * @param name
         * @param resources
         */
        public ActivityRef(String name, String reference, Map resources) {
            super(name, reference, resources);
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getReference()
         */
        public MascotEntities getReference() throws MascotMachineException {
            return ActivityEntity.this;
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.model.IMascotReferences#getIncarnation()
         */
        public EntityInstance getIncarnation() throws MascotMachineException {
            return ActivityEntity.this.getCurrentIncarnation();
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
            return ACTIVITY_REF;
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#referenceFactory()
     */
    public IMascotReferences referenceFactory( String name, Map resources) {
        return new ActivityRef( name, this.getName(), resources);
    }
	/**
	 */
	public ActivityEntity(String name, Object root, String factoryMethod) {
		super(name, root, factoryMethod);
	}

	/**
	 * @param name
	 * @param root
	 * @param factoryMethod
	 * @param qualifiedName
	 */
	public ActivityEntity(String name, Object root, String factoryMethod, String qualifiedName) {
		super(name, root, factoryMethod );
        this.qualifiedName = qualifiedName;
	}

	public void doDeallocate(Object anInstance) {
		if (mustDeallocate && currentIncarnation != null) {
			//Special measures are needed here.  If it is the case that the enclosing subsystem is dying then
			//it is save to process the following code.  If not then don't do this
			if (anInstance instanceof IRoot) {
				Subsystem mySub = ((IRoot) anInstance).getSubsystem(); //cast for ease of use

				if (mySub==null ) {
					incarnations.remove(currentIncarnation);
					currentIncarnation = null;
					factoryInstance = null;
				}
			}
		}
	}

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MEInstanced#eiInstance(com.objectforge.mascot.machine.model.MEInstanced)
     */
    public EInstance eiInstance() {
        return new EsActivity(this);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getType()
     */
    public String getType() {
        return "activity";
    }
    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getTypeID()
     */
    public int getTypeID() {
        return ACTIVITY_TID;
    }

}
