/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.scheduler;

import java.util.Hashtable;
import java.util.Vector;

import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * <p>Control queues are the primary monitor structure in the Mascot Machine.  A control que is used as a guard on 
 * shared resources in the system.</p>
 * 
 * <p>This implementation has both blocking (Mascot traditional) and non-blocking behaviour.  In the
 * traditional case the logic is:</p>
 * 
 * <blockquote><pre>
 * <b>&lt;join the control que&gt;</b>
 *   if &lt;control que not owned&gt;
 * 	    become the owner and return
 *   else
 * 	    added to the waiters list and wait until you become the owner
 *   fi
 * <b>end</b>
 * 
 * <b>&lt;leave the control que&gt;</b>
 *   if <owner>
 * 	    relinquish ownership and install a new owner if the is someone waiting
 *   else
 *	    return non-owner error
 *   fi
 * <b>end</b>
 * 
 * <b>&lt;stim the control que&gt;</b>
 *   release the current ower of the que to run
 * <b?end</b>
 * 
 * <b>&lt;wait on the control que&gt;</b>
 *   if &lt;stim available&gt;
 *     return immediately
 *   else
 *     wait on the stim location
 *   fi
 * <b>end</b>
 *</pre></blockquote>
 * <p>Join, leave, and wait force a scheduling pass (in Java, a yield()).  Stim does not although there
 * may be a pass anyway due to a notify().</p>
 * 
 *<p> The above behaviour is blocking.  The calling thread is blocked as part of the join and blocks 
 * on the wait.  In the non-blocking case the idea is to modify the join behaviour so the caller
 * does not block and inform him when he becomes the owner.  The 
 * <blockquote>
 * 			cqJoin( SOInterface so, CQListener listener)
 * </blockquote>
 * method implements this behaviour as follows:</p>
 * <blockquote><pre> 
 * <b>&lt;join the que&gt;</b>
 *   if &lt;control que not owned&gt;
 *     take ownership
 *       notify the new owner via the listern object if not null
 *       let other tasks know something has happened by releasing anyone hanging on the notify object
 *       return
 *     else
 *       add task to cq waiters
 *       return
 *     fi
 * <b>end</b>
 * </pre></blockquote>
 * <p>Upon return the caller checks whether he owns the que.  If not, he cakks notify.watForRlease() to monitor
 * activity on the notify object and then checks for ownership on each release.  An activity can monitor
 * multiple control queues by passing the same notify object to multiple joins.  See the philosopher example
 * to see how this works.</p>
 * 
 * <p>In the case of a non-blocking join, the leave processing works as follows:</p>
 * <blockquote><pre>
 * <b>&lt;leave the que&gt;</b>
 *   if &lt;owner&gt;
 *     copy the owner reference to oldOwner
 *     find a new owner for the control que.  This essentially invokes the same processing
 *         as in the join above.
 *     inform oldOwner that something has happened if listener was set
 *     let other tasks know something has happend by releasing waiters on the oldOwner's
 *         notify object
 *   else
 *     return non-owner error
 *   fi
 * <b>end</b>
 * </pre></blockquote>
 * <p>Deferred stim processing is not currently implemented.  The idea (when it is fully formed) will be that
 * acitities will wait on a deferred stim object to monitor stims on multiple ques.  They must own the 
 * que for this to work.</p>
 * <blockquote><pre>
 * <b>&lt;stim the cq&gt;</b>
 *   Do an soStim on the deferred stim object
 * <b>end</b>
 * 
 * <b>&lt;wait on the que&gt;</b>
 *   Do a waitForStim on the deferred stim object
 * <b>end</b>
 * </pre></blockquote>
 * <p>All of this code is sensitive to dead threads.  A thread is dead if it is either not-alive, ie. not running
 * in the virtual machine, or dead in the Mascot sense, ie. the activity's subsystem has either been terminated
 *  or suspended.</p>
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */
public class ControlQueue {

    /******************************************************
     * Static constants
     *****************************************************/

    //Statys returns
    public static final int CQ_JOINED = 1;
    //The cq has been joined but the thread is not the owner
    public static final int CQ_OWNED = 2;
    //The current thread owns the control que
    public static final int CQ_STIMMED = 3; //This cq has a stim waiting
    public static final int CQ_WAITING = 4;
    //The current thread is already waiting on the cq
    public static final int CQ_STIM_DELEGATED = 5;
    //Returned by cqWait() if the stim location is delegated

    //Errors
    public static final int CQ_OK = 0; //No error but no info
    public static final int CQ_ERROR = -1; //General error condition
    public static final int CQ_NOT_OWNER = -2; //Not owner
    public static final int CQ_THREAD_DEAD = -3; //Mascot thread was marked as dead
    public static final int CQ_OWNER_NOT_ALIVE = -4; //The owner of the queue is not longer alive

    //Map return codes to strings
    private static Hashtable errorMap;
    static {
        errorMap = new Hashtable();
        errorMap.put(new Integer(CQ_JOINED), "CQ_JOINED");
        errorMap.put(new Integer(CQ_OWNED), "CQ_OWNED");
        errorMap.put(new Integer(CQ_STIMMED), "CQ_STIMMED");
        errorMap.put(new Integer(CQ_WAITING), "CQ_WAITING");
        errorMap.put(new Integer(CQ_STIM_DELEGATED), "CQ_STIM_DELEGATED");
        errorMap.put(new Integer(CQ_OK), "CQ_OK");
        errorMap.put(new Integer(CQ_ERROR), "CQ_ERROR");
        errorMap.put(new Integer(CQ_NOT_OWNER), "CQ_NOT_OWNER");
        errorMap.put(new Integer(CQ_THREAD_DEAD), "CQ_THREAD_DEAD");
        errorMap.put(new Integer(CQ_OWNER_NOT_ALIVE), "CQ_OWNER_NOT_ALIVE");
    }

    /******************************************************
     * Synchronization and other fields
     *****************************************************/

    //Threads joined but not owner
    private volatile Vector waiting = new Vector();
    //An object for stim processing.  The owner waits on this
    //object until a stim arrives
    private volatile Object stim = new Object();
    //Boolean to track a delivered stim
    private volatile boolean stimmed = false;
    //If the stim is delegated this is the object the owner
    //specified on the join
    private volatile StimObject delegatedStim;
    private volatile Thread owner; //Control queue owner
    private volatile DeferredStimObject ownerDso; //the current DSO
    private volatile CQEvent cqe; //For deferred stim processing

    /********************************************************
     * Suspend processing
     ********************************************************/
    private boolean allowSuspended = false;

    /**
     * Returns the allowSuspended.
     */
    public boolean isAllowSuspended() {
        return allowSuspended;
    }

    /**
     * Sets the allowSuspended.
     */
    public void setAllowSuspended(boolean allowSuspended) {
        this.allowSuspended = allowSuspended;
    }

    /*********************************************************
     * 
     * CountingQ
     * 
     *********************************************************/

    /**
     * CountingQ objects count the number of threads waiting on the embedded control queue
     */

    protected class CountingQ implements CountingQI {
        private ControlQueue pendingQ = new ControlQueue();
        private ControlQueue processQ = ControlQueue.this;
        private boolean suspendAllowed;
        private int pending = 0;

        private int doLeave(ControlQueue que) {
            int retval = CQ_OK;
            try {
                retval = que.cqLeave();
            } catch (MascotRuntimeException e) {
                if (!suspendAllowed) {
                    throw new MascotRuntimeException(e.toString());
                }
            }
            return retval;
        }

        /**
         * Method init.
         * Initialiaze a CountingQ
         */
        public void init() {
            pendingQ.cqJoin();
            pending = 0;
            doLeave(pendingQ);
        }

        /**
         * Method que.
         * Return the embedded control queue
         */
        public ControlQueue que() {
            return processQ;
        }

        /**
         * Method join.
         * Join the control queue
         */
        public int join() {
            pendingQ.cqJoin();
            pending++;
            doLeave(pendingQ);

            return processQ.cqJoin();
        }

        /**
         * Method leave.
         * Leave the control queue
         */
        public boolean leave() {
            pendingQ.cqJoin();
            doLeave(processQ);

            pending--;
            boolean retval = pending > 0;
            doLeave(pendingQ);
            return retval;
        }

        /**
         * Method stim.
         * Stim the embedded control queue
         */
        public void stim() {
            processQ.cqStim();
        }

        /**
         * Method waitQ.
         * Wait on the embedded queue
         */
        public int waitQ() {
            return processQ.cqWait();
        }

        /**
         * Method status.
         * Return true if there are threads waiting to become the owner
         */
        public boolean status() {
            pendingQ.cqJoin();
            boolean retval = pending > 0;
            doLeave(pendingQ);

            return retval;
        }

        /* (non-Javadoc)
         * @see com.objectforge.mascot.machine.scheduler.CountingQI#setAllowSuspended(boolean)
         */
        public void setAllowSuspended() {
            processQ.setAllowSuspended(true);
            pendingQ.setAllowSuspended(true);
            suspendAllowed = true;
        }
    }

    /********************************************************
    * Static Methods
    *******************************************************/

    /**
     * Method errorString.
     * Convert a return value to a string for printing/display
     */
    public static String errorString(int errorVal) {
        String translation = (String) errorMap.get(new Integer(errorVal));

        if (translation != null)
            return translation;
        return "UNRECOGNIAZED";
    }

    /**************************************************************
     * Factory methods
     **************************************************************/

    /**
     * Method CountingQFactory.
     * Return an instance of a CountingQ
     */
    public static CountingQ CountingQFactory() {
        return (new ControlQueue()).new CountingQ();
    }

    //Various ways to produce a DeferredStimObject
    /**
     * Method DSOFactory.
     */
    public static DSOInterface DSOFactory(boolean block, SOInterface notify, SOInterface deferred) {
        return new DeferredStimObject(block, notify, deferred);
    }

    /**
     * Method DSOFactory.
     */
    public static DSOInterface DSOFactory(boolean block, SOInterface notify) {
        return new DeferredStimObject(block, notify, null);
    }

    /**
     * Method DSOFactory.
     */
    public static DSOInterface DSOFactory(boolean block) {
        return new DeferredStimObject(block, null, null);
    }

    /**
     * Method SOFactory.
     * Return an new StimObject
     */
    public static SOInterface SOFactory() {
        return new StimObject();
    }

    /************************************************************
     * JOIN
     ***********************************************************/

    /**
     * Method cqJoin.
     * Blocking (MASCOT traditional) Join
     */
    public int cqJoin() {
        return cqJoin(DSOFactory(true));
    }

    /**
     * Method cqJoin.
     * MASCOT 2002 non-blocking join.  Thread waiting on notify will be informed when queue ownership
     * changes.
     */
    public int cqJoin(SOInterface notify) {
        return cqJoin(DSOFactory(false, notify, null));
    }

    public int cqJoin(SOInterface notify, CQListener listener) {
        DeferredStimObject myDso = (DeferredStimObject) DSOFactory(false, notify, null);
        myDso.listener = listener;
        return cqJoin(myDso);
    }

    /**
     * Method cqJoin.
     * The actual join
     */
    protected int cqJoin(DSOInterface dso) {
        DeferredStimObject myDso = (DeferredStimObject) dso;
        int retval = CQ_ERROR;

        //Check whether the client is terminated.  If so, take a runtime error
        if (isDead(myDso.thread, (myDso.thread == Thread.currentThread()), this)) {
            myDso.thread.interrupt();
            retval = CQ_THREAD_DEAD;
        } else {
            synchronized (waiting) {

                retval = doCqJoin(myDso);
                switch (retval) {
                    case CQ_WAITING :
                    case CQ_JOINED :
                        if (myDso.block) {
                            //In the event of blocking call wait here
                            while (true) {
                                if (isOwner())
                                    break;
                                try {
                                    waiting.wait();
                                } catch (InterruptedException e) {
                                    isDead(Thread.currentThread(), true, this);
                                }
                            }
                        }
                        break;

                    case CQ_ERROR :
                        break;

                    case CQ_THREAD_DEAD :
                        //Tell someone about thread death
                        throw new MascotRuntimeException("Thread dead " + myDso.thread.getName());

                    default :
                        break;
                }
            }
        }
        MascotThread.yield();
        return retval;
    }

    /**
     * Method doCqJoin.
     * The heart of the join processing.  If blocking and can become owner the current thread
     * is suspended.  If non-blocking and not owner then an event is created to allow later
     * notification.
     */
    private synchronized int doCqJoin(DeferredStimObject myDso) {
        int retval = CQ_ERROR;

        //If the owner is dead then kick him out
        if (isDead(owner, false, this)) {
            //Deliver an extra stim and then kick the old owner out
            cqStim();
            owner.interrupt();
            owner = null;
        }

        //If the new owner is dead declare it
        if (isDead(myDso.thread, false, this)) {
            return (myDso.thread.isAlive()) ? CQ_THREAD_DEAD : CQ_OWNER_NOT_ALIVE;
        }

        //Check whether I already own the que or no one owns it
        if (owner == null || myDso.thread == owner) {
            //No owner or I am the owner, take the que for me
            owner = myDso.thread;
            retval = CQ_OWNED;
        } else { //There is an owner - I need to wait
            if (!waiting.contains(myDso)) {
                //If I'm not already waiting on this thing
                waiting.add(myDso);
                retval = CQ_JOINED;
            } else
                retval = CQ_WAITING;
        }
        switch (retval) {
            case CQ_OWNED :
                delegatedStim = null;
                ownerDso = myDso;
                //Now that I have the que tell the caller if he's interested
                if (myDso.listener != null) {
                    myDso.listener.inform(this);
                }
                if (!myDso.block) {
                    if (myDso.notify != null) {
                        delegatedStim = (StimObject) myDso.deferred;
                        if (delegatedStim != null) {
                            cqe = new CQEvent(this);
                            delegatedStim.addEvent(cqe);
                        }
                        //Now that I have the resource, release anyone who cares
                        synchronized (myDso.notify) {
                            myDso.notify.soRelease();
                        }
                    } else
                        retval = CQ_ERROR;
                }
                break;
            case CQ_JOINED :
                break;

            case CQ_WAITING :
                break;

            default :
                break;
        }
        return retval;
    }

    /*********************************************************************
     * LEAVE
     ********************************************************************/

    /**
     * Method cqLeave.
     * Relinquish control of a control queue
     */
    public int cqLeave() {
        int retval;

        synchronized (waiting) {
            retval = doCqLeave();
            switch (retval) {
                case CQ_NOT_OWNER :
                    //Not the owner - this is an error
                    break;

                case CQ_OWNED :
                    //Give the new owner a poke on the way out
                    owner.interrupt();
                    retval = CQ_OK;
                    break;

                default :
                    //In all other cases tell anyone waiting that something has happened
                    waiting.notifyAll();
                    retval = CQ_OK;
                    break;
            }
        }
        //If the client is dead then kill him off
        //		isDead(true);
        MascotThread.yield();
        return retval;
    }

    /**
     * Method doCqLeave.
     * Leave processing.  Thumbs through the waiting vector to find a new owner for
     * the control queue if there is one.  A new owner's validity is determined
     * by doCqJion().  Takes care not to add dead threads.
     */
    private synchronized int doCqLeave() {
        DeferredStimObject released = ownerDso;
        int retval = CQ_OK;

        //In all cases this thread is no longer the owner so remove his allocation tag
        allocTags.remove(Thread.currentThread());
        //If the owner is dead then kick him out
        if (isDead(owner, false, this)) {
            //Deliver an extra stim and then kick the old owner out
            cqStim();
            owner.interrupt();
        } else if (owner != null && !isOwner())
            //If not the owner then simply exit
            return CQ_NOT_OWNER;

        //Before finding a new owner remove my deferred stim event if there is one
        if (delegatedStim != null) {
            delegatedStim.removeEvent(cqe);
        }
        cqe = null;
        owner = null;

        if (waiting.size() > 0) {
            DeferredStimObject newowner = null;

            outer : while (true) {
                synchronized (waiting) {
                    if (waiting.size() > 0) {
                        newowner = (DeferredStimObject) waiting.remove(0);
                    } else {
                        newowner = null;
                    }
                }
                if (newowner == null) {
                    retval = CQ_OK;
                    break;
                }
                switch ((retval = doCqJoin(newowner))) {
                    case CQ_THREAD_DEAD :
                    case CQ_OWNER_NOT_ALIVE :
                        continue;
                    default :
                        allocTags.remove(Thread.currentThread());
                        break outer;
                }
            }
        }
        if (released != null) {
            if (released.listener != null) {
                released.listener.inform(this);
            }
            if (released.notify != null) {
                released.notify.soRelease();
            }
        }
        return retval;
    }

    /********************************************************************
     * STIM
     *******************************************************************/

    /**
     * Method doStim.
     * The actual stim processing.  Takes no notice of delegation.
     */
    protected void doStim() {
        synchronized (stim) {
            if (!stimmed) {
                stimmed = true;
                stim.notify();
            }
        }
    }

    /**
     * Method cqStim.
     * Deliver a stim to a control queue.  Note that the stim does not force a yield
     */
    public void cqStim() {
        if (delegatedStim != null) {
            delegatedStim.soStim();
            return;
        }
        doStim();
    }

    /*********************************************************************
     * WAIT
     ********************************************************************/
    /**
     * Method doWait.
     * The actual wait porocessing.  Take no notice of delegation.
     */
    protected void doWait() {
        synchronized (stim) {
            if (!stimmed) {
                try {
                    stim.wait();
                } catch (InterruptedException e) {
                    isDead(Thread.currentThread(), true, this);
                }
            }
            stimmed = false; //Eat the stim if there is one
        }
    }

    /**
     * Method cqWait.
     * Wait on a control queue
     */
    public int cqWait() {
        int retval = CQ_ERROR;

        //if either the owner or client is dead then turn this into a cqLeave call
        isDead(Thread.currentThread(), true, this);

        if (isDead(owner, true, this)) {
            doCqLeave();
        }

        if (isOwner()) {
            retval = CQ_OK;
            if (delegatedStim != null) {
                delegatedStim.waitForStim();
                return retval;
            } else {
                doWait();
            }
        } else
            retval = CQ_NOT_OWNER;
        MascotThread.yield();
        return retval;
    }

    /**************************************************************************
     * Utility methods
     *************************************************************************/

    /**
     * Method isOwner.
     * True if the current thread is the owner of the control queue
     */
    public boolean isOwner() {
        return isOwner(Thread.currentThread());
    }

    /**
     * Method isOwner.
     * Return true if Thread check is the owner
     */
    public synchronized boolean isOwner(Thread check) {
        return check == owner;
    }

    //Various versions of determinig whether a MascotThread is dead.  Terminating an activity
    //marks its thread as dead but does not kill it off since that might well lead to hanging
    //resources.  These methods look at the dead flag and act accordingly.

    /**
     * Method isDead.
     * If the current thread is a MascotThread and dead then throw a MascotRuntimeException,
     * otherwise return false.
     */
    protected static boolean isDead() {
        return isDead(Thread.currentThread(), true, null);
    }

    /**
     * Method isDead.
     * If the current thread is a MascotThread then determin if it is dead.  Behavious is controlled
     * by the state of throwit.  If true, then throw a MascotRuntimeError if the thread is dead. 
     * otherwise return true/false.
     */
    protected static boolean isDead(boolean throwit) {
        return isDead(Thread.currentThread(), throwit, null);
    }

    /**
     * Method isDead.
     * If the thread aThread is a MascotThread then determin if it is dead.  Behavious is controlled
     * by the state of throwit.  If true, then throw a MascotRuntimeError if the thread is dead. 
     * otherwise return true/false.
     */
    protected static boolean isDead(Thread aThread, boolean throwit, ControlQueue cq) {
        String errorString = null;
        boolean dead = false;

        if (aThread != null) {
            if (cq != null) {
                cq.checkAllocation(aThread);
            }

            if (!aThread.isAlive()) {
                errorString = "Thread not alive ";
            } else if (aThread instanceof MascotThread && ((MascotThread) aThread).dead) {
                dead = true;
                errorString = "Dead thread ";
            }
            if (dead && cq != null && cq.isAllowSuspended() && ((MascotThread) aThread).suspended)
                errorString = null;

            if (throwit && errorString != null) {
                //				cq.allocTags.remove(Thread.currentThread());
                Thread.interrupted();
                throw new MascotRuntimeException(errorString + aThread.getName());
            }
            return errorString != null;
        }
        return false;
    }

    //A Hashtable to hold the allocation tags as they were when a MascotThread entered this cq
    private Hashtable allocTags = new Hashtable();

    protected void checkAllocation(Thread aThread) {
        //		if (aThread instanceof MascotThread) {
        //			if (allocTags.contains(aThread) && allocTags.get(aThread) != ((MascotThread) aThread).getAllocationTag()) {
        //				MascotDebug.println(9,"*** MascotThread allocation tag conflict ***");
        //			} else {
        //				allocTags.put(aThread, ((MascotThread) aThread).getAllocationTag());
        //			}
        //		}
    }

    /**
     * @return
     */
    public Thread getOwner() {
        return owner;
    }

}