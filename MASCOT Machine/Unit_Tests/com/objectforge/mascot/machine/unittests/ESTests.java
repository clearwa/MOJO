package com.objectforge.mascot.machine.unittests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Copyright Object Forge, 2002,2003
 * @author - Clearwa
 * 
 * 
 */
public class ESTests {

	public static Test suite() {
		TestSuite suite =
			new TestSuite("Test for com.objectforge.mascot.machine.unittests");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(ESStaticTests.class));
        suite.addTest(new TestSuite(ESDynamicTests.class));
		//$JUnit-END$
		return suite;
	}
}
