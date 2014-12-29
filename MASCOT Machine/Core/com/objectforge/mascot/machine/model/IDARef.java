/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.model;

import java.util.Map;

/**
 * IDARef
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 30-Mar-2004 by @author Clearwa
*/
public abstract class IDARef extends MascotReferences {
    /*
     * This class is the superclass of all IDA reference objects
     */

    /**
     * @param name
     * @param resources
     */
    public IDARef(String name, String reference, Map resources) {
        super( name, reference, resources );
    }
    
    abstract String getScope();
}
