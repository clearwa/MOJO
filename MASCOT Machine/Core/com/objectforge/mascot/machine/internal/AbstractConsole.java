/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.internal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.scheduler.MascotThread;

/**
 * <p>The Mascot Machine Console.  The machine maintians one instance of this object and it is
 * the interface for interactions with the machine.  It implements for following commands:</p>
 * 
 * <p>enroll<br>
 * Read, parse, and install the entities defined in a XML acp document.  There are 2 forms
 * of this command, one to read fram in InputStream, the second to read directly from a 
 * local file.</p>
 * 
 * <p>form<br>
 * The process of enrolling creates the information needed to instantiate instances 
 * of all the elements defined in a SET.  'Forming' a subsystem creates a concrete instance
 * of the entities that constitute a subsystem and puts the instance in a suspended state.</p>
 * 
 * <p>start<br>
 * Run a previously formed subsystem.</p>
 * 
 * <p>halt<br>
 * Place a subsystem in supended animation.  This means all of the threads that represent 
 * the activities in the systems will be suspended and may have unpredicatable results where
 * a number of cooperating subsystems are involved.</p>
 * 
 * <p>resume<br>
 * Take a previously halted subsystem out of suspended animation and run it.</p>
 * 
 * <p>terminate<br>
 * Tear down and destroy a previously running subsystem.  This operation is tricky where
 * thera are cooperating subsystems since the process needs to ensure that all resources are
 * released.  It is a little unclear to me how to do this at the moment.</p>
 * 
 * <p>destroy (optional, may not be worth the bother)<br>
 * Remove the elements defined by an enrolled SET.  This is clearly dependent on whether
 * the elements are instanced and in use.  I only see a limited use for this command.</p>
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 * 
 */

public abstract class AbstractConsole {
    private Hashtable subsystems = new Hashtable();
    private AbstractConsole mascotConsole = null;

    /**
     * Do a lazy initialization on the mascotConsole variable.  The behaviour is that
     * the first console instance is the default Mascot Machine Console
     */
    public AbstractConsole() {
        super();
        if (mascotConsole == null) {
            mascotConsole = this;
        }
    }

    public Subsystem form(String name) throws MascotMachineException {
        MascotThread thisThread = (MascotThread) Thread.currentThread();
        Subsystem currentSub = thisThread.getActivity().subsystem;

        return form(name, currentSub);
    }

    public Subsystem formHandler(String name) throws MascotMachineException {
        Subsystem subs = EntityStore.formHandler(name);
        return subs;
    }

    public Subsystem form(String name, Subsystem container) throws MascotMachineException {
        Subsystem subs = EntityStore.formSubsystem(name, container);
        return subs;
    }

    public void start(Subsystem subsystem) {
        subsystem.subStart();
    }

    public Enumeration knownSubsystems() {
        return subsystems.keys();
    }

    /**
     * Returns the mascotConsole.
     */
    public AbstractConsole getMascotConsole() {
        return mascotConsole;
    }

    /**
     * Sets the mascotConsole.
     */
    public void setMascotConsole(AbstractConsole mascotConsole) {
        this.mascotConsole = mascotConsole;
    }

    public static void treeWalk(Subsystem sub, String prefix, PrintWriter printer) {
        String iname =
            (sub.container == null) ? sub.getName() : (String) sub.container.getSubsystems().get(sub);
        printer.println(prefix + "Subsystem " + iname);
        for (Enumeration i = sub.getSubsystems().keys(); i.hasMoreElements();) {
            treeWalk((Subsystem) i.nextElement(), "  " + prefix, printer);
        }
    }

    static void doFind(List retval, Subsystem current, String subName) {
        if (current.getName().startsWith(subName)) {
            retval.add(current);
        }
        for (Enumeration i = current.getSubsystems().keys(); i.hasMoreElements();) {
            doFind(retval, (Subsystem) i.nextElement(), subName);
        }
    }

    public static List findSubsystems(String subName) {
        List retval = new ArrayList();
        doFind(retval, EntityStore.getGlobalSubsystem(), subName);
        return retval;
    }

}