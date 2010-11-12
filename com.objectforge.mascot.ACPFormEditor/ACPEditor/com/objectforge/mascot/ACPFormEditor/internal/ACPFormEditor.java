/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK, 2002 - 2007
 * 	    	All Rights Reserved
 *
*/

/*
 * Created on 20-Jan-2005
 *
 */

package com.objectforge.mascot.ACPFormEditor.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.xml.sax.SAXParseException;

import com.objectforge.mascot.ACPFormEditor.docprovider.ACPDocumentProvider;
import com.objectforge.mascot.ACPFormEditor.model.xml.ACPxml;
import com.objectforge.mascot.ACPFormEditor.pages.ACPpage;
import com.objectforge.mascot.ACPFormEditor.pages.xml.XMLEditor;
import com.objectforge.mascot.utility.MascotRuntimeException;
import com.objectforge.mascot.xml.MascotXmlChangedException;

/**
 * @author Clearwa
 * 
 */
public class ACPFormEditor extends FormEditor {
    /* Property change values */
    
    // This indicates that the xml editor has changed the underlying XML text
    public final static int ACPXML_EDITED = 1;
    // This is shipped if the acp editor has changed the parsed XML document.  In this case
    // xmlDirty is true.
    public final static int ACPXML_CHANGED = 2;
    // Say the editor is to revert.  This will occur after the revert has happened
    public final static int ACPXML_REVERTED = 3;

    /* The editor xml resources */
    private boolean inSave = false;
    private XMLEditor xmlPage;
    private ACPpage acpPage;
    private FileEditorInput acpInput;
    private ACPDocumentProvider acpDocProvider;
    //This variable reflects the default page number as that results from XML parsing
    private int parsePage = ACP_OVERVIEW_PAGE_ID;
    // Some constants
    public final static int ACP_OVERVIEW_PAGE_ID = 0;
    public final static int ACP_XML_PAGE_ID = 1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof IFileEditorInput)) {
			throw new PartInitException(
					"ACPFormEditor<init>: EditorInput is not an instance of IFileEditorInput");
		}
		// Create a new instance of ACPEditor input
		acpInput = new FileEditorInput(((IFileEditorInput) input).getFile());
		super.init(site, (IEditorInput) acpInput);
        setPartName( acpInput.getPath().lastSegment());
		try {
			acpDocProvider = new ACPDocumentProvider();
			acpDocProvider.connect(acpInput);
			acpDocProvider.getDocument(acpInput).addDocumentListener(
					new IDocumentListener() {

						public void documentAboutToBeChanged(DocumentEvent event) {
                            System.out.println( "About to change from page " + getActivePage() );
                            firePropertyChange( (getActivePage()==0)?ACPXML_CHANGED:ACPXML_EDITED );
						}

						public void documentChanged(DocumentEvent event) {
                            System.out.println( "Changed from page " + getActivePage() );
                            editorDirtyStateChanged();
						}

					});
		} catch (CoreException e) {
		    e.printStackTrace();
		}
	}

	public void loadXML() {
		ByteArrayInputStream xmlStream = new ByteArrayInputStream(
				acpDocProvider.getDocument(acpInput).get().getBytes());
		loadXML(xmlStream);
	}

	public void loadXML(InputStream xmlStream) {
        boolean takeException = false;
        
		// Parse the input and build tha ACP model structure
		getModel().setValid(false);
        // Set the parse page to 1, the one for an unsuccessful parse
        parsePage = ACP_XML_PAGE_ID;
		try {
			getModel().setDocument(xmlStream, true);
			String mstring = null;
			mstring = getModel().toXML();
			System.out.println("The document - \n" + mstring);
			acpDocProvider.getDocument(acpInput).set(mstring);

			// setWarning( "ACP Structure Warning","The document has been
			// modified\nPlease check in the XML editor" );
			getModel().setValid(true);
		} catch (Error er) {
			System.out.println("ACPEditor error: xmldoc -- " + er);

		} catch (MascotXmlChangedException e1) {
			System.out.println("ACPEditor: xmldoc -- " + e1);
			try {
				xmlStream.reset();
				getModel().setDocument(xmlStream, false);
			} catch (Error er1) {
			} catch (Exception ex1) {
			}
			getModel().setValid(true);
			parsePage = ACP_OVERVIEW_PAGE_ID;
		} catch (SAXParseException e2) {
            System.out.println( "PARSE - " + e2.getMessage());
            ErrorDialog.openError( null, "XML Parse Excpetion", "Parse error at line " +e2.getLineNumber() 
                    + ", column " + e2.getColumnNumber() + "\rYou will only be able to edit this SET\r" +
                            "document in the XML Source Editor", 
                    new Status(Status.WARNING,"com.objectforge.mascot.ACPFormEditor.internal",Status.OK, 
                    e2.getMessage(), null));
            takeException = true;
		} catch (Exception e) {
            e.printStackTrace();
        } finally {
			try {
				xmlStream.close();
			} catch (IOException e) {
			}
		}
		getModel().setXmlDirty(false);
        if( takeException ){
            throw new MascotRuntimeException( "Don't change ");           
        }
	}

	/**
	 * @return Returns the acpDocProvider.
	 */
	public ACPDocumentProvider getAcpDocProvider() {
		return acpDocProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	protected void addPages() {
		try {
			xmlPage = new XMLEditor(this, "SETs Source", "XML Source");
			acpPage = new ACPpage(this, "SETs Editor", "Structured Editor");
			addPage(ACP_OVERVIEW_PAGE_ID,acpPage);
			addPage(ACP_XML_PAGE_ID, xmlPage);
            setActivePage( parsePage );
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
    
    public void doSaveFor(){
        doSave( acpDocProvider.getProgressMonitor() );
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
        //Pass this on to the xml editor
        xmlPage.doSave( monitor );
    }
        
	/**
     * This is the presave part of the save processing.  This method checks that the XML that is
     * about to be asved has the correct structure and syncs the underlying models prior to the save
     * 
	 * @return
	 */
	public boolean formPreSave() {
        System.out.println("Do save");
        inSave = true;
        if (isDirty()) {
            acpDocProvider.aboutToChange(acpInput);
            // If the xml model is stale then the latest incarnation is in the
            // document provider. Do a check parse on this
            if ( xmlPage.isDirty() ) {
                try {
                    acpPage.checkXMLload();
                } catch (MascotRuntimeException e) {
                    // There has been a parse error.  This should force a page change if need be.
                    return true;
                }
                // I need to do something about an error if it happens
            }
            // Dump the contents of the xml model
            try {
                acpDocProvider.getDocument(getAcpInput()).set(acpDocProvider.getModel().toXML());
            } catch (IOException e) {
                inSave = false;
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Like the save, forward this on to the XML source editor
     */
    public void doRevertFor() {
        xmlPage.doRevertToSaved();
        
    }

    public void formRevert() {
        inSave = true; 
        xmlPage.doPerformRevert();
        System.out.println("Revert:");
        // At this point the input in the document provider has been replaced.
        // Now it's time to check whether it is valid XML.
//        int cp = getCurrentPage();
        firePropertyChange(ACPXML_REVERTED);
        formPostSave();
        staleParts();
    }
    
    public void staleParts(){
        IFormPart pages[] = acpPage.getManagedForm().getParts();
        for( int i=0;i<pages.length;i++ ){
            System.out.println( "Part " + pages[i].getClass().getName() + " is " +
                    (pages[i].isStale()? "stale" : "not stale" ) );
        }
        return;
    }

    /**
     * The tail end processing for a save.  This tidies up state.
     * 
     */
    public void formPostSave() {
        // I may have loaded or saved an invalid XML document
        // Everything is synched now
        acpDocProvider.getModel().setXmlDirty(false);
        firePropertyChange(PROP_DIRTY);
        inSave = false;
    }

	/*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
	public void doSaveAs() {
		System.out.println("Do save as");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * @return Returns the acpPage.
	 */
	public ACPpage getAcpPage() {
		return acpPage;
	}

	/**
	 * @return Returns the model.
	 */
	public ACPxml getModel() {
		return acpDocProvider.getModel();
	}

	/**
	 * @return Returns the xmlPage.
	 */
	public XMLEditor getXmlPage() {
		return xmlPage;
	}

	/**
	 * @return Returns the acpInput.
	 */
	public FileEditorInput getAcpInput() {
		return acpInput;
	}

    public Composite formContainer() {
        return getContainer();
    }

    public void setFormControl(int index, Control partControl) {
        setControl( index, partControl );
    }

    public boolean isDirty() {
        return acpDocProvider.canSaveDocument( acpInput ) || super.isDirty();
    }

    public boolean isInSave() {
        return inSave;
    }

    public int getParsePage() {
        return parsePage;
    }

    public void dispose() {
        super.dispose();
    }

    protected void setActivePage(int pageIndex) {
        super.setActivePage(pageIndex);
    }

}
