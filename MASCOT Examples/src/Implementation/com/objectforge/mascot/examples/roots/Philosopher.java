/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.examples.roots;

import com.objectforge.mascot.IDA.SPElement;
import com.objectforge.mascot.IDA.StatusPool;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.machine.scheduler.CQListener;
import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.SOInterface;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.3 $
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Philosopher extends AbstractRoot {
	String id;
	Object[] args;
	Integer index;

	public static final int PH_SLEEPS = 0;
	public static final int PH_THINKS = 0;
	public static final int PH_HUNGRY = 1;
	public static final int PH_HAS_RIGHT = 2;
	public static final int PH_HAS_LEFT = 3;
	public static final int PH_EATS = 4;

	public static final int PH_ID = 0;
	public static final int PH_MESSAGE = 1;
	public static final int PH_INDEX = 2;
	public static final int PH_STATE = 3;
	public static final int PH_ARG_SIZE = 4;

	volatile int eat;
	volatile int sleep;
	volatile SOInterface utensils = ControlQueue.SOFactory();

	int[] statusModcounts = new int[5];

	private void message(String message, int status) {
		Object[] element = new Object[PH_ARG_SIZE];

		element[PH_ID] = id;
		element[PH_MESSAGE] = message;
		element[PH_INDEX] = index;
		element[PH_STATE] = new Integer(status);
		try {
			write("status-pool", element);
		} catch (MascotMachineException e) {
		}
		checkStatus();
	}

	/**
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		id = "Philosopher " + args[0] + " ";
		this.args = args;
		this.index = new Integer((String) args[0]);
		setTimes(((Integer) args[1]).intValue());
		resumeRoot();
	}

	void setTimes(int time) {
		eat = (int) (2 * Math.random() * time);
		sleep = (int) (4 * Math.random() * time);
		eat = (eat < 100) ? (int) (90 + (20 * Math.random())) : eat;
		sleep = (sleep < 200) ? (int) (180 + (40 * Math.random())) : sleep;
	}

	void checkStatus() {
		Object[] status;
		try {
			status = status("status-pool");
		} catch (MascotMachineException e) {
			MascotDebug.println(9, "SP Error: " + e);
			return;
		}
		SPElement element = (SPElement) status[StatusPool.STAT_RUN];

		while (!StatusPool.booleanValue(element)) {
			try {
				Thread.sleep(1000);
				status = status("status-pool");
			} catch (InterruptedException e) {
			} catch (MascotMachineException e) {
			}
		}
		statusModcounts[StatusPool.STAT_RUN] = element.modCount;

		element = (SPElement) status[StatusPool.STAT_TIME];
		if (element.modCount > statusModcounts[StatusPool.STAT_TIME]) {
			setTimes(StatusPool.intValue(element));
			statusModcounts[StatusPool.STAT_TIME] = element.modCount;
		}
	}

	private volatile boolean stateRight;
	private volatile boolean stateLeft;

	private class Utensil implements CQListener {
		public Thread myThread = Thread.currentThread();
		public String msg;
		public int state;
		public boolean which;
		private ControlQueue hold = new ControlQueue();

		public Utensil(String msg, int state, boolean which) {
			this.msg = msg;
			this.state = state;
			this.which = which;
		}

		public void inform(Object arg) {
			hold.cqJoin();
			boolean currentState = ((ControlQueue) arg).isOwner(myThread);

			if (which)
				stateRight = currentState;
			else
				stateLeft = currentState;
			hold.cqLeave();
		}

		public void sendMessage() {
			hold.cqJoin();
			message(msg, state);
			hold.cqLeave();
		}
	}

	public void resumeRoot() {
		Utensil haveRight = new Utensil("holding his right fork", PH_HAS_RIGHT, true);
		Utensil haveLeft = new Utensil("holding his left fork", PH_HAS_LEFT, false);
		int mysleep;
		int myeat;
		boolean order = (index.intValue() % 2) == 0;

		message("joins the table", PH_SLEEPS);
		ControlQueue left = null;
		ControlQueue right = null;
		try {
			left = (ControlQueue) read(args[2]);
			right = (ControlQueue) read(args[3]);
		} catch (MascotMachineException e) {
		}

		while (args!=null) {
			mysleep = sleep;
			myeat = eat;

			message("thinking", PH_SLEEPS);
			try {
				Thread.sleep(mysleep);
			} catch (InterruptedException e) {
			}
			message("hungry", PH_HUNGRY);
			if (order) {
				left.cqJoin(utensils, haveLeft);
				right.cqJoin(utensils, haveRight);
			} else {
				right.cqJoin(utensils, haveRight);
				left.cqJoin(utensils, haveLeft);
			}
			while (!(stateLeft && stateRight)) {
				if (stateLeft)
					haveLeft.sendMessage(); 
				else if (stateRight)
					haveRight.sendMessage();

				utensils.waitForRelease();
			}
			message("dining, holds both forks", PH_EATS);
			try {
				Thread.sleep(myeat);
			} catch (InterruptedException e) {
			}
			try {
				write(args[2], null);
				write(args[3], null);
			} catch (MascotMachineException e) {
			}
		}
	}
}
