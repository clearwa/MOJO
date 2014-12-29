/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.estore;

import java.util.Hashtable;

import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.MascotEntities;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 * An object that represents the insatnce of a Mascot Machine Entity.  Objects implemting this
 * interface hold information for the Mascot Macine relating to Entity implementation classes.
 * It is incumbent on these objects to maintian information relating to class instances since
 * these need to be cleaned up when a subsystem is terminated.
 */
public interface EntityInstance {
	/**
	 * Method getClassName.
	 * Get this instances classaname.
	 */
	public String getClassName();
	
	/**
	 * Method setClassName.
	 * Set the class name to name
	 */
	public void setClassName(String name);
	
	/**
	 */
	public Class getImpClass();
	
	/**
	 */
	public void setImpClass(Object aClass);
	
	/**
	 * Method getInstance.
	 * This is a factory method that returns new instance of the class identified
	 * in classname.  Throws MascotMachineException if the class cannot be instanced.
	 */
	public Object getInstance() throws MascotMachineException;
	
	/**
	 */
	public boolean containsInstance(Object compare);

	/**
	 * Method toString.
	 * Print a representation appended to prefix
	 */
	public String toString(String prefix);

	/**
	 */
	public String toString();
	/**
	 * Return a reference to the MascotEntity that crated this EInstance
	* Project: MASCOT Machine
	* Package: com.objectforge.mascot.machine.internal
	* Created on 31-May-2003
	 */
	public MascotEntities getParentEntity();
	/**
	 * Get the hastable of instances
	* Project: MASCOT Machine
	* Package: com.objectforge.mascot.machine.internal
	* Created on 31-May-2003
	 */
	public Hashtable getInstances();
	/**
	 * Do deallocation
	* Project: MASCOT Machine
	* Package: com.objectforge.mascot.machine.internal
	* Created on 01-Jun-2003
	 */
	public void deallocate(Object anInstance);
}
