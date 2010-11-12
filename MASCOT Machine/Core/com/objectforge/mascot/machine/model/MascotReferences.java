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
 * MascotReferences
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 26-Mar-2004 by @author Clearwa
*/
public abstract class MascotReferences implements IMascotReferences{
    String name;
    String reference;
    Map resources;
    
    public MascotReferences( String name, String reference, Map resources){
        this.name = name;
        this.reference = reference;
        this.resources = resources;
    }
    
    public MascotReferences(){
        this( null, null, null );
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getResources()
     */
    public Map getResources() {
        return resources;
    }
    
    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getReftype()
     */
    public abstract int getReftype();

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IMascotReferences#getRefName()
     */
    public String getRefName() {
        return reference;
    }
    
    public String toString(){
        return toString("");
    }

    /**
     * @param spaces
     * @return
     */
    public String toString(String spaces) {
        return spaces + "name = " + name + ", reference = " + reference;
    }

}
