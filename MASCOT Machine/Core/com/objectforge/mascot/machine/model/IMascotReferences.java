/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.model;

import java.util.Map;

import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * IMascotReferences
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 26-Mar-2004 by @author Clearwa
*/
public interface IMascotReferences {
    //Define the reference types
    public final int ACTIVITY_REF = 1;
    public final int DEVICE_REF = 2;
    public final int SUBSYSTEM_REF = 3;
    public final int ARGUMENT_IDA_REF = 4;
    public final int CONTAINER_IDA_REF = 5;
    public final int DEVICE_IDA_REF = 6;
    public final int GLOBAL_IDA_REF = 7;
    public final int LOCAL_IDA_REF = 8;
    
    String getName();
    String getRefName();
    MascotEntities getReference() throws MascotMachineException;
    EntityInstance getIncarnation() throws MascotMachineException;
    Object getInstance() throws MascotMachineException;
    Map getResources();
    int getReftype();
}
