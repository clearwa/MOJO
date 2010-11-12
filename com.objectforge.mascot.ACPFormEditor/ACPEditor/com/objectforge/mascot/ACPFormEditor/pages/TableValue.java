/**
 * The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *
 *
 * Portions derrived from code supplied as part of the Eclipse project
 *     All Copyrights apply
*/


package com.objectforge.mascot.ACPFormEditor.pages;

import org.w3c.dom.Node;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name:  $
 *
 */
public class TableValue {
	public String name = null;
	public String value = null;
	public Node node = null;
	public boolean canModify = true;

	public TableValue(String name, String value) {
		this.name = name;
		this.value = value;
	}
}

