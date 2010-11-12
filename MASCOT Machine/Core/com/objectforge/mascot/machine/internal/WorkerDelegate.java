/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

/*
 * Created on 06-Mar-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.objectforge.mascot.machine.internal;

import java.util.Vector;

import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.IRoot;

/**
 * The WorkerDelegate is a container for worker activities.  There is only one instahce in the
 * global store and it cannot be instantiated
  * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
*/
public class WorkerDelegate implements IEIAccess {
    private EntityInstance workers;
    String name;
    boolean globalInstance;
    Object container;

    /**
     * Create the single instance of a worker subsystem.
     */
    public WorkerDelegate() {
        super();
        name = "worker";
        globalInstance = true;
        container = null;
    }

    public EntityInstance getEInstance() {
        return workers;
    }

    public void setEInstance(EntityInstance anInstance) {
        workers = anInstance;
    }

    /**
     * @throws MascotMachineException
     */
    public WorkerDelegate(Subsystem container, String name) throws MascotMachineException {
        throw new MascotMachineException("WorkerSubsystem: worker subsystems cannot be instanced");
    }

    /**
     * Add a worker activity to a subsystem
     * @throws MascotMachineException
     */
    public static Object addWorker(Subsystem sub, IMascotReferences root, Vector args, Vector reapers)
        throws MascotMachineException {
        String name = EntityStore.rootName(root) + "-" + root.hashCode();
        return addWorker(sub, name, root, args, reapers);
    }

    protected static Object addWorker(
        Subsystem sub,
        String name,
        IMascotReferences root,
        Vector args,
        Vector reapers)
        throws MascotMachineException {

        //Create the activities
        IRoot myroot = (IRoot) root.getInstance();
        Activity myactivity = new Activity(sub, sub.idas, myroot, args);
        sub.activities.put(myroot, myactivity);

        //Get the MascotEntity that produced this instance
        if (reapers != null) {
            reapers.add( root.getReference() );
        }
        return myactivity;
    }

    public static Object addWorker(
        Subsystem sub,
        IMascotReferences root,
        Vector args,
        String tag,
        Vector reapers)
        throws MascotMachineException {
        String name = EntityStore.rootName(root) + "-" + tag + "-" + root.hashCode();
        return addWorker(sub, name, root, args, reapers);
    }
}
