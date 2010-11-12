/*
 * File:  ACPOverview.java
 * Copyright - The Object Forge, Malvern, Worcs.  UK, 2002-2005
 * 	    	   All Rights Reserved
 * 
 * CVS Info:
 * Header:  $Header: /Objectforge_H/com.objectforge.mascot.ACPFormEditor/ACPEditor/com/objectforge/mascot/ACPFormEditor/pages/ACPOverview.java,v 1.1.2.1 2007/05/03 13:14:26 clearwa Exp $
 * Log:
 * $Log: ACPOverview.java,v $
 * Revision 1.1.2.1  2007/05/03 13:14:26  clearwa
 * Rebuild of the MASCOT projects and  the ACP editor under 3.2 prior to moving over the subversion
 *
 * Revision 1.1.2.5  2005/06/10 19:25:10  Clearwa
 * The new editor looks like it's done
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
 * Revision 1.1  2005/02/05 16:02:28  Clearwa
 * Initial checkin
 *
 * 		  
 */
package com.objectforge.mascot.ACPFormEditor.pages;

import java.util.Hashtable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.Element;

import com.objectforge.mascot.ACPFormEditor.internal.ACPFormEditor;
import com.objectforge.mascot.ACPFormEditor.model.xml.ACPxml;
import com.objectforge.mascot.ACPFormEditor.pages.xml.XMLEditor;

/**
 * @author Clearwa
 * @date 04-Feb-2005
 * 
 */
public class ACPOverview extends MasterDetailsBlock {

    protected TreeViewer treeViewer;
    protected ACPxml model;
    protected Tree tree;
    protected ACPpage page;
    protected Composite parent;
    private SectionPart spart;
    protected IManagedForm myMForm;

    public class InsertionMenu implements IMenuCreator{
        private Menu myMenu;
        private StructuredSelection selected;

        public void dispose() {
            if( myMenu!=null ){
                myMenu.dispose();
            }
        }

        public Menu getMenu(Control parent) {
            selected = (StructuredSelection) treeViewer.getSelection();
            myMenu = new Menu(parent);
            if( createItems( true ) ){
                createItems( false );
            }
            return myMenu;
        }
        
        public Menu getMenu(Menu parent) {
            selected = (StructuredSelection) treeViewer.getSelection();
            myMenu = parent;
            if( createItems( true ) ){
                createItems( false );
            }
            System.out.println( "InsertionMenu menu for menu parent ");
            return myMenu;
        }
        
        private boolean createItems(boolean forParent) {
            // Set the insertion point to the top of the DOM document. This is
            // the default.
            Element insertionPoint = (Element) model.getDocument().getElementsByTagName("SETS").item(0);
            DetailsNode pnode = null;

            // If there is a selection then get the detals node associated with
            // it
            if (!selected.isEmpty()) {
                DetailsNode element = (DetailsNode) InsertionMenu.this.selected.getFirstElement();
                Element iPoint = element.getThisNode();
                pnode = element;
                if (forParent) {
                    pnode = element.getParent();
                    insertionPoint = (pnode!=null)?pnode.getThisNode():insertionPoint;
                } else {
                    insertionPoint = iPoint;
                }
            } else {
                forParent = false;
            }

            // Prepare to create the menu items
            String[] menuEntries = (String[]) model.getValueFor(insertionPoint.getTagName(), "allowed-new");
            final Hashtable translate = new Hashtable();
            final Element target = insertionPoint;
            final DetailsNode parent = pnode;

            for (int i = 0; i < menuEntries.length; i++) {
                MenuItem menuItem = new MenuItem(this.myMenu, SWT.NONE);
                menuItem.setText((String) model.getValueFor(menuEntries[i], "display-label"));
                translate.put(menuItem.getText(), menuEntries[i]);
                menuItem.addSelectionListener(new SelectionListener() {

                    public void widgetSelected(SelectionEvent e) {
                        Element element = model.createElement((String) translate.get(((MenuItem) e.widget)
                                .getText()));
                        InsertionNode newNode = new InsertionNode(element, parent, target);
                        ACPOverview.this.myMForm.fireSelectionChanged(ACPOverview.this.spart,
                                new StructuredSelection(newNode));
                    }

                    public void widgetDefaultSelected(SelectionEvent e) {
                        // Not called
                    }
                });
            }
            if (forParent) {
                new MenuItem(myMenu, SWT.SEPARATOR);
            }
            return forParent;
        }

    }
    public ACPOverview(ACPxml model, ACPpage page) {
        this.model = model;
        this.page = page;
        this.page.getACPEditor().addPropertyListener(new IPropertyListener() {

            public void propertyChanged(Object source, int propId) {
                ACPFormEditor editor = (ACPFormEditor) source;
                if (propId!=ACPFormEditor.ACPXML_REVERTED && editor.getActivePageInstance() instanceof XMLEditor) {
                    switch (propId) {
                    case ACPFormEditor.ACP_OVERVIEW_PAGE_ID:
                    case ACPFormEditor.ACP_XML_PAGE_ID:
                        if (!((ACPFormEditor) ACPOverview.this.page.getEditor()).isInSave()) {
                            spart.markStale();
                            System.out.println("ACPOverview - Property change " + propId);
                        }
                        break;

                    default:
                        break;
                    }
                } else if (propId == ACPFormEditor.ACPXML_REVERTED) {
                    // The version of the part is always stale in this case.
                    // Call refresh to make
                    // sure the page is stale but not dirty.
                    try {
                        ACPOverview.this.page.getACPEditor().loadXML();
                        getTreeViewer().setInput(ACPOverview.this.model.getDocument());
                    } catch (RuntimeException e) {
                        // On a parse error do nothing
                    }
                    spart.refresh();
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.MasterDetailsBlock#createContent(org.eclipse.ui.forms.IManagedForm)
     * 
     * Since it is possible that this is called more than once, dispose of any
     * existing parts before I create the new ones.
     */
    public void createContent(IManagedForm managedForm) {
        IFormPart parts[] = managedForm.getParts();
        for( int i=0;i<parts.length;i++){
            parts[i].dispose();
        }
        super.createContent(managedForm);
    }

    protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
        this.parent = parent; // save my parent
        this.myMForm = managedForm;
        
        FormToolkit toolkit = managedForm.getToolkit();
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        parent.setLayout(gridLayout);

        final GridData gridData = new GridData(GridData.FILL_BOTH);
        spart = new SectionPart(parent,toolkit,ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);
        spart.initialize(managedForm);
        spart.markStale();
        
        Section section = spart.getSection();
        section.setLayoutData(gridData);
        section.setText("ACP Overview");
        ((ACPpage) managedForm.getContainer()).setFormMenu( section );

        final Composite composite = toolkit.createComposite(section, SWT.NONE);
        composite.setLayout(new GridLayout());
        toolkit.paintBordersFor(composite);
        section.setClient(composite);

        treeViewer = new TreeViewer(composite, SWT.BORDER|SWT.MULTI);
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
                managedForm.fireSelectionChanged(spart, e.getSelection());
            }
        });
        tree = treeViewer.getTree();
        final GridData gridData_1 = new GridData(GridData.FILL_BOTH);
        gridData_1.heightHint = 200;
        gridData_1.widthHint = 220;
        tree.setLayoutData(gridData_1);
        ((ACPpage) managedForm.getContainer()).setFormMenu( treeViewer.getControl() );
        treeViewer.setLabelProvider(new ACPLabelProvider(model));
        treeViewer.setContentProvider(new TreeContentProvider(model));
        treeViewer.setAutoExpandLevel(2);
        
        managedForm.addPart( spart );
    }

    protected void registerPages(DetailsPart detailsPart) {
        detailsPart.registerPage( DetailsNode.class, new ACPDefaultDetails( this ) );
        detailsPart.registerPage( InsertionNode.class, new ACPInsertionDetails( this ));
    }

    protected void createToolBarActions(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
 
        Action revertDoc = new Action("Revert", IAction.AS_PUSH_BUTTON) {
            public void run() {
                ((ACPFormEditor)page.getEditor()).doRevertFor();
                spart.getManagedForm().dirtyStateChanged();
            }
        };
        revertDoc.setToolTipText("Revert to the last saved document");
        Action saveDoc = new Action("Save", IAction.AS_PUSH_BUTTON) {
            public void run() {
                ((ACPFormEditor)page.getEditor()).doSaveFor(); 
                spart.getManagedForm().dirtyStateChanged();
            }
        };
        saveDoc.setToolTipText("Save the edited document");
        Action insertItem = new Action("Insert", IAction.AS_DROP_DOWN_MENU) {
            public void run() {
            }
        };
        insertItem.setMenuCreator( new InsertionMenu() );
        form.getToolBarManager().add(insertItem);
        form.getToolBarManager().add( new Separator() );
        form.getToolBarManager().add(revertDoc);
        form.getToolBarManager().add( new Separator() );
        form.getToolBarManager().add(saveDoc);
    }

    protected void setXMLDoc() {
        ((TreeContentProvider) treeViewer.getContentProvider())
                .setLabelProvider(((ACPLabelProvider) treeViewer.getLabelProvider()));
        treeViewer.setInput(model.getDocument());
        // It is possible (because of early initialization) that the tree is completely empty.  If
        // so return silently
        if( tree.getItems().length==0 ){
            return;
        }
        TreeItem sel = tree.getItems()[0];
        tree.showItem(sel);
        tree.setSelection(new TreeItem[] { sel });
        page.getManagedForm().refresh();
    }

    /**
     * @return Returns the treeViewer.
     */
    public final TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public Menu insertionMenu(MenuItem insert) {
        Menu insertion = new Menu( insert );
        InsertionMenu insertCreate = new InsertionMenu();
        return insertCreate.getMenu( insertion );
    }

    public boolean isTreeSelect() {
        if (treeViewer != null) {
            ISelection selected = treeViewer.getSelection();
            return !(selected == null || selected.isEmpty());
        }
        return false;
    }

}
