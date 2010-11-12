/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.roots;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;

import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.idas.DeviceRefIDA;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.IEIAccess;
import com.objectforge.mascot.machine.internal.MascotAlloc;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.WorkerDelegate;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.IRoot;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;
import com.objectforge.mascot.utility.MascotUtilities;

/**
 * AbstractRoot is the concrete implementation of IRoot; all activity roots must extend this class.  Subclasses
 * are required to implement root() and resumeRoot().
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */

public abstract class AbstractRoot implements IRoot, MascotAlloc, IEIAccess {
    private EntityInstance eInstance;
    protected Activity activity;
    protected Object[] args;
    protected Subsystem subsystem;

    public AbstractRoot() {
        super();
    }

    //A finalizer to monitor deallocation
    protected void finalizer() throws Throwable {
        super.finalize();
        MascotDebug.println(11, "-------------- Root finalized -------------------");
    }

    public boolean verify() {
        return true;
    }

    /* (non-Javadoc)
     */
    public abstract void mascotRoot(Activity activity, Object[] args);

    /* (non-Javadoc)
     */
    public void startRoot(Activity activity, Object[] args) {
        this.activity = activity;
        this.args = args;
        this.subsystem = activity.subsystem;

        mascotRoot(activity, args);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#endRoot()
     * 
     * I would like to use control queues here but the threads are dead so ControlQueue will barf.  Use the 
     * underlying synchronization mechanism instead
     */
    synchronized public void endRoot() {
        this.activity = null;
        this.args = null;
        this.subsystem = null;
    }

    /* (non-Javadoc)
     */
    public void printRoot() {
        if (activity == null) {
            MascotDebug.println(9, "Activity for this root is gone!");
        }
        String tag = EntityStore.instanceNameFor(this) + "(" + activity.subsystem.getInstanceName() + ")";

        MascotDebug.println(9, "Activity " + tag + " starts");
        MascotDebug.println(9, "\tRoot\t" + EntityStore.instanceNameFor(this) + "\n\tIDAs:");

        for (Enumeration i = activity.idas.keys(); i.hasMoreElements();) {
            String key = (String) i.nextElement();
            MascotDebug.println(9, "\t\t" + key + "\t" + EntityStore.instanceNameFor(activity.idas.get(key)));
        }
    }

    /* (non-Javadoc)
     */
    public Object resolve(Object key) {
        if (activity == null) {
            MascotUtilities.throwMRE("Acitity died - quit");
        }
        return activity.resolve(key);
    }

    /* (non-Javadoc)
     */
    public Object resolve(Object deviceRef, String connectionRef) {
        if (activity == null) {
            MascotUtilities.throwMRE("Acitity died - quit");
        }
        return activity.resolve(deviceRef, connectionRef);
    }

    /* (non-Javadoc)
     */
    public Object read(Object idaRef) throws MascotMachineException {
        try {
            IIDA delegate = (IIDA) resolve(idaRef);

            return delegate.read();
        } catch (MascotRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MascotMachineException("AbstractRoot read: " + e);
        }
    }

    /* (non-Javadoc)
     */
    public void write(Object idaRef, Object contents) throws MascotMachineException {
        try {
            IIDA delegate = (IIDA) resolve(idaRef);

            delegate.write(contents);
            return;
        } catch (MascotRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MascotMachineException("AbstractR%oot write: " + e);
        }
    }

    /* (non-Javadoc)
     */
    public Object[] status(Object idaRef) throws MascotMachineException {
        try {
            IIDA delegate = (IIDA) resolve(idaRef);

            return delegate.status();
        } catch (MascotRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MascotMachineException("AbstractRoot status: " + e);
        }
    }

    /* (non-Javadoc)
     */
    public void setCapacity(Object idaRef, int capacity) throws MascotMachineException {
        try {
            IIDA delegate = (IIDA) resolve(idaRef);

            Method sc = delegate.getClass().getMethod("setCapacity", new Class[] { Integer.class });
            sc.invoke(delegate, new Object[] { new Integer(capacity)});
        } catch (MascotRuntimeException e) {
            throw e;
        }
        /*
         * If the method invocation fails then do nothing.  This implementation does not understand
         * setCapacity.
         */
        catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    //Routines to manipulate devices
    private Object[] doDeviceAddOrOpen(Object idaRef, Object connectionRef) throws MascotMachineException {
        Object delegate = resolve(idaRef);

        if (!(delegate instanceof DeviceRefIDA)) {
            throw new MascotMachineException("AbstractRoot: Attempt to add or open an invalid reference");
        }
        try {
            return ((DeviceRefIDA) delegate).add(connectionRef);
        } catch (MascotRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MascotMachineException("AbstractRoot: open" + e);
        }
    }

    /* (non-Javadoc)
     */
    public Object[] add(Object idaRef, Object connectionRef) throws MascotMachineException {
        if (activity == null) {
            MascotUtilities.throwMRE("Acitity died - quit");
        }

        Object[] deviceChans = doDeviceAddOrOpen(idaRef, connectionRef);

        //After an add but before an open the only thing a user can do is configure the device
        //via the write side.
        deviceChans[1] = null;
        activity.deviceRefs.put(connectionRef, deviceChans);
        return (Object[]) activity.deviceRefs.get(connectionRef);
    }

    public Object[] open(Object idaRef, Object connectionRef) throws MascotMachineException {
        if (activity == null) {
            MascotUtilities.throwMRE("Acitity died - quit");
        }
        activity.deviceRefs.put(connectionRef, doDeviceAddOrOpen(idaRef, connectionRef));
        return (Object[]) activity.deviceRefs.get(connectionRef);
    }

    /* (non-Javadoc)
     */
    public Object[] close(Object idaRef, Object connectionRef) throws MascotMachineException {
        try {
            DeviceRefIDA delegate = (DeviceRefIDA) resolve(idaRef);

            return delegate.close(connectionRef);
        } catch (MascotRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MascotMachineException("AbstractRoot: close" + e);
        }
    }

    /* (non-Javadoc)
     */
    public Activity getActivity() {
        return activity;
    }

    /* (non-Javadoc)
     */
    public Object[] getArgs() {
        return args;
    }

    /* (non-Javadoc)
     */
    public Subsystem getSubsystem() {
        return subsystem;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
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

    //Methods to create and add workers
    /**
     * Add a worker activity to a subsystem
     * @throws MascotMachineException
     */
    public Object addWorker(Subsystem sub, IMascotReferences root, Vector args)
        throws MascotMachineException {
        return WorkerDelegate.addWorker(sub, root, args, activity.getReapers());
    }

    /**
     * Add a worker activity to a subsystem but the delegate entity will not be reaped.  Behaves
     * @throws MascotMachineException
     */
    public Object addWorkerNR(Subsystem sub, IMascotReferences root, Vector args, Vector reaper)
        throws MascotMachineException {
        return WorkerDelegate.addWorker(sub, root, args, (reaper == null) ? new Vector() : reaper);
    }

    public Object addWorker(Subsystem sub, IMascotReferences root, Vector args, String tag)
        throws MascotMachineException {
        return WorkerDelegate.addWorker(sub, root, args, tag, activity.getReapers());
    }

}
