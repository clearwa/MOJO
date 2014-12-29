/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 *
 * CVS Info:
 * $Id$, $Name: 1.3 $
 */
package com.objectforge.mascot.prevayler;

import java.io.Serializable;

/**
 * PersistentBundle
 *
 * Project: MASCOT Examples
 * Package: com.objectforge.mascot.prevayler
 * Created on 08-Jan-2004 by @author Clearwa
 *
 * Concrete implementations of this class are classes that can be stored in a
 * persistent channel.  For the channel to work properly, objects must return
 * a consistent hashcode regardless of their position in memory.  The VM
 * uses an object's memroy address as its default hashcode and so objects
 * using Hashtables will not function correctly when reloaded.  It is thus
 * the case that any object persisted in a PersistentChannel must subclass
 * PersistentBundle.  Subclasses must implement the setHashcode() method
 * and may want to override equals().
 *
*/
public abstract class PersistentBundle implements Serializable {
    protected int hashcode;
    protected String msgDigest = "empty";
    private static final long serialVersionUID = 1L;

    /**
     * A concrete implementation that set the hashcode.
     *
     */
    protected abstract void setHashcode(String o);

    public boolean equals(Object o) {
        try {
            if (o instanceof PersistentBundle) {
                //If these hashcode matches then check that the hast string matches as well.
                if (this.hashcode == ((PersistentBundle) o).hashCode()) {
                    return (msgDigest.equals(((PersistentBundle) o).msgDigest));
                }
            }
        } catch (RuntimeException e) {
        }
        return false;
    }

    /**
     * Make sure the hashcode is filled in.
     *
     */
    protected PersistentBundle(String o) {
        this.setHashcode(o);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public final int hashCode() {
        return hashcode;
    }

    public final int getObjectHash() {
        return super.hashCode();
    }
}
