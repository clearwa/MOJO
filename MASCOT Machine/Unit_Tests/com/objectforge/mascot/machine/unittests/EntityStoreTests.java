/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.unittests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * EntityStoreTests
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests
 * Created on 25-Mar-2004 by @author Clearwa
*/
public class EntityStoreTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Mascot Machine Entity Store Tests");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(ESStaticTests.class));
        //$JUnit-END$
        return suite;
    }
}
