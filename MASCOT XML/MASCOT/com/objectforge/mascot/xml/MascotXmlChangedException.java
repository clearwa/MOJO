/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
 *
*/


package com.objectforge.mascot.xml;

/**
 * An excepion specific to SETS/ACP processing.  This exception is triggered during parsing in
 * the event where the document parses correctly but has been modified by the reader to conform
 * to a minimum standard.  In this case the in core representation is dirty with respect to the
 * XML document used to create it and the user needs to be informed of this situation.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 *
 */
public class MascotXmlChangedException extends Exception {

    /**
     * Serialized version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for ACPxmlChangedException.
     */
    public MascotXmlChangedException() {
        super();
    }

    /**
     * Constructor for ACPxmlChangedException.
     */
    public MascotXmlChangedException(String arg0) {
        super(arg0);
    }
}
