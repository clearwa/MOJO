/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
*/

package com.objectforge.mascot.machine.model.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.objectforge.mascot.machine.device.Device;
import com.objectforge.mascot.machine.estore.EntityStore;
import com.objectforge.mascot.machine.estore.EsSubsystem;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.DeferredRef;
import com.objectforge.mascot.machine.model.IMascotReferences;
import com.objectforge.mascot.machine.model.MascotEntities;
import com.objectforge.mascot.machine.model.SETEntity;
import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.utility.MascotRuntimeException;
import com.objectforge.mascot.utility.MascotUtilities;
import com.objectforge.mascot.xml.AbstractMachineXML;
import com.objectforge.mascot.xml.MascotXmlChangedException;

/**
 * MachineXML is a concrete implmentaion of AbstractMachineXML.  It extends this class with
 * specific methods that support the parsing of ACP files to create structures specific to the
 * Mascot Machine.  In particular, it fills an EntityStore with the contents of a parsed
 * ACP file.
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2 $
 *
 */
public class MachineXML extends AbstractMachineXML {
    public final static String JAR_FILE = "mascot.machine.xmljar";
    public final static String TAGS_ENTRY = "mascot.machine.xmltags";
    Document xmldoc;

    /*The static initialization of the tag table.  JAR_FILE should be in the classpath; the
     * initialization file TAGS_ENTRY in that file.  The tag file defines the structure of
     * SETS documents and is shared betweem tje ACP editor and the Mascot Machine.
     */
    static {
        InputStream xml_file =
            MascotUtilities.mascotOpen(MascotUtilities.getMascotResource(MascotUtilities.TAGS_ENTRY));
        if (xml_file == null) {
            MascotUtilities.throwMRE("Tags file cannot be found");
        }
        tags = (new MachineXML()).new TagTable(xml_file);
    }

    /**
     */
    public MachineXML() {
        listeners = new ArrayList();
    }

    /**
     * Method MachineModelFromStream.
     * Read and parse an ACP document from input.  Upon sucess return a NodeList of 
     * the top level SET nodes.
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static NodeList MachineModelFromStream(InputStream input, MachineXML mxml)
        throws FactoryConfigurationError, ParserConfigurationException, SAXException, IOException {

        try {
            mxml.xmldoc = ACPDocumentFromStream(input, false);
        } catch (MascotXmlChangedException e) {
        }
        //OK, the document is parsed, now find the top level SET constructs
        NodeList sets = mxml.xmldoc.getElementsByTagName(SET_TAG);
        for (int i = 0; i < sets.getLength(); i++) {
            MascotDebug.println(9, "Node " + i + " - " + sets.item(i));
        }
        return sets;
    }

    public static NodeList MachineModelIncludes(MachineXML mxml) throws MascotRuntimeException {
        if (mxml.xmldoc == null)
            throw new MascotRuntimeException("MachinXML<MachineModelIncludes>:null xml document");

        //Get the included document nodes
        NodeList includes = mxml.xmldoc.getElementsByTagName(INCLUDE_TAG);
        for (int i = 0; i < includes.getLength(); i++) {
            MascotDebug.println(9, "Node " + i + " - " + includes.item(i));
        }
        return includes;
    }

    /**
     * Check name and reference strings.
     * The rule is that if name is set then return name, if reference is set but name is null, return 
     * the reference, and if both are null then throw exception.
     * 
     * @param name
     * @param reference
     * @return
     * @throws MascotMachineException
     */
    private class CheckRef {
        String name;
        String reference;

        CheckRef(Element entity) throws MascotMachineException {
            //Name
            name = entity.getAttribute(AbstractMachineXML.NAME_ATTR);
            //Reference
            reference = entity.getAttribute(AbstractMachineXML.REFERENCE_ATTR);
            boolean blankName = (name == null) || name.equals("");
            boolean blankReference = (reference == null) || reference.equals("");
            if (blankName && blankReference) {
                throw new MascotMachineException("EntityStore<checkRef>: Null name and reference");
            }
            if (blankName) {
                name = reference;
            } else if (blankReference) {
                reference = name;
            }
        }
    }

    /**
     * Method fillSubsystem.
     * A method to fill in the subsystem structure for each SET.  The method specifically
     * looks for and parses IDAs
     */
    protected void fillSubsystem(EsSubsystem eiSub, Element node, EntityStore es)
        throws MascotMachineException {
        NodeList children = node.getChildNodes();

        //for each of the subsystem children
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element))
                continue;

            Element entity = (Element) children.item(i);
            try {
                CheckRef checked = new CheckRef(entity);
                //Activity references
                if (entity.getTagName().equals(AbstractMachineXML.ACTIVITY_REF_TAG)) {
                    //Activities may have arguments
                    eiSub.putReference(
                        new DeferredRef(
                            checked.name,
                            checked.reference,
                            getArgsFor(entity),
                            IMascotReferences.ACTIVITY_REF,
                            null));
                }
                //Subsystem references
                else if (entity.getTagName().equals(AbstractMachineXML.SUBSYS_REF_TAG)) {
                    //Close on empty
                    boolean termFlag =
                        entity.getAttribute(AbstractMachineXML.CLOSE_ON_EMPTY_ATTR).equals("true");
                    DeferredRef subRef =
                        new DeferredRef(
                            checked.name,
                            checked.reference,
                            getArgsFor(entity),
                            IMascotReferences.SUBSYSTEM_REF,
                            null);
                    eiSub.putReference(subRef);
                    subRef.setFlag(termFlag);
                }
                //The various kinds of IDA references
                //Local IDA
                else if (entity.getTagName().equals(AbstractMachineXML.LOCAL_IDA_REF_TAG)) {
                    eiSub.putReference(
                        new DeferredRef(
                            checked.name,
                            checked.reference,
                            null,
                            IMascotReferences.LOCAL_IDA_REF,
                            null));
                }
                //Device Reference IDA
                else if (entity.getTagName().equals(AbstractMachineXML.DEVICE_IDA_REF_TAG)) {
                    eiSub.putReference(
                        new DeferredRef(
                            checked.name,
                            checked.reference,
                            null,
                            IMascotReferences.DEVICE_IDA_REF,
                            null));
                }
                /**
                 * The following references do not need to be deferred
                 */
                //Argument
                else if (entity.getTagName().equals(AbstractMachineXML.ARGUMENT_IDA_REF_TAG)) {
                    eiSub.argumentIDARef(checked.name, checked.reference);
                }
                //Container
                else if (entity.getTagName().equals(AbstractMachineXML.CONTAINER_IDA_REF_TAG)) {
                    eiSub.containerIDARef(checked.name, checked.reference);
                }
                //Global
                else if (entity.getTagName().equals(AbstractMachineXML.GLOBAL_IDA_REF_TAG)) {
                    eiSub.globalIDARef(checked.name, checked.reference);
                }
            } catch (MascotMachineException e) { //ignore
                MascotDebug.println(9, "MXML: " + e);
                throw e;
            }
        }
    }

    class Arguments {
        HashMap resources = new HashMap();
        HashMap args = new HashMap();
    }

    //Array of argument tags
    private final String[] argTags = { "String", "Integer" };

    protected Map getArgsFor(Element entity) throws MascotMachineException {
        Arguments retval = new Arguments();
        NodeList children = entity.getChildNodes();
        for (int k = 0; k < children.getLength(); k++) {
            if( !(children.item(k) instanceof Element )){
                continue;
            }
            Element child = (Element) children.item(k);
            if( !child.getTagName().equals(AbstractMachineXML.ARGUMENTS_TAG)){
                continue;
            }
            for (int i = 0; i < argTags.length; i++) {
                NodeList argList = child.getElementsByTagName(argTags[i]);
                int length = argList.getLength();

                for (int j = 0; j < length; j++) {
                    Element argnode = (Element) argList.item(j);
                    //Index
                    Object index = argnode.getAttribute(AbstractMachineXML.INDEX_ATTR);
                    //Value
                    Object value = argnode.getAttribute(AbstractMachineXML.VALUE_ATTR);
                    //Always put the argument value away no matter what it is
                    retval.resources.put(EntityStore.checkArgKey((String) index), value);
                    //If the declared type was Integer then try a conversion
                    if (argTags[i].equals("Integer")) {
                        try {
                            value = new Integer((String) value);
                        } catch (NumberFormatException e2) {
                            MascotDebug.println(
                                0,
                                "MachineXML<getArgsFor>: Value '"
                                    + value
                                    + "' cannot be converted to an integer");
                            continue;
                        }
                        //If the conversion succeeds then put the integer away
                        retval.resources.put( index, value );
                    }
                    //All integer indicies are for the argument array.  Note that value
                    //will be the proper type at this point
                    try {
                        //See if I can enter in the argument array as well
                        Integer idx = new Integer((String) index);
                        //Add the value as a string
                        retval.args.put(idx, value);
                        //... and remove the index from the resources table
                        retval.resources.remove(index);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        //Create the argument array form the contents of the args map.
        Vector args = new Vector(10);
        args.setSize(10);
        for (Iterator i = retval.args.keySet().iterator(); i.hasNext();) {
            Integer index = (Integer) i.next();
            if (index.intValue() > args.size()) {
                args.setSize(index.intValue() + 1);
            }
            args.set(index.intValue(), retval.args.get(index));
        }
        retval.resources.put(EntityStore.ACTIVITY_ARGS, args);
        return retval.resources;
    }

    protected void fillSET(EntityStore estore, InputStream input)
        throws
            MascotMachineException,
            FactoryConfigurationError,
            ParserConfigurationException,
            SAXException,
            IOException {
        //Get the list of SET nodes
        NodeList nodes = MachineModelFromStream(input, this);

        //for each SET node in the list fill in the entity store
        for (int i = 0; i < nodes.getLength(); i++) {
            Element setNode = (Element) nodes.item(i);
            NodeList entities = setNode.getChildNodes();
            String setName = setNode.getAttribute(AbstractMachineXML.NAME_ATTR);
            SETEntity theSET;
            if (!estore.getSetDescriptors().containsKey(setName)) {
                theSET = (SETEntity) estore.createSET(setName).getParentEntity();
            } else {
                theSET = estore.getSET(setName);
            }
            theSET.getResources().putAll(getArgsFor(setNode));
            for (int children = 0; children < entities.getLength(); children++) {
                if (entities.item(children).getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) entities.item(children);
                    String name = child.getTagName();
                    //Subsystems
                    if (name.equals(AbstractMachineXML.SUBSYSTEM_TAG)) {
                        //Defer the subsystem processing until the other entities are defined
                        processSubsystemNodes(child, estore, setName);
                    }
                    //Activities
                    else if (name.equals(AbstractMachineXML.ACTIVITY_TAG)) {
                        estore.createActivity(
                        //Name
                        child.getAttribute(AbstractMachineXML.NAME_ATTR),
                        //Root
                        child.getAttribute(AbstractMachineXML.ROOT_ATTR),
                        //Factory
                        child.getAttribute(AbstractMachineXML.FACTORY_ATTR), setName);
                    }
                    //Devices
                    else if (name.equals(AbstractMachineXML.DEVICE_TAG)) {
                        estore.createDevice(
                        //Device name
                        child.getAttribute(AbstractMachineXML.DEVICE_NAME_ATTR),
                        //Handler
                        child.getAttribute(AbstractMachineXML.HANDLER_ATTR), setName);
                    }
                    //IDAs
                    else if (name.equals(AbstractMachineXML.IDA_TAG)) {
                        estore.createIDA(
                        //Name
                        //The name is checked here to assure that there is no possibility that
                        //name will conflict with the device pool name.
                        Device.checkPoolName(child.getAttribute(AbstractMachineXML.NAME_ATTR)),
                        //Implementation
                        child.getAttribute(AbstractMachineXML.IMP_ATTR),
                        //Factory
                        child.getAttribute(AbstractMachineXML.FACTORY_ATTR),
                        //Type
                        child.getAttribute(AbstractMachineXML.TYPE_ATTR), setName);
                    }
                }
            }
        }
        //Process the includes
        nodes = MachineModelIncludes(this);
        for (int j = 0; j < nodes.getLength(); j++) {
            Element includeNode = (Element) nodes.item(j);
            //Filename
            String filename = includeNode.getAttribute(AbstractMachineXML.FILENAME_ATTR);

            InputStream stream = MascotUtilities.mascotOpen(filename);
            fillSET(estore, stream);
        }
    }

    /**
     * Method getSETS.
     * Instance method that returns an EntityStore filled with data from an SETS/ACP document.
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public EntityStore getSETS(InputStream input)
        throws
            FactoryConfigurationError,
            ParserConfigurationException,
            SAXException,
            IOException,
            MascotMachineException {
        xmldoc = null;

        EntityStore estore = EntityStore.entityStoreFactory();
        fillSET(estore, input);
        return estore;
    }

    void processSubsystemNodes(Element subsysNode, EntityStore estore, String setName)
        throws MascotMachineException {
        //Type
        String type = subsysNode.getAttribute(AbstractMachineXML.TYPE_ATTR);
        //Name
        String name = subsysNode.getAttribute(AbstractMachineXML.NAME_ATTR);
        EsSubsystem eiSub = null;

        if (type.equals(AbstractMachineXML.GLOBAL_TYPE) || name.equals(AbstractMachineXML.GLOBAL_TYPE)) {
            //The global subsystem always exists, pick up the current incarnation
            eiSub =
                (EsSubsystem) ((MascotEntities) estore
                    .getSubsystemDescriptors()
                    .get(AbstractMachineXML.GLOBAL_TYPE))
                    .getCurrentIncarnation();
        } else {
            //Name
            name = subsysNode.getAttribute(AbstractMachineXML.NAME_ATTR);
            //Close on empty
            boolean termFlag = subsysNode.getAttribute(AbstractMachineXML.CLOSE_ON_EMPTY_ATTR).equals("true");
            //fill in the subsystem from its children
            try {
                eiSub = estore.createSubsystem(type, name, termFlag, setName);
            } catch (MascotMachineException e) {
                MascotDebug.println(9, e.getMessage());
            }
        }
        fillSubsystem(eiSub, subsysNode, estore);
    }
}
