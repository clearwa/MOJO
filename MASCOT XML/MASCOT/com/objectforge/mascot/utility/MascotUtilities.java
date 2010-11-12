/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 *          All Rights Reserved
 *
 */

/*
 * Created on 07-Mar-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.objectforge.mascot.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Commonly useful utilities
 * 
 * @author Allan Clearwaters
 * @version $Id$,
 *          $Name: 1.1.2.2 $
 */
public class MascotUtilities {
    public final static String JAR_FILE = "mascot.machine.xmljar";

    public final static String TAGS_ENTRY = "mascot.machine.xmltags";

    public final static String BUNDLE = "mascot_machine";

    private static PropertyResourceBundle bundle = null;

    private static String JarCP;

    public static void throwMRE(String line) throws MascotRuntimeException {
        throw new MascotRuntimeException(line);
    }
    
    public static PropertyResourceBundle getMascotBundle(){
        return getMascotBundle( false );
    }

    public static PropertyResourceBundle getMascotBundle( boolean force ) {
        // System.out.println( "Test 3");
        if (bundle == null) {
            jarCP( force ); // Update that classpath from the jar file if need be
            try {
                MascotDebug.debug = 9;
                // Have a look in the working dir for the properties file first
                // - this overrides
                // the file in any of the jar files
                InputStream in = mascotOpen(BUNDLE + ".properties");
                if (in != null)
                    bundle = new PropertyResourceBundle(in);
            } catch (IOException e) {
                MascotDebug.println(9, "getMascotBundle: property file is not in .");
            } catch (RuntimeException e) {
                bundle = null;
                MascotDebug.println(9, "getMascotBundle: No property file -  " + e);
            }
        }
        if (bundle == null) {
            // have a look in the jar files
            bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle(BUNDLE);
        }
        return bundle;
    }

    public static String getMascotResource(String entry) {
        getMascotBundle();
        try {
            return bundle.getString(entry);
        } catch (MissingResourceException e) {
            MascotDebug.println(9, "MascotUtilities(getMascotResource): missing key " + entry);
            return null;
        }

    }

    /**
     * Tokenize a path on either ":" (Unix) or ";" (Windows).  For windows systems the path]
     * must not be tokenized on ":"
     * @param path
     */
    private static StringTokenizer tokenizeJCP( String path ){
    	if(path==null){
    		return null;
    	}
    	String sepString = System.getProperty("path.separator");
    	// In the case of unix allow both ":" and ";"
    	if( sepString==":" ){
    		sepString += ";";
    	}
    	return new StringTokenizer( path, sepString );
    }
    
    private static String jarCP( boolean force ) {
        String retval = null;

        /*
         * The idea (simplistic, I know, is that if the classpatth consists of a
         * single jar file then it is likely that the applicaiton was executed
         * with -jar option. In any case, try to open the defined classpath as a
         * whole as a jar file. The first thing to do is check for the presence
         * of a ; separator in the current classpath.
         */
        String currentPath = System.getProperty("java.class.path");
        //The default is for JarCP to equal the classpath
        JarCP = currentPath;
        if ( (currentPath != null 
        		&& currentPath.charAt(0) != System.getProperty("path.separator").charAt(0)
        		&& !(currentPath.indexOf(System.getProperty("path.separator")) > 0)) || force ) {
            if( force){
                /* Thumb through the path to find the first jar file
                 */
                StringTokenizer paths = tokenizeJCP( currentPath );
                while( paths.hasMoreTokens() ){
                    String value = paths.nextToken();
                    if( value.endsWith(".jar") ){
                        currentPath = value;
                        break;
                    }
                }
            }
            try {
                File fs = new File(currentPath);
                if (fs != null) {
                    // Open the thing
                    Manifest manifest = (new JarInputStream(new FileInputStream(fs))).getManifest();
                    // Now that I have the manifest does it have a class-path
                    // entry
                    Attributes attrib = manifest.getMainAttributes();
                    if (attrib.getValue("Class-Path") != null) {
                        StringTokenizer tokens = new StringTokenizer((String) attrib.getValue("Class-Path"),
                                " ");
                        JarCP = "";
                        do {
                            JarCP += tokens.nextToken() + ((tokens.hasMoreTokens()) 
                            		? System.getProperty("path.separator") : "");
                        } while (tokens.hasMoreTokens());
                        System.out.println("JarCP: " + JarCP);
                        if( force ){
                            System.setProperty("java.class.path", JarCP );
                        }
                    }
                } else { // Take the claaspath whatever it may be
                    JarCP = System.getProperty("java.class.path");
                }
            } catch (IOException e) {
                // Bail - do nothing
                MascotDebug.println(0, "No classpath in the manifest - " + currentPath);
            }
        }
        return retval;
    }

    public static InputStream mascotOpen(String fileName) {
        InputStream stream = null;
        String target = canonicalFilename(fileName);
        StringBuffer url = new StringBuffer("");
        InputStream retval = null;
        /*
         * The logic here is to search along the classpath if the bundle is
         * null, ie. has not been defined
         */
        StringTokenizer paths = tokenizeJCP(JarCP);

        MascotDebug.println(8, "mascotOpen: opening \"" + target + "\"");
        for (int i = 0; i < target.length(); i++) {
            if (!(target.charAt(i) == '\\')) {
                url.append(target.charAt(i));
            }
        }

        String locName = url.toString();

        // If I hava a valid url then look there
        try {
            if (locName.indexOf("://") > 0) {
                URL location = new URL(url.toString());
                stream = location.openStream();
            }
            // else {
            // stream = new FileInputStream(locName);
            // }
        } catch (MalformedURLException e2) {
        } catch (FileNotFoundException e2) {
        } catch (IOException e2) {
        }
        if (stream != null)
            return stream;

        // The url didn't work so look down the defined paths. The order is
        // bundle paths first, classpath
        // next
        paths = tokenizeJCP((bundle != null) ? bundle.getString(JAR_FILE) 
        		+ System.getProperty("path.separator") + JarCP : JarCP);

        try {
            String currentPath = null;
            while (paths.hasMoreTokens()) {
                try {
                    currentPath = paths.nextToken();

                    // Try this as a jar first
                    MascotDebug.print(6, "Trying " + currentPath + "/" + target + " ... ");
                    File fs = new File(currentPath);
                    try {
                        JarFile jar_file = new JarFile(fs, false);
                        JarEntry jar_entry = jar_file.getJarEntry(target);
                        retval = jar_file.getInputStream(jar_entry);
                        MascotDebug.println(6,"found jar");
                        return retval;
                    } catch (Exception e3) {
                        // Now try the path element as a normal file
                        retval = new FileInputStream(currentPath + "/" + target);
                        MascotDebug.println(6,"found file");
                        return retval;
                    }
                } catch (Exception e1) {
                    MascotDebug.println(6, "failed" );
                    MascotDebug.println(7, "\texception: " + e1);
                    continue;
                }
            }
        } catch (MissingResourceException e) {
            MascotDebug.println(9, "mascotOpen: Missing resource exception - " + e);
        }
        MascotDebug.println(9, "mascotOpen: Cannot open file " + target);
        throw new MascotRuntimeException("MascotXML(mascotOpen) - Cannot open file " + target);
    }

    public static String canonicalFilename(String filename) {
        if (filename == null) {
            return null;
        }

        String ret = "";
        char[] chars = filename.toCharArray();

        for (int i = 0; i < filename.length(); i++) {
            switch (chars[i]) {
            case '\\':
                ret += "/";
                continue;

            case ' ':
                ret += "\\ ";
                continue;

            default:
                ret += chars[i];
                break;
            }
        }
        return ret;
    }

    /*
     * Return a consistent and configurable extended header string.  This should look in the properties
     * for a key or return the default as below.  For now, it simply return the default
     */
    public static String alreadyProcessedString() {
        return "X-ACE-Processed";
    }
    
    
    
}
