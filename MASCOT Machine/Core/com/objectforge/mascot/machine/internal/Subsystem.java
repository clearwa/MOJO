/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.internal;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.estore.EsSubsystem;
import com.objectforge.mascot.machine.estore.IEsSubsystem.IInstallRecord;
import com.objectforge.mascot.machine.idas.ArguementIDA;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.DeferredRef;
import com.objectforge.mascot.machine.model.IDARef;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.IRoot;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.machine.model.MascotReferences;
import com.objectforge.mascot.machine.model.SubsystemEntity;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.CountingQI;
import com.objectforge.mascot.machine.scheduler.MascotThread;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;
import com.objectforge.mascot.utility.MascotUtilities;

/**
 * <p>Subsystems are the primary container for IDAs, devices, and activities.  The only way
 * to create a collection of these entities is to define them in the context of a subsystems.
 * Subsystems are also the smallest controllable unit in Mascot, ie. subsystems may be started,
 * suspended, resumed, and terminated.</p>
 * 
 * <p>In the Mascot 2000 implementation entities IDAs have scope with respect to the subsystem
 * in which they are defined.  Scopes are:<br>
 * 		local - resolves to the enclosing subsystem<br>
 * 		container - resoloves to the scope of the enclosing subsystem's container<br>
 * 		global - resolves to the global subsystem<br>
 * 		argument - resolves at runtime</p>
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 * 
 */
public class Subsystem implements MascotAlloc, IEIAccess {
    protected MascotEntities reaper;
    protected EntityInstance eInstance;
    /*
     * Hashtables to hold the entities that make up the subsystem
     */
    Hashtable idas = new Hashtable();
    Hashtable activities = new Hashtable();
    Hashtable subsystems = new Hashtable();
    Vector subsysArgs = new Vector();

    //Entries in this hashtable will be deallocated with the subsystem
    Hashtable localidas = new Hashtable();

    /*
     * Threads controlled by this subsystem
     */
    protected ThreadGroup threadGroup;

    /*
     * Hold a reference to the Assassin activity
     */
    private static IMascotReferences assassinRef;

    /*
     * 
     */
    protected Subsystem container; //container is null only for the global subsystem
    Map resources;
    String name;
    String instanceName;

    boolean globalInstance = false;

    public boolean suspended = false;
    public ControlQueue subsysSync = new ControlQueue();
    public ControlQueue startSync = new ControlQueue();
    public ControlQueue startSignal = new ControlQueue();
    public CountingQI suspendObject = ControlQueue.CountingQFactory();
    protected CountingQI waiters = ControlQueue.CountingQFactory();
    volatile boolean assassinRunning = false;
    volatile boolean assassinInstalled = false;
    ControlQueue assassinationSync = new ControlQueue();
    private static Random assassinCounter = new Random();

    //Define what happens when all acitivities have terminated.  If set the terminate the subsystem as well
    boolean closeOnEmpty;

    public Subsystem() {
        this.subsysArgs.setSize(10);
        suspendObject.setAllowSuspended();
    }

    public Subsystem(Subsystem container, String name) throws MascotMachineException {
        this();
        this.container = container;
        this.name = name;
        if (container == null) {
            this.name = null;
            throw new MascotMachineException("Subsystem: Only the global subsystem can have a null container");
        }
        if (name == null) {
            this.container = null;
            throw new MascotMachineException("Subsystem: A subsystem must have a name");
        }
    }

    /**
     * Return a subsystem to it's primordial state
     * @param container
     * @param name
     */
    public void initialize(Subsystem container, String name) throws MascotMachineException {
        idas = new Hashtable();
        localidas = new Hashtable();
        activities = new Hashtable();
        subsystems = new Hashtable();
        subsysArgs = new Vector();

        /*
         * Threads controlled by this subsystem
         */
        threadGroup = null;
        /*
         * 
         */
        this.container = container;
        //container is null only for the global subsystem
        this.name = name;
        eInstance = null;
        globalInstance = false;

        suspended = false;
        waiters = ControlQueue.CountingQFactory();
        assassinRunning = false;
        assassinInstalled = false;
        if (container == null) {
            this.name = null;
            throw new MascotMachineException("Subsystem: Only the global subsystem can have a null container");
        }
        if (name == null) {
            this.container = null;
            throw new MascotMachineException("Subsystem: A subsystem must have a name");
        }
    }

    protected synchronized void doStart(Vector startList) {
        EntityStore.getInitCounter();

        for (Enumeration i = activities.elements(); i.hasMoreElements();) {
            Activity activity = (Activity) i.nextElement();
            String name = EntityStore.instanceNameFor(activity.root);
            MascotThread thread = new MascotThread(threadGroup, activity, name);

            startList.add(new Object[] { activity, thread });
        }

        //now start the subsystems contained by this one
        for (Enumeration i = subsystems.keys(); i.hasMoreElements();) {
            Subsystem subsystem = (Subsystem) i.nextElement();

            subsystem.containerStart(startList);
        }
    }

    public void subStart() {
        threadGroup = new ThreadGroup(instanceName);
        Vector startList = new Vector();
        doStart(startList);

        // Grab subsystemSync before ligthing off the threads.  Each activity will hang until this
        // object is released.
        subsysSync.cqJoin();
        startSignal.cqJoin();
        for (Iterator i = startList.iterator(); i.hasNext();) {
            Object[] toStart = (Object[]) i.next();
            if (!((Activity) toStart[0]).isStarted()) {
                ((Activity) toStart[0]).start((MascotThread) toStart[1]);
                startSignal.cqWait();
            }
        }
        startSignal.cqLeave();
        subsysSync.cqLeave();
    }

    /**
     * Method containerStart.
     */
    protected void containerStart(Vector startList) {
        subsysSync = container.subsysSync;
        threadGroup = new ThreadGroup(container.threadGroup, instanceName);
        startSync = container.startSync;
        startSignal = container.startSignal;
        doStart(startList);
    }

    /**
     * Deallocate the subsystem.  Access is synchronized since the threads may be dead and so
     * ControlQueue may barf
     */
    protected synchronized void subDeallocate() {
        // Do the activities
        for (Enumeration acts = activities.elements(); acts.hasMoreElements();) {
            ((Activity) acts.nextElement()).freeInstance();
        }

        // Do the IDAs
        for (Enumeration lidas = localidas.elements(); lidas.hasMoreElements();) {
            IIDA lida = (IIDA) lidas.nextElement();
            ((IEIAccess) lida).getEInstance().getInstances().remove(lida);
        }

        //... and finally myself
         ((IEIAccess) this).getEInstance().getInstances().remove(this);
    }

    protected void lockForAssassin(TerminationState termState) {
        MascotDebug.println(9, "Assassin marks subsystem " + name);

        for (Enumeration subs = subsystems.keys(); subs.hasMoreElements();) {
            //Continue regardless of exceptions
            try {
                ((Subsystem) subs.nextElement()).doSubTerminate(termState);
            } catch (MascotRuntimeException e) {
                continue;
            }
        }
    }

    private void doSubTerminate(TerminationState suspend) {
        try {
            Vector reapers = new Vector();

            assassinationSync.cqJoin();
            String tag = "" + assassinCounter.nextInt(0x10000000);
            try {
                assassinRef =
                    EntityStore.mascotRepository().addActivityToWorker(
                        this.getName(),
                        Assassinate.class.getName(),
                        null,
                        null,
                        tag);
            } catch (MascotMachineException e) {
                throw new MascotRuntimeException(
                    "Subsystem<doStart>: Error creating assassin worker entity - " + e);
            }

            if (!assassinRunning) {
                assassinRunning = true;

                //Add a worker to the current subsystem - it's job is to assassinate this subsystem
                //and all of it's contained subsystems.  This acitivity also handles suspending subsystems
                //when incarnated with suspend = true
                assassinInstalled = true;
                //Manufacture a new assassin activity
                try {
                    Activity act =
                        (Activity) WorkerDelegate.addWorker(
                            this,
                            assassinRef,
                            new Vector(Arrays.asList(new Object[] { suspend, this })),
                            tag,
                            reapers);
                    suspended = suspend.isSuspend();
                    act.actStart("Assassin");
                } catch (MascotMachineException e1) {
                    //if I take any error then I must leave the queue
                    MascotUtilities.throwMRE("Subsustem<subTermainate adding worker>: " + e1);
                }
                reaper = (MascotEntities) reapers.remove(0);
                assassinationSync.cqWait();
                assassinRunning = false;
                assassinationSync.cqLeave();
            }
        } catch (RuntimeException e) {
            MascotDebug.println(9, "Subsystem<doSubTerminate>: exit on error " + e);
        }

    }

    public void subTerminate() {
        doSubTerminate(TerminationState.kill());
    }

    public void subSuspend() {
        doSubTerminate(TerminationState.suspend());
    }

    public void subResume() {
        doSubTerminate(TerminationState.resume());
    }

    public void suicide() {
        subTerminate();
    }

    public boolean endActivity() {
        MascotThread thisThread = (MascotThread) Thread.currentThread();

        if (assassinRunning || thisThread.isDead())
            return false;

        //Check whether there are any more treads that are alive
        boolean allDead = true;

        synchronized (this) {
            Thread[] enumerated = new Thread[Thread.activeCount()];
            int count = threadGroup.enumerate(enumerated, false);

            for (int i = 0; i < count && allDead; i++) {
                if (enumerated[i] instanceof MascotThread && !(thisThread == enumerated[i])) {
                    allDead =
                        ((MascotThread) enumerated[i]).isDead()
                            && !((MascotThread) enumerated[i]).isSuspended();
                } else {
                    allDead = true;
                }
            }
        }

        if (allDead && closeOnEmpty) {
            return true;
        } else {
            thisThread.setDead(true);
            thisThread.setSuspended(false);
            waiters.stim();
        }
        return false;
    }

    protected MascotReferences subsysResolveRef(MascotReferences ref) throws MascotMachineException {
        if (ref instanceof DeferredRef) {
            return (MascotReferences) ((DeferredRef) ref).resolve(
                (EsSubsystem) this.eInstance,
                EntityStore.mascotRepository());
        }
        return ref;
    }

    /**
     * Find the records that have not been installed.
     * 
     * @param entities
     * @param results
     * @param target
     * @return
     */
    protected Map notInstalled(Map entities, Map results) {
        results = (results == null) ? new Hashtable() : results;

        for (Iterator i = entities.values().iterator(); i.hasNext();) {
            IInstallRecord ref = (IInstallRecord) i.next();
            //If this reference is not installed then add it to the results map
            if (!ref.isInstalled()) {
                results.put(ref.getReference().getName(), ref);
                ref.setInstalled(true); //Set the reference as insatlled
            }
        }
        return results;
    }

    /**
     */
    protected void addIDAs(Map myIDAs) throws MascotMachineException {
        AddToIdas adder = new AddToIdas();

        adder.add(myIDAs);
    }

    //Access control
    private ControlQueue adderGate = new ControlQueue();

    /**
     * This class has one method, add( ... ) that implements the process of adding idas
     * to a subsystem
     */
    class AddToIdas {
        /**
         * Traverse the container heirarchy to find the named reference
         * @param entity
         * @return
         */
        private Object findInContainer(IDARef reference) {
            Object instance = null;

            for (Subsystem csys = container; csys != null && instance == null; csys = csys.getContainer()) {
                instance = csys.getIdas().get(reference.getRefName());
            }
            return instance;
        }

        private Object adderResolve(MascotReferences mref) throws MascotMachineException {
            Object instance;
            mref = subsysResolveRef(mref);
            switch (mref.getReftype()) {
                case IMascotReferences.CONTAINER_IDA_REF :
                    //Look in the contianer for the IDA instance
                    instance = findInContainer((IDARef) mref);
                    break;

                case IMascotReferences.GLOBAL_IDA_REF :
                    //Look in the global subsystem for the IDA instance
                    instance = EntityStore.getGlobalSubsystem().getIdas().get(mref.getRefName());
                    break;

                case IMascotReferences.ARGUMENT_IDA_REF :
                    //In this case the name is the argument index
                    instance = new ArguementIDA(mref.getRefName());
                    break;

                case IMascotReferences.DEVICE_IDA_REF :
                case IMascotReferences.LOCAL_IDA_REF :
                    //In both of these cases, produce a real object locally
                    instance = mref.getInstance();
                    if (instance != null) {
                        localidas.put(mref.getName(), instance);
                    }
                    break;

                default :
                    throw new MascotMachineException("Subsyste<AddToIDAs>: Undefined IDA Reference type");
            }
            return instance;
        }

        /**
         * <p>Implements to core logic for adding idas and devices to a Subsystem object.
         * The method finds IDA instances as follows:</p>
         * 
         *<pre>
         * if [instanceof IDAEntities]
         *   if [scope is global] then
         *     find the instance in the global subsystem
         *   else if [scope is container]
         *     find the instance in the containing subsystem
         *   else
         *     manufacture a new instance in this subsystem
         * else if[instanceof DeviceEntity]
         *   find the instance in the global context
         * </pre>
         * 
         * <p>The new instance is added to the Hastable table.</p>
         * @throws MascotMachineException
         */
        public void add(Map table) throws MascotMachineException {
            adderGate.cqJoin();
            try {
                //Add instances of the references found in the table.  The reference tells me where
                //to looke, the type tells me how to look, and the name tells me where to filr
                //the result
                for (Iterator i = table.values().iterator(); i.hasNext();) {
                    IInstallRecord ref = (IInstallRecord) i.next();
                    MascotReferences mref = ref.getReference();
                    Object instance = adderResolve(mref);
                    if (instance != null) {
                        idas.put(mref.getName(), instance);
                    } else {
                        throw new MascotMachineException("Subsyste<AddToIDAs>: Reference produced null IDA");
                    }
                    ref.setInstalled(true);
                }
            } finally {
                adderGate.cqLeave();
            }
        }

        public void add(MascotReferences mref) throws MascotMachineException {
            adderGate.cqJoin();
            try {
                if (!idas.containsKey(mref.getName())) {
                    Object instance = adderResolve(mref);
                    if (instance != null) {
                        idas.put(mref.getName(), instance);
                    } else {
                        throw new MascotMachineException("Subsyste<AddToIDAs>: Reference produced null IDA");
                    }
                }
            } finally {
                adderGate.cqLeave();
            }
        }
    }

    void addIDAs() throws MascotMachineException {
        AddToIdas adder = new AddToIdas();

        adder.add(((EsSubsystem) eInstance).idas);
    }

    void addDeviceRefs() throws MascotMachineException {
        AddToIdas adder = new AddToIdas();

        adder.add(((EsSubsystem) eInstance).deviceref);
    }

    public void addIDA(IDARef idaRef) throws MascotMachineException {
        AddToIdas adder = new AddToIdas();
        adder.add(idaRef);
    }

    void addActivities() throws MascotMachineException {
        //Create the activities
        for (Iterator i = ((EsSubsystem) eInstance).roots.values().iterator(); i.hasNext();) {
            IInstallRecord ref = (IInstallRecord) i.next();
            MascotReferences reference = subsysResolveRef(ref.getReference());
            IRoot myroot = (IRoot) reference.getIncarnation().getInstance();
            activities.put(
                myroot,
                new Activity(
                    this,
                    idas,
                    myroot,
                    (Vector) ((MascotReferences) ref.getReference()).getResources().get(
                        EntityStore.ACTIVITY_ARGS)));
            ref.setInstalled(true);
        }
    }

    void addSubsystems() throws MascotMachineException {
        //Create subsystems
        try {
            for (Iterator i = ((EsSubsystem) eInstance).subsystems.values().iterator(); i.hasNext();) {
                IInstallRecord ref = (IInstallRecord) i.next();
                MascotReferences reference = subsysResolveRef(ref.getReference());
                EntityStore.formSubsystem((SubsystemEntity.SubsystemRef) reference, this);
                ref.setInstalled(true);
            }
        } catch (MascotMachineException e) {
        }
    }

    /**
     * essubsystem is an instance of EntityStore.EsSubsystem and describes the entities that
     * make up this subsystme.  Forming the subsystem creates a collection of objects (idas and
     * activities) that can be started.  The logic here is to lookup the named EntityInstances in
     * the EntityStore and get instnaces. If this succeeds, ie. if all of the classes are defined
     * and can be instanced, then the subsystem is ready to start.  At the moment the subsystem is
     * left in this state; a correct Mascot implementation would acitivate (create the threads for)
     * the subsystem and then leave it in a suspended state.
     * @throws MascotMachineException
     */
    public void form(final SubsystemEntity.SubsystemRef subRef) throws MascotMachineException {
        form(subRef, null);
    }

    /**
     * Do a form and give the subsystem an alternate name
     * @param es
     * @param name
     */
    public synchronized void form(SubsystemEntity.SubsystemRef subRef, String name)
        throws MascotMachineException {
        eInstance = subRef.getIncarnation();
        String subName = (name == null) ? eInstance.getParentEntity().getName() : name;

        setInstanceName(subName + EntityStore.instanceNameFor(this));
        try {
            closeOnEmpty = subRef.isCloseOnExit();
        } catch (MascotMachineException e) {
            closeOnEmpty = ((EsSubsystem) eInstance).closeOnEmpty;
        }

        //Start wiith channels, pools, and devices
        addIDAs();
        addDeviceRefs();

        //If any device refs have been installed then there will be side effects.  Clean
        //these up
        EsSubsystem esubsystem = (EsSubsystem) eInstance;
        Map myIDAs = notInstalled(esubsystem.idas, null);
        addIDAs(myIDAs);

        //Add activities and subsystems
        addActivities();
        addSubsystems();
    }

    /**
     * Returns the activities.
     */
    public Hashtable getActivities() {
        return activities;
    }

    /**
     * Returns the container.
     */
    public Subsystem getContainer() {
        return container;
    }

    /**
     * Returns the idas.
     */
    public Hashtable getIdas() {
        return idas;
    }

    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }

    public String toString(String indent) {
        return null;
    }

    public String toString() {
        return toString("");
    }

    /**
     * Method addSubsystem.
     */
    public void addSubsystem(Subsystem subsystem) {
        synchronized (subsystems) {
            subsystems.put(subsystem, subsystem.getName() + "-" + subsystem.hashCode());
        }
    }

    public void removeSubsystem(Subsystem subsystem) {
        synchronized (subsystems) {
            subsystems.remove(subsystem);
            //If this subsystem is marked as close on empty
            if (closeOnEmpty && subsystems.size() == 0 && !isAlive()) {
                suicide();
            }

        }
    }

    public void removeActivity(Activity activity) {
        synchronized (activities) {
            activities.values().remove(this);
        }
    }

    /**
     * Returns the instanceName.
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Sets the instanceName.
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    //Routines for manipulating subsystem arguments
    public void purgeArgs() {
        subsysArgs = new Vector();
    }

    public synchronized void addArgToSubsystem(int index, Object element) {
        if (subsysArgs.size() <= index) {
            Object[] newContents = new Object[index + 10];

            subsysArgs.copyInto(newContents);
            subsysArgs = new Vector(Arrays.asList(newContents));
        }
        subsysArgs.setElementAt(element, index);
    }

    public Object getArgFromSubsystem(int index) {
        Object retval;

        synchronized (subsysArgs) {
            retval = subsysArgs.get(index);
        }
        return retval;
    }

    public int numberOfArgs() {
        return subsysArgs.size();
    }

    public boolean isAlive() {
        if (threadGroup == null)
            return false;
        //This checks everything in this system and below
        return threadGroup.activeCount() > 0;
    }

    /**
     */
    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    /**
     * 
     */
    public void waitForMe() {
        waiters.join();
        waiters.waitQ();
        waiters.leave();
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.internal.MascotAlloc#verify()
     */
    public boolean verify() {
        return !isAlive();
    }
    /**
     * @return
     */
    public Hashtable getSubsystems() {
        return subsystems;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        super.finalize();
        MascotDebug.println(11, "-------------- Subsystem finalized -------------------");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.internal.IEIAccess#getEInstance()
     */
    public EntityInstance getEInstance() {
        return eInstance;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.internal.IEIAccess#setEInstance(com.objectforge.mascot.machine.internal.EntityInstance)
     */
    public void setEInstance(EntityInstance anInstance) {
        eInstance = anInstance;
    }

    /**
     * Lookup the key in this subsystem, the contianer hierarchy, and then the SET
     * 
     * @return
     */
    public Object getResource(Object key) {
        if (resources != null && resources.containsKey(key)) {
            return resources.get(key);
        }
        //Look up the container chain
        if (container != null) {
            return container.getResource(key, (EsSubsystem) eInstance);
        }
        //If I can't look any further then look in the SET
        return ((EsSubsystem) eInstance).getResource(key);
    }

    protected Object getResource(Object key, EsSubsystem esub) {
        if (resources != null && resources.containsKey(key)) {
            return resources.get(key);
        }
        //Look up the container chain
        if (container != null) {
            return container.getResource(key, (EsSubsystem) eInstance);
        }
        //If I can't look any further then look in the SET
        return esub.getResource(key);
    }

    /**
     * @return
     */
    public Map getResources() {
        return resources;
    }

    /**
     * @param resources
     */
    public void setResources(Map resources) {
        this.resources = resources;
    }

    /**
     * @return
     */
    public boolean isCloseOnEmpty() {
        return closeOnEmpty;
    }

    /**
     * @param b
     */
    public void setCloseOnEmpty(boolean b) {
        closeOnEmpty = b;
    }

}
