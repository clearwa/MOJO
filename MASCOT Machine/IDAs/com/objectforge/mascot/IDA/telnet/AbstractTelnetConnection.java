/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.IDA.telnet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * AbstractTelnetConnection
 * 
 * Project: MASCOT Machine
 * Package: com.objectforge.mascot.IDA.telnet
 * Created on 26-Feb-2004 by @author Clearwa
*/
public abstract class AbstractTelnetConnection implements ITelnetConnection{
    public static final char NUL = 0;
    public static final char LF = 10;
    public static final char CR = 13;
    public static final char DEL = 127;
    public static final char BEL = 7;
    public static final char BS = 8;
    public static final char HT = 9;
    public static final char FF = 12;
    public static final char BRK = 243;

    protected Socket socket;
    protected BufferedOutputStream out;
    protected BufferedInputStream in;
    protected OutputStream inetOuputStream;

    //For internet protocols, a filtered stream that translates an LF terminator to 
    //CRLF for the internet
    protected class InetOutput extends FilterOutputStream{
        int previous;

        /**
         * @param out
         */
        public InetOutput(OutputStream out) {
            super(out);
        }
        /* (non-Javadoc)
         * @see java.io.OutputStream#write(int)
         * Translate \n to CRLF
         */
        public void write(int b) throws IOException {
            //Check for an unaddorned newline
            if( b=='\n' && previous!='\r'){
                super.write('\r');
            }
            super.write(b);
            previous = b;
        }
        
    }

    public AbstractTelnetConnection(Socket socket) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(250);
        socket.setTcpNoDelay(true);
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
        inetOuputStream = new InetOutput( out );
    }
    
    public abstract Object readLine() throws IOException;
    
    public void flush() throws IOException{
        out.flush();
    }
    
    public void send(char c) throws IOException {
        out.write(c);
        out.flush();
    }

    public void print(char c) throws IOException {
        out.write(c);

            // Character-at-a-time mode
            flush();
    }

    public void println() throws IOException {
        print(CR);
        print(LF);
    }

    public void print(String s) throws IOException {
        if (s == null)
            return;

        for (int i = 0; i < s.length(); i++) {
            //newline is the line termination character
            if (s.charAt(i) == LF) {
                println();
            } else
                print(s.charAt(i));
        }
    }

    public void println(String s) throws IOException {
        print(s);
        println();
    }

    protected boolean closing = false;
    /**
     * 
     */
    public void setClosing() {
        closing = true;
    }

    public boolean isClosing() {
        return closing;
    }
    
    public void setLinemode( boolean mode ){
        //Does nothing by default
    }

    public void close() throws IOException {
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }
}
