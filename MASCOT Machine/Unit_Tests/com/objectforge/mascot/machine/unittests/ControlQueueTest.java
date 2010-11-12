package com.objectforge.mascot.machine.unittests;

import junit.framework.TestCase;
import com.objectforge.mascot.machine.scheduler.*;

/**
 * Copyright Object Forge, 2002,2003
 * @author - Clearwa
 * @date - 17-Jan-2003
 * 
 * 
 */
public class ControlQueueTest extends TestCase {
	public static StimObject testSO1 = new StimObject();
	public static ControlQueue testCQ1 = new ControlQueue();

	/**
	 * Constructor for ControlQueueTest.
	 */
	public ControlQueueTest(String arg0) {
		super(arg0);
	}

	/**
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test for int cqJoin()
	 */
	public void testCqJoin() {
	}

	/*
	 * Test for int cqJoin(SOInterface)
	 */
	public void testCqJoinSOInterface() {
		testCQ1.cqJoin( testSO1 );
		testCQ1.cqStim();
		testCQ1.cqWait();		
		testCQ1.cqLeave();
	}

	public void testCqLeave() {
		testCQ1.cqLeave();
	}

	public void testCqStim() {
		testCQ1.cqStim();
	}

	public void testCqWait() {
		testCQ1.cqWait();
	}

}
