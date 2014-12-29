/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.idas;

import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.internal.IEIAccess;
import com.objectforge.mascot.machine.internal.MascotAlloc;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * The root object for IDA implementations.  All IDAs are subclasses of this object
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public abstract class AbstractIDA implements IIDA, Cloneable, MascotAlloc, IEIAccess {
	private EntityInstance eInstance;
	
	public AbstractIDA(){
		super();
	}
	
	public boolean verify(){
		return true;
	}
	
	/**
	 */
	public Object read() throws IDAException{
		MascotDebug.println(9, "IDA read - idaRef " + this);
		return null;
	}

	/**
	 */
	public void write(Object contents) throws IDAException {
		MascotDebug.println(9, "IDA write - idaRef " + this +"\n\tcontents " + contents);
	}

	/**
	 */
	public Object[] status() {
		MascotDebug.println(9, "IDA status - idaref " + this);
		return null;
	}
	
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		MascotDebug.println(11,"-------------- IDA finalized -------------------");
	}
    
	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.internal.IEIAccess#getEInstance()
	 */
	public EntityInstance getEInstance() {
		return eInstance;
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.internal.IEIAccess#setEInstance(com.objectforge.mascot.machine.internal.EntityInstance)
	 */
	public void setEInstance(EntityInstance anInstance) {
		eInstance = anInstance;
	}

}
