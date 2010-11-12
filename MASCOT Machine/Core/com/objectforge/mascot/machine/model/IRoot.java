/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.model;

import java.util.Vector;

import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;

/**
 * All roots implement this interface.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */

public interface IRoot {
	/**
	 * Method root.
	 * Invoked by the activity framework to plugin a root.  
	 */
	public void mascotRoot(Activity activity, Object[] args);
	/**
	 * Method resumeRoot.
	 * The activity code expects to invoke this method after syspending a subsystem.
	 * Typically, a root does all of it's initialization in the root method and
	 * then calls this method for the real processing.
	 */
	public void resumeRoot() throws Exception;
	/**
	 * Method startRoot.
	 * The first method called by the framework
	 */
	public void startRoot(Activity activity, Object[] args);
	/**
	 * endRoot brackets startRoot and frees any references held in the root
	 *
	 */
	public void endRoot();
	/**
	 * Method printRoot.
	 * Print information about the root
	 */
	public void printRoot();

	/**
	 * Method resolve.
	 * Resolve the connection refernce to a real IDA
	 */
	public Object resolve(Object connectionRef);

	/**
	 * Resolve to the reader or writer side of a device reference
	 */
	public Object resolve(Object connectionRef, String which);

	/**
	 * Method read.
	 * Read from the IDA referenced by idaRef.  The meaning of the returned object is IDA implementation
	 * specific.
	 * @throws MascotMachineException
	 */
	public Object read(Object idaRef) throws MascotMachineException;
	/**
	 * Method write.
	 * Write contnets to the IDA referenced by idaRef.  The meaning of contents is IDA impelmentation
	 * specfic.
	 * @throws MascotMachineException
	 */
	public void write(Object idaRef, Object contents)
		throws MascotMachineException;

	/**
	 * Do a status call on the IDA referenced by idaRef.  The contents of the returned object array is
	 * dependent on the implementation of the IDA.
	 * @throws MascotMachineException
	 */
	public Object[] status(Object idaRef) throws MascotMachineException;

	/**
	 * Some IDAs have a notion of capacity.  Set it if it means anything.
	 * @throws MascotMachineException
	 */
	public void setCapacity(Object idaRef, int capacity)
		throws MascotMachineException;

	//Routines to manipulate devices

	/**
	 * The arg idaRef should resolve to an DeviceRefIDA.  Open assumes this and uses it to
	 * to find the device instance this open referrs to.  The arg connectionRef is the handle by
	 * which the activity that called open will refer to the channels returned by the open call.  The
	 * returned array contains resolved references to the reader and writer channels for the referenced
	 * instance of the device.  These are the channels resolved by the resolve( connectionRef,which )
	 * above.
	 * @throws MascotMachineException
	 */
	public Object[] add(Object idaRef, Object connectionRef)
		throws MascotMachineException;

	/**
	 * Open a connection to a device
	 * @param idaRef
	 * @param connectionRef
	 * @return
	 * @throws MascotMachineException
	 */
	public Object[] open(Object idaRef, Object connectionRef)
		throws MascotMachineException;

	/**
	 * As per open.
	 * @throws MascotMachineException
	 */
	public Object[] close(Object idaRef, Object connectionRef)
		throws MascotMachineException;

	/**
	 * Returns the activity.
	 * Return the activity object associated with this root
	 */
	public Activity getActivity();
	/**
	 * Returns the args.
	 * Return the activity argument array
	 */
	public Object[] getArgs();
	/**
	 * Returns the subsystem.
	 * Return the subsystem in which this root is running
	 */
	public Subsystem getSubsystem();

	/**
	 * Add a worker activity for a root
	 * @param sub
	 * @param root
	 * @param factoryMethod
	 * @param args
	 * @return
	 * @throws MascotMachineException
	 */
	public Object addWorker(
		Subsystem sub,
		IMascotReferences root,
		Vector args)
		throws MascotMachineException;

    /**
     * Add a worker activity for a root that will not reap the associated
     * worker delegate entity
     * @param sub
     * @param root
     * @param factoryMethod
     * @param args
     * @return
     * @throws MascotMachineException
     */
    public Object addWorkerNR(
        Subsystem sub,
        IMascotReferences root,
        Vector args, Vector reap)
        throws MascotMachineException;

	/**
	 * Add a worker activity for a root
	 * @param sub
	 * @param root
	 * @param factoryMethod
	 * @param args
	 * @param tag
	 * @return
	 * @throws MascotMachineException
	 */
	public Object addWorker(
		Subsystem sub,
		IMascotReferences root,
		Vector args,
		String tag)
		throws MascotMachineException;
}
