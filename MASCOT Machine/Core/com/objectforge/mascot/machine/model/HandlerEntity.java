/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


/*
 * Created on 27-Feb-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.objectforge.mascot.machine.model;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public class HandlerEntity extends SubsystemEntity {

    /**
     * @param name
     * @param object
     * @param closeOnExit
     */
    public HandlerEntity(String name, Object object, boolean closeOnExit) {
        super(name, object, closeOnExit);
    }

	/**
	 */
	public HandlerEntity(String name, Object object) {
		super(name, object);
	}

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getType()
     */
    public String getType() {
        return "handler";
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getTypeID()
     */
    public int getTypeID() {
        return HANDLER_TID;
    }

}
