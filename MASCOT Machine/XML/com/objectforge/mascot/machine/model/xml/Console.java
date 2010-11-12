/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2.2.1 $
 */
package com.objectforge.mascot.machine.model.xml;

import java.io.InputStream;
import java.util.Vector;

import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.internal.AbstractConsole;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.Bootstrap;
import com.objectforge.mascot.machine.internal.Reaper;
import com.objectforge.mascot.machine.internal.WorkerDelegate;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotUtilities;

/**
 * Console
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model.xml
 * Created on 11-Jun-2003 by @author Clearwa
*/
public class Console extends AbstractConsole {

	/**
     * 
     */
    public Console() {
        super();
    }

    //The reaper vector
    protected static MascotEntities reaper;
    
    public static void main(String[] argv) {
    	loadSystem( argv );
    }
    
    public static void loadIt(){
    	String[] dummyArgs = {};
    	loadSystem( dummyArgs );
    }
    
    public static void loadSystem( String[] argv ){
        Vector reapers = new Vector();

        //Form the primordial instance of the global subsystem.
        EntityStore baseES = EntityStore.mascotRepository();
        //Determin the boostrap class
        System.out.println("Classpath: " + System.getProperty("java.class.path"));
        String bootstrapClassName = MascotUtilities.getMascotResource("mascot.machine.bootstrapClass");
        Class bootstrapClass = Bootstrap.class;
        
        if( bootstrapClassName!=null){
            try {
                bootstrapClass = Class.forName( bootstrapClassName );
            } catch (ClassNotFoundException e) {
                MascotDebug.println(0,"Boot class not found - defaulting to Bootstrap");
            }
        }
        try {
            IMascotReferences bootActivity = baseES.addActivityToWorker("global", bootstrapClass.getName(), null, null);
            IMascotReferences reaperActivity = baseES.addActivityToWorker("global", Reaper.class.getName(),null,null);
            Activity boot =
                (Activity) WorkerDelegate.addWorker(
                    EntityStore.getGlobalSubsystem(),
                    bootActivity,
                    null,
                    null,
                    reapers);
            Activity reap =
                (Activity) WorkerDelegate.addWorker(
                    EntityStore.getGlobalSubsystem(),
                    reaperActivity,
                    null,
                    null,
                    reapers);
            reaper = (MascotEntities) reapers.remove(0);
            reap.actStart("Reaper");
            boot.actStart("Bootstrap");
        } catch (MascotMachineException e1) {
            MascotUtilities.throwMRE("Subsustem<subTermainate adding worker>: " + e1);
        }
        MascotDebug.println(9, "Mainline exits");

    }

    private static MachineXML xml;
    {
        /*
         * On the first time through make a call to initialize the classpath if need be
         */
        System.getProperties();
        if( System.getProperty( "objectforgeServer")!=null ){
            MascotUtilities.getMascotBundle( System.getProperty( "objectforgeServer").equals( "true") );
        }
        try {
            xml = new MachineXML();
        } catch (Exception e) {
            MascotDebug.println(9, "Console constructor: " + e);
        }
    }

    public EntityStore enroll(InputStream input) {
        try {
            return xml.getSETS(input);
        } catch (Exception e) {
            MascotDebug.println(9, "XMLE: " + e);
            return null;
        }
    }

    public EntityStore enroll(String filename) {
        try {
            InputStream stream = MascotUtilities.mascotOpen(filename);

            return enroll(stream);
        } catch (Exception e) {
            MascotDebug.println(9, "XMLE: " + e);
            return null;
        }
    }

    /**
     * @return
     */
    public static MascotEntities getReaper() {
        return reaper;
    }

}
