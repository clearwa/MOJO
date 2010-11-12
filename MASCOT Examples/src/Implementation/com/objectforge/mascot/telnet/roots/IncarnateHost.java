/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package com.objectforge.mascot.telnet.roots;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.device.DeviceControl;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.estore.EsSubsystem;
import com.objectforge.mascot.machine.idas.IDAException;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IDARef;
import com.objectforge.mascot.machine.model.IIDA;
import com.objectforge.mascot.machine.model.MascotReferences;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * IncarnateHost
 * 
 * Project: MASCOT Examples
 * Package: com.objectforge.mascot.telnet.roots
 * Created on 18-Apr-2004 by @author Clearwa
*/
public class IncarnateHost extends AbstractRoot {

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#mascotRoot(com.objectforge.mascot.machine.internal.Activity, java.lang.Object[])
     */
    public void mascotRoot(Activity activity, Object[] args) {
        //Pull the arguments
        String deviceImp = (String) getSubsystem().getResource("device-implementation");
        int port = ((Integer) getSubsystem().getResource("device-port")).intValue();
        String deviceName = deviceImp + "-" + port;

        try {
            //Need to create the device IDA reference if it does not already exist
            if (!EntityStore.mascotRepository().getIdaDescriptors().containsKey(deviceName)) {
                EntityStore.mascotRepository().createIDA(deviceName, deviceImp, null, "device", "system");
            }
            //The next thing to do is create a reference to this IDA
            MascotReferences mref =
                ((EsSubsystem) getSubsystem().getEInstance()).deviceIDARef(
                    EntityStore.mascotRepository(),
                    deviceName,
                    deviceName);
            getSubsystem().addIDA((IDARef) mref);
            //Incarnate the device instance
            add(deviceName, "device-1");
            //Now tell the newly created server device what port it's running on
             ((IIDA) resolve("device-1", Device.READER)).write(new DeviceControl(new Integer(port)));
        } catch (MascotMachineException e) {
            throw new MascotRuntimeException(
                "IncarnateHost<mascotRoot>: MascotMachineException " + e.getMessage());
        } catch (IDAException e) {
            throw new MascotRuntimeException("IncarnateHost<mascotRoot>: IDAException " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see com.objectforge.mascot.machine.model.IRoot#resumeRoot()
     */
    public void resumeRoot() throws Exception {
        // Does nothing
    }

}
