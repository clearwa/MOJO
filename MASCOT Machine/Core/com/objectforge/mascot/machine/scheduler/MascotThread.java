/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.scheduler;

import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.utility.MascotDebug;

/**
* A subclass of threads for the Mascot Machine that adds a boolean to indicate
 * termination.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
  */
public class MascotThread extends Thread {
    protected boolean dead = false;
    protected boolean suspended = false;
    //The allocation tag is unique to each start of the thread
    private Object allocationTag = new Object();
    private static Object gate = new Object();
    //The Activity associated with this thread
    private Activity activity;

    /**
     * Constructor for MascotThread.
     */
    public MascotThread() {
        super();
    }

    /**
     * Constructor for MascotThread.
     */
    public MascotThread(Runnable arg0) {
        super(arg0);
        setActivity(arg0);
    }

    /**
     * Constructor for MascotThread.
     */
    public MascotThread(ThreadGroup arg0, Runnable arg1) {
        super(arg0, arg1);
        setActivity(arg1);
    }

    /**
     * Constructor for MascotThread.
     */
    public MascotThread(String arg0) {
        super(arg0);
    }

    /**
     * Constructor for MascotThread.
     */
    public MascotThread(ThreadGroup arg0, String arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor for MascotThread.
     */
    public MascotThread(Runnable arg0, String arg1) {
        super(arg0, arg1);
        setActivity(arg0);
    }

    /**
     * Constructor for MascotThread.
     */
    public MascotThread(ThreadGroup arg0, Runnable arg1, String arg2) {
        super(arg0, arg1, arg2);
        setActivity(arg1);
    }

    //	**java 1.4.1
    //	/**
    //	 * Constructor for MascotThread.
    //	 */
    //	public MascotThread(ThreadGroup arg0, Runnable arg1, String arg2, long arg3) {
    //		super(arg0, arg1, arg2, arg3);
    //		setActivity(arg1);
    //		EntityStore.mrefs.put(this,this.getClass().getName() + "-" + this.hashCode());
    //	}
    //
    /**
     * A version of yield that checks for terminated threads
     */
    public static void yield() {
        Thread thread;

        synchronized (gate) {
            thread = Thread.currentThread();
        }
        if (Thread.interrupted())
            ControlQueue.isDead(thread, true, null);
        Thread.yield();
    }
    /**
     * Returns the dead.
     */
    public synchronized boolean isDead() {
        return dead;
    }

    /**
     * Returns the suspended.
     */
    public synchronized boolean isSuspended() {
        return suspended;
    }

    /**
     * Sets the dead.
     */
    public synchronized void setDead(boolean dead) {
        this.dead = dead;
        if (dead)
            allocationTag = new Object();
    }

    /**
     * Sets the suspended.
     */
    public synchronized void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    /* (non-Javadoc)
     */
    public synchronized void start() {
        allocationTag = new Object();
        super.start();
    }

    /**
     */
    public synchronized Object getAllocationTag() {
        return allocationTag;
    }

    /**
     * @return
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * @param activity
     */
    public void setActivity(Runnable activity) {
        if (activity instanceof Activity)
            this.activity = (Activity) activity;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        super.finalize();
        MascotDebug.println(11, "+++++++++++++++ Thread finalized +++++++++++++++++");
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // Light off the thread as normal
        super.run();

        //Time to cleanup
        activity = null;
        MascotDebug.println(0, Thread.currentThread().getName() + " terminates");
    }

}
