/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;


/**
 * TestEntityStore
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests.components
 * Created on 08-Apr-2004 by @author Clearwa
*/
public class TestEntityStore extends EntityStore {
    public static void init(){
        EntityStore.init( new TestEntityStore());
    }
}
