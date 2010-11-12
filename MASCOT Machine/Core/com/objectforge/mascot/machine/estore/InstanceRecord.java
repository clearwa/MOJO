/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;

import com.objectforge.mascot.utility.MascotDebug;


public class InstanceRecord{
	private boolean allocated;
	private Object anInstance;
	
	public InstanceRecord(Object anObject ){
		allocated = true;
		anInstance = anObject;
	}
	
	/**
	 * Test the allocation flag
	 * @return
	 */
	public boolean isAllocated() {
		return allocated;
	}

	/**
	 * Return the contained instance
	 * @return
	 */
	public Object getAnInstance() {
		return anInstance;
	}

	/**
	 * Set the allocation flag
	 */
	public void allocate() {
		allocated = true;
	}
	/**
	 * Unset the allocation flag
	 */
	public void deallocate(){
		allocated = false;
	}

	/**
	 * Set the internal instance object
	 * @param object
	 */
	public void setAnInstance(Object object) {
		anInstance = object;
	}

	public String toString(){
		return anInstance.getClass().getName() + "-" + anInstance.hashCode() + (allocated?"(allocated)":"(unallocated)");
	}

	/**
	 * @param anInstance
	 */
	public void setAndAllocate(Object object) {
		allocated = true;
		anInstance = object;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		MascotDebug.println(11,"----- Instance record finalized ------");
	}

}