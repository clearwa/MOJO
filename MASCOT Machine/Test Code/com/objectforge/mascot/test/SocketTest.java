/**
 * Copyright The Object Forge, Malvern, UK - 2002, 2003
 * @author Allan Clearwaters
 * 
 * CVS Info:
 * $Id$, $Name: 1.2 $
 */
package com.objectforge.mascot.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
//import java.net.SocketTimeoutException;	//**java 1.4.1

/*
* Project: MASCOT Machine
* Package: com.objectforge.mascot.test
* Created on 15-May-2003
*/
public class SocketTest {
	public static void main(String[] args) {
		Socket socket;
		ServerSocket server;

		try {
			server = new ServerSocket(23);
			server.setSoTimeout(500);

			while (true) {
				try {
					socket = server.accept();
					break;
//				} catch (SocketTimeoutException s1) {		//**java 1.4.1
				} catch (SocketException s1) {		//**java 1.3.1
					continue;
				} catch (InterruptedIOException e1) {
					System.out.println("Accept IIOE");
					throw new RuntimeException();
				}
			}

			socket.setSoTimeout(500);

			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			PrintWriter output = new PrintWriter(os);
			InputStreamReader input = new InputStreamReader(is);

			output.println("Source test code now reads");
			output.flush();
			while (true) {
				try {
					int c = input.read();
					if (c >= 0) {
						output.println(c);
						{
						}
						output.flush();
					} else {
						output.flush();
						socket.close();
						break;
					}
//				} catch (SocketTimeoutException s1) {	//**java 1.4.1
				} catch (SocketException s1) {	//**java 1.3.1
					continue;
				} catch (InterruptedIOException e1) {
					System.out.println("Read IIOE");
					throw new RuntimeException();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
