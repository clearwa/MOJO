/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
 *
*/


package com.objectforge.mascot.xml;

import java.util.EventObject;

/**
 * A general purpose event used within the context of the MachineXML
 * processing
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 *
 */
public class MachineXmlEvent extends EventObject{

    /**
     * Serialized version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for ACPxmlEvent.
     */
    public MachineXmlEvent( Object anObject) {
        super( anObject );
    }

}
