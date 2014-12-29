/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package mascot.test.persistentchannel;

import junit.framework.TestCase;

import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.prevayler.PersistentChannel;
/**
 * PersistentChannelTest
 * 
 * Project: MASCOT Examples
 * Package: mascot.test.persistentchannel
 * Created on 10-Oct-2003 by @author Clearwa
*/
public class PersistentChannelTest extends TestCase {
    private static final String ts1 = "Test string 1";
    private static final String ts2 = "Test string 2";

    /**
     * Constructor for PersistentChannelTest.
     * @param arg0
     */
    public PersistentChannelTest(String arg0) {
        super(arg0);
    }
    
    public void testCreation() {
        PersistentChannel myChan = new PersistentChannel();
        
        //Create the channel
       assertTrue( "Creation Test", PersistentTestUtils.channelControl( myChan, PersistentChannel.PCCREATE,"pc_testdir" ));
    }
    
    public void testInit(){
        PersistentChannel myChan = new PersistentChannel();

        assertTrue("Initialization Test", PersistentTestUtils.channelControl( myChan, PersistentChannel.PCINIT,"pc_testdir" ));
        assertEquals("Init status", 0, ((Integer)((myChan.status())[0])).intValue());
    }
    
    public void testWriteRead(){
        PersistentChannel myChan = new PersistentChannel();
        
        PersistentTestUtils.channelControl( myChan, PersistentChannel.PCINIT,"pc_testdir" );
        try {
            myChan.write( ts1 );
        } catch (IDAException e) {
            e.printStackTrace();
        } finally {
            try {
                assertEquals( "Write/Read test", ts1, (String)myChan.read());
            } catch (IDAException e1) {
                e1.printStackTrace();
            }
        }
        //Add 2 records for the recovery test
        try {
            myChan.write( ts1 );
            myChan.write( ts2 );
        } catch (IDAException e1) {
            e1.printStackTrace();
        }
    }
    
    public void testRecovery(){
        PersistentChannel myChan = new PersistentChannel();
        
        PersistentTestUtils.channelControl( myChan, PersistentChannel.PCINIT,"pc_testdir" );
        try {
            assertEquals( "Recovery test - read 1",ts1, myChan.read());
            assertEquals( "Recovery test - read 2",ts2, myChan.read());
        } catch (IDAException e) {
            e.printStackTrace();
        }
        assertEquals("Init status", 0, ((Integer)((myChan.status())[0])).intValue());
    }
    
}
