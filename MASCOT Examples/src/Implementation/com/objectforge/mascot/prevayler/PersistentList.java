/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package com.objectforge.mascot.prevayler;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeMap;

import org.prevayler.Query;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;

import com.objectforge.mascot.utility.MascotDebug;
/**
 * PersistentList
 * 
 * Project: MASCOT Examples
 * Package: com.objectforge.mascot.prevayler
 * Created on 09-Oct-2003 by @author Clearwa
 * 
 * This class comtains 2 lists, container and relay, and an underlying set that holds the union of
 * the list contents.The intent here is to assure no objects written to the
 * channel are not lost in the event of a system shutdown.  It is assumed that there is some period during
 * which an objects reader will be processing the object he just obtained.  When the reader has finished
 * processing it is assumed he will call discard to inform the channel is it safe to clense the 
 * object from the discard set.  In the event of an error/shutdown prior to the discard, the
 * persisted PersistentList will retain the read object in the discard list.  On restart, a call to 
 * elide adds the contents of the discard list to the channel contents so any discarded objects can
 * be reprocessed on the restart.
*/
public class PersistentList implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private ArrayList container = new ArrayList();
    private TreeMap relay = new TreeMap();
    private HashSet discard = new HashSet();

    /**
     * This tranaction reads an object from the container.  Prior to returning the object to
     * the caller it is retained in the discard list.  The caller is expected to make a call
     * to the discard method to remove the object for the discard list.
     * @return
     */
    public static class doReadContainerQuery implements TransactionWithQuery {
        /**
         * Serialized version
         */
        private static final long serialVersionUID = 1L;

        public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
            Object retval = ((PersistentList) prevalentSystem).container.remove(0);
            return retval;
        }
    }

    public static TransactionWithQuery readContainerQuery() {
        return new doReadContainerQuery();
    }

    /**
     * Do a read on the relay list
     * @return
     */
    public static class doReadRelayQuery implements TransactionWithQuery {
        /**
         * Serialized version
         */
        private static final long serialVersionUID = 1L;

        public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
            PersistentList myList = (PersistentList) prevalentSystem;
            try {
                return myList.relay.remove(myList.relay.firstKey());
            } catch (RuntimeException e) {
                MascotDebug.println(5, "PersistentList<readRelay>: No such element - return null");
                return null;
            }
        }
    }

    public static TransactionWithQuery readRelayQuery() {
        return new doReadRelayQuery();
    }

    /**
     * A user is expected to make a call to discard to clear an object from the discard list after
     * it has been read.  If the user forgets to nake this call then the discard will be retained
     * and added to the conainter when the system restarts.  This ensures that no object is lost
     * due to a system crash.
     * @param payload
     * @return
     */
    public static class doDiscard implements Transaction {
        /**
         * Serialized version
         */
        private static final long serialVersionUID = 1L;
        final Object payload;

        public doDiscard(Object payload) {
            this.payload = payload;
        }

        public void executeOn(Object prevalentSystem, Date executionTime) {
            if (!((PersistentList) prevalentSystem).container.contains(payload)
                && !((PersistentList) prevalentSystem).relay.values().contains(payload)) {
                ((PersistentList) prevalentSystem).discard.remove(payload);
            }
        }
    }

    public static Transaction discard(final Object payload) {
        return new doDiscard(payload);
    }

    /**
    * Add the contents of the discard list the to contents list.  The discard list is then
    * cleared.
    * @return
    */
    public static class doElide implements Transaction {
        /**
         * Serialized version
         */
        private static final long serialVersionUID = 1L;

        public void executeOn(Object prevalentSystem, Date executionTime) {
            PersistentList pList = (PersistentList) prevalentSystem;

            //Remove any elements from the underlying set that are already on the lists
            ArrayList retained = new ArrayList(pList.discard);

            retained.removeAll(new ArrayList(pList.container));
            retained.removeAll(new ArrayList(pList.relay.values()));
            //Add the discards to the container list
            pList.container.addAll(retained);

        }
    }

    public static Transaction elide() {
        return new doElide();
    }

    /**
     * Add the payload to the contents.
     * @param payload
     * @return
     */
    public static class doWriteContainer implements Transaction {
        /**
         * Serialized version
         */
        private static final long serialVersionUID = 1L;
        final Object payload;

        public doWriteContainer(Object payload) {
            this.payload = payload;
        }

        public void executeOn(Object prevalentSystem, Date executionTime) {
            if (((PersistentList) prevalentSystem).discard.contains(payload)) {
                ((PersistentList) prevalentSystem).discard.add(payload);
            }
            ((PersistentList) prevalentSystem).container.add(payload);
        }
    }
    public static Transaction writeContainer(final Object payload) {
        return new doWriteContainer(payload);
    }

    /**
     * Put an entry on the relay list.  The rule is that you can't do this unless the
     * object is already in the discard set.  This implies that payload has already
     * come through the front door; this is not a back door way to get an object into
     * the persistent list.
     * @param payload
     * @return
     */
    public static class doWriteRelay implements Transaction {
        /**
         * Serialized version
         */
        private static final long serialVersionUID = 1L;
        final Object[] payload;

        public doWriteRelay(Object payload[]) {
            this.payload = payload;
        }

        public void executeOn(Object prevalentSystem, Date executionTime) {
            if (((PersistentList) prevalentSystem).discard.contains(payload[1])) {
                ((PersistentList) prevalentSystem).discard.add(payload[1]);
            }
            ((PersistentList) prevalentSystem).relay.put(payload[0], payload[1]);
        }
    }

    public static Transaction writeRelay(final Object[] payload) {
        return new doWriteRelay(payload);
    }

    /**
     * Return the size of the contents list
     * @return
     */
    public static class doContainerStatusQuery implements Query {
        public Object query(Object prevalentSystem, Date executionTime) {
            Object[] retval = { new Integer(((PersistentList) prevalentSystem).container.size())};

            return retval;
        }
    }

    public static Query containerStatusQuery() {
        return new doContainerStatusQuery();
    }

    /**
     * Take the status of the relay list
     * 0 => there is nothing in the list
     * 1 => there is something in the list but the trigger time has not expired
     * 2 => there is something in the list to read
     * @return
     */
    public static class doRelayStatusQuery implements Query {
        public Object query(Object prevalentSystem, Date executionTime) {
            PersistentList myList = (PersistentList) prevalentSystem;
            int retval;

            if (myList.relay.size() < 1) {
                retval = 0;
            } else if (((Date) myList.relay.firstKey()).before(executionTime)) {
                retval = 2;
            } else {
                retval = 1;
            }

            return new Object[] { new Integer(retval)};
        }
    }

    public Query relayStatusQuery() {
        return new doRelayStatusQuery();
    }

}
