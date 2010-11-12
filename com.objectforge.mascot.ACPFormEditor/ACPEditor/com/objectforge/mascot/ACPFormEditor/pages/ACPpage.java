/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK, 2002 - 2007
 * 	    	All Rights Reserved
 *
*/

/*
 * Created on 20-Jan-2005
 *
 */

package com.objectforge.mascot.ACPFormEditor.pages;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.objectforge.mascot.ACPFormEditor.internal.ACPFormEditor;
import com.objectforge.mascot.ACPFormEditor.model.xml.ACPxml;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * @author Clearwa
 *
 */
public class ACPpage extends FormPage {
    private ACPOverview master;
    private ACPFormEditor acpEditor;
    
    public boolean checkXMLload(){
        return checkXMLload( true );
    }
    
    public boolean checkXMLload(boolean doRefresh) {
//        boolean retval = false;

        if (getManagedForm() != null && getManagedForm().isStale()) {
            try {
                acpEditor.loadXML();
                if (doRefresh) {
                    master.getTreeViewer().setInput(model.getDocument());
                    getManagedForm().refresh();
                    return true;
                }
            } catch (MascotRuntimeException me) {
                System.out.println("Error on parse, don't switch pages ");
                throw me;   // Rethrow the error to let someone else know
            }
        } else
            return true;
        return false;
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.IFormPage#setActive(boolean)
     */
    public void setActive(boolean active) {
        acpEditor.staleParts();
        if (active && getManagedForm().isStale() ) {
            try {
                checkXMLload();
            } catch (RuntimeException e) {
                // Do nothing on a parse error
            }
            super.setActive(active);
        } else if( !active && model.isXmlDirty()){
            try {
                acpEditor.getAcpDocProvider().getDocument(acpEditor.getAcpInput()).set( acpEditor.getModel().toXML() );
                // This line cleans up any side effects of reloading the document.  Since I set the damn thing I know 
                // its state and it isn't stale.
                getManagedForm().refresh();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }
       
//	private Text text;
    private ACPxml model;
    
	public ACPpage(String id, String title) {
		super(id, title);
	}

	public ACPpage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	protected void createFormContent(IManagedForm managedForm) {
        FormToolkit toolkit = managedForm.getToolkit();
        ScrolledForm form = managedForm.getForm();
        form.setText("SETS Editor");
        form.setExpandHorizontal(true);
        form.setExpandVertical(true);
        Composite body = form.getBody();
        toolkit.paintBordersFor(body);
        master = new ACPOverview(model, this);
        master.createContent(managedForm);

        // Set the xml document
        try {
            checkXMLload();
            master.setXMLDoc();
            // Make sure there is no selection
            master.getTreeViewer().setSelection(new ISelection() {
                public boolean isEmpty() {
                    return true;
                }

            }, true);
        } catch (RuntimeException e) {
            // Do nothing on a parse error
        }       
    }
    
    public boolean isDirty() {
        return model.isXmlDirty();
    }

    public Menu setFormMenu(final Control form) {
        class PMListener implements MenuListener {

            public void menuHidden(MenuEvent e) {
                // Clean out the old, swap in the new
                Menu newMenu = new Menu( form );
                Menu source = (Menu) e.getSource();
                source.removeMenuListener(this);
                newMenu.addMenuListener(new PMListener());
                form.setMenu(newMenu);
            }

            public void menuShown(MenuEvent e) {
                Menu source = (Menu) e.getSource();
                final MenuItem save = new MenuItem(source, SWT.NONE);
                save.setText("Save");
                save.addSelectionListener( new SelectionListener () {

                    public void widgetSelected(SelectionEvent e) {
                        acpEditor.doSaveFor();
                        getManagedForm().dirtyStateChanged();
                    }

                    public void widgetDefaultSelected(SelectionEvent e) {
                        // Do nothing
                        
                    }} );
                save.setEnabled( acpEditor.isDirty() || 
                        acpEditor.getAcpDocProvider().canSaveDocument( acpEditor.getAcpInput() ) );
                final MenuItem revert = new MenuItem(source, SWT.NONE);
                revert.setText("Revert");
                revert.addSelectionListener( new SelectionListener () {

                    public void widgetSelected(SelectionEvent e) {
                        acpEditor.doRevertFor();
                        getManagedForm().dirtyStateChanged();
                    }

                    public void widgetDefaultSelected(SelectionEvent e) {
                        // Do nothing
                        
                    }} );
                revert.setEnabled( acpEditor.isDirty() || 
                        acpEditor.getAcpDocProvider().canSaveDocument( acpEditor.getAcpInput() ) );
                new MenuItem(source, SWT.SEPARATOR);
                final MenuItem delete = new MenuItem(source, SWT.NONE);
                delete.setText("Delete");
                delete.setEnabled( master.isTreeSelect() );
                final MenuItem insert = new MenuItem( source, SWT.CASCADE);
                delete.addSelectionListener(new SelectionListener() {

                    public void widgetSelected(SelectionEvent e) {
                        StructuredSelection selected = (StructuredSelection) master.getTreeViewer().getSelection();
                        master.getTreeViewer().setSelection(StructuredSelection.EMPTY, true);
                        // Since all of the Enode elements in enods point to the
                        // same
                        Object [] details = selected.toArray();
                        Arrays.sort( details, new Comparator() {

                            public int compare(Object arg0, Object arg1) {
                                return DetailsNode.compare( arg0,arg1 );
                            }}  );
                        
                        // Now removee the nodes from the underlying tree model
                        master.getTreeViewer().remove(details);
                        for (int i = 0; i < details.length; i++) {
                            ((DetailsNode)details[i]).deleteDetails();
                            model.setXmlDirty(true);
                        }
                        master.page.getEditor().editorDirtyStateChanged();
                        Object parent = ((DetailsNode) details[ details.length - 1 ]).getParent();
                        if (parent != null) {
                            StructuredSelection newselect = new StructuredSelection(parent);
                            master.getTreeViewer().setSelection(newselect, true);
                        } else {
                            master.getTreeViewer().setSelection(StructuredSelection.EMPTY);
                        }
                    }

                    public void widgetDefaultSelected(SelectionEvent e) {
                        // Never called             
                    }

                });
                insert.setText( "Insert" );
                insert.setMenu( master.insertionMenu( insert ) );
            }
        }
        Menu pageMenu = new Menu( form );
        form.setMenu(pageMenu);
        pageMenu.addMenuListener(new PMListener());
        return pageMenu;
    }
    
    public ACPFormEditor getACPEditor() {
        return acpEditor;
    }

    public void initialize(FormEditor editor) {
        // Create the page early since we may need it before it's diplayed
        acpEditor  = (ACPFormEditor)editor;
        model = this.acpEditor.getModel();
        super.initialize(editor);
        super.createPartControl( ((ACPFormEditor)editor).formContainer());
    }
}
