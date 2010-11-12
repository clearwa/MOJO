/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.scheduler;

/**
 * 
 * Container for parameters of interest - filled in depending on the constructor.
 * CQEvents are held by StimObjects.
  * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
*/
public class CQEvent {
	public int id;
	public ControlQueue cq;
	public Thread thread;
	private static int idCounter = 0;

	public CQEvent(int id, ControlQueue cq) {
		super();
		this.id = id;
		this.cq = cq;
		thread = Thread.currentThread();
	}

	public CQEvent(ControlQueue cq) {
		super();
		thread = Thread.currentThread();
		this.cq = cq;
		synchronized (this) {
			id = ++idCounter;
		}
	}

	public CQEvent(Thread thread) {
		super();
		this.thread = thread;
		synchronized (this) {
			this.id = ++idCounter;
		}
	}

	public CQEvent() {
		super();
		thread = Thread.currentThread();
		synchronized (this) {
			this.id = ++idCounter;
		}
	}
}
