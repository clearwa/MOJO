/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/
package com.objectforge.mascot.machine.idas;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.model.IDevice;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 */
public class DeviceRefIDA extends AbstractIDA implements IDevice {
	private Object targetDevice;
	private Object instanceName;

	public DeviceRefIDA() {
		super();
	}

	/**
	 * @param localName
	 * @param target
	 */
	public DeviceRefIDA(Object localName, Object target) {
		super();
		targetDevice = target;
		instanceName = localName;
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.IDevice#add(java.lang.Object)
	 */
	public Object[] add(Object ref) throws IDAException {
			return (Object[]) Device.createInstance(targetDevice, instanceName);
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.IDevice#open(java.lang.Object)
	 */
	public Object[] open(Object ref) throws IDAException{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.IDevice#close(java.lang.Object)
	 */
	public Object[] close(Object ref) throws IDAException{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.IDevice#status(java.lang.Object)
	 */
	public Object[] status(Object ref) throws IDAException{
		return null;
	}

}
