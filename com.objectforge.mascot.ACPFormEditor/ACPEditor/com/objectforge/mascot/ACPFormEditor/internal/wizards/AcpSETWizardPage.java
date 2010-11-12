package com.objectforge.mascot.ACPFormEditor.internal.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.objectforge.mascot.ACPFormEditor.internal.ACPFormEditorPlugin;
import com.swtdesigner.ResourceManager;

public class AcpSETWizardPage extends WizardPage {

    private Text fileText;
    private Text containerText;
    private IStructuredSelection selection;
    
    public AcpSETWizardPage( IStructuredSelection selection ) {
        super("ACPSETsWizard");
        setTitle("Mascot ACP SETS Wizard");
        setDescription("Create a blank SETs document");
        setImageDescriptor(ResourceManager.getPluginImageDescriptor(ACPFormEditorPlugin.getDefault(), "icons/DButton.gif"));
        this.selection = selection;
    }

    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);
        setControl(container);

        final Label label_1 = new Label(container, SWT.NONE);
        final GridData gridData_3 = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData_3.horizontalIndent = 20;
        gridData_3.widthHint = 80;
        label_1.setLayoutData(gridData_3);
        label_1.setText("Container");

        containerText = new Text(container, SWT.BORDER);
        containerText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });
        final GridData gridData_1 = new GridData(GridData.FILL_HORIZONTAL);
        gridData_1.widthHint = 226;
        containerText.setLayoutData(gridData_1);

        final Button button = new Button(container, SWT.NONE);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleBrowse();
            }
        });
        final GridData gridData_2 = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        gridData_2.widthHint = 80;
        button.setLayoutData(gridData_2);
        button.setText("Browse...");

        final Label label = new Label(container, SWT.NONE);
        final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalIndent = 20;
        gridData.widthHint = 80;
        gridData.verticalIndent = -2;
        label.setLayoutData(gridData);
        label.setText("File");

        fileText = new Text(container, SWT.BORDER);
        fileText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });
        fileText.setText("new_set.macp");
        final GridData gridData_1_1 = new GridData(GridData.FILL_HORIZONTAL);
        gridData_1_1.widthHint = 226;
        fileText.setLayoutData(gridData_1_1);
        
        //fill in the selection
        initialize();
    }
    
    private void initialize() {
        if (selection!=null && selection.isEmpty()==false && selection instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection)selection;
            if (ssel.size()>1) return;
            Object obj = ssel.getFirstElement();
            if (obj instanceof IResource) {
                IContainer container;
                if (obj instanceof IContainer)
                    container = (IContainer)obj;
                else
                    container = ((IResource)obj).getParent();
                containerText.setText( makeCtext(container.getFullPath().toString()) );
            }
        }
    }
    
    private String makeCtext( String text ){
        return (text.length()>1)?text.substring(1):"";
    }

    /**
     * Uses the standard container selection dialog to
     * choose the new value for the container field.
     */

    private void handleBrowse() {
        ContainerSelectionDialog dialog =
            new ContainerSelectionDialog(
                getShell(),
                ResourcesPlugin.getWorkspace().getRoot(),
                false,
                "Select a new file container");
        if (dialog.open() == ContainerSelectionDialog.OK) {
            Object[] result = dialog.getResult();
            if (result.length == 1) {
                containerText.setText( makeCtext( ((Path)result[0]).toOSString()) );
            }
        }
    }

    /**
     * Ensures that both text fields are set.
     */

    private void dialogChanged() {
        String container = getContainerName();
        String fileName = getFileName();

        if (container.length() == 0) {
            updateStatus("File container must be specified");
            return;
        }
        if (fileName.length() == 0) {
            updateStatus("File name must be specified");
            return;
        }
        int dotLoc = fileName.lastIndexOf('.');
        if (dotLoc != -1) {
            String ext = fileName.substring(dotLoc + 1);
            if (ext.equalsIgnoreCase("macp") == false) {
                updateStatus("File extension must be \"macp\"");
                return;
            }
        }
        updateStatus(null);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getContainerName() {
        return containerText.getText();
    }
    public String getFileName() {
        return fileText.getText();
    }
}
