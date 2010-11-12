/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.estore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;

import com.objectforge.mascot.machine.internal.IEIAccess;
import com.objectforge.mascot.machine.internal.MascotAlloc;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.MEDealloc;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.machine.scheduler.MascotTransactionQueue;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * EInstance is the concrete implementation of the EntityInstance interface.  EInstances hold
 * information on how to manufacture instances of the various entities held in the global
 * EntityStore.  The objects also hold references to all manufactured instances.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public abstract class EInstance implements EntityInstance {
    protected volatile String className;
    protected volatile Object classClass;
    protected volatile Hashtable instances;
    protected MascotEntities parentEntity;

    //Access control for incarntaions
    MascotTransactionQueue gate = new MascotTransactionQueue();
    //Acess control for the instances hashtable
    private MascotTransactionQueue alloGate = new MascotTransactionQueue();

    public class FactoryInstance {
        public Object factoryInstance;
        public String factoryMethodName;
        public Method factoryMethod; //either a method or a constructor
    }

    public static FactoryInstance factoryInstanceFactory(EntityInstance owner) {
        return ((EInstance) owner).new FactoryInstance();
    }

    public EInstance(String name, Class aClass, MascotEntities entity) {
        super();
        className = name;
        classClass = aClass;
        instances = new Hashtable();
        parentEntity = entity;
    }

    public EInstance(MascotEntities entity) {
        this(entity.getName(), null, entity);
    }

    public MascotEntities getParentEntity() {
        return parentEntity;
    }

    /**
     */
    public Class getImpClass() {
        return (Class) classClass;
    }

    /**
     */
    public String getClassName() {
        return className;
    }

    /**
     */
    public void setImpClass(Object classClass) {
        this.classClass = classClass;
    }

    /**
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Return the instances hashtable
     */
    public Hashtable getInstances() {
        return instances;
    }

    public void allocate(Object key) {
        InstanceRecord record = (InstanceRecord) instances.get(key);
        if (record != null) {
            record.allocate();
        }
    }

    private InstanceRecord reallocate(InstanceRecord record) throws MascotRuntimeException {
        alloGate.cqJoin();
        try {
            MascotAlloc theObject = (MascotAlloc) record.getAnInstance();
            if (theObject.verify()) {
                instances.remove(record.getAnInstance());
                record.setAnInstance(null);
                return record;
            }
            throw new MascotRuntimeException("EInstance:nothing to reallocate");
        } finally {
            alloGate.cqLeave();
        }
    }

    public InstanceRecord findUnallocatedInstanceRecord() {
        alloGate.cqJoin();
        try {
            for (Enumeration i = instances.elements(); i.hasMoreElements();) {
                InstanceRecord inst = (InstanceRecord) i.nextElement();

                if (!inst.isAllocated())
                    try {
                        return reallocate(inst);
                    } catch (MascotRuntimeException e1) {
                        //The reallocate has failed, create a new one
                    }
            }
            return new InstanceRecord(null);
        } finally {
            alloGate.cqLeave();
        }
    }

    /**
     */
    public Object getInstance() throws MascotMachineException {
        if ((className == null || classClass == null) && !(this instanceof EsSubsystem)) {
            throw new MascotMachineException("EInstance: Class not defined");
        }

        Object anInstance;
        try {
            if (!(classClass instanceof FactoryInstance)) {
                anInstance = ((Class) classClass).newInstance();
            } else {
                FactoryInstance fs = (FactoryInstance) classClass;

                if (fs.factoryMethod == null) {
                    anInstance = ((Class) fs.factoryInstance).newInstance();
                } else {
                    anInstance = fs.factoryMethod.invoke(fs.factoryInstance, (new Object[0]));
                }
            }
        } catch (IllegalArgumentException e) {
            throw new MascotMachineException("EInstance: " + e);
        } catch (IllegalAccessException e) {
            throw new MascotMachineException("EInstance: " + e);
        } catch (InvocationTargetException e) {
            throw new MascotMachineException("EInstance: " + e);
        } catch (InstantiationException e) {
            throw new MascotMachineException("EInstance: " + e);
        }
        allocateInstance(anInstance);
        ((IEIAccess) anInstance).setEInstance(this);
        return anInstance;
    }

    protected void allocateInstance(Object anInstance) {
        try {
            InstanceRecord ir = new InstanceRecord(anInstance);
            instances.put(anInstance, ir);
        } finally {
            alloGate.cqLeave();
        }
    }

    /**
     */
    public boolean containsInstance(Object compare) {
        return (compare == null) ? false : instances.containsKey(compare);
    }

    public String toString(final String prefix) {
        alloGate.cqJoin();
        try {
            StringWriter buffer = new StringWriter();
            PrintWriter writer = new PrintWriter(buffer, true);

            String first =
                prefix
                    + "EInstance: name=\""
                    + className
                    + "\", class is "
                    + ((classClass == null) ? "null" : "valid");

            writer.println(first + ", number of instances = " + instances.size());
            for (Enumeration i = instances.elements(); i.hasMoreElements();) {
                writer.println(prefix + "  " + (InstanceRecord) i.nextElement());
            }
            return buffer.toString();
        } finally {
            alloGate.cqLeave();
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.estore.EntityInstance#deallocate(java.lang.Object)
     * 
     * Use the underlying sychronization mechanism here because the thread is likely to
     * be dead.
     */
    public synchronized void deallocate(Object anInstance) {
        instances.remove(anInstance);
        if (parentEntity instanceof MEDealloc) {
            ((MEDealloc) parentEntity).doDeallocate(anInstance);
        }
    }
}
