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
 * EsActivity
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.estore
 * Created on 25-Mar-2004 by @author Clearwa
*/
public class EsActivity extends EInstance {

    /**
     * @param entity
     */
    public EsActivity(MascotEntities entity) {
        super(entity);
    }

    /**
     * @param name
     * @param aClass
     * @param entity
     */
    public EsActivity(String name, Class aClass, MascotEntities entity) {
        super(name, aClass, entity);
    }

}
