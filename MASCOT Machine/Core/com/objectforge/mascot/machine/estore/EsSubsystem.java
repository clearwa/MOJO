/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.objectforge.mascot.machine.internal.IEIAccess;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.ActivityEntity;
import com.objectforge.mascot.machine.model.ArgumentIDARef;
import com.objectforge.mascot.machine.model.ContainerIDARef;
import com.objectforge.mascot.machine.model.DeferredRef;
import com.objectforge.mascot.machine.model.GlobalIDARef;
import com.objectforge.mascot.machine.model.IDAEntities;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.machine.model.MascotReferences;
import com.objectforge.mascot.machine.model.SETEntity;
import com.objectforge.mascot.machine.model.SubsystemEntity;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 *
 * Subsystems require more structure; this object encapsulates that information.  The hashtables here
 * hold MascotEntities which provide fine detail for each element.
 */
public class EsSubsystem extends EInstance implements IESRoots, IEsSubsystem {
    //Refernce entries for this subsystem.  These tables hold InstallRecord objects
//    public Map channels = new Hashtable();
//    public Map pools = new Hashtable();
    public Map idas = new Hashtable();
    public Map roots = new Hashtable();
    public Map subsystems = new Hashtable();
    public Map deviceref = new Hashtable();
    public boolean closeOnEmpty;

    /*
     * The global subsystem is a singleton - it will not be and cannot be instanced more 
     * than once - and so it supports incremental install behaviour.  In this case the the subsystem
     * maps contain information about whether the references in the subsystem have been instanced.  At
     * present, normal subsystems ignore the install information
    */
    class InstallRecord implements IInstallRecord {
        MascotReferences reference; //The reference object
        boolean installed = false;

        public InstallRecord(MascotReferences reference) {
            this.reference = reference;
        }

        /**
         * @return
         */
        public boolean isInstalled() {
            return installed;
        }

        /**
         * @return
         */
        public MascotReferences getReference() {
            return reference;
        }

        /**
         * @param b
         */
        public void setInstalled(boolean b) {
            installed = b;
        }

    }

    public MascotReferences putReference(MascotReferences reference) {
        //Sort out what to put where
        gate.cqJoin();
        try {
            String refname = reference.getName();
            InstallRecord record = new InstallRecord(reference);

            switch (reference.getReftype()) {
                case IMascotReferences.ACTIVITY_REF :
                    roots.put(refname, record);
                    break;

                case IMascotReferences.ARGUMENT_IDA_REF :
                case IMascotReferences.CONTAINER_IDA_REF :
                case IMascotReferences.GLOBAL_IDA_REF :
                    idas.put(refname, record);
                    break;

                case IMascotReferences.DEVICE_IDA_REF :
                    deviceref.put(refname, record);
                    break;

                case IMascotReferences.LOCAL_IDA_REF :
                    idas.put(refname, record);
                    break;

                case IMascotReferences.SUBSYSTEM_REF :
                    subsystems.put(refname, record);
                    break;

                default :
                    throw new MascotRuntimeException(
                        "EsSubsystem<putReference>: Unknown reference type " + reference.getReftype());
            }
            return reference;
        } finally {
            gate.cqLeave();
        }
    }

    /**
     * Get the subsystem roots
     * 
     * @return
     */
    public Map getRoots() {
        return roots;
    }

    public EsSubsystem(String name, Class aClass, boolean closeOnEmpty, SubsystemEntity parentEntity) {
        super(name, aClass, parentEntity);
        this.closeOnEmpty = closeOnEmpty;
    }

    /**
     * Method toString.
     * Produce a formatted string that represents the  contents of an EsSubsystem object.  The indent is prepended 
     * to each output line.
     * 
     */
    //Print the contents of a subsystem
    public String toString(final String indent) {
        final MascotPrinter printer = new MascotPrinter();
        PrintWriter out = printer.out;

        out.println(super.toString(indent));

        Object[][] contents = { { "Channels & Pools", idas },  {
                "Device References", deviceref }, {
                "Activities", roots }, {
                "Subsystems", subsystems }
        };

        for (int i = 0; i < contents.length; i++) {
            printer.referencePrint(indent, (String) contents[i][0], (Hashtable) contents[i][1]);
        }
        return printer.toString();
    }

    public String toString() {
        return toString("");
    }

    public Subsystem getInstance(Subsystem container, String name) {
        Subsystem newsub = null;
        try {
            newsub = new Subsystem(container, name);
        } catch (MascotMachineException e) {
            MascotDebug.println(9, "EsSubsystem.getInstance: subsystem allocation - " + e);
        }
        allocateInstance(newsub);
        ((IEIAccess) newsub).setEInstance(this);
        return newsub;
    }

    public void addInstance(InstanceRecord ir) {
        instances.put(ir.getAnInstance(), ir);
    }

    public void removeInstance(Object instance) {
        instances.remove(instance);
    }
    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.internal.EntityInstance#getInstance()
     */
    public Object getInstance() throws MascotMachineException {
        throw new MascotMachineException("EsSubsystem.getInstace: Subsystem instances cannot be obtined this way");
    }

    /**
     * These methods deal with entity references
     * EsSubsystem
     * 
     */

    /**
     * Check that the referenced entity really exists
     * 
     * @param reference
     * @param table
     * @param message
     * @throws MascotMachineException
     */
    public static MascotEntities checkReference(String reference, Map[] maps, String message)
        throws MascotMachineException {
        for (int i = 0; i < maps.length; i++) {
            if (maps[i].containsKey(reference)) {
                return (MascotEntities) maps[i].get(reference);
            }
        }
        throw new MascotMachineException(message);
    }

    /**
     * Produce a reference to an acitivity and put it the the passed subsystem's context
     * 
     * @param desc
     * @param name
     * @param reference
     * @param args
     * @throws MascotMachineException
     */
    public MascotReferences addActivityRef(EntityStore estore, String name, String reference, Map resources)
        throws MascotMachineException {
        //Look up the reference.  Check in the passed entity store and the repository
        ActivityEntity activity =
            (ActivityEntity) checkReference(reference,
                new Map[] {
                    estore.getActivityDescriptors(),
                    EntityStore.mascotRepository().getActivityDescriptors()},
                "EsSubsystem<addActivityRef>: Activity entity does not exist - " + reference);
        return putReference((MascotReferences) activity.referenceFactory(name, resources));
    }

    /**
     * Method addSubsystem reference.
     * Add an enclosed name to subsystem as a contained subsystem.  Throws MascotMachineException if subsystem
     * does not have an entry.  This is a degenerate entity - there is no implementation.
     * 
     * @param eiSub
     * @param string
     * @param string2
     */
    public MascotReferences addSubsystemRef(EntityStore estore, String name, String reference, Map resources)
        throws MascotMachineException {
        SubsystemEntity desc = null;
        try {
            desc = EntityStore.checkSubsystem(estore, reference, getParentEntity().getName());
        } catch (MascotMachineException e) {
            // Tell the user that the reference does not exist
            MascotDebug.println(
                0,
                "Warning: adding a currently undefined subsystem \""
                    + reference
                    + "\" to the contianer \""
                    + getParentEntity().getName()
                    + "\"");
            MascotDebug.println(
                0,
                "\tConsider reorganizing the SETs document if possible - the reference has not been added");
            throw new MascotMachineException(
                "EntityStore<addSubsystemRef>: Subsystem reference to " + reference + " not added");
        }
        return putReference((MascotReferences) desc.referenceFactory(name, resources));
    }

    /**
     * Check that an IDA reference name is not a valid integer.  If this is the case, then throw
     * an exception.  Integer names are reserved for argument IDAs
     * 
     * @param name
     * @param tag
     * @throws MascotMachineException
     */
    /*
     * Add the various types of IDA references
    */

    protected void checkForInt(String name, String tag) throws MascotMachineException {
        try {
            new Integer(name);
        } catch (NumberFormatException e) {
            return;
        }
        throw new MascotMachineException(tag + ": Integer names are reserved for argument IDAs.");
    }

    /**
     * Method localIDARef.
     * Create a local reference.  In this case, the instance is local to the subsystem and so an
     * instance must be produced
     * 
     * @param desc
     * @param name
     * @param reference
     * @param scope
     * @throws MascotMachineException
     */
    public synchronized MascotReferences localIDARef(EntityStore estore, String name, String reference)
        throws MascotMachineException {
        IDAEntities ida =
            (IDAEntities) checkReference(reference,
                new Map[] { estore.getIdaDescriptors(), EntityStore.mascotRepository().getIdaDescriptors()},
                "EntityStore<localIDARef>: IDA entity does not exist - " + reference);
        checkForInt(name, "EntityStore<localIDARef>");
        return putReference((MascotReferences) ida.referenceFactory(name, new Hashtable()));
    }

    /**
     * Create a container IDA reference
     * 
     * @param desc
     * @param name
     * @param reference
     */
    public synchronized MascotReferences containerIDARef(String name, String reference)
        throws MascotMachineException {
        checkForInt(name, "EntityStore<containerIDARef>");
        ContainerIDARef cref = new ContainerIDARef(name, reference, new Hashtable());
        return putReference(cref);
    }

    /**
     * Create a global IDA reference
     * 
     * @param desc
     * @param name
     * @param reference
     */
    public synchronized MascotReferences globalIDARef(String name, String reference)
        throws MascotMachineException {
        checkForInt(name, "EntityStore<globalIDARef>");
        GlobalIDARef cref = new GlobalIDARef(name, reference, new Hashtable());
        return putReference(cref);
    }

    /**
     * Create a device IDA reference
     * @param desc
     * @param name
     * @param reference
     */
    public synchronized MascotReferences deviceIDARef(EntityStore estore, String name, String reference)
        throws MascotMachineException {
        IDAEntities ida =
            (IDAEntities) checkReference(reference,
                new Map[] { estore.getIdaDescriptors(), EntityStore.mascotRepository().getIdaDescriptors()},
                "EntityStore<deviceIDARef>: Device IDA entity does not exist - " + reference);
        checkForInt(name, "EntityStore<deviceIDARef>");
        return putReference((MascotReferences) ida.referenceFactory(name, new Hashtable()));
    }

    /**
     * Create an argument IDA reference
     * @param desc
     * @param name
     * @param reference
     */
    public synchronized MascotReferences argumentIDARef(String name, String reference)
        throws MascotMachineException {
        checkForInt(name, "EntityStore<argumentIDARef>");
        //Check that the reference is a valid integer
        Integer index = null;
        try {
            index = new Integer(reference);
        } catch (NumberFormatException e) {
        }
        if (index == null || index.intValue() < 0) {
            throw new MascotMachineException(
                "EntityStore<arguementIDARef>: Arg index '" + reference + "' is invalid or < 0.");
        }
        ArgumentIDARef aref = new ArgumentIDARef(name, reference, new Hashtable());
        return putReference(aref);
    }

    /**
     * Fill in any deferred references
     * 
     * @param estore
     */
    public void resolveDeferred(final EntityStore estore) throws MascotMachineException {
        class doResolve {
            public void doIt(Map toResolve) throws MascotMachineException {
                for (Iterator i = toResolve.values().iterator(); i.hasNext();) {
                    InstallRecord ref = (InstallRecord) i.next();
                    if (ref.getReference() instanceof DeferredRef) {
                        ((DeferredRef) ref.getReference()).resolve(EsSubsystem.this, estore);
                    }
                }
            }
        }
        gate.cqJoin();
        try {
            doResolve resolver = new doResolve();
            resolver.doIt(roots);
            resolver.doIt(idas);
            resolver.doIt(subsystems);
            resolver.doIt(deviceref);
        } finally {
            gate.cqLeave();
        }
    }

    public Object getResource(Object key) {
        SubsystemEntity esub = (SubsystemEntity) getParentEntity();
        return (
            (SETEntity) EntityStore.mascotRepository().getSetDescriptors().get(
                esub.getMembership())).getResource(
            key);
    }

}