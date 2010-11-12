package com.objectforge.mascot.samples.telnetj;

/**
* <br><br><center><table border="1" width="80%"><hr>
* <strong><a href="http://www.amherst.edu/~tliron/telnetj">telnetj</a></strong>
* <p>
* Copyright (C) 2001 by Tal Liron
* <p>
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public License
* as published by the Free Software Foundation; either version 2.1
* of the License, or (at your option) any later version.
* <p>
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* <a href="http://www.gnu.org/copyleft/lesser.html">GNU Lesser General Public License</a>
* for more details.
* <p>
* You should have received a copy of the <a href="http://www.gnu.org/copyleft/lesser.html">
* GNU Lesser General Public License</a> along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
* <hr></table></center>
**/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TelnetConnection
{
	//
	// Static attributes
	//

	// NVT keyboard	
	public static final char NUL  = 0;
	public static final char LF   = 10;
	public static final char CR   = 13;
	public static final char DEL  = 255;
	public static final char BEL  = 7;
	public static final char BS   = 8;
	public static final char HT   = 9;
	public static final char FF   = 12;
	public static final char BRK  = 243; // Break - Indicates that the "break" or "attention" key was hi.
	public static final char IP   = 244; // Suspend - Interrupt or abort the process to which the NVT is connected.
	public static final char AO   = 245; // Abort output - Allows the current process to run to completion but does not send its output to the user.
	public static final char AYT  = 246; // Are you there - Send back to the NVT some visible evidence that the AYT was received.
	public static final char EC   = 247; // Erase character - The receiver should delete the last preceding undeleted character from the data stream.
	public static final char EL   = 248; // Erase line -  Delete characters from the data stream back to but not including the previous CRLF.

	// Transmission of data	
	public static final char NOP  = 241; // No operation
	public static final char GA   = 249; // Go ahead - Under certain circumstances used to tell the other end that it can transmit.
	public static final char DM   = 242; // Data mark - Indicates the position of a Synch event within the data stream. This should always be accompanied by a TCP urgent notification.

	// Either end of a Telnet conversation can locally or remotely enable or disable an option.
	// The initiator sends a 3-byte command of the form:
	//
	// IAC + <operation> + <option>
	//
	// Some option's values need to be communicated after support of the option has been agreed.
	// This is done using sub-option negotiation. Values are negotiated using value query commands
	// and responses in the following form:
	//
	// Value required:
	// IAC + SB + <option> + 1 + IAC + SE
	//
	// Value supplied:
	// IAC + SB + <option> + 0 + <value> + IAC + SE
	
	public static final char IAC  = 255; // Interpret as a command
	public static final char SE   = 240; // End of subnegotiation parameters
	public static final char SB   = 250; // Subnegotiation - Subnegotiation of the indicated option follows.
	
	// <operation>
	public static final char WILL = 251; // Will - Indicates the desire to begin performing, or confirmation that you are now performing, the indicated option.
	public static final char WONT = 252; // Won't - Indicates the refusal to perform, or continue performing, the indicated option.
	public static final char DO   = 253; // Do - Indicates the request that the other party perform, or confirmation that you are expecting the other party to perform, the indicated option.
	public static final char DONT = 254; // Don't - Indicates the demand that the other party stop performing, or confirmation that you are no longer expecting the other party to perform, the indicated option.
	
	// <option>
	public static final char SUPPRESS_GO_AHEAD = 3;    // RFC858
	public static final char STATUS = 5;               // RFC859
	public static final char ECHO = 1;                 // RFC857
	public static final char TIMING_MARK = 6;          // RFC860
	public static final char TERMINAL_TYPE = 24;       // RFC1091
	public static final char WINDOW_SIZE = 31;         // RFC1073
	public static final char TERMINAL_SPEED = 32;      // RFC1079
	public static final char REMOTE_FLOW_CONTROL = 33; // RFC1372
	public static final char LINEMODE = 34;            // RFC1184
	public static final char ENVIRONMENT = 36;         // RFC1408
	
	public static final String CRLF = "" + CR + LF;

	//
	// Construction
	//
	
	public TelnetConnection( Socket socket ) throws IOException
	{
		this.socket = socket;

		in = new BufferedInputStream( socket.getInputStream() );
		out = new BufferedOutputStream( socket.getOutputStream() );		

		log( "Connected", 1 );

		// Send options		
		sendOption( SUPPRESS_GO_AHEAD, true );
		sendOption( ECHO, true );
		//sendOptionRequest( ECHO, false );
	}
	
	//
	// Operations
	//
	public int available(){
		int retval = 0;
		
		try {
			retval = in.available();
		} catch (IOException e) {
		}
		return retval;
	}
	
	public void close() throws IOException
	{
		log( "Disconnected", 1 );
		socket.close();
	}
	
	public char nextChar() throws IOException
	{
		int c;
		while( true )
		{
			c = in.read();
			switch( c )
			{
			case -1:
				throw new IOException();
				
			case BRK:
				log( string( c ), 10 );
				break;
	
			case IP:
				log( string( c ), 10 );
				// throw new TelnetInterruptException();
				break;
				
			case AO:
				log( string( c ), 10 );
				// throw new TelnetAbortOutputException();
				break;
	
			case AYT:
				log( string( c ), 10 );
				// are you there?
				break;
				
			case NOP:
				log( string( c ), 10 );
				break;
				
			case GA:
				log( string( c ), 10 );
				break;
			
			case DM:
				log( string( c ), 10 );
				break;
				
			case IAC:
				interpretAsCommand();
				break;
				
			case EC:
				log( string( c ), 10 );
				break;
				
			case EL:
				log( string( c ), 10 );
				break;
				
			default:
				return (char) c;
			}
		}
	}
	
	//
	// Input
	//
	
	public String readLine() throws IOException
	{
		return readLine( true );
	}

	public String readLine( boolean echo ) throws IOException
	{
		StringBuffer line = new StringBuffer( "" );
		int cursor = 0;
		boolean cr = false;
		boolean lf = false;
		String echoString;
		
		int c;
		while( true )
		{
			c = nextChar();
			echoString = new String( new char[] {(char) c} );
			
			switch( c )
			{
				case CR:
					log( "CR", 20 );
					cr = true;
//					echoString = CRLF;
					break;
					
				case LF:
					log( "LF", 20 );
					lf = true;
//					echoString = CRLF;
					break;
					
				case 0:
					if( cr )
					{
						// Some clients send CR,0
						log( "0", 20 );
						lf = true;
						echoString = CRLF;
						c = LF;
					}
					break;
					
				case BS:
				case DEL:
					if( cursor > 0 )
					{
						cursor--;
						line.deleteCharAt( cursor );
						echoString = "" + BS + " " + BS;
					}
					else
					{
						c = 0;
						echoString = "";
					}
					break;
					
				default:
					log( "" + c, 20 );
					line.insert( cursor++, (char) c );
					break;
			}
			
			if( echo )
			{
				// We will echo
				for( int k=0;k<echoString.length();k++ )
					send( echoString.charAt(k) );
			}
			
			if( cr && lf )
			{
				break;
			}
		}
		
		return line.toString();
	}
	
	//
	// Output
	//
	
	public void flush() throws IOException
	{
		if( kludge )
		{
			// Reset end-of-line detection
			kludgeCR = false;
			kludgeLF = false;
		}
		
		out.flush();
	}
	
	public void send( char c ) throws IOException
	{
		out.write( c );
		out.flush();
	}
	
	public void print( char c ) throws IOException
	{
		out.write( c );
		
		if( kludge )
		{
			// Kludge mode
			if( c == CR )
			{
				kludgeCR = true;
			}
			else if( c == LF )
			{
				kludgeLF = true;
			}
				
			if( kludgeCR && kludgeLF )
			{
				// End of line, so flush
				flush();
			}
		}
		else
		{
			// Character-at-a-time mode
			flush();
		}
	}
	
	public void println() throws IOException
	{
		print( CR );
		print( LF );
	}
	
	public void print( String s ) throws IOException
	{
		if( s == null )
			return;
		
		for( int i = 0; i < s.length(); i++ )
		{
			//newline is the line termination character
			if( s.charAt(i) == LF ){
				println();
			} else
				print( s.charAt( i ) );
		}
	}
	
	public void println( String s ) throws IOException
	{
		print( s );
		println();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	private Socket socket;
	private BufferedOutputStream out;
	private BufferedInputStream in;
	private boolean subnegotiating = false;
	private boolean kludge = true;
	private boolean kludgeCR = false;
	private boolean kludgeLF = false;

	private int logLevel = 0;
	// 1  - entry
	// 10 - special keys and commands
	// 20 - all keys
	
	private void log( String text, int level )
	{
		if( level <= logLevel )
			System.err.println( socket.getInetAddress().getHostName() + ": " + text );
	}

	private void interpretAsCommand() throws IOException
	{
		int operation = in.read();
		int option = in.read();
		
		log( string( IAC ) + " " + string( operation ) + " " + string( option ), 10 );
		
		switch( operation )
		{
		case SB:
			// Subnegotiaton
			subnegotiating = true;
			boolean request = ( in.read() == 1 );
			if( request )
			{
				// Send value to client
			}
			else
			{
				// Get value from client
				String value = "";
				while( subnegotiating )
				{
					value += nextChar();					
				}
				log( value, 10 );
			}
			break;
			
		case SE:
			subnegotiating = false;
			break;
			
		case WILL:
			break;
			
		case WONT:
			break;
			
		case DO:
		case DONT:
			switch( option )
			{
			case SUPPRESS_GO_AHEAD:
			case ECHO:
				sendOption( (char) option, true );
				break;
			default:
				// Unsupported option
				sendOption( (char) option, false );
			}
			break;
		}
	}
	
	private void sendOption( char option, boolean state ) throws IOException
	{
		send( IAC );
		send( state ? WILL : WONT );
		send( option );
		log( "[" + ( state ? "WILL " : "WONT " ) + string( option ) + "]", 10 );
	}
	
	private static String string( int c )
	{
		switch( c )
		{
		case BRK:
			return "BRK" ;
		case IP:
			return "IP";
		case AO:
			return "AO";
		case AYT:
			return "AYT";
		case EC:
			return "EC";
		case EL:
			return "EL";

		case NOP:
			return "NOP";
		case GA:
			return "GA";
		case DM:
			return "DM";
			
		case IAC:
			return "IAC";			
		case SB:
			return "SB";
		case SE:
			return "SE";
			
		case WILL:
			return "WILL";
		case WONT:
			return "WONT";
		case DO:
			return "DO";
		case DONT:
			return "DONT";
			
		case SUPPRESS_GO_AHEAD:
			return "SUPPRESS_GO_AHEAD";
		case STATUS:
			return "STATUS";
		case ECHO:
			return "ECHO";
		case TIMING_MARK:
			return "TIMING_MARK";
		case TERMINAL_TYPE:
			return "TERMINAL_TYPE";
		case WINDOW_SIZE:
			return "WINDOW_SIZE";
		case TERMINAL_SPEED:
			return "TERMINAL_SPEED";
		case REMOTE_FLOW_CONTROL:
			return "REMOTE_FLOW_CONTROL";
		case LINEMODE:
			return "LINEMODE";
		case ENVIRONMENT:
			return "ENVIRONMENT";
	
		default:
			return "" + c;
		}
	}
}
