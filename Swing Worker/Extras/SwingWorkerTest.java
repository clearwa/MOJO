/* 
 * $Id$
 * 
 * Copyright � 2005 Sun Microsystems, Inc. All rights
 * reserved. Use is subject to license terms.
 */
package test.org.jdesktop.swingworker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import util.org.jdesktop.swingworker.*;
import junit.framework.TestCase;

//import org.jdesktop.swingworker.SwingWorker.StateValue;

public class SwingWorkerTest extends TestCase {

    private final static int TIME_OUT = 30;
    private final static TimeUnit TIME_OUT_UNIT = TimeUnit.SECONDS;
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.run(SwingWorkerTest.class);
    }
   
    
    // is to be run on a worker thread.
    public final void testdoInBackground() throws Exception {
        SwingWorker<Thread,?> test = new SwingWorker<Thread, Object>() {
            @Override
            protected Thread doInBackground() throws Exception {
                return Thread.currentThread();
            }
        };
        test.execute();
        Thread result = test.get(TIME_OUT, TIME_OUT_UNIT);
        assertNotNull(result);
        assertNotSame(Thread.currentThread(), result);
    }

    //{@code process} gets everything from {@code publish}
    //should be executed on the EDT
    public final void testPublishAndProcess() throws Exception {
        final Exchanger<List<Integer>> listExchanger = 
            new Exchanger<List<Integer>>();
        final Exchanger<Boolean> boolExchanger = 
            new Exchanger<Boolean>();
        SwingWorker<List<Integer>,Integer> test = 
            new SwingWorker<List<Integer>, Integer>() {
                List<Integer> receivedArgs = 
                    Collections.synchronizedList(new ArrayList<Integer>());
                Boolean isOnEDT = Boolean.TRUE;
                final int NUMBERS = 100;
                @Override
                protected List<Integer> doInBackground() throws Exception {
                    List<Integer> ret = 
                        Collections.synchronizedList(
                            new ArrayList<Integer>(NUMBERS));
                    for (int i = 0; i < NUMBERS; i++) {
                        publish(i);
                        ret.add(i);
                    }
                    return ret;
                }
                @Override
                protected void process(Integer... args) {
                    for(Integer i : args) {
                        receivedArgs.add(i);
                    }
                    isOnEDT = isOnEDT && SwingUtilities.isEventDispatchThread();
                    if (receivedArgs.size() == NUMBERS) {
                        try {
                            boolExchanger.exchange(isOnEDT);
                            listExchanger.exchange(receivedArgs);
                        } catch (InterruptedException ignore) {
                            ignore.printStackTrace();
                        }
                    }
                }
        };
        test.execute();
        assertTrue(boolExchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
        assertEquals(test.get(TIME_OUT, TIME_OUT_UNIT), 
            listExchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
    }

    // done is executed on the EDT
    // receives the return value from doInBackground using get()
    public final void testDone() throws Exception {
        final String testString  = "test"; 
        final Exchanger<Boolean> exchanger = new Exchanger<Boolean>();
        SwingWorker<?,?> test = new SwingWorker<String, Object>() {
            @Override
            protected String doInBackground() throws Exception {
                return testString;
            }
            @Override
            protected void done() {
                try {
                    exchanger.exchange(
                        testString == get()
                        && SwingUtilities.isEventDispatchThread());
                } catch (Exception ignore) {
                }
            }
        };
        test.execute();
        assertTrue(exchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
    }

    //PropertyChangeListener should be notified on the EDT only
    public final void testPropertyChange() throws Exception {
        final Exchanger<Boolean> boolExchanger = 
            new Exchanger<Boolean>();
        final SwingWorker<?,?> test = 
            new SwingWorker<Object, Object>() {
                @Override
                protected Object doInBackground() throws Exception {
                    firePropertyChange("test", null, "test");
                    return null;
                }
            };
        test.addPropertyChangeListener(
            new PropertyChangeListener() {
                boolean isOnEDT = true;

                public  void propertyChange(PropertyChangeEvent evt) {
                    isOnEDT &= SwingUtilities.isEventDispatchThread();
                    if ("state".equals(evt.getPropertyName())
                        && StateValue.DONE == evt.getNewValue()) {
                        try {
                            boolExchanger.exchange(isOnEDT);
                        } catch (Exception ignore) {
                            ignore.printStackTrace();
                        }
                    }
                }
            });
        test.execute();
        assertTrue(boolExchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
    }
    
    //the sequence should be
    //StateValue.STARTED, done, StateValue.DONE
    public final void testWorkFlow() throws Exception {
        final List<Object> goldenSequence = 
            Arrays.asList(new Object[]{StateValue.STARTED, "done", 
                                       StateValue.DONE});
        final List<Object> sequence = 
                    Collections.synchronizedList(new ArrayList<Object>());

        final Exchanger<List<Object>> listExchanger = new Exchanger<List<Object>>();
        
        final SwingWorker<?,?> test = 
            new SwingWorker<Object,Object>() {
                @Override
                protected Object doInBackground() throws Exception {
                    return null;
                }
                @Override
                protected void done() {
                    sequence.add("done");
                }
            };
        test.addPropertyChangeListener(
            new PropertyChangeListener() {
                public  void propertyChange(PropertyChangeEvent evt) {
                    if ("state".equals(evt.getPropertyName())) {
                        sequence.add(evt.getNewValue());
                        if (StateValue.DONE == evt.getNewValue()) {
                            try {
                                listExchanger.exchange(sequence);
                            } catch (Exception ignore) {
                                ignore.printStackTrace();
                            }
                        }
                    }
                }
            });
        test.execute();
        assertEquals(goldenSequence, 
                     listExchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
    }

}
