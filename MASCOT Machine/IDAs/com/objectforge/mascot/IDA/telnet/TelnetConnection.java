/**
 * Copyright - The Object Forge, Malvern, Worcs.  UK - 2002,2003
 * 	    	All Rights Reserved
 *
<<<<<<< TelnetConnection.java
=======
 * Version:
 *     $Id$
 * Name:
 *     $Name: 1.2.2.1 $
>>>>>>> 1.2.2.3
*/

package com.objectforge.mascot.IDA.telnet;

/**
* <br><br><center><table border="1" width="80%"><hr>
* <strong><a href="http://www.amherst.edu/~tliron/telnetj">telnetj</a></strong>
* <p>
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
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2.2.1 $
 * 
**/

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;

import com.objectforge.mascot.machine.scheduler.ControlQueue;
import com.objectforge.mascot.machine.scheduler.MascotThread;
import com.objectforge.mascot.utility.MascotRuntimeException;

/**
 * @author Allan Clearwaters
 * @version $Id$, $Name: 1.2.2.1 $
 * 
 * This class does the dog work of reading and writing a socket.  Many thanks to Tal Liron for 
 * making it available under the restircted GNU license.
 */
public class TelnetConnection extends AbstractTelnetConnection {
	//
	// Static attributes
	//

	// NVT keyboard	
	// Break - Indicates that the "break" or "attention" key was hi.
	public static final char IP = 244;
	// Suspend - Interrupt or abort the process to which the NVT is connected.
	public static final char AO = 245;
	// Abort output - Allows the current process to run to completion but does not send its output to the user.
	public static final char AYT = 246;
	// Are you there - Send back to the NVT some visible evidence that the AYT was received.
	public static final char EC = 247;
	// Erase character - The receiver should delete the last preceding undeleted character from the data stream.
	public static final char EL = 248;
	// Erase line -  Delete characters from the data stream back to but not including the previous CRLF.

	// Transmission of data	
	public static final char NOP = 241; // No operation
	public static final char GA = 249;
	// Go ahead - Under certain circumstances used to tell the other end that it can transmit.
	public static final char DM = 242;
	// Data mark - Indicates the position of a Synch event within the data stream. This should always be accompanied by a TCP urgent notification.

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

	public static final char IAC = 255; // Interpret as a command
	public static final char SE = 240; // End of subnegotiation parameters
	public static final char SB = 250;
	// Subnegotiation - Subnegotiation of the indicated option follows.

	// <operation>
	public static final char WILL = 251;
	// Will - Indicates the desire to begin performing, or confirmation that you are now performing, the indicated option.
	public static final char WONT = 252;
	// Won't - Indicates the refusal to perform, or continue performing, the indicated option.
	public static final char DO = 253;
	// Do - Indicates the request that the other party perform, or confirmation that you are expecting the other party to perform, the indicated option.
	public static final char DONT = 254;
	// Don't - Indicates the demand that the other party stop performing, or confirmation that you are no longer expecting the other party to perform, the indicated option.

	// <option>
	public static final char SUPPRESS_GO_AHEAD = 3; // RFC858
	public static final char STATUS = 5; // RFC859
	public static final char ECHO = 1; // RFC857
	public static final char TIMING_MARK = 6; // RFC860
	public static final char TERMINAL_TYPE = 24; // RFC1091
	public static final char WINDOW_SIZE = 31; // RFC1073
	public static final char TERMINAL_SPEED = 32; // RFC1079
	public static final char REMOTE_FLOW_CONTROL = 33; // RFC1372
	public static final char LINEMODE = 34; // RFC1184
	public static final char ENVIRONMENT = 36; // RFC1408

	public static final String CRLF = "" + CR + LF;

	private ControlQueue gate = new ControlQueue();
	private boolean linemode = true;
	private boolean suppressNegotiation = false;
    
	//
	// Construction
	//

	public TelnetConnection(Socket socket, boolean negotiation) throws IOException {
        super(socket);
		this.suppressNegotiation = negotiation;

		if (!suppressNegotiation) {
			// Send options		
			sendOption(SUPPRESS_GO_AHEAD, true);
			sendOption(ECHO, true);
		}
	}

	public TelnetConnection(Socket socket) throws IOException {
		this(socket, false);
	}

	/**
	 * While not closing read the socket.  If the read times out check for thread death; if so 
	 * exit the thread, if not reissue the read.
	 */
	private char socketRead() {
		int val;

		while (!closing) {
			try {
				val = in.read();

				if (val < 0) {
					throw new EOFException("TelnetConnection<socketRead>: EOF");
				}
				return (char) (val & 0xff);

			} catch (InterruptedIOException ioe) {
				if ( Thread.currentThread() instanceof MascotThread &&
						((MascotThread) Thread.currentThread()).isDead()) {
					throw new MascotRuntimeException("TelentConnection(socketRead): Thread is dead!!");
				}
				continue;
			} catch (IOException e1) {
				break;
			}
		}
		throw new MascotRuntimeException("TelnetConnection(socketRead): closing ");
	}

	//
	// Operations
	//
	public char nextChar() throws IOException {
		int c;
		while (true) {
			c = socketRead();
			switch (c) {
				case -1 :
					throw new IOException();

				case BRK :
					break;

				case IP :
					// throw new TelnetInterruptException();
					break;

				case AO :
					// throw new TelnetAbortOutputException();
					break;

				case AYT :
					// are you there?
					break;

				case NOP :
					break;

				case GA :
					break;

				case DM :
					break;

				case IAC :
					interpretAsCommand();
					break;

				case EC :
					break;

				case EL :
					break;

				default :
					return (char) (c & 0x7f);
			}
		}
	}

	//
	// Input
	//

	public Object readLine() throws IOException {
		return readLine(!suppressNegotiation);
	}

	/**
	 * @throws IOException
	 * Readline's behaviour is dependent on linemode.  If linemode is true then process characters
	 * until a cr or lf arrives; if it is false the act like nextchar.
	 */
	public Object readLine(boolean echo) throws IOException {
		StringBuffer line = new StringBuffer("");
		int cursor = 0;
		boolean cr = false;
		boolean lf = false;
		String echoString;

		int c;
		while (true) {
			c = nextChar();
			if (!linemode) {
				return new Character((char) c);
			}
			echoString = new String(new char[] {(char) c });

			switch (c) {
				case CR :
					cr = true;
					//					echoString = CRLF;
					break;

				case LF :
					lf = true;
					//					echoString = CRLF;
					break;

				case 0 :
					if (cr) {
						// Some clients send CR,0
						lf = true;
						echoString = CRLF;
						c = LF;
					}
					break;

				case BS :
				case DEL :
					if (cursor > 0) {
						cursor--;
						line.deleteCharAt(cursor);
						echoString = "" + BS + " " + BS;
					} else {
						c = 0;
						echoString = "";
					}
					break;

				default :
					line.insert(cursor++, (char) c);
					break;
			}

			if (echo) {
				// We will echo
				for (int k = 0; k < echoString.length(); k++)
					send(echoString.charAt(k));
			}

			if (cr && lf) {
				break;
			}
		}

		return line.toString();
	}

	//
	// Output
	//

	public void flush() throws IOException {
		if (kludge) {
			// Reset end-of-line detection
			kludgeCR = false;
			kludgeLF = false;
		}

		out.flush();
	}

	public void print(char c) throws IOException {
		out.write(c);

		if (kludge) {
			// Kludge mode
			if (c == CR) {
				kludgeCR = true;
			} else if (c == LF) {
				kludgeLF = true;
			}

			if (kludgeCR && kludgeLF) {
				// End of line, so flush
				flush();
			}
		} else {
			// Character-at-a-time mode
			flush();
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	private boolean subnegotiating = false;
	private boolean kludge = true;
	private boolean kludgeCR = false;
	private boolean kludgeLF = false;

	private void interpretAsCommand() throws IOException {
		if (suppressNegotiation)
			return;

		int operation = socketRead();
		int option = socketRead();

		switch (operation) {
			case SB :
				// Subnegotiaton
				subnegotiating = true;
				boolean request = (socketRead() == 1);
				if (request) {
					// Send value to client
				} else {
					// Get value from client
					String value = "";
					while (subnegotiating) {
						value += nextChar();
					}
				}
				break;

			case SE :
				subnegotiating = false;
				break;

			case WILL :
				break;

			case WONT :
				break;

			case DO :
			case DONT :
				switch (option) {
					case SUPPRESS_GO_AHEAD :
					case ECHO :
						sendOption((char) option, true);
						break;
					default :
						// Unsupported option
						sendOption((char) option, false);
				}
				break;
		}
	}

	private void sendOption(char option, boolean state) throws IOException {
		if (suppressNegotiation)
			return;

		send(IAC);
		send(state ? WILL : WONT);
		send(option);
	}

/**
	 */
	public boolean isLinemode() {
		boolean retval;

		gate.cqJoin();
		retval = linemode;
		gate.cqLeave();
		return retval;
	}

	/**
	 * Sets the linemode.
	 */
	public void setLinemode(boolean linemode) {
		gate.cqJoin();
		this.linemode = linemode;
		gate.cqLeave();
	}

    /**
     * @return
     */
    public OutputStream getInetOuputStream() {
        return inetOuputStream;
    }

    /**
     * @return
     */
    public BufferedInputStream getIn() {
        return in;
    }

}
