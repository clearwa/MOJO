/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;

import com.objectforge.mascot.machine.model.MascotEntities;

/**
 * EsSET
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.estore
 * Created on 25-Mar-2004 by @author Clearwa
*/
public class EsSET extends EInstance {

    /**
     * @param name
     * @param aClass
     * @param entity
     */
    public EsSET(String name, Class aClass, MascotEntities entity) {
        super(name, aClass, entity);
    }

    /**
     * @param entity
     */
    public EsSET(MascotEntities entity) {
        super(entity);
    }

}
