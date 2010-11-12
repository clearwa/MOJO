/*
 * File:  TreeContentProvider.java
 * Copyright - The Object Forge, Malvern, Worcs.  UK, 2002-2005
 * 	    	   All Rights Reserved
 * 
 * CVS Info:
 * Header:  $Header: /Objectforge_H/com.objectforge.mascot.ACPFormEditor/ACPEditor/com/objectforge/mascot/ACPFormEditor/pages/TreeContentProvider.java,v 1.1.2.1 2007/05/03 13:14:30 clearwa Exp $
 * Log:
 * $Log: TreeContentProvider.java,v $
 * Revision 1.1.2.1  2007/05/03 13:14:30  clearwa
 * Rebuild of the MASCOT projects and  the ACP editor under 3.2 prior to moving over the subversion
 *
 * Revision 1.1.2.4  2005/06/07 17:22:06  Clearwa
 * The ACP editor is at its first release
 *
 * Revision 1.1.2.3  2005/04/02 07:42:47  Clearwa
 * Checkpoint
 *
 * Revision 1.1.2.1  2005/03/23 14:42:11  Clearwa
 * Checkin from ganymede after work while I was a dad's
 *
 * Revision 1.1.2.1  2005/02/19 15:54:23  Clearwa
 * Half way there!!
 *
 * Revision 1.1  2005/02/05 16:02:27  Clearwa
 * Initial checkin
 *
 * 		  
 */
package com.objectforge.mascot.ACPFormEditor.pages;

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.objectforge.mascot.ACPFormEditor.model.xml.ACPxml;


public class TreeContentProvider implements ITreeContentProvider{
	private ACPxml model;
	private ACPLabelProvider labelProvider;
    private Hashtable nodes = new Hashtable();
	
	/* Constructor */
	public TreeContentProvider( ACPxml model ){
		this.model = model;
	}
    
    DetailsNode createDetails( Element node, DetailsNode parent ){
        if( nodes.containsKey( node ) ){
            return (DetailsNode) nodes.get( node );
        }
        DetailsNode retval = new DetailsNode( node, parent );
        nodes.put( node, retval );
        return retval;
    }
    
    public DetailsNode findDetialFor( Element node ){
        if( nodes.containsKey( node ) ){
            return (DetailsNode)nodes.get( node );
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if( parentElement instanceof DetailsNode ){
            NodeList nList = ((DetailsNode)parentElement).getThisNode().getChildNodes();
            Vector children = new Vector();
            if (nList.getLength() > 0) {
                for (int i = 0; i < nList.getLength(); i++) {
                    if (nList.item(i) instanceof Element)
                        children.add( createDetails((Element) nList.item(i), (DetailsNode) parentElement ));
                }
            }
            return children.toArray();
        } else{
            return new Object[0];
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if(element == null || !(element instanceof DetailsNode))
            return null;
        return ((DetailsNode)element).getParent();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if( element instanceof DetailsNode )
            return ((DetailsNode)element).hasSiblings();
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        NodeList setList = null;
        NodeList includeList = null;
        if( inputElement instanceof Document){
            setList = ((Document)inputElement).getElementsByTagName("SET");
            includeList = ((Document)inputElement).getElementsByTagName("Include");
        } else
            return new Object[0];
        Vector retval = new Vector();
        for( int i=0;i<includeList.getLength();i++ )
            retval.add( createDetails( (Element)includeList.item(i), null ));
        for( int i=0;i<setList.getLength();i++ )
            retval.add( createDetails( (Element)setList.item(i), null ));
        return retval.toArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // The input has changed, create a new hashtable
        nodes = new Hashtable();        
    }
    
	/**
	 * @return Returns the labelProvider.
	 */
	public  ACPLabelProvider getLabelProvider() {
		return labelProvider;
	}
	/**
	 * @param labelProvider The labelProvider to set.
	 */
	public  void setLabelProvider(ACPLabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}
	/**
	 * @return Returns the model.
	 */
	public  ACPxml getModel() {
		return model;
	}
	/**
	 * @param model The model to set.
	 */
	public  void setModel(ACPxml model) {
		this.model = model;
	}
}