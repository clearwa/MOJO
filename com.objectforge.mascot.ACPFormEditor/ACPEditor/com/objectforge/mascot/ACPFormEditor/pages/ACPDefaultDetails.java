/*
 * File:  ACPDefaultDetails.java
 * Copyright - The Object Forge, Malvern, Worcs.  UK, 2002-2005
 * 	    	   All Rights Reserved
 * 
 * CVS Info:
 * Header:  $Header: /Objectforge_H/com.objectforge.mascot.ACPFormEditor/ACPEditor/com/objectforge/mascot/ACPFormEditor/pages/ACPDefaultDetails.java,v 1.1.2.1 2007/05/03 13:14:29 clearwa Exp $
 * Log:
 * $Log: ACPDefaultDetails.java,v $
 * Revision 1.1.2.1  2007/05/03 13:14:29  clearwa
 * Rebuild of the MASCOT projects and  the ACP editor under 3.2 prior to moving over the subversion
 *
 * Revision 1.1.2.7  2005/06/13 09:12:59  Clearwa
 * Checkpoint
 *
 * Revision 1.1.2.6  2005/06/10 19:25:10  Clearwa
 * The new editor looks like it's done
 *
 * Revision 1.1.2.5  2005/06/07 17:22:06  Clearwa
 * The ACP editor is at its first release
 *
 * Revision 1.1.2.4  2005/04/02 07:42:47  Clearwa
 * Checkpoint
 *
 * Revision 1.1.2.2  2005/03/23 14:42:12  Clearwa
 * Checkin from ganymede after work while I was a dad's
 *
 * Revision 1.1.2.2  2005/02/19 15:54:23  Clearwa
 * Half way there!!
 *
 * Revision 1.1.2.1  2005/02/05 18:33:27  Clearwa
 * 3.x dev branch created
 *
 * Revision 1.1  2005/02/05 16:02:28  Clearwa
 * Initial checkin
 *
 * 		  
 */
package com.objectforge.mascot.ACPFormEditor.pages;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.Element;

import com.objectforge.mascot.ACPFormEditor.internal.ACPFormEditor;
import com.objectforge.mascot.ACPFormEditor.model.xml.ACPxml;
import com.objectforge.mascot.xml.TagtableEntry;

/**
 * @author Clearwa
 * @date   01-Feb-2005
 *
 */
public class ACPDefaultDetails implements IDetailsPage {

    protected Composite composite;
    private Section section;
    private IManagedForm managedForm;
    protected FormToolkit toolkit;
    ACPxml model;
    private ACPLabelProvider labelProvider;
    
    //This hashtable holds contents keyed by attribute field
    Hashtable contents = new Hashtable();
	TreeViewer treeViewer;
	Object thisSelection;
	Vector enodes = new Vector();
    ACPOverview master;

    class Ednode implements ModifyListener, SelectionListener{
    	private DetailsNode node;
//    	private boolean modified = false;
    	private String attrib;
    	private String text;
    	private String modText;
    	private Widget widget;	//the widget this listener is associated with
    	
    	public Ednode( DetailsNode node, String attrib, String text){
    		this.node = node;
    		this.attrib = attrib;
    		this.text = this.modText = text;
    	}
        
        public DetailsNode getDetails(){
            return node;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
			modText = (e.widget instanceof CCombo)?((CCombo)e.widget).getText():((Text)e.widget).getText();
		}
		
		/**
		 * Update the XML Document structure
		 */
		public void doFieldUpdate(){
            boolean dirty;
			if( (dirty = (text!=null)?!text.equals(modText):false) ){
				node.getThisNode().setAttribute( attrib, modText);
				text = modText;
			}
            //Mark the model as dirty.  If it is dirty then I have been this way before.  If this is
            //the first time, then update the document provider as well to force change propagation
            //through the system.  The actual xml document is dumped to the document provider when I
            //exit upon the call to ACPPage::setActive().
            if( (dirty || attrib==null) && !model.isXmlDirty() ){
                ACPFormEditor editor = master.page.getACPEditor();
                model.setXmlDirty(true);
                editor.getAcpDocProvider().getDocument( editor.getAcpInput() ).set( "Dirty" );
            }   
		}
		
		/**
		 * Cancel any pending changes
		 */
		public void doCancel(){
			node.getThisNode().setAttribute( attrib,text);
			modText = text;
			if(widget instanceof CCombo){
				((CCombo)widget).setText(text);
			} else if( widget instanceof Text){
				((Text)widget).setText(text);
			}
		}

		/**
		 * @param widget The widget to set.
		 */
		public void setWidget(Widget widget) {
			this.widget = widget;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			//CCombo sends this event when the list selection changes
			widgetDefaultSelected( e );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
			// A double-click or return
			modText = (e.widget instanceof CCombo)?((CCombo)e.widget).getText():((Text)e.widget).getText();			
		}
    }
    
    public ACPDefaultDetails() {
    }

    /**
	 * @param model
	 * @param labelProvider
	 */
	public ACPDefaultDetails( ACPOverview master ) {
        this.model = master.model;
		this.labelProvider = (ACPLabelProvider) master.treeViewer.getLabelProvider();
		this.treeViewer = master.treeViewer;
        this.master = master;
        DetailsNode.setModel( this.model );
	}

	public void initialize(IManagedForm managedForm) {
        this.managedForm = managedForm;
    }

    public void createContents(Composite parent) {
        toolkit = managedForm.getToolkit();
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        parent.setLayout(gridLayout);

        section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);
        section.setLayoutData(new GridData(GridData.FILL_BOTH));
        section.setText("Defaut Details");
    }

    public void dispose() {
	}

    public void setFocus() {
    }

    private void update() {
        section.layout();
    }

    public boolean setFormInput(Object input) {
        return false;
    }
    
    DetailsNode selected;
    String myTag;
    protected String pathPrefix;
    
    public void selectionChanged(IFormPart part, ISelection selection) {
        StructuredSelection structuredSelection = (StructuredSelection) selection;
        selected = (DetailsNode) ((StructuredSelection)selection).getFirstElement();
        myTag = labelProvider.getColumnText( selected,0 );
        
        if( composite!=null){
        	composite.dispose();
        	composite = null;
        }
        if( thisSelection!=null){
			treeViewer.update(thisSelection,null);
			thisSelection = null;
        }
        ((ACPpage)managedForm.getContainer()).setFormMenu( section );
        thisSelection = structuredSelection.getFirstElement();
        enodes = new Vector();
        composite = toolkit.createComposite(section, SWT.FLAT);
        final GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 3;
        composite.setLayout(gridLayout1);
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        
        //Create the editors for the element
        createEditors( selected );
        //Set title - this causes a refresh as well
        titlePath( );
    }
    
    String titlePath( ){
        DetailsNode parent = selected;
        String myPath = pathElement( selected );
        
        while( parent.getParent()!=null){
            parent = parent.getParent();
            myPath =  pathElement(parent) + "->" + myPath;
        }
       section.setText( ( (pathPrefix==null?myTag:pathPrefix + " " + myTag) + "\r" + myPath ) );
       update();
       refresh();
       return myPath;
    }
    
    private Object createEditor( String key, Hashtable attributes, Element node ){
        TagtableEntry entry = (TagtableEntry) attributes.get(key);
        Hashtable editorDesc = (Hashtable) entry.contents;
        Object retval;

        // Create the label
        toolkit.createLabel(composite, key, SWT.NONE);
        // Create a spacer
        toolkit.createLabel(composite, "", SWT.NONE);
        // Now create the editor and fill in the current value
        String currentValue = node.getAttribute(key);
        Ednode listener = new Ednode(selected, key, currentValue);
        enodes.add(listener);

        if (editorDesc.get("editor").equals("combo")) {
            // This is to be a combo editor
            CCombo cb = new CCombo(composite, SWT.FLAT);
            toolkit.adapt(cb);
            cb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            cb.setItems((String[]) editorDesc.get("strings"));
            cb.setText(currentValue);
            cb.addSelectionListener(listener);
            cb.setEditable(false);
            listener.setWidget(cb);
            retval = cb;
        } else {
            Text tx = toolkit.createText(composite, currentValue, SWT.NONE);
            tx.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            tx.addModifyListener(listener);
            listener.setWidget(tx);
            retval = tx;
        }
        return retval;  
    }
    
    void createEditors( DetailsNode selected ){
        doCreateEditors( selected, false );
    }
    
    void doCreateEditors(DetailsNode selected, boolean do_insert) {
        Element node = selected.getThisNode();
        String tag = node.getTagName();
        // Get the defaultattr attribute
        String defaultattr = model.getDefaultAttributeFor(tag);
        Hashtable attributes = model.getAttributesFor(tag);

        if (attributes.isEmpty()) {
            // Make a text label for now if there are no fields
            toolkit.createLabel(composite, "No fields for this element", SWT.NONE);
            // Put in some spacers
            toolkit.createLabel(composite, "", SWT.NONE);
            toolkit.createLabel(composite, "", SWT.NONE);
            enodes.add(new Ednode(selected, null, null));
        } else {
            // The first field in the list should be the default attribute
            // if it is defined
            if (defaultattr != null && attributes.containsKey(defaultattr)) {
                Object editor = createEditor(defaultattr, attributes, node);
                if (editor instanceof Text && do_insert) {
                    ((Text) editor).setText("New " + model.getDisplayLabel(tag) +
                            " " + defaultattr );
                }
            }

            // For each of the attributes create a label/editor entry
            for (Enumeration i = attributes.keys(); i.hasMoreElements();) {
                String edname = (String) i.nextElement();
                if (!(defaultattr != null && edname.equals(defaultattr))) {
                    createEditor(edname, attributes, node);
                }
            }
        }

        makeButtons();
    }
    
    protected void makeButtons(){
        toolkit.createButton(composite,"Commit",SWT.FLAT).addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				// Upon select, update the xml model
				for( Iterator i=enodes.iterator();i.hasNext();)	{
					((Ednode)i.next()).doFieldUpdate();
				}
				if( thisSelection!=null){
					treeViewer.update( thisSelection,null );
                    titlePath( );
                }
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// Never called here			
			}
        	
        });
        toolkit.createButton(composite,"Reset",SWT.FLAT).addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				// Cancel any edits
				for( Iterator i=enodes.iterator();i.hasNext();)	{
					((Ednode)i.next()).doCancel();
				}
				if( thisSelection!=null)
					treeViewer.update( thisSelection,null );
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// Never called				
			}
        	
        });
        toolkit.createButton(composite,"Delete",SWT.FLAT).addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                StructuredSelection selected = (StructuredSelection) treeViewer.getSelection();
                treeViewer.setSelection(StructuredSelection.EMPTY, true);
                // Since all of the Enode elements in enods point to the same
                // underlying DetailsNode pick the first one
                DetailsNode details = ((Ednode) enodes.firstElement()).getDetails();
                // Now removee the node from the underlying tree model
                treeViewer.remove( details );
                details.deleteDetails();
                model.setXmlDirty( true );
                master.page.getEditor().editorDirtyStateChanged();
                Object parent = ((DetailsNode) selected.getFirstElement()).getParent();
                if (parent != null) {
                    StructuredSelection newselect = new StructuredSelection(parent);
                    treeViewer.setSelection(newselect, true);
                } else {
                    treeViewer.setSelection( StructuredSelection.EMPTY );
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // Never called             
            }
            
        });
  }
    
    private String pathElement( DetailsNode node){
    	String retval = labelProvider.getColumnText(node,1);
    	if( retval.equals("") ){
    		retval = labelProvider.getColumnText(node,0);
    	}
    	return retval;
    }

    public void commit(boolean onSave) {
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isStale() {
        return false;
    }

    public void refresh() {
        update();
    }
}
