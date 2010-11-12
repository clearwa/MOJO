/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.internal;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.estore.EsGlobalSubsystem;
import com.objectforge.mascot.machine.estore.IEsSubsystem.IInstallRecord;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IRoot;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.machine.model.MascotReferences;
import com.objectforge.mascot.machine.model.SubsystemEntity;
import com.objectforge.mascot.machine.model.SubsystemEntity.SubsystemRef;
import com.objectforge.mascot.machine.scheduler.MascotThread;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public class GlobalSubsystem extends Subsystem {

    Hashtable devices = new Hashtable();
    Hashtable handlers = new Hashtable();

    //There is only one instance of the global subsystem.
    private static boolean incarnated;

    public GlobalSubsystem() {
        super();
        name = "global";
        globalInstance = true;
        container = null;
        incarnated = true;
        threadGroup = new ThreadGroup("global");
    }

    private void addDevices(Map newDevices) {
        //Create the devices
        for (Iterator i = newDevices.values().iterator(); i.hasNext();) {
            MascotEntities entity = (MascotEntities) i.next();
            Device myDevice = null;
            try {
                myDevice = (Device) entity.getCurrentIncarnation().getInstance();
            } catch (MascotMachineException e) {
                // Note the error
                MascotDebug.println(9, "GlobalSubsystem: cannot get device instance");
            }

            devices.put(entity.getName(), myDevice);
        }
    }

    public static void incarnate() throws MascotMachineException {
        GlobalSubsystem global = EntityStore.getGlobalSubsystem();

        if (incarnated) {
            global.install();
        } else {
            EsGlobalSubsystem esubsystem =
                (EsGlobalSubsystem) EntityStore.getGlobalSubsystem().getEInstance();
            incarnated = true;
            try {
                global.form((SubsystemRef) esubsystem.getParentEntity().referenceFactory("global", null));
            } catch (MascotMachineException e) {
                esubsystem.removeInstance(global);
                throw new MascotMachineException("Form global subsystem: " + e);
            }
            global.subStart();
        }
    }

    /**
     * Method form.
     * essubsystem is an instance of EntityStore.EsSubsystem and describes the entities that
     * make up this subsystme.  Forming the subsystem creates a collection of objects (idas and
     * activities) that can be started.  The logic here is to lookup the named EntityInstances in
     * the EntityStore and get instnaces. If this succeeds, ie. if all of the classes are defined
     * and can be instanced, then the subsystem is ready to start.  At the moment the subsystem is
     * left in this state; a correct Mascot implementation would acitivate (create the threads for)
     * the subsystem and then leave it in a suspended state.
     * @throws MascotMachineException
     */
    public synchronized void form(final EsGlobalSubsystem esubsystem) throws MascotMachineException {

        //do the subsystem form.  This picks up channels, pools, activities, and subsystems.
        super.form((SubsystemRef) esubsystem.getParentEntity().referenceFactory("global", null));

        //Now add devices and handlers
        addDevices(esubsystem.devices);
        incarnated = true;
    }

    /**
     * Installs are specific to the global subsystem and called when there is the possibility
     * that more IDAs, activitiies, or (in particular) devices have been added to the running
     * global subsystem.
     * 
     * @throws MascotMachineException
     */
    public void install() throws MascotMachineException {
        EsGlobalSubsystem esubsystem = (EsGlobalSubsystem) getEInstance();
        GlobalSubsystem global = EntityStore.getGlobalSubsystem();

        MascotDebug.println(9, "Global subsystem install");

        //The idea here add only those things I don't know about already
        //First of all, thumb through the IDAs and devices
        Map myIDAs = notInstalled(esubsystem.idas, null);
        notInstalled(esubsystem.deviceref, myIDAs);
        addIDAs(myIDAs);

        //Deferred device reference install have side effects.  Clean
        //these up
        myIDAs = notInstalled(esubsystem.idas, null);
        addIDAs(myIDAs);

        //...and the activities
        Map myRoots = notInstalled(esubsystem.roots, null);
        for (Iterator i = myRoots.values().iterator(); i.hasNext();) {
            IInstallRecord ref = (IInstallRecord) i.next();
            MascotReferences mref = subsysResolveRef(ref.getReference());
            IRoot myroot = (IRoot) mref.getInstance();
            Activity act =
                new Activity(
                    global,
                    idas,
                    myroot,
                    (Vector) mref.getResources().get(EntityStore.ACTIVITY_ARGS));

            String actName = mref.getName();
            global.activities.put(actName, act);
            act.actStart(actName + act.hashCode());
        }

        //Now that everything is in let's look at subsystems
        Map mySubsystems = notInstalled(esubsystem.subsystems, null);

        for (Iterator i = mySubsystems.values().iterator(); i.hasNext();) {
            IInstallRecord ref = (IInstallRecord) i.next();
            MascotReferences reference = subsysResolveRef(ref.getReference());
            Subsystem subs = EntityStore.formSubsystem((SubsystemEntity.SubsystemRef) reference, this);
            global.containerStart(subs);
        }

    }

    /**
     * @param subs
     */
    private void containerStart(Subsystem subs) {
        GlobalSubsystem global = EntityStore.getGlobalSubsystem();

        subs.subsysSync = global.subsysSync;
        subs.threadGroup = new ThreadGroup(global.threadGroup, instanceName);
        subs.startSync = global.startSync;
        subs.startSignal = global.startSignal;
        Vector startList = new Vector();
        subs.doStart(startList);

        global.subsysSync.cqJoin();
        global.startSignal.cqJoin();
        for (Iterator i = startList.iterator(); i.hasNext();) {
            Object[] toStart = (Object[]) i.next();
            if (!((Activity) toStart[0]).isStarted()) {
                ((Activity) toStart[0]).start((MascotThread) toStart[1]);
                startSignal.cqWait();
            }
        }
        global.startSignal.cqLeave();
        global.subsysSync.cqLeave();
    }

    /**
     * @return
     */
    public Hashtable getDevices() {
        return devices;
    }

}
