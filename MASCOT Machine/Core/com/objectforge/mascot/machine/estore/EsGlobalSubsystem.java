/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;

import java.util.Hashtable;
import java.util.Map;

import com.objectforge.mascot.machine.internal.GlobalSubsystem;
import com.objectforge.mascot.machine.internal.IEIAccess;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.model.SubsystemEntity;

public class EsGlobalSubsystem extends EsSubsystem {
    public Map devices = new Hashtable();
    public Map handlers = new Hashtable();

    /**
     */
    public EsGlobalSubsystem(String name, Class aClass, boolean closeOnEmpty) {
        //The global subsystem never terminates when there are no activities in it
        super(
            name,
            aClass,
            false,
            (SubsystemEntity) EntityStore.mascotRepository().subsystemDescriptors().get("global"));
    }

    /**
     * Method merge.
     * 
     * Merge the source with this.  Note that if there are duplicate keys the source wins.
     */
    public void merge(EsGlobalSubsystem destination, boolean replace) {
        gate.cqJoin();
        try {
            destination.devices.putAll(devices);
            destination.handlers.putAll(handlers);
            destination.idas.putAll(idas);
            destination.roots.putAll(roots);
            destination.subsystems.putAll(subsystems);
            destination.deviceref.putAll(deviceref);
        } finally {
            gate.cqLeave();
        }
    }

    Object[][] printContents() {
        Object[][] retval = { { "Channels & Pools", idas },  {
                "Activities", roots }, {
                "Devices", devices }, {
                "Handlers", handlers }, {
                "Subsystems", subsystems }
        };
        return retval;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.internal.EsSubsystem#getInstance(com.objectforge.mascot.machine.internal.Subsystem, java.lang.String)
     */
    public Subsystem getInstance(Subsystem container, String name) {
        GlobalSubsystem newgs = new GlobalSubsystem();
        allocateInstance(newgs);
        ((IEIAccess) newgs).setEInstance(this);
        return newgs;
    }

}