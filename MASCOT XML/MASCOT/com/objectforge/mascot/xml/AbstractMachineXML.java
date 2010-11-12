/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
*/

package com.objectforge.mascot.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotUtilities;

/**
 * This class as the root of the Mascot Machine and ACP editor classes that implement
 * reading SETS (*.acp) documents.  SET documents contain information describing Mascot
 * entities to the Mascot Machine and are generated via the Eclipse ACP editor plugin.
 * This class reads and parses 2 kinds of XML file:
 * 
 * The xml tag file
 * The tag file contains structural information about the relationship between the different
 * tags in an SET document.  The tags mean:
 *  <acptag value=name defaultattr=...> - Document element tags. Value is the tag name 
 *                  in a SETS doc.  Defaultattr is the default attribute that will be 
 *                  displayed in a properties editor for a particular acptag.
 *      <attrib value=name editor=> - An attribute for a tag, value is the name.  Editor may
 *                  be missing, empty, or one of "combo" or "text"
 *          <entry value=...> - If the editor type is combo then these tags list the values
 *                  that fill the combo box for choices
 *      <allowed-new value=...> - Those tags that are allowed to be created under the current
 *                  acptag
 *      <display-label value=".."> - A text representation of the associated acptag.  If this is
 *                  not set then the acptag name is the default value.
 * This class expects the tag file to be read and parsed during static initialization.  As such, it
 * is the responsibility of the concrete subclasses to implement this process.
 * 
 * SETS documents
 * Once initialized this class reads and parses SETS documents.  The structure of these document is
 * described eslewhere.  The interpretation of the resulting Document object is dependent on context
 * and the concrete implementations.
 * 
 * There are 2 known concrete implementations, MachineXML and ACPwml. 
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 *
 */
 
public abstract class AbstractMachineXML {
    //Constants
    public final static String DEFAULT_TYPE = "global";
    
    /**
     * SETs docment parse constants
     * 
     * These are the tags and attributes that constitute the legal contents of a SETs
     * document.  If the contents of the tag file change then these need to change
     * as well.
     */
    public final static String SETS_TAG = "SETS"; //The top level tag
    //SETS consist of these
    public final static String SET_TAG = "SET";
    public final static String INCLUDE_TAG = "Include";
    public final static String ENTITY_MAPS_TAG = "entity-maps";
    //Include attributes
    public final static String FILENAME_ATTR = "Filename";
    //Any set has these
    public final static String SUBSYSTEM_TAG = "Subsystem";
    public final static String ACTIVITY_TAG = "Activity";
    public final static String DEVICE_TAG = "Device";
    public final static String IDA_TAG = "IDA";
    //Common attribute tags
    public final static String NAME_ATTR = "name";
    public final static String TYPE_ATTR = "type";
    public final static String FACTORY_ATTR = "factory";
    public final static String REFERENCE_ATTR = "reference";
    public final static String DEFAULT_NAME = "default";
    public final static String GLOBAL_TYPE = "global";
    public final static String SUBSYSTERM_TYPE = "subsystem";
    //Activity specific attributes
    public final static String ROOT_ATTR = "root";
    //Device specific tags
    public final static String DEVICE_NAME_ATTR = "device-name";
    public final static String HANDLER_ATTR = "handler";
    //IDA specific tags
    public final static String IMP_ATTR = "imp";
    
    //Subsystem specific attributes
    public final static String CLOSE_ON_EMPTY_ATTR = "close_on_empty";
    //For Subsystems these are the reference tags.  Each have a name and reference attribute
    public final static String ACTIVITY_REF_TAG = "activity-ref";
    public final static String SUBSYS_REF_TAG = "subsys-ref";
    public final static String LOCAL_IDA_REF_TAG = "localIDA-ref";
    public final static String CONTAINER_IDA_REF_TAG = "containerIDA-ref";
    public final static String GLOBAL_IDA_REF_TAG = "globalIDA-ref";
    public final static String ARGUMENT_IDA_REF_TAG = "argumentIDA-ref";
    public final static String DEVICE_IDA_REF_TAG = "deviceIDA-ref";
    public final static String[] REF_TAGS = new String[] {ACTIVITY_REF_TAG,
        SUBSYS_REF_TAG,
        LOCAL_IDA_REF_TAG,
        CONTAINER_IDA_REF_TAG,
        GLOBAL_IDA_REF_TAG,
        ARGUMENT_IDA_REF_TAG,
        DEVICE_IDA_REF_TAG
    };
    
    //The argument tag
    public final static String ARGUMENTS_TAG = "arguments";
    //Argument values
    public final static String INT_ARG_TAG = "Integer";
    public final static String STRING_ARG_TAG = "String";
    //Argument value attributes
    public final static String INDEX_ATTR = "index";
    public final static String VALUE_ATTR = "value";
    
    //Entity maps
    //Entity maps attributes
    public final static String PLATFORM_ATTR = "platform";
    //Individual enttity map elements have name and imp attributes
    public final static String ENTITY_MAP_TAG = "entity-map";
    
    //End of constants
    
    protected ArrayList listeners;
    protected Document document;
    protected static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    //xmlDirty is used to tag instances of this class as being 'dirty', ie. the representation
    //held internall in in doc is modified with respect to the XML file that was parsed to
    //create it originally
    protected boolean xmlDirty = false;

    //valid is set true upon a successful parse
    protected boolean valid = false;
    protected ResourceBundle resourceBundle;

    //These must be filled by static initialization
    protected static TagTable tags;

    public static final String MX_TAGFILEKEY = "mascot.acp.xmltagfile";

    /**
     *
     * An instance of this class holds the in-core representation of the XML tag
     * file.
     */
    protected class TagTable {
        Hashtable tagtable = new Hashtable();

        /**
         * Method TagTable.
         * 
         * Build a representation of the tag file in memory
         */
        public TagTable(InputStream xmlfile) {
            DocumentBuilder builder;

            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                MascotDebug.println(9, "TagTable: parse config error - " + e);
                return;
            }

            try {
                Document document = builder.parse(xmlfile);

                NodeList nodes = document.getElementsByTagName("acptag");
                // for each defined tag in the input document
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (!(nodes.item(i) instanceof Element))
                        continue;

                    //tagnode to the node for a particular tag
                    Element tagnode = (Element) nodes.item(i);
                    if (!tagnode.hasAttribute("value"))
                        continue;

                    //if has this attribute then make a note
                    String defaultval =
                        (tagnode.hasAttribute("defaultattr")) ? tagnode.getAttribute("defaultattr") : null;

                    //find all the attribute tags.  By default the hashtable is empty
                    NodeList attribs = tagnode.getElementsByTagName("attrib");
                    Hashtable attribTable = new Hashtable();
                    tagtable.put(tagnode.getAttribute("value"), attribTable);
                    Hashtable attributes = new Hashtable();
                    attribTable.put("attributes", attributes);

                    //fof all the attrib tags
                    for (int j = 0; j < attribs.getLength(); j++) {
                        NamedNodeMap nodemap = attribs.item(j).getAttributes();
                        Node node;

                        if ((node = nodemap.getNamedItem("value")) != null) {
                            Hashtable nodeTable = new Hashtable();
                            TagtableEntry tte =
                                new TagtableEntry(true, defaultval.equals(node.getNodeValue()), nodeTable);

                            attributes.put(node.getNodeValue(), tte);
                            addAttribEntries(nodemap, nodeTable, attribs.item(j));
                        }
                    }

                    //now look at entrites that define the what nodes can be insterted
                    attribs = tagnode.getElementsByTagName("allowed-new");
                    String[] allowedTable = new String[attribs.getLength()];
                    attribTable.put("allowed-new", allowedTable);

                    for (int k = 0; k < attribs.getLength(); k++) {
                        Node allowed = attribs.item(k).getAttributes().getNamedItem("value");

                        if (allowed != null)
                            allowedTable[k] = allowed.getNodeValue();
                    }
                    
                    //Add an ehtry for the default text label
                    String displayText = ( tagnode.hasAttribute( "display-label"))?tagnode.getAttribute("display-label"):tagnode.getAttribute("value");
                    attribTable.put("display-label",displayText);
                }
            } catch (SAXException e) {
                MascotDebug.println(9, "tagbuilder: " + e);
            } catch (IOException e) {
                MascotDebug.println(9, "tagbuilder: " + e);
            }
            return;
        }

        /**
         * Method addAttribEntries.
         * 
         * Fill in the editor representation.  By default the editor is text
         */
        private void addAttribEntries(NamedNodeMap nodes, Hashtable table, Node item) {
            Node editor;

            if ((editor = nodes.getNamedItem("editor")) == null) {
                table.put("editor", "text");
                return;
            }
            table.put("editor", editor.getNodeValue());
            if (item instanceof Element && editor.getNodeValue().equals("combo")) {
                NodeList entries = ((Element) item).getElementsByTagName("entry");
                String[] strings = new String[entries.getLength()];

                for (int i = 0; i < entries.getLength(); i++) {
                    Node map = entries.item(i).getAttributes().getNamedItem("value");

                    if (map != null)
                        strings[i] = map.getNodeValue();
                }
                table.put("strings", strings);
            }
        }

        /**
         * Method getValueFor.
         */
        public Object getValueFor(String key1, String key2) {
            return ((Hashtable) tagtable.get(key1)).get(key2);
        }

        /**
         * Method getValuesFor.
         */
        public Object getValuesFor(String key) {
            return tagtable.get(key);
        }

        public Hashtable getTagtable() {
            return tagtable;
        }
        

    }

    static class Tester {
        boolean success = true;
        DocumentBuilder builder;
        Document xdocument;
        
        public Tester( InputStream input ) throws ParserConfigurationException, SAXException, IOException{
            builder  = factory.newDocumentBuilder();
            xdocument = builder.parse(input);
        }
        
        public Document getDocument(){
            return xdocument;
        }
            
        public boolean testItem(NamedNodeMap current, String attrString, String defaultString) {
            boolean modified = false;

            if (current.getNamedItem(attrString) == null) {
                modified = true;
                Attr node = xdocument.createAttribute(attrString);
                node.setValue(defaultString);
                current.setNamedItem(node);
            }
            return modified;
        }
            
        public boolean getSuccess(){
            return success;
        }
            
        public void setSuccess( boolean b ){
            success &= b;
        }
            
        /**
         * Items that must always hava a name
         */
        public void checkTag( String listTag, String attrTag, String defaultTag){
            NodeList sets = xdocument.getElementsByTagName( listTag );
            for (int i = 0; i < sets.getLength(); i++) {
                NamedNodeMap current = sets.item(i).getAttributes();
                if (testItem(current, attrTag, defaultTag)) {
                    Comment comment = xdocument.createComment("**MODIFIED " + attrTag + "**");
                    sets.item(i).getParentNode().insertBefore(comment, sets.item(i) );
                    setSuccess(false);
                }
            }
        }
    }       
    /**
     * Method ACPDocumentFromStream.
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws MascotXmlChangedException
     * 
     * Create a DOM document from input.  As part of this process the document is checked
     * for validity and modified to conform to a minimum standard.  If the takeException flag
     * is set the method exits via MascotXmlChangedException upon a successful parse.  This allows
     * the caller to detect the case where a successful parse results from a
     * modified document.  If this flag is false then the dom document is always returned regardless
     * of modification if no other exceptions occur.
     */
    public static Document ACPDocumentFromStream(InputStream input, boolean takeException)
        throws
            FactoryConfigurationError,
            ParserConfigurationException,
            SAXException,
            IOException,
            MascotXmlChangedException {

        Tester tester = new Tester(input);
        Random itag = new Random();
        
        /*
         * Need code here to test preconditions for a well formed ACP document
        */
        
        //Check subsystems
        tester.checkTag( SUBSYSTEM_TAG, NAME_ATTR, "subsystem" + itag.nextInt(100));
        tester.checkTag( SUBSYSTEM_TAG, TYPE_ATTR, "subsystem");
        //Check the rest of the entities
        tester.checkTag( SET_TAG, NAME_ATTR, "default" );
        tester.checkTag( DEVICE_TAG, DEVICE_NAME_ATTR, "device" + itag.nextInt(100));
        tester.checkTag( ACTIVITY_TAG, NAME_ATTR, "activity" + itag.nextInt(100) );
        tester.checkTag( IDA_TAG , NAME_ATTR, "IDA" + itag.nextInt(100));
        //Check that the references have a name
        for( int i = 0;i<REF_TAGS.length; i++){
            tester.checkTag( REF_TAGS[i], NAME_ATTR, REF_TAGS[i] + itag.nextInt(100) );      
        }
        
        if (tester.getSuccess() && takeException)
            throw new MascotXmlChangedException("Valid ACP document");
        return tester.getDocument();
    }

    /**
     * Method ACPDocumentToString.
     * @throws IOException
     * Create a string that represents the input Document as XML
     */
    public static String ACPDocumentToString(Document doc) throws IOException {
        XMLFormattedOutput xmloutput = new XMLFormattedOutput(doc);
        return xmloutput.toString();
    }

    /**
     * Method addACPxmlEventListener.
     * Add a listener for events relating to the parsing ACP files
     */
    public void addACPxmlEventListener(MachineXmlListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    /**
     * Method removeACPxmlEventListener.
     */
    public void removeACPxmlEventListener(MachineXmlListener listener) {
        if (!listeners.contains(listener))
            listeners.remove(listener);
    }

    /**
     * Method fireACPxmlEvent.
     */
    public void fireACPxmlEvent(MachineXmlEvent event) {
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            MachineXmlListener listener = (MachineXmlListener) i.next();
            listener.xmlModelEvent(event);
        }
    }
    /**
     * Returns the document.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Sets the document.
     * The document to set
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * Method setDocument.
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws MascotXmlChangedException
     * Instance method to parse a document.  See the class method
     */
    public void setDocument(InputStream input, boolean takeException)
        throws
            FactoryConfigurationError,
            ParserConfigurationException,
            SAXException,
            IOException,
            MascotXmlChangedException {
        document = ACPDocumentFromStream(input, takeException);
    }

    /**
     * Method toXML.
     * @throws IOException
     * Instance method to dump the contents of dos as an XML string
     */
    public String toXML() throws IOException {
        return ACPDocumentToString(document);
    }

    /**
     * Returns the xmlDirty.
     * 
     */
    public boolean isXmlDirty() {
        return xmlDirty;
    }

    /**
     * Sets the xmlDirty.
     */
    public void setXmlDirty(boolean xmlDirty) {
        this.xmlDirty = xmlDirty;
    }

    /**
     * Returns the valid.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the valid.
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Returns the resourceBundle.
     * Return the resource bundle that was loaded to find the location of the XML
     * file used to create the TagTable.  This process is part of the class' static
     * initialization and must be implemented by subclasses.
     */
    public ResourceBundle getResourceBundle() {
        return MascotUtilities.getMascotBundle();
    }

    /**
     * Method getValueFor.
     * User access to the tag table
     */
    public Object getValueFor(String key1, String key2) {
        return tags.getValueFor(key1, key2);
    }

    /**
     * Method getValuesFor.
     * User access to the tag table
     */
    public Object getValuesFor(String key) {
        return tags.getValuesFor(key);
    }
    
    public boolean checkTag( String tag ){
        return tags.getTagtable().containsKey( tag );
    }

    /**
     * Method getAttributesFor.
     * Return a Hashtable that holds the attribute values for the tag key
     */
    public Hashtable getAttributesFor(String key) {
        return (Hashtable) ((Hashtable) getValuesFor(key)).get("attributes");
    }

    /**
     * Method getDefaultAttributeFor.
     * For the tag key find the default attribute if it is defined
     */
    public String getDefaultAttributeFor(String key) {
        Hashtable attribs = getAttributesFor(key);

        for (Enumeration keys = attribs.keys(); keys.hasMoreElements();) {
            String aKey = (String) keys.nextElement();
            if (((TagtableEntry) attribs.get(aKey)).defaultItem)
                return aKey;
        }
        return null;
    }
    
    /* Pull the display name for an acp tag */
    public String getDisplayLabel( String tag ){
        return (String) getValueFor( tag,"display-label");
    }

    /**
     * Method deleteNodes.
     * Concenience routine to remove a child node
     */
    public void deleteNodes(Object[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            Node node = (Node) nodes[i];

            try {
                node.getParentNode().removeChild(node);
            } catch (Exception e) {
                MascotDebug.println(9, "Node removal exception: " + e);
            } catch (Error er) {
                MascotDebug.println(9, "Node removal error: " + er);
            }
        }

    }

}
