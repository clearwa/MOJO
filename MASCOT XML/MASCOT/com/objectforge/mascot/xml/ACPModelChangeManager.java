/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
 *
*/


package com.objectforge.mascot.xml;

import java.util.Iterator;
import java.util.Vector;

/**
 * General purpose callback manager.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 */
public class ACPModelChangeManager {
    private Vector listeners;
    
    public ACPModelChangeManager( ) {
        super();
        listeners = new Vector();
    }
    
    /**
     * Method addListener.
     * Add a listener to this instance's list.
     */
    public void addListener( IACPModelChangeListener listener ) {
        listeners.add( listener );
    }
    
    /**
     * Method removeListener.
     * Remove a listener from this instance's list.
     */
    public void removeListener( IACPModelChangeListener listener ) {
        listeners.removeElement( listener );
    }
    
    /**
     * Method fireACPModelChangeEvent.
     * Fire the event to all listeners on the list.
     */
    public void fireACPModelChangeEvent( ACPModelEvent event ) {
        if( listeners.size()>0 ){
            for( Iterator it = listeners.iterator(); it.hasNext(); ){
                ((IACPModelChangeListener)(it.next())).acpModelChanged( event );
            }
        }
    }

}
