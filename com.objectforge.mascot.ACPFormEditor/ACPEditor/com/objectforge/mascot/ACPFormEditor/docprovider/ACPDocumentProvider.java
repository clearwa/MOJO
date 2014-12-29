package com.objectforge.mascot.ACPFormEditor.docprovider;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import com.objectforge.mascot.ACPFormEditor.model.xml.ACPxml;
import com.objectforge.mascot.ACPFormEditor.pages.xml.XMLPartitionScanner;

public class ACPDocumentProvider extends FileDocumentProvider {
	private ACPxml model = new ACPxml();
	
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner =
				new FastPartitioner(
					new XMLPartitionScanner(),
					new String[] {
						XMLPartitionScanner.XML_TAG,
						XMLPartitionScanner.XML_COMMENT });
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
	/**
	 * @return Returns the model.
	 */
	public ACPxml getModel() {
		return model;
	}
}