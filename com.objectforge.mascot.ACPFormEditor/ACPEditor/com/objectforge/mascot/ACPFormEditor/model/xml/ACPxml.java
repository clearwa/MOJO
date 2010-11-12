/**
 * The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *
 *
 * Portions derrived from code supplied as part of the Eclipse project
 *     All Copyrights apply
*/


package com.objectforge.mascot.ACPFormEditor.model.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Element;

import com.objectforge.mascot.ACPFormEditor.internal.ACPFormEditorPlugin;
import com.objectforge.mascot.xml.AbstractMachineXML;
/**
 * @author Allan Clearwaters
 * @version $Id$, $Name:  $
 *
 */
public class ACPxml extends AbstractMachineXML {
	public ACPxml(){
		listeners = new ArrayList();
		ACPFormEditorPlugin plugin = ACPFormEditorPlugin.getDefault();
        // The the tagfile.  This can be in a number of places so the resource string may be a 
        // ';' delimited string of paths.  Tokenize the string and see which path I can open.
		StringTokenizer tokens = 
            new StringTokenizer( ACPFormEditorPlugin.getResourceString(MX_TAGFILEKEY),";" );
        InputStream xmlfile = null;
        
        while( tokens.hasMoreTokens() ){
        	Path pathToken = new Path( tokens.nextToken());
            try {
            	xmlfile = FileLocator.openStream(plugin.getBundle(), pathToken, false);
                //xmlfile = plugin.openStream(new Path( tokens.nextToken() );
                break;
            } catch (IOException e) {
                System.out.println( "TagTable: cannot find " + pathToken );
            }            
        }

		// If I couldn't open a file then simply bag it
        if ( xmlfile==null )
			return;
		tags = new TagTable( xmlfile );
	}
    
    /**
     * Create a new element in the current document with the tagvalue of tag.  The defined attributes are
     * there but empty
     * 
     * @param tag
     * @return
     */
    public Element createElement( String tag ){
        //Make sure the tag is valid
        if( !checkTag( tag )){
            return null;
        }
        //Create the new element and get it's attributes
        Element element = document.createElement( tag );
        Hashtable attributes = getAttributesFor( tag );
        for( Enumeration i=attributes.keys();i.hasMoreElements(); ){
            String key = (String) i.nextElement();
            element.setAttribute( key, "");
        }
        return element;
    }

}
