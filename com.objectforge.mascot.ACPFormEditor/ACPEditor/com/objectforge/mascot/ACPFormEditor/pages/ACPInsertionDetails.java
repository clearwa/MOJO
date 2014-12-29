package com.objectforge.mascot.ACPFormEditor.pages;

import java.util.Iterator;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class ACPInsertionDetails extends ACPDefaultDetails {

    void createEditors(DetailsNode selected) {
        doCreateEditors(selected, true);
    }

    public ACPInsertionDetails() {
        super();
    }

    public ACPInsertionDetails(ACPOverview master) {
        super(master);
        pathPrefix = "Insert";
    }

    protected void makeButtons() {
        toolkit.createButton(composite, "Insert", SWT.FLAT).addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                // First, update the field update
                for (Iterator i = enodes.iterator(); i.hasNext();) {
                    ((Ednode) i.next()).doFieldUpdate();
                }
                // The Enode information is now in the model, time to stitch it
                // in. If my parent is not
                // null then that is where to put this thing. When I created
                // this node I set it's parent
                // to the current selection. Now its time to make the insertion
                // real
                InsertionNode inserted = (InsertionNode) ((Ednode)enodes.firstElement()).getDetails();
                inserted.getInsertionPoint().appendChild( inserted.getThisNode() );
                // The instertion is done, now figure out how to update the viewer
                // Inserted is not part of the viewer's current structure but it's parent may be.  In order
                // for the refresh to create an internal node then the parent needs to be expanded.
                DetailsNode iparent = inserted.getParent();
                if( iparent!=null ){
                    // This expamds the parent if need be
                    treeViewer.setExpandedState( inserted.getParent(), true );                    
                }
                Object [] exstate = treeViewer.getVisibleExpandedElements();
                // After the refresh all of my DetailsNode references are no longer valid.  I need to find
                // them again using findDetailFor
                treeViewer.refresh( inserted.getParent(),true );
                // Restore the expansion state after the refresh
                TreeContentProvider tcp = (TreeContentProvider)treeViewer.getContentProvider();
                // Look up the new nodes
                for( int i=0;i<exstate.length;i++ ){
                   exstate[i] = tcp.findDetialFor( ((DetailsNode)exstate[i]).getThisNode() ); 
                }
                treeViewer.setExpandedState( exstate, true );
                treeViewer.setSelection( new StructuredSelection( tcp.findDetialFor( inserted.getThisNode() )));
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // Never called here
            }
            
        });
        toolkit.createButton(composite,"Cancel",SWT.FLAT).addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                StructuredSelection current = (StructuredSelection)treeViewer.getSelection();
                StructuredSelection newsel = 
                    (StructuredSelection) ((current==null || current.isEmpty())?StructuredSelection.EMPTY:
                        new StructuredSelection( current.getFirstElement()));
                // Reset the tree selection
                treeViewer.setSelection(  newsel );
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // Never called             
            }
            
        });
  }
}
