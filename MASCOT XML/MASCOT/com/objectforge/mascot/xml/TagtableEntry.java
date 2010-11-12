/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
 *
*/


package com.objectforge.mascot.xml;

/**
 * An object used to hold information in the tag table.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 */
public class TagtableEntry {
    public boolean display; //should this item be displayed in property lists
    public boolean defaultItem; //is this the default property value
    public Object contents;     //a place to put the contents
    
    public TagtableEntry( boolean display, boolean defaultItem, Object contents ){
        this.display = display;
        this.defaultItem = defaultItem;
        this.contents = contents;
    }
    
    public TagtableEntry(){
        this( false,false,null );
    }
}
