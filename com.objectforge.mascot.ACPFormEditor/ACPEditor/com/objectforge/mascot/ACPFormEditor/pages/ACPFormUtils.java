/**
 * The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *
 *
 * Portions derrived from code supplied as part of the Eclipse project
 *     All Copyrights apply
 */

package com.objectforge.mascot.ACPFormEditor.pages;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.objectforge.mascot.ACPFormEditor.internal.ACPFormEditor;
import com.objectforge.mascot.ACPFormEditor.model.xml.ACPxml;
import com.objectforge.mascot.xml.TagtableEntry;

/**
 * @author Allan Clearwaters
 * @version $Id: ACPFormUtils.java 72 2007-07-09 07:15:18Z  $,
 *          $Name$
 * 
 */
public class ACPFormUtils {
    private ACPxml model;

    private ACPFormEditor view;

    private boolean suppress = false;

    public ACPFormUtils() {
        super();
    }

    public ACPFormUtils(ACPFormEditor view) {
        this.model = view.getModel();
        this.view = view;
    }

    // Elements are attribute nodes from the tag selected in the tree
    public class XMLTableLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        public Image getColumnImage(Object element, int index) {
            return null;
        }

        public String getColumnText(Object element, int index) {
            TableValue node = (TableValue) element;
            if (node == null)
                return "null";

            switch (index) {
            case 0:
                return node.name;

            case 1:
                return (node.value == null) ? "" : node.value;

            default:
                return "<empty>";
            }
        }
    }

    public class TableContentProvider implements IStructuredContentProvider,
            ITreeContentProvider {

        public Object getParent(Object element) {
            return null;
        }

        public Object[] getChildren(Object element) {
            return null;
        }

        public boolean hasChildren(Object element) {
            return false;
        }

        // Input is the node selected in the tree
        public Object[] getElements(Object input) {
            NamedNodeMap nm = ((Node) input).getAttributes();
            Hashtable nObjects = new Hashtable();
            Hashtable attributes = model.getAttributesFor(((Node) input)
                    .getNodeName());

            for (Enumeration i = attributes.keys(); i.hasMoreElements();) {
                String key = (String) i.nextElement();
                nObjects.put(key, new TableValue(key, null));
            }

            // Fill in any predefined values from the XML document
            for (int i = 0; i < nm.getLength(); i++) {
                if (nObjects.containsKey(nm.item(i).getNodeName())) {
                    TableValue tvt = (TableValue) nObjects.get(nm.item(i)
                            .getNodeName());

                    tvt.value = nm.item(i).getNodeValue();
                    tvt.node = nm.item(i);
                }
            }

            if (nObjects.values().toArray().length == 0) {
                TableValue tvt = new TableValue("", null);
                tvt.canModify = false;
                tvt.value = "<no attributes to set>";
                return new Object[] { tvt };
            }

            return nObjects.values().toArray();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }
    }

    public class TextCellModifier implements ICellModifier {
        private Node input = null;

        private TableViewer viewer;

        public void setViewer(TableViewer viewer) {
            this.viewer = viewer;
        }

        public void setInput(Node input) {
            this.input = input;
        }

        public boolean canModify(Object element, String property) {
            return ((TableValue) element).canModify;
        }

        public Object getValue(Object element, String property) {
            return (((TableValue) element).value == null) ? ""
                    : ((TableValue) element).value;
        }

        public void modify(Object element, String property, Object value) {
            TableItem item = (TableItem) element;
            TableValue aNode = (TableValue) (item.getData());

            System.out.println("New value is <" + value + ">");
            checkXML((String) value, aNode.node, item, input);
            viewer.refresh(element, true);
            return;
        }
    }

    public class ComboCellModifier implements ICellModifier {
        private String[] values;

        private Node input;

        private TableViewer viewer;

        public void setViewer(TableViewer viewer) {
            this.viewer = viewer;
        }

        public void setInput(Node input) {
            this.input = input;
        }

        public boolean canModify(Object element, String property) {
            return true;
        }

        public void setValues(String[] newValues) {
            values = newValues;
        }

        public Object getValue(Object element, String property) {
            String current = ((TableValue) element).value;

            if (current != null)
                for (int i = 0; i < values.length; i++) {
                    if (current.equals(values[i]))
                        return new Integer(i);
                }
            return new Integer(0);
        }

        public void modify(Object element, String property, Object value) {
            int index = ((Integer) value).intValue();
            TableItem item = (TableItem) element;

            System.out.println("New value is <" + value + ">");
            TableValue aNode = (TableValue) item.getData();

            if (!(index < 0))
                checkXML(values[index], aNode.node, item, input);
            viewer.refresh(element, true);
            return;
        }
    }

    private void checkXML(String newVal, Node aNode, TableItem item, Node input) {
        boolean update = false;

        if ((update = (aNode == null))) {
            Element element = (Element) input;
            TableValue tv = (TableValue) item.getData();

            element.setAttribute(tv.name, newVal);
        } else if ((update = (!newVal.equals(aNode.getNodeValue()))))
            aNode.setNodeValue(newVal);

        if (update) {
            model.setXmlDirty(true);
            item.setText(1, newVal);
        }
        if (model.isXmlDirty() && !suppress)
            view.getAcpDocProvider().getDocument(view.getEditorInput()).set("");
    }

    public void createCellEditor(TableValue aNode, Object input,
            TableViewer viewer) {
        CellEditor anEditor = null;
        String attribname = aNode.name;
        TagtableEntry attributes = (TagtableEntry) model.getAttributesFor(
                ((Node) input).getNodeName()).get(attribname);

        if (attributes == null)
            return;

        Hashtable tagvalues = (Hashtable) attributes.contents;

        if (tagvalues.containsKey("editor")) {
            String editor = (String) tagvalues.get("editor");

            if (editor.equals("combo")) {
                ComboCellModifier modifier = new ComboCellModifier();
                viewer.setCellModifier(modifier);

                String[] choices = (String[]) tagvalues.get("strings");
                modifier.setValues(choices);
                modifier.setInput((Node) input);
                modifier.setViewer(viewer);
                anEditor = new ComboBoxCellEditor(viewer.getTable(),
                        (choices == null) ? new String[] { "" } : choices);
            } else { // If not told otherwise then the default is a textcell
                        // editor
                TextCellModifier modifier = new TextCellModifier();

                modifier.setInput((Node) input);
                modifier.setViewer(viewer);
                viewer.setCellModifier(modifier);
                anEditor = new TextCellEditor(viewer.getTable());
            }
        }
        viewer.setCellEditors(new CellEditor[] { null, anEditor });
    }

    public Composite createTable(Composite parent, Object[] pass) {
        Group group = new Group(parent, SWT.NULL);
        group.setLayout(new FillLayout());
        group.setText("Properties");

        Table aTable = new Table(group, SWT.BORDER | SWT.FULL_SELECTION);
        aTable.setHeaderVisible(true);

        TableColumn column1 = new TableColumn(aTable, SWT.NULL);
        column1.setText("Name");

        TableColumn column2 = new TableColumn(aTable, SWT.NULL);
        column2.setText("Value");

        TableLayout tLayout = new TableLayout();
        tLayout.addColumnData(new ColumnWeightData(30, 100, true));
        tLayout.addColumnData(new ColumnWeightData(70, 300, true));
        aTable.setLayout(tLayout);
        aTable.setLinesVisible(true);
        if (pass.length > 1)
            pass[1] = aTable;

        TableViewer tviewer = new TableViewer(aTable);
        configureTableViewer(tviewer);
        pass[0] = tviewer;
        return group;
    }

    public void configureTableViewer(TableViewer tviewer) {
        tviewer.setContentProvider(new TableContentProvider());
        tviewer.setLabelProvider(new XMLTableLabelProvider());
        tviewer.setColumnProperties(new String[] { "name", "value" });
    }

    public static void ConfigureTableViewer(TableViewer tviewer) {
        (new ACPFormUtils()).configureTableViewer(tviewer);
    }

    public static Composite CreateTable(Composite parent, Object[] pass) {
        return (new ACPFormUtils()).createTable(parent, pass);
    }

    public void setSuppress(boolean suppress) {
        this.suppress = suppress;
    }

}
