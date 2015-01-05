/** 
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/
package com.objectforge.mascot.machine.estore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.Vector;
import java.util.Map.Entry;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.device.DevicePool;
import com.objectforge.mascot.machine.internal.GlobalSubsystem;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.ActivityEntity;
import com.objectforge.mascot.machine.model.ChannelEntity;
import com.objectforge.mascot.machine.model.DeviceEntity;
import com.objectforge.mascot.machine.model.GlobalSubsystemEntity;
import com.objectforge.mascot.machine.model.HandlerEntity;
import com.objectforge.mascot.machine.model.IDADeviceEntity;
import com.objectforge.mascot.machine.model.IDAEntities;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.machine.model.PoolEntity;
import com.objectforge.mascot.machine.model.SETEntity;
import com.objectforge.mascot.machine.model.SETReference;
import com.objectforge.mascot.machine.model.SubsystemEntity;
import com.objectforge.mascot.machine.model.SubsystemEntity.SubsystemRef;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.machine.scheduler.IMascotTransaction;
import com.objectforge.mascot.machine.scheduler.MascotTransactionQueue;
import com.objectforge.mascot.machine.scheduler.MascotTransactions;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * An EntityStore is the central repository for all of a Mascot Machine's structural information. 
 * The class holds a single instance; this represents the validated state of the respository. 
 * Users cannot directly access this insatnce.  It is created in a primordial state when the class 
 * is loaded and modified by merging instances (produced by calls to entityStoreFactory()) into 
 * the root store.  There are also methods to remove keys from the store generally, ie. currently 
 * across all of the primary Hashtables
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */
public class EntityStore {
    private static final String Header[] =
        {
            "MOJO: A Java implementation of the MASCOT 2015 Machine",
            "Copyright - The Object Forge, 2002,2003,2010,2015 (www.object-forge.com)",
            "",
            "MOJO is licensed material and is the property of the Object Forge,",
            "Malvern, UK; it is not freeware or shareware and is not subject to any",
            "open source licenses except where noted.  It is supplied for evaluation",
            "and educational purposes only; production installations require a license",
            "from the Object Forge.",
            "",
            "MOJO comes with the usual health warnings and the Object Forge offers no warrantee",
            "either expicit or implied.  Users run MOJO at thier own risk; Object Forge",
            "accepts no responsibility for any damage, loss, or inconvenience arising either",
            "directly or indirectly from the use of this software.  Please contact Object Forge",
            "with comments and licensing queries at aupport@object-forge.com or visit our site at",
            "www.object-forge.com." };

    public static PropertyResourceBundle MascotBundle;

    public static final String Revision() {
        String rev = "\nRevision: ";

        if (MascotBundle != null) {
            try {
                rev += MascotBundle.getString("mascot.machine.release");
            } catch (MissingResourceException e) {
            }
        }
        return rev;
    }

    public static final String Banner() {
        String ret = "";

        for (int i = 0; i < Header.length; i++) {
            ret += Header[i] + "\n";
        }
        ret += Revision() + "\n";
        return ret;
    }

    //This table holds the entity information that describes a subsystem.  Indexed by
    //subsystem name - implies names must be unique across all loaded SETS files
    protected final DescriptorMap subsystemDescriptors = new DescriptorMap();

    //Descriptor table for devices - indexed by handler name
    protected final DescriptorMap handlerDescriptors = new DescriptorMap();

    //Descriptor table for activities
    protected final DescriptorMap activityDescriptors = new DescriptorMap();

    //Descriptor table for idas
    protected final DescriptorMap idaDescriptors = new DescriptorMap();

    //Descriptor table for devices
    protected final DescriptorMap deviceDescriptors = new DescriptorMap();

    //Descriptor table for loaded SETs
    protected final SETMap setDescriptors = new SETMap();

    //An instance of nullTableEntity.  Used to mark unfilled values in tables since
    //null is no allowed
    public static NullTableEntry nullEntry;

    //The instance of the system entity store
    private static EntityStore mascotRepository;

    //The global subsystem
    private static GlobalSubsystem globalSub;

    static {
        init(new Object());
    }

    //Access control for the class
    protected static MascotTransactionQueue eGate;
    //Access control for an instance
    protected MascotTransactionQueue gate = new MascotTransactionQueue();

    //Maps with a doMerge behavior
    public class DescriptorMap extends Hashtable implements MascotTransactions {
        /**
         * Serialized version
         */
        private static final long serialVersionUID = 1L;
        MascotTransactionQueue gate = new MascotTransactionQueue();

        /**
         * Method doMerge.
         * 
         * Internal merge method.  It is sensitive to instances of EsSubsystem and will invoke the merge method on
         * those objects when it finds them.
         */
        public void doMerge(final Map source, final boolean replace) throws MascotMachineException {
            class runner implements IMascotTransaction {

                /* (non-Javadoc)
                 * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
                 */
                public Object kernel(Object[] packet) throws MascotMachineException {
                    if (source.isEmpty()) {
                        return null; //If there is nothing to nothing
                    }
                    for (Iterator i = DescriptorMap.this.entrySet().iterator(); i.hasNext();) {
                        Map.Entry entry = (Entry) i.next();
                        //If the source contains the key than pass the merge request to
                        //the entity
                        if (source.containsKey(entry.getKey())) {
                            ((MascotEntities) source.get(entry.getKey())).merge(entry, replace);
                            source.remove(entry.getKey()); //Remove the source record
                        }
                    }
                    //All the common records have been removed, simply add the rest
                    DescriptorMap.this.putAll(source);
                    return null;
                }
            }
            transaction(new runner(), null);
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.scheduler.MascotTransactions#transaction(com.objectforge.mascot.machine.scheduler.IMascotTransaction, java.lang.Object[])
         */
        public Object transaction(IMascotTransaction runner, Object[] packet) throws MascotMachineException {
            return gate.doIt(runner, packet);
        }
    }

    /**
     * 
     * SETMap
     * 
     * Project: MASCOT Machine
     * Package: com.objectforge.mascot.machine.mascotRepository
     * Created on 06-Apr-2004 by @author Clearwa
     * 
     * This class overrides the DescriptorMap doMerge behavior.  In essence, it reverse the sense of
     * this opertation so the SET entity's merge is called when an entry does not exist in the 
     * repository.
     */
    public class SETMap extends DescriptorMap {

        /**
         * Serialized version
         */
        private static final long serialVersionUID = 1L;

        /* (non-Javadoc)
        * @see com.objectforge.mascot.machine.mascotRepository.EntityStore.DescriptorMap#doMerge(java.util.Map, boolean)
        */
        public void doMerge(final Map source, final boolean replace) throws MascotMachineException {
            class runner implements IMascotTransaction {

                /* (non-Javadoc)
                 * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
                 */
                public Object kernel(Object[] packet) throws MascotMachineException {
                    if (source.isEmpty()) {
                        return null; //If there is nothing do nothing
                    }
                    //For all of the SETs in the source
                    for (Iterator i = source.entrySet().iterator(); i.hasNext();) {
                        //Get a SET entry from the source context
                        Map.Entry aSet = (Entry) i.next();
                        //newSET contains either a new SET entity if it does not exist already or
                        //the current SET entity if it does
                        SETEntity newSET =
                            (SETEntity) ((setDescriptors.containsKey(aSet.getKey()))
                                ? setDescriptors.get(aSet.getKey())
                                : (mascotRepository().createSET((String) aSet.getKey())).getParentEntity());
                        List newContents = newSET.getSetContents();
                        //For the contents of aSET (a SET entry in the source) see if I need to add any of the source entries
                        //to the current contents of newSET
                        for (Iterator j = ((SETEntity) aSet.getValue()).getSetContents().iterator();
                            j.hasNext();
                            ) {
                            SETReference sr = (SETReference) j.next();
                            //Check whether the destination already has this entry
                            if (!newContents.contains(sr)) {
                                switch (sr.getTypeID()) {
                                    case MascotEntities.ACTIVITY_TID :
                                        newSET.addMember(sr.getTag(), activityDescriptors, sr.getTypeID());
                                        continue;

                                    case MascotEntities.CHANNEL_TID :
                                    case MascotEntities.POOL_TID :
                                    case MascotEntities.IDA_DEVICE_TID :
                                        newSET.addMember(sr.getTag(), idaDescriptors, sr.getTypeID());
                                        break;

                                    case MascotEntities.SUBSYS_TID :
                                        newSET.addMember(sr.getTag(), subsystemDescriptors, sr.getTypeID());
                                        break;

                                    case MascotEntities.HANDLER_TID :
                                        newSET.addMember(sr.getTag(), handlerDescriptors, sr.getTypeID());
                                        break;

                                    case MascotEntities.DEVICE_TID :
                                        newSET.addMember(sr.getTag(), deviceDescriptors, sr.getTypeID());
                                        break;

                                    default :
                                        throw new MascotMachineException(
                                            "SETMap<doMerge>: Unknown entity type id = " + sr.getTypeID());
                                }
                            }
                        }
                        //Add or replace resources
                        newSET.getResources().putAll(((SETEntity) aSet.getValue()).getResources());
                    }
                    return null;
                }
            }
            transaction(new runner(), null);
        }
    }

    /**
     * The constructor for an entity store is not public - use entityStoreFactory.
     */
    protected EntityStore() {
        super();
    }

    //The value here changes every timet he repository is initialized
    private static Random counterGen;
    private static int initCounter;

    /**
     * Retrun the current value of the initialization counter
     * @return
     */
    public static final int getInitCounter() {
        return initCounter;
    }

    /**
     * The init method is only for testing.  Allows the reinitialization of the repostiory and
     * is only for unit tests.
     * 
     */
    protected final static void init(Object test) {
        if (test instanceof TestEntityStore || mascotRepository == null) {
            if (eGate == null) {
                eGate = new MascotTransactionQueue();
            }
            eGate.cqJoin();
            try { //Fill in the init counter
                counterGen = new Random();
                initCounter = counterGen.nextInt(0x7fffffff);
                // Initialize the root store
                mascotRepository = new EntityStore();
                nullEntry = new NullTableEntry();

                //Create the system and worker sets in mascotRepository
                mascotRepository.setDescriptors.put("system", new SETEntity("system", null));
                mascotRepository.setDescriptors.put("worker", new SETEntity("worker", null));

                //The global and worker subsystems are special and always defined
                GlobalSubsystemEntity globalES = new GlobalSubsystemEntity("global", null);
                mascotRepository.subsystemDescriptors.put("global", globalES);

                //Add the global and worker subsystems to the appropriate SETs
                try {
                    globalES.setMembership("system", mascotRepository.subsystemDescriptors);
                } catch (MascotMachineException e1) {
                    e1.printStackTrace();
                }

                //Now create the primordial system
                SubsystemRef globalRef = (SubsystemRef) globalES.referenceFactory("global", null);
                try {
                    globalSub = (GlobalSubsystem) globalRef.getInstance(null, "global");
                    globalSub.form(globalRef);
                } catch (MascotMachineException e) {
                    MascotDebug.println(0, "Error forming primordial systems!!  MOJO exits");
                    throw new MascotRuntimeException("Primoridal install failure");
                }
            } finally {
                eGate.cqLeave();
            }
        }
    }

    /**
     * Method entityStoreFactory.
     * 
     * Produce an instance of an EntityStore
     */
    public static EntityStore entityStoreFactory() {
        final EntityStore newes = new EntityStore();
        //Make sure there is a global subsystem and the the system set
        newes.setDescriptors.put("system", new SETEntity("system", null));
        GlobalSubsystemEntity globalES = new GlobalSubsystemEntity("global", null);
        newes.subsystemDescriptors.put("global", globalES);
        try {
            globalES.setMembership("system", newes.subsystemDescriptors, newes);
        } catch (MascotMachineException e) {
            e.printStackTrace();
        }
        return newes;
    }

    /**
     * Method storeToString.
     * 
     * Produce a string that is a formatted representation of the root EntityStore object, ie. the one
     * held by the class.
     */
    public static String storeToString() {
        return mascotRepository.toString();
    }

    /**
     * Produce a string that is a formatted representation of the receiver
     */
    public String toString() {
        final MascotPrinter printer = new MascotPrinter();
        final PrintWriter out = printer.out;
        final Object[][] topLevel = { { "SETs", setDescriptors }, {
                "Activities", activityDescriptors }, {
                "IDAs", idaDescriptors }, {
                "Devices", deviceDescriptors }, {
                "Handlers", handlerDescriptors }, {
                "Subsystems", subsystemDescriptors }
        };
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                printer.print((Map) packet[0], (String) packet[1], ((Boolean) packet[2]).booleanValue());
                return null;
            }
        }

        gate.cqJoin();
        try {
            //For each of the tables in the entity store
            runner myRunner = new runner();
            out.println("\n+++  EntityStore map +++");
            for (int i = 0; i < topLevel.length; i++) {
                try {
                    ((DescriptorMap) topLevel[i][1]).transaction(
                        myRunner,
                        new Object[] { topLevel[i][1], topLevel[i][0], new Boolean(!(i == 0))});
                } catch (MascotMachineException e) {
                }
            }
            return printer.toString();
        } finally {
            gate.cqLeave();
        }
    }

    /**
    * Method merge.
    * 
    * Merge the source EntityStore with the root
    */
    private static void merge(final EntityStore source, final boolean replace)
        throws MascotMachineException {
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                // SETs need to be merged first
                mascotRepository.setDescriptors.doMerge(source.setDescriptors, replace);

                mascotRepository.handlerDescriptors.doMerge(source.handlerDescriptors, replace);
                mascotRepository.subsystemDescriptors.doMerge(source.subsystemDescriptors, replace);
                mascotRepository.activityDescriptors.doMerge(source.activityDescriptors, replace);
                mascotRepository.deviceDescriptors.doMerge(source.deviceDescriptors, replace);
                mascotRepository.idaDescriptors.doMerge(source.idaDescriptors, replace);
                //The repository now contains all of the defined elemens.  Fill in the subsystems
                ArrayList subs = new ArrayList(mascotRepository.getSubsystemDescriptors().values());
                subs.addAll(mascotRepository.getHandlerDescriptors().values());
                for (Iterator i = subs.iterator(); i.hasNext();) {
                    EsSubsystem eiSub = (EsSubsystem) ((SubsystemEntity) i.next()).getCurrentIncarnation();
                    eiSub.resolveDeferred(mascotRepository);
                }
                try {
                    EntityStore.formGlobalSubsystem();
                } catch (MascotMachineException e) {
                    MascotDebug.println(9, "EntityStore:merge() - forming global subsystem:\n\t" + e);
                }
                return null;
            }
        }
        mascotRepository.getGate().doIt(new runner(), null);
    }

    public static void merge(EntityStore source) throws MascotMachineException {
        merge(source, false);
    }

    public static void replace(EntityStore newStore) throws MascotMachineException {
        merge(newStore, true);
    }

    /*
     * Entity creation routines
     */

    /**
     * Method createActivity
     * 
     * @param name
     * @param root
     * @param factory
     * @param membership
     * @return
     * @throws MascotMachineException
     */
    public EsActivity createActivity(
        final String name,
        final String root,
        final String factory,
        final String membership)
        throws MascotMachineException {
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                ActivityEntity activity = new ActivityEntity(name, root, factory);
                activity.setMembership(membership, activityDescriptors, EntityStore.this);
                activityDescriptors.put(name, activity);
                return activity.getCurrentIncarnation();
            }
        }
        return (EsActivity) activityDescriptors.transaction(new runner(), null);
    }

    /**
     * Create a device entity record
     * 
     * @param name
     * @param handler
     * @param membership
     * @return
     * @throws MascotMachineException
     */
    public EsDevice createDevice(final String name, final String handler, final String membership)
        throws MascotMachineException {
        try {
            checkHandler(handler, name);
        } catch (MascotMachineException e) {
            MascotDebug.println(
                0,
                "Warning: Handler \"" + handler + "\" is not currently defined for device \"" + name + "\"");
            MascotDebug.println(0, "\tConsider reorganizing the SETs document if possible.");
        }
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                DeviceEntity device = new DeviceEntity(name, handler);
                //Devices and their associated pool entities go into the system SET regardless of where
                //they are defined
                device.setMembership("system", deviceDescriptors, EntityStore.this);
                deviceDescriptors.put(name, device);

                //Adding a device implies the creation of of a device pool in the global subsystem under
                //the name of the device.  Create this IDA and then add a reference to the global subsystem
                String poolName = Device.makePoolName(name);
                createIDA(poolName, DevicePool.class.getName(), "devicePoolFactory", "pool", "system");
                (
                    (EsSubsystem) ((MascotEntities) subsystemDescriptors.get("global"))
                        .getCurrentIncarnation())
                        .localIDARef(
                    EntityStore.this,
                    poolName,
                    poolName);
                return device.getCurrentIncarnation();
            }
        }
        return (EsDevice) deviceDescriptors.transaction(new runner(), null);
    }

    /**
     * Create an IDA entity
     * 
     * @param name
     * @param implementation
     * @param factory
     * @param type
     * @param membership
     * @return
     * @throws MascotMachineException
     */
    public EsIDA createIDA(
        final String name,
        final String implementation,
        final String factory,
        final String type,
        final String membership)
        throws MascotMachineException {
        class runner implements IMascotTransaction {
            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                IDAEntities ida;
                if (type.equals("pool")) {
                    ida = new PoolEntity(name, implementation, factory, type);
                } else if (type.equals("device")) {
                    ida = new IDADeviceEntity(name, implementation, factory, type);
                } else {
                    ida = new ChannelEntity(name, implementation, factory, type);
                }
                ida.setMembership(membership, idaDescriptors, EntityStore.this);
                idaDescriptors.put(name, ida);
                return ida.getCurrentIncarnation();
            }
        }
        return (EsIDA) idaDescriptors.transaction(new runner(), null);
    }

    /**
     * Method createSET
     * 
     * @param name
     * @return
     * @throws MascotMachineException
     */
    public EsSET createSET(final String name) throws MascotMachineException {
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                SETEntity set = new SETEntity(name, null);
                setDescriptors.put(name, set);
                return set.getCurrentIncarnation();
            }
        }
        return (EsSET) setDescriptors.transaction(new runner(), null);
    }

    /**
     * Allow access to SET entities, particularly the system SET
     * @param name
     * @return
     */
    public SETEntity getSET(final String name) {
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                return (SETEntity) setDescriptors.get(name);
            }
        }
        try {
            return (SETEntity) setDescriptors.transaction(new runner(), null);
        } catch (MascotMachineException e) {
            return null;
        }
    }

    /**
     * Method createSubsystem.
     * Creates an instance of EsSubsystem and insterts it into the systemDescriptors table.
     * 
     * @param type
     * @param name
     * @param termFlag
     * @param membership
     * @return
     * @throws MascotMachineException
     */
    public EsSubsystem createSubsystem(
        final String type,
        final String name,
        final boolean termFlag,
        final String membership)
        throws MascotMachineException {
        SubsystemEntity subsystem;
        DescriptorMap descriptor = null;
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                ((Map) packet[0]).put(packet[1], packet[2]);
                return ((SubsystemEntity) packet[2]).getCurrentIncarnation();
            }
        }
        if (type.equals("global")) {
            subsystem = (SubsystemEntity) subsystemDescriptors.get("global");
            return (EsSubsystem) subsystem.getCurrentIncarnation();
        } else {
            if (type.equals("subsystem")) {
                subsystem = new SubsystemEntity(name, null, termFlag);
                descriptor = subsystemDescriptors;
            } else if (type.equals("handler")) {
                subsystem = new HandlerEntity(name, null, termFlag);
                descriptor = handlerDescriptors;
            } else {
                throw new MascotMachineException(
                    "EntityStore(createSubsystem): Unknown subsystem type \"" + type + "\"");
            }
            subsystem.setMembership(membership, descriptor, this);
        }
        return (EsSubsystem) descriptor.transaction(
            new runner(),
            new Object[] { descriptor, name, subsystem });
    }

    /*
     * Existnace checks
     */

    /**
     * Method checkSubsystem.
     * 
     * Checks that the subsystem at key exists.  If not throws MascotMachineException.
     * @throws MascotMachineException
     */
    public SubsystemEntity checkSubsystem(String key, String name) throws MascotMachineException {
        if (!subsystemDescriptors.containsKey(key)) {
            try {
                return checkHandler(key, name);
            } catch (MascotMachineException e) {
            }
            throw new MascotMachineException(
                "Cannot add entity " + name + " to nonexistant subsystem " + key);
        }
        return (SubsystemEntity) subsystemDescriptors.get(key);
    }

    /**
     * Static access to check subsystem
     * 
     * @param es
     * @param key
     * @param name
     * @return
     * @throws MascotMachineException
     */
    public static SubsystemEntity checkSubsystem(EntityStore es, String key, String name)
        throws MascotMachineException {
        return es.checkSubsystem(key, name);
    }

    /**
     * Check for the existance of a handler entity
     * 
     * @param key
     * @param name
     * @return
     * @throws MascotMachineException
     */
    private SubsystemEntity checkHandler(String key, String name) throws MascotMachineException {
        if (!handlerDescriptors.containsKey(key))
            throw new MascotMachineException(
                "Cannot add device " + name + " with nonexistant handler " + key);
        return (SubsystemEntity) handlerDescriptors.get(key);
    }

    /**
     * Static access to check handler
     * 
     * @param es
     * @param key
     * @param name
     * @return
     * @throws MascotMachineException
     */
    public static SubsystemEntity checkHandler(EntityStore es, String key, String name)
        throws MascotMachineException {
        return es.checkHandler(key, name);
    }

    /*
     * Subsystems (and handlers) hold references to entitities.  References are the look like:
     *  name - the reference's, local to the subsystem
     *  reference - the name of the entity to which a reference refers
     */

    public static final String ACTIVITY_ARGS = "activity-args";

    /*
     * An array of reserved argument keys.  These are names that the user cannot express
     */
    public static final String[] reservedArgKeys = { ACTIVITY_ARGS };
    private static List argKeys = Arrays.asList(reservedArgKeys);

    /**
     * Check that the passed key is not reserved
     * 
     * @param key
     * @return
     * @throws MascotMachineException
     */
    public static String checkArgKey(String key) throws MascotMachineException {
        if (!argKeys.contains(key)) {
            return key;
        }
        throw new MascotMachineException("EntityStore<checkArgKey>: Key " + key + "is not allowed.");
    }

    /*
    * These methods deal with workers
    */

    /**
     * Put an activity descriptor in the worker set.
     * 
     * @param subsystem
     * @param name
     * @param root
     * @param factoryMethod
     * @param args
     * @throws MascotMachineException
     */
    protected IMascotReferences addActivityToWorker(
        final String subsystem,
        final String name,
        final Object root,
        final String factoryMethod,
        final Vector args)
        throws MascotMachineException {
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                String qualifiedName = makeQualifiedName(subsystem, name);
                ActivityEntity activity = new ActivityEntity(name, root, factoryMethod, qualifiedName);
                activity.setMembership("worker", activityDescriptors, mascotRepository);
                activity.getCurrentIncarnation();
                if (!activityDescriptors.containsKey(name)) {
                    activityDescriptors.put(name, activity);
                }
                return activity.referenceFactory(qualifiedName, null);
            }
        }
        return (IMascotReferences) activityDescriptors.transaction(new runner(), null);
    }

    public static String rootName(Object name) {
        return (name instanceof String) ? (String) name : name.getClass().getName();
    }

    public IMascotReferences addActivityToWorker(
        String subsystem,
        Object root,
        String factoryMethod,
        Vector args)
        throws MascotMachineException {
        String name = rootName(root) + "-" + root.hashCode();
        return addActivityToWorker(subsystem, name, root, factoryMethod, args);
    }

    public IMascotReferences addActivityToWorker(
        String subsystem,
        Object root,
        String factoryMethod,
        Vector args,
        String tag)
        throws MascotMachineException {
        String name = rootName(root) + "-" + tag + "-" + root.hashCode();
        return addActivityToWorker(subsystem, name, root, factoryMethod, args);
    }

    /**
     * @param subsystem
     * @param name
     * @return
     */
    public static String makeQualifiedName(String subsystem, String name) {
        return subsystem + "://" + name;
    }

    /**
         * Returns the globalSubsystem.
         */
    public static GlobalSubsystem getGlobalSubsystem() {
        return globalSub;
    }

    public static void formGlobalSubsystem() throws MascotMachineException {
        GlobalSubsystem.incarnate();
    }

    private Subsystem doFormSubsystem(
        final SubsystemEntity.SubsystemRef subRef,
        final Subsystem container,
        final String name,
        final Map resources)
        throws MascotMachineException {

        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                EsSubsystem es = (EsSubsystem) subRef.getIncarnation();
                Subsystem subsystem = subRef.getInstance(container, name);
                InstanceRecord ir = new InstanceRecord(subsystem);
                es.addInstance(ir);
                subsystem.setResources(resources);

                try {
                    subsystem.form(subRef, name);
                    container.addSubsystem(subsystem);

                    StringWriter buffer = new StringWriter();
                    PrintWriter printer = new PrintWriter(buffer);
                    Console.treeWalk(container, "", printer);
                    printer.println("----------------------");
                    MascotDebug.println(9, buffer.toString());
                    return subsystem;
                } catch (MascotMachineException e) {
                    MascotDebug.println(9, "formSubsystem: " + e);
                    es.removeInstance(subsystem);
                }
                return subsystem;
            }
        }
        if (container == null || subRef == null || name == null) {
            throw new MascotMachineException("EntityStore(doFormSubsystem) - null parameter");
        }
        return (Subsystem) gate.doIt(new runner(), null);
    }

    /**
     * Return a tag string
     * @param name
     * @param container
     * @return
     * @throws MascotMachineException
     */
    public static String instanceNameFor(Object query) {
        String prefix =
            (query instanceof AbstractRoot)
                ? ((AbstractRoot) query).getEInstance().getParentEntity().getName()
                : "unknown";
        return prefix + "@" + query.getClass().getName() + "-" + query.hashCode();
    }

    public static Subsystem formSubsystem(String name, Subsystem container) throws MascotMachineException {
        if (container == null || name == null) {
            throw new MascotMachineException("formSubsystem: Name or contianer null - illegal");
        }

        SubsystemEntity.SubsystemRef sub =
            (SubsystemRef)
                ((SubsystemEntity) mascotRepository.subsystemDescriptors.get(name)).referenceFactory(
                name,
                null);
        return formSubsystem(sub, container);
    }

    public static Subsystem formSubsystem(SubsystemEntity.SubsystemRef subref, Subsystem container)
        throws MascotMachineException {
        if (container == null || subref == null) {
            throw new MascotMachineException("formSubsystem: Name or contianer null - illegal");
        }
        return mascotRepository.doFormSubsystem(subref, container, subref.getName(), subref.getResources());
    }

    public static Subsystem formHandler(String name) throws MascotMachineException {
        SubsystemEntity.SubsystemRef sub =
            (SubsystemRef)
                ((SubsystemEntity) mascotRepository.handlerDescriptors.get(name)).referenceFactory(
                name,
                null);
        return formSubsystem(sub, EntityStore.getGlobalSubsystem());
    }

    /**
     */
    public final Map getHandlerDescriptors() {
        return handlerDescriptors;
    }

    /**
     */
    public static final EntityStore mascotRepository() {
        eGate.cqJoin();
        try {
            return mascotRepository;
        } finally {
            eGate.cqLeave();
        }
    }

    /**
     */
    public final Map subsystemDescriptors() {
        return subsystemDescriptors;
    }

    /**
     * @return
     */
    public final Map getActivityDescriptors() {
        return activityDescriptors;
    }

    /**
     * @return
     */
    public final Map getDeviceDescriptors() {
        return deviceDescriptors;
    }

    /**
     * @return
     */
    public final Map getIdaDescriptors() {
        return idaDescriptors;
    }

    /**
     * @return
     */
    public final Map getSetDescriptors() {
        return setDescriptors;
    }

    /**
     * @return
     */
    public final Map getSubsystemDescriptors() {
        return subsystemDescriptors;
    }

    /**
     * @return
     */
    public MascotTransactionQueue getGate() {
        return gate;
    }

}
