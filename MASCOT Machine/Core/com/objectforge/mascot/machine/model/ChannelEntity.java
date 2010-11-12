/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.model;



/**
 * A channel entity holds the information necessary to instantiate a channel.  See MascotEntities
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */

public class ChannelEntity extends IDAEntities{
	/**
	 * @param name
	 * @param root
	 * @param factoryMethod
	 * @param qualifiedName
	 */
	public ChannelEntity(String name, Object root, String factoryMethod, String type) {
		super(name, root, factoryMethod, type);
	}

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.MascotEntities#getTypeID()
     */
    public int getTypeID() {
        return CHANNEL_TID; 
    }

}
