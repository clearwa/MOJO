/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
 *
*/


package com.objectforge.mascot.xml;

import java.util.EventObject;

/**
 *
 * General purpose event.  Fill it with an object and fire.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
*/
public class ACPModelEvent extends EventObject {
    
    /**
     * Serialized version
     */
    private static final long serialVersionUID = 1L;

    public ACPModelEvent( Object anObject ) {
        super( anObject );
    }

}
