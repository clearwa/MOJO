/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.model;

import java.util.Hashtable;
import java.util.Map;

import com.objectforge.mascot.machine.estore.EInstance;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.estore.InstanceRecord;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * MascotEntities is the base class for all Mascot entites.  Behaviour common to these objects is defined here.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */
public abstract class MascotEntities {
	//Constants
	public final static String CHANNEL_PREFIX = "channel$";
	public final static String POOL_PREFIX = "pool$";
	public final static String ACTIVITY_PREFIX = "root$";
	public final static String SUBSYSTEM_PREFIX = "subsys$";
	public final static String DEVICE_PREFIX = "device$";
	public final static String HANDLER_PREFIX = "handler$";
    public final static String SET_PREFIX = "SET$";
    public final static String IDADEV_PREFIX = "IDADev$";
	public final static String DEFAULT_PREFIX = "unknown$";

	// This entity's name
	private String name;
	// A prefix for the entity's name.  Used in gnerating a unique name
	private String prefix;
	//Unique name - if there is one.  This reamins for now - included when my ideas were ill formed and nay not
	//be needed.  Apples to prefix as well.
	protected String uniqueName;
	//Identify this entity's SET membership.
	private String membership;

	// The name of the class that implements this entity
	private String impClassName;
	// Hangover - probably due to be removed
	private Class impClass;
	//Each entity holds a vector of records
	Hashtable incarnations = new Hashtable();
    //Holds the reference to the current incarnation
	EntityInstance currentIncarnation;
	//The reaper checks this value to determin if this record is a candidate for reaping
	boolean reap = false;
	
	//Method that control the allocation of EntityInstances, ie. incarnation
	protected abstract EntityInstance addIncarnation() throws MascotMachineException;
    
    //Method to produce a reference to this entity
    public abstract IMascotReferences referenceFactory( String name, Map resources );
	
    //Deliver the specific entity instance associated with the instanced mascot entity
    public abstract EInstance eiInstance();
    
    /**
     * The default merge behavior is to replace tne entry value.  Override to merge entites with
     * structure.
     * 
     * @param entry
     * @param replace
     */
    public void merge( Map.Entry entry, boolean replace ) throws MascotMachineException {
        entry.setValue( this );
    }
    
	protected EntityInstance addIncarnation( EntityInstance es ){
		InstanceRecord ir = new InstanceRecord(es);
		
		//Add the new incarnation to the history
		incarnations.put(es,ir);
		currentIncarnation = es;	//Set it as the current incarnation
		return es;
	}
	
	public EntityInstance getCurrentIncarnation() throws MascotMachineException{
		//Do a lazy initialization if the current location is empty
		if(currentIncarnation==null){
			currentIncarnation = addIncarnation();
		}
		return currentIncarnation;
	}
	
	/**
	 * Method makeUniqueName.
	 * Create a unique name for an entity.
	 */
	public static String makeUniqueName(MascotEntities anEntity) {
		String basename = (anEntity instanceof MEInstanced )?((MEInstanced)anEntity).qualifiedName:anEntity.getName();

		return makeUniqueName(anEntity.getPrefix(), basename);
	}

	/**
	 * Method makeUniqueName.
	 * Make a uniique name from prefix and name.
	 */
	public static String makeUniqueName(String prefix, String name) {
		return prefix + name;
	}

	/**
	 * Method MascotEntities.
	 * 
	 * An entity with name and implementation class className
	 */
	public MascotEntities(String name, String className) {
		super();
		setName(name);
		setClassName(className);
	}
	
	//The default constructor
	public MascotEntities(){
	}

	/**
	 * Method setName.
	 * Set the entity's name.  Also initilaizes prefix and uniquename
	 */
	protected void setName(String aName) {
		name = aName;

		if (aName == null) {
			prefix = uniqueName = null;
			return;
		}

		if (this instanceof ChannelEntity)
			prefix = MascotEntities.CHANNEL_PREFIX;
		else if (this instanceof PoolEntity)
			prefix = MascotEntities.POOL_PREFIX;
		else if (this instanceof ActivityEntity)
			prefix = MascotEntities.ACTIVITY_PREFIX;
		else if (this instanceof SubsystemEntity)
			prefix = MascotEntities.SUBSYSTEM_PREFIX;
		else if (this instanceof DeviceEntity)
			prefix = MascotEntities.DEVICE_PREFIX;
        else if (this instanceof IDADeviceEntity)
            prefix = MascotEntities.IDADEV_PREFIX;
        else if (this instanceof SETEntity )
            prefix = MascotEntities.SET_PREFIX;
		else
			prefix = MascotEntities.DEFAULT_PREFIX;

		uniqueName = MascotEntities.makeUniqueName(this);
	}

	/**
	 * Method getName.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Method getUniqueName.
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * Method getPrefix.
	 */
	protected String getPrefix() {
		return prefix;
	}

	/**
	 * Method setClassName.
	 */
	protected void setClassName(String aString) {
		impClass = null;
		impClassName = aString;
	}

	/**
	 * Method getClassName.
	 */
	public String getClassName() {
		return impClassName;
	}

	/**
	 * Method getImplementationClass.
	 * Attempts to load this entity's implementation class.  If successful, returns the class; throws 
	 * MascotMachineException otherwise
	 * @throws MascotMachineException
	 * 
	 */
	public Object getImplementationClass() throws MascotMachineException {
		if (impClassName == null)
			throw new MascotMachineException("entityClassFactory: Classname is null");
		if (impClass == null) {
			try {
				impClass = Class.forName(impClassName);
			} catch (ClassNotFoundException cnf) {
				throw new MascotMachineException("entityClassFactory: Cannot find class: " + cnf);
			}
		}
		return impClass;
	}

	/**
	 * Returns the membership.
	 */
	public String getMembership() {
		return membership;
	}

	/**
	 * Sets the membership.
	 */
	public SETReference setMembership(String membership, Map table, EntityStore es) throws MascotMachineException {
		this.membership = membership;
        return ((SETEntity)es.getSetDescriptors().get(membership)).addMember( getName(), table, getTypeID() );
	}
    
    public SETReference setMembership( String membership, Map table ) throws MascotMachineException{
        return setMembership( membership, table, EntityStore.mascotRepository() );
    }
    
    /*
     * Entity type constants
     */
    public final static int ACTIVITY_TID = 0;
    public final static int CHANNEL_TID = 1;
    public final static int DEVICE_TID = 2;
    public final static int IDA_DEVICE_TID = 3;
    public final static int POOL_TID = 4;
    public final static int NO_TID = -1;
    public final static int SUBSYS_TID = 5;
    public final static int HANDLER_TID = 6;
    
    /*
     * Methods that identify this entity
     */
    public abstract String getType();
    public abstract int getTypeID();
    
	/**
	 * Method toString.
	 * Return a formatted string that represents this object.  Spaces are prependend to the returned steing.
	 * 
	 */
	public String toString(String spaces) {
		return spaces + "name = " + name + ", className = " + impClassName + ", membership = " + membership;
	}

	public String toString() {
		return toString("");
	}

	/**
	 * @return
	 */
	public boolean isReap() {
		return reap;
	}

	/**
	 */
	public void setReap() {
		reap = true;
	}

}
