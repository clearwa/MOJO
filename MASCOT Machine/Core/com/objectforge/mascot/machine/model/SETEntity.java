/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.objectforge.mascot.machine.estore.EInstance;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.estore.EsSET;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.scheduler.IMascotTransaction;
import com.objectforge.mascot.machine.scheduler.MascotTransactionQueue;

/**
 * SETEntity
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 25-Mar-2004 by @author Clearwa
*/
public class SETEntity extends MascotEntities {
    /*
     * This is a Set of SETReference objects that holds the contents of this set.
     * I don't hold direct object references here because if I remove a refernce from
     * one of the tales in the EntityStore I don't want to trawl through efery set
     * to make sure the refernce diappears in them as well.  The EntityStore tables always
     * exist.
     */
    Map setContents = new HashMap();
    /*
     * SETs contain resources that are global to all members of the set
     */
    Map resources = new Hashtable();
    //Access control
    MascotTransactionQueue gate = new MascotTransactionQueue();

    /**
     * Create a SETEntity object
     * @param name
     * @param className
     */
    public SETEntity(String name, String className) {
        super(name, className);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#addIncarnation()
     */
    protected EntityInstance addIncarnation() throws MascotMachineException {
        EInstance ei = eiInstance();

        //Having created the entity instance now set it as the current
        addIncarnation((EntityInstance) ei);

        //This entity has no class and cannot be instnaced
        ei.setImpClass(null);
        return ei;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#eiInstance()
     */
    public EInstance eiInstance() {
        return new EsSET(this);
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#referenceFactory(java.lang.String, java.util.Map)
     */
    public IMascotReferences referenceFactory(String name, Map resources) {
        return null;
    }

    /**
     * Add a member to the SET.  This creeates and adds a SETReference record
     * @param name
     * @param table
     * @return
     */
    public SETReference addMember(final String name, final Map table, final int typeID)
        throws MascotMachineException {
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                if( setContents.containsKey(name)){
                    return setContents.get(name);
                }
                SETReference ref = new SETReference(name, table, typeID);
                setContents.put(name,ref);
                return ref;
            }
        }
        return (SETReference) gate.doIt(new runner(), null);
    }

    public void removeMember(final SETReference setRef) {
        class runner implements IMascotTransaction {

            /* (non-Javadoc)
             * @see com.objectforge.mascot.machine.scheduler.IMascotTransaction#kernel(java.lang.Object[])
             */
            public Object kernel(Object[] packet) throws MascotMachineException {
                setRef.getTable().remove(setRef.getTag());
                return null;
            }
        }
        gate.cqJoin();
        try {
            if (setContents.containsKey(setRef.getTag())) {
                setContents.remove(setRef.getTag());
                ((EntityStore.DescriptorMap) setRef.getTable()).transaction(new runner(), null);
            }
        } catch (MascotMachineException e) {
        } finally {
            gate.cqLeave();
        }
    }

    /**
     * Resolve a SETReference to an entity
     * @param ref
     * @return
     * @throws MascotMachineException
     */
    public MascotEntities resolve(SETReference ref) throws MascotMachineException {
        gate.cqJoin();
        try {
            if (setContents.containsKey(ref.getTag())) {
                MascotEntities resolved = (MascotEntities) ((SETReference)setContents.get(ref.getTag())).lookup();
                if (resolved != null) {
                    return resolved;
                }
            }
            throw new MascotMachineException("SETEntity<resolve>: cannot resolve reference");
        } finally {
            gate.cqLeave();
        }
    }

    /**
     * Return the entities that make up the SET
     * 
     * @return
     * @throws MascotMachineException
     */
    public Set entities() throws MascotMachineException {
        gate.cqJoin();
        try {
            Set retval = new HashSet();

            for (Iterator i = setContents.values().iterator(); i.hasNext();) {
                retval.add(resolve((SETReference) i.next()));
            }
            return retval;
        } finally {
            gate.cqLeave();
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#toString(java.lang.String)
     */
    public String toString(String spaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(super.toString(spaces));
        spaces += "  ";
        buffer.append("\n" + spaces + "Contents: size = " + setContents.size() + "\n");
        spaces += "  ";
        int counter = 1;
        try {
            for (Iterator i = (new ArrayList(setContents.values())).iterator(); i.hasNext(); counter++) {
                SETReference setRef = (SETReference) i.next();
                buffer.append(spaces + counter + ":  " + setRef.tag + "(");
                try {
                    buffer.append(resolve(setRef).getType());
                } catch (MascotMachineException e) {
                    buffer.append("unresolved");
                }
                buffer.append(")\n");
            }
            return buffer.toString();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getType()
     */
    public String getType() {
        return "SET";
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#merge(java.util.Map.Entry, boolean)
     */
    /**
     * Merge is overrides here because the opertaion needs to make sure the referenced system
     * descriptor tables are correct.  This is handled by the EntityStore.SETMap class; you cannot
     * use the merge here.
     */
    public void merge(Entry entry, boolean replace) throws MascotMachineException {
        throw new MascotMachineException("SETEntity<merge>: You cannot merge SETs this way");
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getTypeID()
     */
    public int getTypeID() {
        return NO_TID;
    }

    /**
     * @return
     */
    public List getSetContents() {
        gate.cqJoin();
        try {
            return new ArrayList(setContents.values());
        } finally {
            gate.cqLeave();
        }
    }

    /**
     * Get the SET's resources
     * @return
     */
    public Map getResources() {
        return resources;
    }

    /**
     * Get the resource at key
     * @return
     */
    public Object getResource(Object key) {
        return (resources.containsKey(key)) ? resources.get(key) : null;
    }

}
