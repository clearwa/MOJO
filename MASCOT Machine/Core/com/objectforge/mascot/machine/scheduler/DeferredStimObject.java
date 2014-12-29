/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.scheduler;

/**
 * DeferredStimObjects encapsulate information used by control queues to 
 * implment non-blocking joins.  In essence they provide storage for state
 * information so the caller can receive notification that he has become
 * an owner or received a stim.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 */
public class DeferredStimObject implements DSOInterface {
	protected SOInterface notify;
	protected SOInterface deferred;
	protected Thread thread;
	protected boolean block;
	protected CQEvent myEvent;
	protected boolean threadAlive;
	protected CQListener listener;

	/**
	 * <p>Method DeferredStimObject.
	 * The parameters are as follows:
	 * <ul>
	 * 		<li>boolean block - whether this call will block or not</li>
	 * 		<li>SOInterface notify - used to inform clients of cq ownership changes</li>
	 * 		<li>SOInterface deferred - used to inform clients of cq stims</li>
	 * </ul></p>
	 */
	public DeferredStimObject(boolean block, SOInterface notify, SOInterface deferred) {
		super();
		this.notify = notify;
		this.deferred = deferred;
		this.block = block;
		thread = Thread.currentThread();
		threadAlive = ControlQueue.isDead(thread, false, null);
	}

	/**
	 * Returns the block.field
	 */
	public boolean isBlocked() {
		return block;
	}

	/**
	 * Returns the deferred.field
	 */
	public SOInterface getDeferred() {
		return deferred;
	}

	/**
	 * Returns the notify.field
	 */
	public SOInterface getNotify() {
		return notify;
	}

	/**
	 * Returns the thread.field
	 */
	public Thread getThread() {
		return thread;
	}

}
