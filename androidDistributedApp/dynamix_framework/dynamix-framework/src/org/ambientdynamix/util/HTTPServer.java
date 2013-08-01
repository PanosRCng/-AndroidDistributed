/*
 * Copyright (C) The Ambient Dynamix Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambientdynamix.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import org.ambientdynamix.core.DynamixService;

import android.util.Log;

/**
 * Experimental HTTP server for the Dynamix Framework.
 * 
 * @author Darren Carlson
 */
public class HTTPServer extends Thread {
	/*
	 * Based on: http://www.prasannatech.net/2008/10/simple-http-server-java.html Mobile Ajax:
	 * http://www.ibm.com/developerworks/opensource/library/wa-aj-mobileajax/index.html?ca=drs-
	 */
	/*
	 * In original AJAX, cross site content pulling is NOT allowed -
	 * https://developer.mozilla.org/en/Same_origin_policy_for_JavaScript - Overcome the cross site limitation using
	 * cross domain access control (CORS): http://www.w3.org/TR/access-control/
	 * http://www.nczonline.net/blog/2010/05/25/cross-domain-ajax-with-cross-origin-resource-sharing/ - We need a way to
	 * validate that the Origin header is authentic, meaning the request truly comes from the specified site. We need
	 * this because remote clients could potentially craft HTTP headers that make it look like an ajax request
	 * originated from a given domain (with a set of security credentials). - https://wiki.mozilla.org/Security/Origin
	 * Notes: - There's a spec for doing such things in HTML 5: http://dev.w3.org/geo/api/spec-source.html
	 */
	static final String HTML_START = "<html>" + "<title>HTTP Server in java</title>" + "<body>";
	static final String HTML_END = "</body>" + "</html>";
	private Socket connectedClient = null;
	private BufferedReader inFromClient = null;
	private DataOutputStream outToClient = null;
	private final static String TAG = HTTPServer.class.getSimpleName();
	private static boolean done;

	private HTTPServer(Socket client) {
		connectedClient = client;
	}

	public static void startServer(final ServerSocket socket) {
		done = false;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// ServerSocket Server = new ServerSocket(5000, 10, InetAddress.getByName("127.0.0.1"));
				Log.i(TAG, "HTTPServer waiting for clients");
				while (!done) {
					Socket connected = null;
					try {
						connected = socket.accept();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					(new HTTPServer(connected)).start();
				}
				Log.i(TAG, "HTTPServer exiting...");
			}
		});
		t.setDaemon(true);
		t.start();
	}

	public static void stopServer() {
		done = true;
	}

	public void run() {
		try {
			System.out.println("The Client " + connectedClient.getInetAddress() + ":" + connectedClient.getPort()
					+ " is connected");
			inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
			outToClient = new DataOutputStream(connectedClient.getOutputStream());
			String requestString = inFromClient.readLine();
			String headerLine = requestString;
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();
			StringBuffer responseBuffer = new StringBuffer();
			responseBuffer.append("<b> This is the HTTP Server Home Page.... </b><BR>");
			responseBuffer.append("The HTTP Client request is ....<BR>");
			String contentTypeLine = "Content-Type: text/html" + "\r\n";
			System.out.println("The HTTP request string is ....");
			while (inFromClient.ready()) {
				// Read the HTTP complete HTTP Query
				responseBuffer.append(requestString + "<BR>");
				System.out.println(requestString);
				requestString = inFromClient.readLine();
			}
			if (httpMethod.equals("GET")) {
				// Original method (experimental)
				// String response = DynamixService.getFirstContextString();
				// sendResponse(200, response, "Content-Type: text/html", false);
				// Another method
				// if (httpQueryString.equals("/")) {
				// // The default home page
				// sendResponse(200, responseBuffer.toString(), "Content-Type: text/html" + "\r\n", false);
				// }
				// else {
				// //This is interpreted as a file name
				//
				//
				// String fileName = httpQueryString.replaceFirst("/", "");
				// fileName = URLDecoder.decode(fileName);
				// if (new File(fileName).isFile()) {
				// sendResponse(200, fileName, "Content-Type: text/html" + "\r\n", true);
				// }
				// else {
				// sendResponse(404, "<b>The Requested resource not found ...."
				// + "Usage: http://127.0.0.1:5000 or http://127.0.0.1:5000/</b>", "Content-Type: text/html" + "\r\n",
				// false);
				// }
				// }
			} else
				sendResponse(404, "<b>The Requested resource not found ...."
						+ "Usage: http://127.0.0.1:5000 or http://127.0.0.1:5000/</b>", "Content-Type: text/html"
						+ "\r\n", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendFile(FileInputStream fin, DataOutputStream out) throws Exception {
		byte[] buffer = new byte[DynamixService.getConfig().getDefaultBufferSize()];
		int bytesRead;
		while ((bytesRead = fin.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}
		fin.close();
	}

	public void sendResponse(int statusCode, String responseString, String contentTypeLine, boolean isFile)
			throws Exception {
		System.out.println("Sending response: " + responseString);
		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer";
		String contentLengthLine = null;
		String fileName = null;
		// String contentTypeLine = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;
		String cors = "Access-Control-Allow-Origin: *" + "\r\n";
		if (statusCode == 200)
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
		if (isFile) {
			fileName = responseString;
			fin = new FileInputStream(fileName);
			contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
			if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
				contentTypeLine = "Content-Type: \r\n";
		} else {
			responseString = HTTPServer.HTML_START + responseString + HTTPServer.HTML_END;
			contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
		}
		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes(cors);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");
		if (isFile)
			sendFile(fin, outToClient);
		else
			outToClient.writeBytes(responseString);
		outToClient.close();
	}
	// public static void main(String args[]) throws Exception {
	//
	// ServerSocket Server = new ServerSocket(5000, 10, InetAddress.getByName("127.0.0.1"));
	// System.out.println("TCPServer Waiting for client on port 5000");
	//
	// while (true) {
	// Socket connected = Server.accept();
	// (new HTTPServer(connected)).start();
	// }
	// }
}
