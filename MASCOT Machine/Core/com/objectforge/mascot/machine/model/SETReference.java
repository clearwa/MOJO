/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.model;

import java.util.Map;
import java.util.Random;

import com.objectforge.mascot.machine.scheduler.MascotTransactionQueue;

/**
 * SETReference
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.model
 * Created on 29-Mar-2004 by @author Clearwa
*/
public class SETReference {
    /*
     * SET reference provides the information to tell me how to look up a reference in the
     * EntityStore
     */
    String tag;
    Map table;
    int typeID;
    int myHash;
    boolean codeSet = false;

    //Access
    private static MascotTransactionQueue gate = new MascotTransactionQueue();
    Random codeGen = new Random();

    public SETReference(String tag, Map table, int typeID) {
        this.tag = tag;
        this.table = table;
        this.typeID = typeID;
    }

    public Object lookup() {
        return table.get(tag);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    /**
     * The typeID is used to distingish equality between objects with the same tag
     */
    public boolean equals(Object obj) {
        if (obj instanceof SETReference) {
            return (((SETReference) obj).tag == tag)
                && (((SETReference) obj).typeID == typeID)
                && (((SETReference) obj).table == table);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    /**
     * Objects with the same tag and typeID are the same
     */
    public int hashCode() {
        //Lazy initialization
        if (!codeSet) {
            gate.cqJoin();
            try {
                codeGen.setSeed((long) tag.hashCode() + typeID + table.hashCode());
                myHash = codeGen.nextInt();
            } finally {
                gate.cqLeave();
            }
        }
        return myHash;
    }

    /**
     * @return
     */
    public int getTypeID() {
        return typeID;
    }

    /**
     * @return
     */
    public String getTag() {
        return tag;
    }

    /**
     * @return
     */
    public Map getTable() {
        return table;
    }

}
