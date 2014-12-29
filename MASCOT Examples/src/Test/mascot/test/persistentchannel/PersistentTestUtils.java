/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package mascot.test.persistentchannel;

import java.io.File;
import java.io.IOException;

import org.prevayler.foundation.FileManager;

import junit.framework.Assert;

import com.objectforge.mascot.prevayler.PersistentChannel;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * PersistentTestUtils
 * 
 * Project: MASCOT Examples
 * Package: mascot.test.persistentchannel
 * Created on 13-Oct-2003 by @author Clearwa
*/
public class PersistentTestUtils {

    protected static boolean channelControl(PersistentChannel chan, int control, String dir){
        try {
            return chan.control( control,dir);
        } catch (IOException e) {
            MascotDebug.println(9,"PersistentChannelTest:<channelControl> - " + e );
            Assert.fail("PersistentChannelTest:<channelControl> - IOException " + e);
        } catch (ClassNotFoundException e) {
            MascotDebug.println(9,"PersistentChannelTest:<channelControl> - " + e );
            Assert.fail("PersistentChannelTest:<channelControl> - ClassNotFoundException " + e);
        }
        return false;
    }
    
    protected static void channelDirDelete( String dir) throws IOException{
        File workingDir = FileManager.produceDirectory(dir);
        File[] files = workingDir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }

    }
}
