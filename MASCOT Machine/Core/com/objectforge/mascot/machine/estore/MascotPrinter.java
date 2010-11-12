/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.estore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import com.objectforge.mascot.machine.estore.IEsSubsystem.IInstallRecord;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.model.MascotEntities;

/**
 * MascotPrinter
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.estore
 * Created on 05-Apr-2004 by @author Clearwa
*/
public class MascotPrinter {
    public PrintWriter out;
    public StringWriter buffer;

    public MascotPrinter() {
        buffer = new StringWriter();
        out = new PrintWriter(buffer);
    }

    public void print(Map table, String title, boolean instance) {
        out.println("    -- " + title + " --:");
        for (Iterator me = table.values().iterator(); me.hasNext();) {
            MascotEntities entity = (MascotEntities) me.next();
            out.println("      -- " + entity.getName() + ":");
            out.println(entity.toString("           "));
            if (instance) {
                try {
                    out.print(entity.getCurrentIncarnation().toString("         "));
                } catch (MascotMachineException e) {
                    // Error somewhere
                    out.println("Failed to get current incarnation");
                }
            }
        }
    }

    public void referencePrint(String indent, String title, Map table) {
        String spaces = indent + "    ";

        out.println(indent + title);
        for (Iterator i = table.values().iterator(); i.hasNext();) {
            IInstallRecord record = (IInstallRecord) i.next();
            out.println(record.getReference().toString(spaces));
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        out.flush();
        out.close();
        return buffer.toString();
    }

}
