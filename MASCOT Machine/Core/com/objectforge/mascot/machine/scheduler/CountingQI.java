/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.scheduler;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 * 
 * 
 */
public interface CountingQI {
		/**
		 * Method init.
		 * Initialiaze a CountingQ
		 */
		public void init(); 
		
		/**
		 * Method que.
		 * Return the embedded control queue
		 */
		public ControlQueue que();

		/**
		 * Method join.
		 * Join the control queue
		 */
		public int join();

		/**
		 * Method leave.
		 * Leave the control queue
		 */
		public boolean leave();
		
		/**
		 * Method stim.
		 * Stim the embedded control queue
		 */
		public void stim();

		/**
		 * Method waitQ.
		 * Wait on the embedded queue
		 */
		public int waitQ();
		
		/**
		 * Method status.
		 * Return true if there are threads waiting to become the owner
		 */
		public boolean status();

		/**
		 * @param b
		 */
		public void setAllowSuspended();

}
