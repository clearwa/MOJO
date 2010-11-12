/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

/*
 * Created on 07-Mar-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.objectforge.mascot.machine.model;

import java.lang.reflect.Method;

import com.objectforge.mascot.machine.estore.EInstance;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EsDeviceIDARef;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public abstract class MEInstanced extends MascotEntities implements MEDealloc {
    //If I have a factory method then these apply
    //The instance used as the target for the factory method
    public Object factoryInstance;
    //The factory method
    public String factoryMethod;
    public Method factoryMethodInstance;
    boolean mustDeallocate = false;
    String qualifiedName;

    /**
     * @param name
     * @param root
     * @param factoryMethod
     * @param qualifiedName
     */
    public MEInstanced(String name, Object root, String factoryMethod) {
        super();
        qualifiedName = name;
        if (root == null || name == null)
            throw new MascotRuntimeException("MEInstanced(new): Either root or name is null");

        setName(name);
        if (root instanceof String) {
            if (factoryMethod == null || factoryMethod.equals("")) {
                setClassName((String) root);
                return;
            }
            try {
                factoryInstance = Class.forName((String) root);
            } catch (ClassNotFoundException e) {
                throw new MascotRuntimeException("MEInstanced(new): " + e);
            }
        } else {
            factoryInstance = root;
        }
        if (factoryMethod != null) {
            this.factoryMethod = factoryMethod;
        }
        setClassName(makeNameWithFactory(this));
        name = this.getClassName();
    }

    public void doDeallocate(Object anInstance) {
        if (mustDeallocate && currentIncarnation != null) {
            incarnations.remove(currentIncarnation);
            currentIncarnation = null;
            factoryInstance = null;
        }
    }

    /**
     */
    public MEInstanced(String name, String className) {
        super(name, className);
    }

    //The default constructor
    public MEInstanced() {
    }

    public static String makeNameWithFactory(MascotEntities activity) {
        if (activity instanceof ActivityEntity) {
            ActivityEntity act1 = (ActivityEntity) activity;

            if (act1.factoryInstance != null) {
                return act1.factoryInstance.getClass().getName() + "-" + act1.factoryInstance.hashCode();
            }
        }
        return activity.uniqueName;
    }

    public static String makeUniqueName(String prefix, String name, Object root, String factoryMethod) {
        if (root instanceof String) {
            return MascotEntities.makeUniqueName(name, (String) root);
        }
        if (root != null) {
            return name + "-" + root.hashCode();
        }
        return prefix + name;
    }

    public Object getImplementationClass() throws MascotMachineException {
        EInstance.FactoryInstance parcel = EInstance.factoryInstanceFactory(this.getCurrentIncarnation());

        if (factoryInstance == null) {
            parcel.factoryInstance = super.getImplementationClass();
            parcel.factoryMethodName = "Constructor";
            parcel.factoryMethod = null;
        } else {
            parcel.factoryInstance = factoryInstance;

            if (factoryMethod == null) {
                parcel.factoryMethodName = "Constructor";
                parcel.factoryMethod = null;
            } else {
                parcel.factoryMethodName = factoryMethod;
                try {
                    if (factoryInstance instanceof Class) {
                        parcel.factoryMethod =
                            ((Class) factoryInstance).getMethod(factoryMethod, (new Class[0]));
                    } else {
                        parcel.factoryMethod =
                            factoryInstance.getClass().getMethod(factoryMethod, (new Class[0]));
                    }
                } catch (SecurityException e) {
                    throw new MascotMachineException("ActivityEntity: " + e);
                } catch (NoSuchMethodException e) {
                    throw new MascotMachineException("ActivityEntity: " + e);
                }
            }
        }

        return parcel;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#addIncarnation()
     */
    protected EntityInstance addIncarnation() throws MascotMachineException {
        EInstance ei = eiInstance();

        //Having created the entity instance now set it as the current except in the case
        //of a device ida reference.  In this case the 'class' is a referrs to a device
        //entity
        addIncarnation((EntityInstance) ei);

        if (!(ei instanceof EsDeviceIDARef)) {
            ei.setImpClass(getImplementationClass());
        }
        return ei;
    }

}
