/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
 *
*/


package com.objectforge.mascot.xml;

/**
 *
 *
 * An interface for any object that wants to listen for MachineXMLEvents
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 */
public interface MachineXmlListener {
    void xmlModelEvent( MachineXmlEvent event );

}
