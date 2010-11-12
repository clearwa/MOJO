/**
 * The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *
 *
 * Portions derrived from code supplied as part of the Eclipse project
 *     All Copyrights apply
*/


package com.objectforge.mascot.ACPFormEditor.docprovider;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name:  $
 *
 */
public class ACPEditorInput extends FileEditorInput {
	
	public ACPEditorInput( IFile input ) {
			super( input );
	}
		

	/**
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/**
	 */
	public String getName() {
		return "ACPEditor";
	}

	/**
	 */
	public String getToolTipText() {
		return "Mascot ACP File";
	}

	/**
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}
}	

