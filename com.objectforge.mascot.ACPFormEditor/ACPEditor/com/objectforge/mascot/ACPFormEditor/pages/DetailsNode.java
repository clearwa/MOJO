/*
 * File:  DetailsNode.java
 * Copyright - The Object Forge, Malvern, Worcs.  UK, 2002-2005
 * 	    	   All Rights Reserved
 * 
 * CVS Info:
 * Header:  $Header: /Objectforge_H/com.objectforge.mascot.ACPFormEditor/ACPEditor/com/objectforge/mascot/ACPFormEditor/pages/DetailsNode.java,v 1.1.2.1 2007/05/03 13:14:31 clearwa Exp $
 * Log:
 * $Log: DetailsNode.java,v $
 * Revision 1.1.2.1  2007/05/03 13:14:31  clearwa
 * Rebuild of the MASCOT projects and  the ACP editor under 3.2 prior to moving over the subversion
 *
 * Revision 1.1.2.5  2005/06/10 19:25:10  Clearwa
 * The new editor looks like it's done
 *
 * Revision 1.1.2.4  2005/06/07 17:22:07  Clearwa
 * The ACP editor is at its first release
 *
 * Revision 1.1.2.3  2005/04/02 07:42:47  Clearwa
 * Checkpoint
 *
 * Revision 1.1.2.1  2005/03/23 14:42:12  Clearwa
 * Checkin from ganymede after work while I was a dad's
 *
 * Revision 1.1.2.1  2005/02/19 15:54:24  Clearwa
 * Half way there!!
 *
 * Revision 1.1  2005/02/05 16:02:28  Clearwa
 * Initial checkin
 *
 * 		  
 */
package com.objectforge.mascot.ACPFormEditor.pages;

import java.io.IOException;

import org.w3c.dom.Element;

import com.objectforge.mascot.ACPFormEditor.model.xml.ACPxml;

/**
 * @author Clearwa
 * @date 04-Feb-2005
 * 
 */
public class DetailsNode {
	private DetailsNode parent;
	private Element thisNode;
    private static ACPxml model;

	public DetailsNode(Element thisNode, DetailsNode parent ) {
		this.thisNode = thisNode;
		this.parent = parent;
	}

	/**
	 * @return Returns the parent.
	 */
	public DetailsNode getParent() {
		return parent;
	}
    
    public static int countParents( DetailsNode node){
        int count = 0;
        while( node.getParent()!=null ){
            node = node.getParent();
            count++;
        }
        return count;
    }
    
    public static int compare( Object o1, Object o2){
        if( o1 instanceof DetailsNode && o2 instanceof DetailsNode ){
            int count1 = countParents( (DetailsNode) o1 );
            int count2 = countParents( (DetailsNode) o2 );
            if( count2<count1 )
                return -1;
            else if( count2>count1 )
                return 1;
            else
                return 0;
        }
        return 0;
    }

	/**
	 * @return Returns the thisNode.
	 */
	public Element getThisNode() {
		return thisNode;
	}

	public DetailsNode deleteDetails() {
		if (thisNode.getParentNode() != null) {
            // Now fix the DOM tree so it matches the displayed structure
            thisNode.getParentNode().removeChild( thisNode );
            try {
                System.out.println( "Delete Details:\r" + model.toXML() );
            } catch (IOException e) {
            }
		}
		return this;
	}

	public boolean hasSiblings() {
		return thisNode.hasChildNodes();
	}

	/**
	 * @return The name of the contained node
	 */
	public String getNodeName( ACPxml model) {
		return "A name";
	}

    public static void setModel(ACPxml model) {
        DetailsNode.model = model;
    }

}
