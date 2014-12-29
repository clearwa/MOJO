/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/


package com.objectforge.mascot.machine.roots;

import com.objectforge.mascot.machine.internal.Activity;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */
public class RootStub extends AbstractRoot {
	public void mascotRoot(Activity activity, Object[] args) {
		super.printRoot();
	}
	
	public void resumeRoot(){
	}
}
