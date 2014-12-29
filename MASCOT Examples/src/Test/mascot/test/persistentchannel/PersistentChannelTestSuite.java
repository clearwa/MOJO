/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package mascot.test.persistentchannel;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * PersistentChannelTestSuite
 * 
 * Project: MASCOT Examples
 * Package: mascot.test.persistentchannel
 * Created on 10-Oct-2003 by @author Clearwa
*/
public class PersistentChannelTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for mascot.test.persistentchannel");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(PersistentChannelTest.class));
        suite.addTest(new TestSuite(PersistentThreadingTest.class));
        //$JUnit-END$
        return suite;
    }
}
