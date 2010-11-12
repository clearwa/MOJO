/*
 * File:  ACPLabelProvider.java
 * Copyright - The Object Forge, Malvern, Worcs.  UK, 2002-2005
 * 	    	   All Rights Reserved
 * 
 * CVS Info:
 * Header:  $Header: /Objectforge_H/com.objectforge.mascot.ACPFormEditor/ACPEditor/com/objectforge/mascot/ACPFormEditor/pages/ACPLabelProvider.java,v 1.1.2.1 2007/05/03 13:14:33 clearwa Exp $
 * Log:
 * $Log: ACPLabelProvider.java,v $
 * Revision 1.1.2.1  2007/05/03 13:14:33  clearwa
 * Rebuild of the MASCOT projects and  the ACP editor under 3.2 prior to moving over the subversion
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
 * Revision 1.1  2005/02/05 16:02:26  Clearwa
 * Initial checkin
 *
 * 		  
 */
package com.objectforge.mascot.ACPFormEditor.pages;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Element;

import com.objectforge.mascot.ACPFormEditor.model.xml.ACPxml;

public class ACPLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	private ACPxml model;

	/**
	 * @param overview
	 */
	ACPLabelProvider(ACPxml model) {
		this.model = model;
	}

	public Image getColumnImage(Object element, int index) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		String retval = getColumnText(element, 0);
		return retval + " (" + getColumnText(element, 1) + ")";
	}

	public String getColumnText(Object element, int index) {
		if (element == null) {
			return "null - " + index;
		} else if (element instanceof DetailsNode) {
			Element node = ((DetailsNode) element).getThisNode();
			String tag = model.getDisplayLabel(node.getTagName());
			String defaultAttr = model.getDefaultAttributeFor(node.getTagName());
			String defVal = (defaultAttr!=null)?node.getAttribute(defaultAttr):"-";
			
			switch (index) {
			case 0:
				return tag /*+ " (" + defVal + ")"*/;
			case 1:
				return defVal;
			default:
				return "<empty node> - " + index;
			}
		}
		return element.getClass().getName() + " - " + index;
	}
}