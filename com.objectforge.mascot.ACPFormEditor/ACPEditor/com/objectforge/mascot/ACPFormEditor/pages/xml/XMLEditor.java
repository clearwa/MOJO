package com.objectforge.mascot.ACPFormEditor.pages.xml;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import com.objectforge.mascot.ACPFormEditor.internal.ACPFormEditor;
import com.objectforge.mascot.utility.MascotRuntimeException;

public class XMLEditor extends TextEditor implements IFormPage{

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    protected void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);
    }
	private ColorManager colorManager;
	private ACPFormEditor formEditor;
	private Control control;
	private String id;
	private boolean active;
	private int index;
	private String title;
	private IWorkbenchPartSite site;
    private boolean dirty;

	public void doSave(IProgressMonitor progressMonitor) {
        if (formEditor.formPreSave()) {

            super.doSave(progressMonitor);
            formEditor.formPostSave();
            dirty = false;
            formEditor.editorDirtyStateChanged();

        }

    }

    protected void performRevert() {
        // The XML side is asking for a revert, sort out the mess that's going to result
        formEditor.formRevert();
    }

    public void doPerformRevert(){
        super.performRevert();
        dirty = false;
        formEditor.editorDirtyStateChanged();
    }

    public XMLEditor(){
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
	}
	public XMLEditor( ACPFormEditor editor, String id, String title ) {
		this();
		this.id = id;
		this.title = title;
		this.site = editor.getSite();
        // Set the editor's document provider
        setDocumentProvider( editor.getAcpDocProvider() );
		initialize( editor );
        editor.addPropertyListener( new IPropertyListener () {

            public void propertyChanged(Object source, int propId) {
                ACPFormEditor editor = (ACPFormEditor) source;
                if (editor.getActivePageInstance() instanceof XMLEditor) {
                    switch (propId) {
                    case ACPFormEditor.ACP_OVERVIEW_PAGE_ID:
                    case ACPFormEditor.ACP_XML_PAGE_ID:
                        if (!XMLEditor.this.formEditor.isInSave()) {
                            System.out.println("XMLEditor - Property change " + propId);
                            dirty = true;
                        }
                        break;

                    default:
                        break;
                    }
                }

            }});
	}

	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
	public String getTitle( )
	{
		return title;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#initialize(org.eclipse.ui.forms.editor.FormEditor)
	 */
	public void initialize(FormEditor editor) {
		formEditor = (ACPFormEditor)editor;
		setDocumentProvider(formEditor.getAcpDocProvider());
		setInput( formEditor.getAcpInput() );
        //Do an early creation on the part control so a save from the ACP side does not fail
        //if it happens before the entire editor is realized.
        super.createPartControl(formEditor.formContainer());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#getEditor()
	 */
	public FormEditor getEditor() {
		return formEditor;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#getManagedForm()
	 */
	public IManagedForm getManagedForm() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#setActive(boolean)
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#isActive()
	 */
	public boolean isActive() {
		return active;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#canLeaveThePage()
	 */
	public boolean canLeaveThePage() {
        //Before I leave this page check that I can do it without a parse error
        try {
            formEditor.getAcpPage().checkXMLload();
        } catch (MascotRuntimeException e) {
            // On a parse error do not allow page change
            return false;
        }
        return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#getPartControl()
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		Control[] children = parent.getChildren();
		control = children[children.length - 1];
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#getPartControl()
	 */
	public Control getPartControl() {
		return control;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#getId()
	 */
	public String getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#getIndex()
	 */
	public int getIndex() {
		return index;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#setIndex(int)
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#isEditor()
	 */
	public boolean isEditor() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#selectReveal(java.lang.Object)
	 */
	public boolean selectReveal(Object object) {
		return false;
	}

	public IWorkbenchPartSite getSite(){
		if ( site != null ){
			return site;
		}
		return super.getSite();
	}
    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    public boolean isDirty() {
        return dirty || super.isDirty();
    }
}
