/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.device;

import java.util.Map;
import java.util.Vector;

import com.objectforge.mascot.IDA.SPElement;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.GlobalSubsystem;
import com.objectforge.mascot.machine.internal.IEIAccess;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.DeviceEntity;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.machine.model.xml.Console;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.MascotThread;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;
import com.objectforge.mascot.utility.MascotUtilities;

/**
 * MASCOT 2002 Device implementaion
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public class Device implements IEIAccess {
    private EntityInstance eInstance;
    static int counter = 0;
    String deviceName; //This device's name
    String handler; //The handler for this device

    Console console = new Console();
    volatile Subsystem hsub = null;
    GlobalSubsystem globalsub = EntityStore.getGlobalSubsystem();
    volatile Device target;
    volatile DevicePool devicePool;
    volatile Object deviceInstanceName;

    public static final int DEVICE_INSTANCE = 0;
    public static final int DEVICE_CHANNS = 1;
    public static final int DEVICE_ICOUNT = 2;

    static ControlQueue serialize = new ControlQueue();

    public static final String READER = "reader";
    public static final String WRITER = "writer";

    static boolean workerInit = false;

    //Do not allow the work entity to be reaped by incarnations of the worker activity.
    //The only time this is allowed would be when the device is closed.  Note this for
    //when closing is implemented
    protected static MascotEntities delegate;

    /*
     * Worker activity references
     */
    IMascotReferences factoryActivity;

    /**
     * DeviceRootPrivate is a worker activity.  The incarnation processing adds it to the handler
     * subsystem before it starts the handler.  It is assumed that the handler defines 2 channels,
     * "reader" and "writer"; any subsystem wishing to access an instance of a device uses these
     * channels to read from and write to the device.
     */
    public class DeviceRootPrivate extends AbstractRoot {

        public DeviceRootPrivate() {
            super();
        }

        /**
         * This root has the sole purpose of resolving referneces to the handler's reader and writer
         * channels and then putting them in the pool under the instance name key.
         */
        public void mascotRoot(Activity activity, Object[] args) {
            MascotDebug.println(9, "Device Activity starts");

            /*
             * Create the array of resolved references.
             */
            Object[] channels = { resolve(READER), resolve(WRITER)};

            /*
             * Write the referneces to the pool
             */
            devicePool.lock();
            try {
                Object[] poolContents = ((Vector) devicePool.snapshot()).toArray();
                PoolContents pc = new PoolContents();
                int index = pc.doFind(deviceInstanceName, poolContents);

                /*
                 * Wait for the instance name record to appear in the pool.  When it does,
                 * overwrite it with a new record that contains the handler's channels.  Note
                 * that if you are holding on to the old refernce from the pool then you will
                 * never see the channels.  After doing this the activity dies.
                 */
                if (((Object[]) ((SPElement) poolContents[index]).contents)[Device.DEVICE_CHANNS] == null) {
                    Object[] newContents = { deviceInstanceName, channels, new Integer(0)};
                    devicePool.write(newContents);
                }
            } finally {
                devicePool.unlock();
            }
        }

        /* (non-Javadoc)
         */
        public void resumeRoot() {

        }
    }

    /**
     * This is a conveniece class that searches returned pool contents for a particular key.
     * doFind searches the passed array and if the key exists then it returns the index, otherwise
     * -1
     */
    public class PoolContents {
        /**
         */
        public int doFind(Object iname, Object[] poolContents) {
            int exitval = -1;

            for (int i = 0; i < poolContents.length; i++) {
                if (((Object[]) ((SPElement) poolContents[i]).contents)[DEVICE_INSTANCE].equals(iname)) {
                    exitval = i;
                    break;
                }
            }
            return exitval;
        }
    }

    /**
     * Creating an instance of a device adds a new worker activity definition to the worker
     * subsystem.  The key to this entity is this instance.  Note that the constructor is not
     * globally accessable.
     */
    Device() {
        super();
    }

    /**
     * Create an instance of DeviceRootPrivate for an instance of Device
     */
    public DeviceRootPrivate DeviceRootFactory() {
        return this.new DeviceRootPrivate();
    }

    /**
     * Create an instance of Device for device "deviceName" with handler "handler"
     */
    public Device(String deviceName, String handler) {
        this();
        this.deviceName = deviceName;
        this.handler = handler;
        try {
            factoryActivity =
                EntityStore.mascotRepository().addActivityToWorker(
                    this.handler,
                    this,
                    "DeviceRootFactory",
                    null);
        } catch (MascotMachineException e) {
            MascotUtilities.throwMRE("Device(Device<Cannot add worker Activity>: " + e);
        }
    }

    /**
     * The creation of a device implies the creation of a deivce pool in the global subsystem.
     * This pool's name cannot be overriden at some later time; if this were the case then
     * all hell would break loose.  These routines generate and check the pool's name and
     * assure that a user simply cannot say a name that might cause a conflict.
     */

    /**
     * Create a non-conflicting name for a device pool
     * 
     * @param base
     * @return
     */
    public static final String makePoolName(String base) {
        return base + "$devicepool";
    }

    public static final Object resolveDevicePool(String key) {
        if (key.endsWith("$devicepool")) {
            return EntityStore.getGlobalSubsystem().getIdas().get(key);
        }
        return null;
    }

    /**
     * It is assumed that the passed name is a candidate for an IDA entity name.  If this
     * name contians a '$' character than it has the potential of overriding a device pool
     * name.  In this case, rejecte it.  If the user can't say a confilicting name then
     * there is no possibility of a conflict.
     * 
     * @param name
     * @return
     * @throws MascotMachineException
     */
    public static final String checkPoolName(String name) throws MascotMachineException {
        if (name.indexOf('$') < 0) {
            return name;
        }
        //If the name contains a '$' character then reject it
        throw new MascotMachineException("Device<checkPoolName>: Illegal IDA name.");
    }

    /**
     * Upon entry I have 2 pieces of informaton, the target device name, ie. the global name
     * for the device itself, and an instance name, ie. the name of a particular incarnation
     * of the device.  I also have a device pool in the global system that stores information
     * relating to intances of the generic device.  Start by checking that the target device
     * exists in the global subsystem (it can't be anywhere else).
     */
    public static Object createInstance(Object targetDevice, Object instanceName) throws IDAException {

        Map devices = EntityStore.mascotRepository().getDeviceDescriptors();
        if (!devices.containsKey(targetDevice)) {
            throw new MascotRuntimeException(
                "createInstance: Cannot resolve device target \"" + targetDevice + "\"");
        }

        /*
         * The target device exists.  Find the instance and proceed to incarnate under the 
         * instanceName
         */
        Device target = null;
        try {
            target =
                (Device) ((DeviceEntity) devices.get(targetDevice))
                    .referenceFactory("device", null)
                    .getInstance();
        } catch (MascotMachineException e) {
            e.printStackTrace();
        }
        return target.incarnate(instanceName);
    }

    /**
     * Incarnting a device is the action of creating a particular instance of a device
     * defined in the global subsystem.  Device instance names are global to the system; this
     * allows sharing across multiple subsystems.  As such this code must decide whether to simply
     * return a reference to an existing instance or a new one.  The device pool associated with
     * the target device holds the information needed to make this decision.
     * 
     * If a new instance must be incarnated then a new instance of the device handler must be
     * started as well.  This code assumes that a handler conforms to a pattern and contains 2
     * channels, a read side named "reader", a write side called "writer", and one or more activities
     * that are controlled via these channels.  Device behaviour and the meaning of channel content is 
     * device specific; the process of incarnation makes no assumptions about this.
     */

    protected Object incarnate(Object instanceName) {
        Object[] retval = null;
        /*
         * This processing is serialized.  First of all resolve the device pool associated
         * with this target in the global subsystem and lock it.
         */
        serialize.cqJoin();
        try {
            deviceInstanceName = instanceName;

            //Currently directly accesses the global subsystem.  Possibly should use the
            //resolution mechanism?
            devicePool = (DevicePool) globalsub.getIdas().get(makePoolName(deviceName));
            devicePool.lock();

            PoolContents searchObj = new PoolContents();
            int pindex;

            /*
             * Decide if I need to crate a new instance.  If not simply unlock the pool
             */
            Object[] poolContents = ((Vector) devicePool.snapshot()).toArray();
            try {
                if ((pindex = searchObj.doFind(instanceName, poolContents)) < 0) {

                    /*
                     * Create a primordial reference to the new incarantion in the pool.
                     * This is an array filled as follows:
                     * 	index 0 - the instance name.  The record is held in the pool under this
                     * 				key.
                     * 	index 1 - an array holding the handler's reader and writer channels.  In
                     * 				the primordial case this is null; when the handler is up
                     * 				and running it is filled.
                     * 	index 2 - refernece count.  A counter that holds the number of references
                     * 				to the instance.  In the primordial case it is 0
                     * The incarnation process starts by writing this record to the pool
                     */
                    Object[] newContents = { instanceName, null, new Integer(0)};
                    devicePool.write(newContents);
                    devicePool.unlock();

                    /*
                     * Form the handler subsystem.
                     */
                    try {
                        hsub = console.formHandler(this.handler);
                    } catch (Exception e) {
                        throw new MascotRuntimeException("Device(incarnate): " + e);
                    }

                    /*
                     * Add a worker to the newly formed subsystem.  In this case the worker's root
                     * is an instance of the inner class DeviceRootPrivate.  See above for it's
                     * behaviour.
                     */
                    try {
                        Vector dv = new Vector();
                        ((Activity) ((MascotThread) Thread.currentThread()).getActivity())
                            .getRoot()
                            .addWorkerNR(
                            hsub,
                            factoryActivity,
                            null,
                            dv);
                        delegate = (MascotEntities) dv.remove(0);
                    } catch (MascotMachineException e1) {
                        MascotUtilities.throwMRE("Device(resumeRoot): " + e1);
                    }
                    //Pass the handler name to the new subsystem
                    hsub.addArgToSubsystem(0, handler);
                    /*
                     * Start the handler subsystem.
                     */
                    console.start(hsub);
                    /*
                     * Monitor the pool.  In particular this code hangs until the worker writes to a 
                     * modified version of the primordial record to the pool in which the channel array 
                     * (index 1) has been filled with live references.  Note that if the handler crashes
                     * this may never complete.
                     */
                    while (true) {
                        poolContents = ((Vector) devicePool.read()).toArray();
                        if ((pindex = searchObj.doFind(instanceName, poolContents)) >= 0) {
                            if (((Object[]) ((SPElement) poolContents[pindex]).contents)[DEVICE_CHANNS]
                                != null)
                                break;
                        }
                    }
                }
            } finally {
                devicePool.unlock();
            }
            /*
             * Lock the pool and update the instance count.  The processing here is common to both
             * the case where the instance has been newly incarnated and where it already exists in the
             * pool.
             */
            devicePool.lock();
            Object[] newContents = (Object[]) ((SPElement) poolContents[pindex]).contents;
            try {
                int count = ((Integer) newContents[DEVICE_ICOUNT]).intValue();
                newContents[DEVICE_ICOUNT] = new Integer(++count);
                devicePool.write(newContents);
            } finally {
                devicePool.unlock();
            }
            /*
             * Return the references to the handler's channels.
             */
            retval = (Object[]) cloneContents(newContents[DEVICE_CHANNS]);
        } finally {
            serialize.cqLeave();
        }
        return retval;
    }

    /**
     * @param object
     * @return
     */
    private Object cloneContents(Object devices) {
        Object[] source = (Object[]) devices;
        Object[] dest = new Object[source.length];

        System.arraycopy(source, 0, dest, 0, source.length);

        //		for (int i = 0; i < source.length; i++) {
        //			try {
        //				dest[i] = ((AbstractIDA) source[i]).clone();
        //			} catch (CloneNotSupportedException e) {
        //				MascotDebug.println(9,"Device<colneContents:  " + e );
        //			}
        //		}
        return dest;
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
}
