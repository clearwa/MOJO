/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.internal;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.idas.ArguementIDA;
import com.objectforge.mascot.machine.model.IActivity;
import com.objectforge.mascot.machine.model.IRoot;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.machine.scheduler.MascotThread;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * A Mascot activity.  The Mascot Machine plugs roots into activity framework to run 
 * them.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class Activity implements IActivity, Runnable {
    //The subsystem instance to which this activity belongs
    public Subsystem subsystem;
    //A hashtable of idas to which this activity has access.  Keyed by name.
    public Hashtable idas;
    //Holds resolved device refs
    public Hashtable deviceRefs = new Hashtable();
    //The root instance for this activity
    IRoot root;
    //The arguments to this activity
    Object[] args;
    //Arg cache - holds resolved IDA references
    Hashtable rCache = new Hashtable();
    //Note whether this activity has been started
    boolean started;
    //A vector of WorkerDelegateEntities.  On the war out the door mark these potential candidates
    //to be reaped from the worker delegate table
    Vector reapers = new Vector();

    /**
     * Construct an activity for subsystem with root root and arguments args
     * Method Activity.
     */
    public Activity(Subsystem subsystem, Hashtable idas, IRoot root, Vector args) {
        super();
        init(subsystem, idas, root, args);
        //		EntityStore.mrefs.put(this, this.getClass().getName() + "-" + this.hashCode());
    }

    /**
     * Fill in the locals for an Activity object
     * Method initialize.
     */
    public void init(Subsystem subsystem, Hashtable idas, IRoot root, Vector args) {
        this.subsystem = subsystem;
        this.idas = idas;
        this.root = root;
        if (args != null)
            this.args = args.toArray();
    }

    public void initialize(Subsystem subsystem, Hashtable idas, IRoot root, Vector args) {
        //The subsystem instance to which this activity belongs
        this.subsystem = subsystem;
        //A hashtable of idas to which this activity has access.  Keyed by name.
        this.idas = idas;
        //Holds resolved device refs
        deviceRefs = new Hashtable();

        //The arguments to this activity
        this.args = args.toArray();
        //The root instance for this activity
        this.root = root;
    }

    /**
     * The subsystem form process fills in idas based on the SET document that defined the
     * activity.  The keys in the table the names declared in the SET; to maintain "access by
     * name" the name needs to be resolved to an actual IDA object.  In the case where the
     * IDA is an argument IDA then completely resolve the reference and cache it.
     */
    public Object resolve(Object key) {
        Object ida = idas.get(key);
        
        //Devices silently enter pool names in the global subsystem.  These names have a '$' character in 
        //them.  If the ida is null check a device pool name.
        if( ida == null){
            ida = Device.resolveDevicePool( (String) key );
        }

        if (ida instanceof ArguementIDA) {
            if (!rCache.contains(ida)) {
                Object ref = subsystem.getArgFromSubsystem(((ArguementIDA) ida).getIndex());
                if (ref != null) {
                    rCache.put(ida, ref);
                    ida = rCache.get(ida);
                } else {
                    return null;
                }
            }
        }
        if (ida == null) {
            MascotDebug.println(9, "IDA ref is null");
        }
        return ida;
    }

    /* (non-Javadoc)
     */
    public Object resolve(Object deviceRef, String connectionRef) {
        Object[] entry = (Object[]) deviceRefs.get(deviceRef);
        Object retval = null;

        if (entry != null) {
            retval = connectionRef.equals(Device.READER) ? entry[0] : entry[1];
        }
        return retval;
    }

    /**
     */
    public void actStart(String threadName) {
        MascotThread myThread = new MascotThread(getSubsystem().getThreadGroup(), this, threadName);
        start(myThread);
    }

    /**
     */
    public void actTerminate() {
    }

    /**
     */
    public void actSuspend() {
    }

    /**
     */
    public void actResume() {
    }

    /**
     * Returns the idas.
     */
    public Hashtable getIdas() {
        return idas;
    }

    /**
     * Method getRoot.
     */
    public IRoot getRoot() {
        return root;
    }

    /**
     * Returns the subsystem.
     */
    public Subsystem getSubsystem() {
        return subsystem;
    }

    /**
     * Method rootSuspend.
     * Suspend the activity.
     */
    private void rootSuspend() {
        //Check whether the subsystem is suspended
        while (subsystem.suspended) {
            MascotDebug.println(
                0,
                Thread.currentThread().getName()
                    + " suspends - flag="
                    + ((MascotThread) Thread.currentThread()).isSuspended());
            try {
                subsystem.suspendObject.join(); //Hang on the suspend object.
                subsystem.suspendObject.waitQ();
            } catch (MascotRuntimeException ex) {
                MascotDebug.println(0, "resumeRoot forced exit: " + Thread.currentThread().getName());
                return;
            }
            //If there are more pending then stim the counting queue
            if (subsystem.suspendObject.leave()) {
                subsystem.suspendObject.stim();
            }
            try {
                root.resumeRoot(); //restart the root
            } catch (Exception e) {
                MascotDebug.println(0, "resumeRoot: " + Thread.currentThread().getName());
                MascotDebug.println(0, "\t" + e);
            }
        }
    }

    /**
     * Start the root for the subsystem
     */
    public void run() {
        try {
            subsystem.startSignal.cqStim(); /* Tell the start code I'm up and running */
            subsystem.subsysSync.cqJoin(); /* Wait for the subsystem start to complete */
            MascotDebug.println(9, Thread.currentThread().getName() + " released");
            subsystem.subsysSync.cqLeave(); //release the sync queue
            try {
                root.startRoot(this, args); //start the root
            } catch (MascotRuntimeException e1) {
                MascotDebug.println(5, "Activity.run: attempt suspension\n    " + e1);
                rootSuspend();
            }
        } catch (RuntimeException e) { //on any error test for suspend otherwise exit
            e.printStackTrace();
            MascotDebug.println(5, "Activity.run: runtime exception\n    " + e);
        } catch (NoClassDefFoundError e2) {
            e2.printStackTrace();
        } finally {
            Subsystem sub = subsystem;

            if (!((MascotThread) Thread.currentThread()).isDead() && subsystem.endActivity()) {
                sub.suicide();
            } else {
                freeInstance();
            }
        }
    }

    /**
     * Free this instance
     * Need to use the underlying synchronization mechanism since the threads may be dead
     * at this point and so ControlQueue will barf.
     *
     */
    synchronized public void freeInstance() {
        ((IEIAccess) root).getEInstance().deallocate(root);
        root.endRoot();

        //Tell the reaper I'm going away
        synchronized (reapers) {
            for (Iterator i = reapers.iterator(); i.hasNext();) {
                ((MascotEntities) i.next()).setReap();
            }
        }
        subsystem.removeActivity(this);
    }

    /**
     * Start this activity's thread
     * @return
     */
    public void start(MascotThread myThread) {
        started = true;
        myThread.start();
    }

    /**
     * @return
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * @return
     */
    public Vector getReapers() {
        return reapers;
    }

    /**
     * Look for resources my subsystem and then up the container chain.
     * @param key
     * @return
     */
    public Object getSubsysResource(Object key) {
        return subsystem.getResource(key);
    }

}
