package com.objectforge.mascot.ACPFormEditor.pages;

import org.eclipse.jface.viewers.ISelection;
import org.w3c.dom.Element;

public class InsertionNode extends DetailsNode implements ISelection {
    /*
     * The value reflects where this insertion is to occur.  It is the node in the model
     * where the insertion as a child will occur 
     */     
    Element insertionPoint;

    public InsertionNode(Element thisNode, DetailsNode parent, Element insertionPoint) {
        super(thisNode, parent);
        this.insertionPoint = insertionPoint;
    }

    public boolean isEmpty() {
        return false;
    }

    public Element getInsertionPoint() {
        return insertionPoint;
    }

}
