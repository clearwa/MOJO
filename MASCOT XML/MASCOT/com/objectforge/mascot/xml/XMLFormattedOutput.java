/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
 *
*/


package com.objectforge.mascot.xml;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Given an XML dom document produce a readable XML representation.  While the Document's 
 * toString() method for will do this as well but the output is not well formatted for
 * reading.  Thus this class. 
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 *
 */
public class XMLFormattedOutput {
    static final String COMMENT_OPEN = "<!-- ";
    static final String COMMENT_CLOSE = " -->";
    static final String TAG_OPEN = "<";
    static final String TAG_CLOSE = ">";
    static final String QUOTE = "\"";
    static final String SLASH = "/";
    public final String SPACE = " ";

    PrintWriter collector;
    Document doc;
    int indent = 0;
    StringWriter myWriter;
    
    /**
     * Method XMLFormattedOutput.
     * Create an instance for the input Document doc
     */
    public XMLFormattedOutput(Document doc) {
        this.doc = doc;
        myWriter = new StringWriter();
        collector = new PrintWriter(myWriter);
    }

    /**
     * Override the default toString() method.  A call produces a formatted
     * string representing doc as an XML document
     */
    public String toString() {
        Node topnode = doc.getDocumentElement();

        collector.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        indent = 0;
        printNode(topnode);
        collector.close();
        return myWriter.getBuffer().toString();
    }

    /**
     * Method indentString.
     * Returns a string of tabs that expresses the indent for the current
     * output level
     */
    protected String indentString() {
        String is = "\n";

        for (int i = 0; i < indent; i++)
            is += "\t";
        return is;
    }

    /**
     * Method printNode.
     * Output a a comment or element node
     */
    protected void printNode(Node currentNode) {
        switch (currentNode.getNodeType()) {
            case Node.COMMENT_NODE :
                collector.print(
                    indentString() + COMMENT_OPEN + currentNode.getNodeValue() + COMMENT_CLOSE);
                break;

            case Node.ELEMENT_NODE :
                printElementNode((Element) currentNode);
                break;

            default :
                break;
        }
    }

    /**
     * Method printElementNode.
     * Print an element node.  If there are children recurssively call printNode for
     * the next level.
     */
    protected void printElementNode(Element currentNode) {
        collector.print(indentString() + TAG_OPEN + currentNode.getNodeName());
        if (currentNode.hasAttributes())
            printAttributes(currentNode);

        if (!currentNode.hasChildNodes()) {
            collector.print(SLASH + TAG_CLOSE);
            return;
        }
        collector.print(TAG_CLOSE);

        NodeList children = currentNode.getChildNodes();
        indent++;
        for (int i = 0; i < children.getLength(); i++)
            printNode(children.item(i));

        indent--;
        collector.print(indentString() + TAG_OPEN + SLASH + currentNode.getNodeName() + TAG_CLOSE);
    }

    /**
     * Method printAttributes.
     * Print an attribute
     */
    protected void printAttributes(Element currentNode) {
        indent++;

        NamedNodeMap attributes = currentNode.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            collector.print(SPACE + attributes.item(i).getNodeName() + "=");
            collector.print(QUOTE + attributes.item(i).getNodeValue() + QUOTE);
        }
        indent--;
    }
}
