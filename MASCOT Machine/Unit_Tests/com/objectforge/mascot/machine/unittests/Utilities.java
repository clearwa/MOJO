/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.machine.unittests;

import java.io.IOException;

import com.objectforge.mascot.machine.estore.EntityStore;

/**
 * Utilities
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.machine.unittests
 * Created on 05-Apr-2004 by @author Clearwa
*/
public class Utilities {
    /**
     * Print an EntityStore to the console and wait for user input
     * 
     * @param es
     */
    public static void printIt(EntityStore es) {
        printIt(es.toString());
    }

    /**
     * Print a message to the console
     * @param message
     */
    public static void printIt(String message) {
        System.out.println(message);
        System.out.flush();
    }

    /**
     * The default stall
     * 
     */
    public static void stall() {
        stall(500);
    }

    /**
     * Wait for the user to respond on the console
     * 
     */
    protected static void stall(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        System.out.print("Output paused - please press the return key to continue...");
        System.out.flush();
        int input = 0;
        do {
            try {
                input = System.in.read();
            } catch (IOException e) {
            }
        } while (input != '\n' && input != -1);
        printIt("\nContiue...\n");
    }

    /**
     * Pump out the message string and then stall
     * 
     * @param message
     */
    public static void stall(String message) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        printIt("\n" + message);
        stall(0);
    }
}
