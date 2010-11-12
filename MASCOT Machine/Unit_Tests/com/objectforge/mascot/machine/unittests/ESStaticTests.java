/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.unittests;

import java.util.HashMap;
import java.util.Set;

import junit.framework.TestCase;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.estore.EntityInstance;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.estore.EsSubsystem;
import com.objectforge.mascot.machine.internal.Subsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.machine.model.SETEntity;
import com.objectforge.mascot.machine.model.xml.Console;

/**
 * ESStaticTests
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests
 * Created on 25-Mar-2004 by @author Clearwa
*/
public class ESStaticTests extends TestCase {
    //    public static EntityStore myStore;
    public static Console console = new Console();
    public static String testset = "Estore Tests";

    /**
     * Constructor for ESStaticTests.
     * @param arg0
     */
    public ESStaticTests(String arg0) {
        super(arg0);
    }

    //Test the entity store create primitives.  This test touches all of the create and reference
    //primitives and forms and starts a subsystem but does not test any of the device stuff other
    //than to create one.
    public void testEntityPrimitives() throws InterruptedException, MascotMachineException {

        //Create a new store for me
        EntityStore myStore = EntityStore.entityStoreFactory();
        try {
            Utilities.printIt(myStore);
            Utilities.stall("A new estore instance...");
            Utilities.printIt("Create the set <" + testset + ">");
            EntityInstance mySet = myStore.createSET(testset);
            Utilities.printIt("Create a simple activity entity");
            MascotEntities myActivity =
                myStore
                    .createActivity(
                        "Test1",
                        "com.objectforge.mascot.machine.unittests.components.TestRootSimple",
                        null,
                        testset)
                    .getParentEntity();
            Utilities.printIt("Create a channel and pool entities");
            MascotEntities myChannel =
                myStore
                    .createIDA(
                        "TestChannel",
                        "com.objectforge.mascot.machine.idas.ChannelStub",
                        null,
                        "channel",
                        testset)
                    .getParentEntity();
            MascotEntities myPool =
                myStore
                    .createIDA(
                        "TestPool",
                        "com.objectforge.mascot.machine.idas.PoolStub",
                        null,
                        "pool",
                        testset)
                    .getParentEntity();
            myStore.createIDA("TestDeviceIDA", "TestDevice", null, "device", testset).getParentEntity();
            Utilities.printIt("Create a device entity");
            MascotEntities myDevice =
                myStore.createDevice("TestDevice", "TestHandler", testset).getParentEntity();
            Utilities.printIt("Create subsystem and handler entities");
            MascotEntities mySubsystem =
                myStore.createSubsystem("subsystem", "TestSub", true, testset).getParentEntity();
            MascotEntities containedSubsystem =
                myStore.createSubsystem("subsystem", "ContainedSub", true, testset).getParentEntity();
            MascotEntities myHandler =
                myStore.createSubsystem("handler", "TestHandler", true, testset).getParentEntity();
            Utilities.printIt(myStore);
            Utilities.stall("The estore instance after adding entities...");

            //Now check the results
            Set contents = ((SETEntity) mySet.getParentEntity()).entities();
            assertTrue(contents.contains(myActivity));
            assertTrue(contents.contains(myChannel));
            assertTrue(contents.contains(myPool));
            assertTrue(contents.contains(mySubsystem));
            assertTrue(contents.contains(myHandler));
            assertTrue(contents.contains(containedSubsystem));

            //Check that the device has been properly setup
            contents = myStore.getSET("system").entities();
            assertTrue(contents.contains(myDevice));

            MascotEntities devpool =
                (MascotEntities) myStore.getIdaDescriptors().get(Device.makePoolName("TestDevice"));
            assertTrue(contents.contains(devpool));

            //Add references to the subsystem - these references actually resolve to an entity I've already created
            //so check them
            EsSubsystem myEsSub = (EsSubsystem) mySubsystem.getCurrentIncarnation();
            assertTrue(
                myEsSub.addActivityRef(myStore, "Test1Ref", "Test1", new HashMap()).getReference()
                    == myActivity);
            assertTrue(
                myEsSub
                    .addSubsystemRef(myStore, "ContainedSubRef", "ContainedSub", new HashMap())
                    .getReference()
                    == containedSubsystem);
            assertTrue(
                myEsSub.localIDARef(myStore, "TestChannelRef", "TestChannel").getReference() == myChannel);
            assertTrue(myEsSub.localIDARef(myStore, "TestPoolRef", "TestPool").getReference() == myPool);
            EsSubsystem containedEs = (EsSubsystem) containedSubsystem.getCurrentIncarnation();
            assertTrue(
                containedEs.addActivityRef(myStore, "Contained1Ref", "Test1", new HashMap()).getReference()
                    == myActivity);
            //These references do not resolve to an entity - create but don't check - need to look at the 
            //console output
            containedEs.containerIDARef("HandlerTCRef", "TestChannelRef");
            containedEs.containerIDARef("HandlerTDRef", Device.makePoolName("TestDevice"));
            myEsSub.globalIDARef("TDRef", Device.makePoolName("TestDevice"));
            myEsSub.argumentIDARef("ArgRef", "1");
            myEsSub.deviceIDARef(myStore, "TestDeviceIDARef", "TestDeviceIDA");
            Utilities.printIt(myStore);
            Utilities.stall("The estore instance after adding the references...");

            //Test all of this by attemption to merge and form the test subsystem
            Utilities.printIt(EntityStore.mascotRepository());
            Utilities.stall("The repository prior to the merge...");
            EntityStore.merge(myStore);
            Utilities.printIt(EntityStore.mascotRepository());
            Utilities.stall("The repository after the merge...");

            Subsystem formed = EntityStore.formSubsystem("TestSub", EntityStore.getGlobalSubsystem());
            formed.subStart();
            //Wait for input
            Utilities.printIt(EntityStore.mascotRepository().toString());
            Utilities.stall("The repository after running the subsystem...");

        } catch (MascotMachineException e) {
            e.printStackTrace();
        }
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
