/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK, 2002 - 2007
 * 	    	All Rights Reserved
 *
*/

/*
 * Created on 21-Jan-2005
 *
 */

package com.objectforge.mascot.ACPFormEditor.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Clearwa
 *
 */
public class OpenACPFormEditorAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow wbWindow;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		wbWindow = window;		//remember the workbench window I am using
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IWorkbenchPage page = wbWindow.getActivePage();
		try {
			page.openEditor(new FileEditorInput(null), "com.objectforge.mascot.ACPFormEditor", true);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

}
