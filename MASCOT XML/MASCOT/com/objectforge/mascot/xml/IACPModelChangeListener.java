/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
 *
*/


package com.objectforge.mascot.xml;



/**
 * An interface.  Implemented by any class wishing to be informed of an ACPModelEvent
 * by an ACPModelChangeManager.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 *
 */
public interface IACPModelChangeListener {
    
    /**
     * Method acpModelChanged.
     * Invoked by an instance of ACPModelChangeManager when an ACPModelEvent event is fired
     */
    public void acpModelChanged( ACPModelEvent event );
    
}
