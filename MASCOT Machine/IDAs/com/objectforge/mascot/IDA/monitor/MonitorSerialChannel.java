/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.monitor;

import com.objectforge.mascot.IDA.SerialChannel;

/*
* Project: MASCOT Machine
* Package: com.objectforge.mascot.IDA.monitor
* Created on 14-May-2003
*/
public class MonitorSerialChannel extends SerialChannel {

	/**
	 * Initialiaze with a capacity.
	 */
	public MonitorSerialChannel( int capacity ) {
		super();
		this.capacity = capacity;
	}
	
	/**
	 * The default capacity is 100
	 */
	public MonitorSerialChannel(){
		this(100);
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.IDA.SerialChannel#addContents(java.lang.Object)
	 * 
	 * This implementation changes the default behaviour so that the channel never
	 * becomes full.
	 */
	protected void addContents(Object contents) {
		//Check whather the channel would become full.  If so, then dumpt the 
		//entry at the head of the vector.
		if (channelContents.size() + 1 >= capacity) {
			channelContents.remove(0);
		}
		channelContents.add(contents);
	}

}
