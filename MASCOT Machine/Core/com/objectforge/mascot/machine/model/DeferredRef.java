/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.model;

import java.util.Map;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.estore.EsSubsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * DeferredRef
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 09-Apr-2004 by @author Clearwa
*/
public class DeferredRef extends MascotReferences {
    int refID;
    Object payload;
    boolean flag;

    /**
     * @param name
     * @param reference
     * @param resources
     */
    public DeferredRef(String name, String reference, Map resources, int refID, Object payload) {
        super(name, reference, resources);
        this.refID = refID;
        this.payload = payload;
    }

    /**
     * 
     */
    public DeferredRef() {
        super();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getReftype()
     */
    public int getReftype() {
        return refID;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getReference()
     */
    public MascotEntities getReference() throws MascotMachineException {
        return new DeferredEntity();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getIncarnation()
     */
    public EntityInstance getIncarnation() throws MascotMachineException {
        throw new MascotMachineException("DeferredRef<getIncarnation>: incarnation not defined for this ref type.");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getInstance()
     */
    public Object getInstance() throws MascotMachineException {
        throw new MascotMachineException("DeferredRef<getIncarnation>: Use reslove to produce an instance.");
    }

    public Object resolve(EsSubsystem sub, EntityStore estore) throws MascotMachineException {
        Object retval;
        switch (refID) {
            case IMascotReferences.ACTIVITY_REF :
                retval = sub.addActivityRef(estore, name, reference, resources);
                break;
            case IMascotReferences.LOCAL_IDA_REF :
                retval = sub.localIDARef(estore, name, reference);
                break;
            case IMascotReferences.DEVICE_IDA_REF :
                //Silently add a reference to the global device pool
                //First find the refernce device
                IDAEntities targetDevice = (IDAEntities) estore.getIdaDescriptors().get(reference);
                if( targetDevice == null){
                    //The reference could be in the repository
                    if( (targetDevice = (IDAEntities) EntityStore.mascotRepository().getDeviceDescriptors().get(reference))==null ){
                        throw new MascotMachineException( "DeferredRef<resolve>: Cannot resolve reference device " + reference );
                    }
                }
                String targetName = Device.makePoolName( targetDevice.getClassName() );
                sub.globalIDARef( targetName, targetName);
                retval = sub.deviceIDARef(estore, name, reference);
                break;
            case IMascotReferences.SUBSYSTEM_REF :
                retval = sub.addSubsystemRef(estore, name, reference, resources);
                ((SubsystemEntity.SubsystemRef)retval).setCloseOnExit( flag );
                break;
            default :
                throw new MascotMachineException(
                    "DeferredRef<resolve>: unresolvable reference type " + refID);
        }
        return retval;
    }

    /**
     * @return
     */
    public boolean isFlag() {
        return flag;
    }

    /**
     * @param b
     */
    public void setFlag(boolean b) {
        flag = b;
    }

}
