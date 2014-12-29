/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.test.roots;

import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.roots.AbstractRoot;

/*
* Project: MASCOT Machine
* Package: com.objectforge.mascot.test.roots
* Created on 25-May-2003
*/
public class Simple1 extends AbstractRoot {

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.IRoot#root(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		int number = ((Integer)getSubsystem().getArgFromSubsystem(0)).intValue();
		int[] storage = new int[1000000];
		
		storage[0] = 1;
		System.out.println("Simple test #" + number + " runs");
	}

	/* (non-Javadoc)
	 * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
	 */
	public void resumeRoot() {
		// Do nothing

	}

}
