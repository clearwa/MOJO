package com.objectforge.mascot.utility;
/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
*/


/**
 * The general debug class
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.1 $
 * 
 */
public class MascotDebug {
    public static int debug = 9;

    /**
     * Print a line to the console
     */
    public static void println(int debugLevel, String line) {
        if (debugLevel <= debug) {
            System.out.println(line);
            System.out.flush();
        }
    }

    /**
     */
    public static void setDebug(int debugFlag) {
        debug = debugFlag;
        System.out.println("Debug level= " + debugFlag);
    }

    /**
     * @param i
     * @param string
     */
    public static void print(int debugLevel, String string) {
        if (debugLevel <= debug) {
            System.out.print(string);
            System.out.flush();
        }
    }
    /**
     * @return
     */
    public static int getDebug() {
        return debug;
    }

}
