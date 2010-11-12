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
 * EsIDA
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.estore
 * Created on 26-Mar-2004 by @author Clearwa
*/
public class EsIDA extends EInstance {

    /**
     * @param name
     * @param aClass
     * @param entity
     */
    public EsIDA(String name, Class aClass, MascotEntities entity) {
        super(name, aClass, entity);
    }

    /**
     * @param entity
     */
    public EsIDA(MascotEntities entity) {
        super(entity);
    }

}
