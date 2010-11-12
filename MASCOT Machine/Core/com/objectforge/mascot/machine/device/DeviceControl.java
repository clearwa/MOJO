/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.device;

/**
 * Instances of DeviceContol are used to pass contol information through IDAs, typically in classes associated
 * with device handlers.  The interpretation of payload is implementaion dependent.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class DeviceControl {
	public Object payload;
	
	public DeviceControl( Object payload ){
		this.payload = payload;
	}

}
