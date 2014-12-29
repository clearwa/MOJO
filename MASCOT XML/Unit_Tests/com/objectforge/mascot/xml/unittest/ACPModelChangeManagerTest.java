package com.objectforge.mascot.xml.unittest;

import com.objectforge.mascot.utility.MascotDebug;
import com.objectforge.mascot.xml.ACPModelChangeManager;
import com.objectforge.mascot.xml.ACPModelEvent;
import com.objectforge.mascot.xml.IACPModelChangeListener;

import junit.framework.TestCase;

/**
 * @author Clearwa
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ACPModelChangeManagerTest extends TestCase {

	/**
	 * Constructor for ACPModelChangeManagerTest.
	 */
	public ACPModelChangeManagerTest(String arg0) {
		super(arg0);
	}
	
	public static ACPModelEvent testEvent;

	/**
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAddListener() {
		ACPModelChangeManager cm = new ACPModelChangeManager();
		cm.addListener(new IACPModelChangeListener() {
			public void acpModelChanged(ACPModelEvent ev) {
				MascotDebug.println(0,"Change event fired");
			}
		});

	}

	public void testRemoveListener() {
	}

	public void testFireACPModelChangeEvent() {
		ACPModelChangeManager cm = new ACPModelChangeManager();
		ACPModelEvent event = new ACPModelEvent( "This is an event" );

		cm.addListener(new IACPModelChangeListener() {
			public void acpModelChanged(ACPModelEvent ev) {
				System.out.println("Change event fired");
				ACPModelChangeManagerTest.testEvent = ev;
			}
		});
		cm.fireACPModelChangeEvent( event );
		assertEquals( event,testEvent );
	}

}
