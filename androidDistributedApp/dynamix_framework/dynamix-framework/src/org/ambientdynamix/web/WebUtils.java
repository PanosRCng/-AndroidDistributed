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
package org.ambientdynamix.web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.contextplugin.PluginConstants;
import org.ambientdynamix.core.DynamixService;
import org.ambientdynamix.util.base64.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Log;

/**
 * Collection of utilities for the Dynamix Web Connector and related classes.
 * 
 * @author Darren Carlson
 * 
 */
public class WebUtils {
	private static String TAG = WebUtils.class.getSimpleName();
	// Shared JSON Object mapper
	private static ObjectMapper mapper = new ObjectMapper();
	private static ActivityManager actMgr;
	private static CertificateFactory certFactory;
	private static PackageManager packMgr;
	static {
		SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		TimeZone tz = TimeZone.getTimeZone("UTC");
		timeFormatter.setTimeZone(tz);
		mapper.setDateFormat(timeFormatter);
	}

	// Singleton constructor
	private WebUtils() {
	}

	/**
	 * xports an certificate to a file.
	 * 
	 * @param cert
	 *            The certificate to export.
	 * @param file
	 *            The destination file.
	 * @param binary
	 *            True if the cert should be written as a binary file; false to encode using Base64.
	 */
	public static void exportCertificate(java.security.cert.Certificate cert, File file, boolean binary) {
		Log.i(TAG, "Writing cert to: " + file.getAbsolutePath());
		try {
			// Get the encoded form which is suitable for exporting
			byte[] buf = cert.getEncoded();
			FileOutputStream os = new FileOutputStream(file);
			if (binary) {
				// Write in binary form
				os.write(buf);
			} else {
				// Write in text form
				Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
				wr.write("-----BEGIN CERTIFICATE-----\n");
				Base64.encodeBase64(buf);
				wr.write("\n-----END CERTIFICATE-----\n");
				wr.flush();
			}
			os.close();
		} catch (Exception e) {
			Log.w(TAG, "Error writing cert for " + file);
		}
	}

	/**
	 * Returns the X509Certificate for the incoming packageName. Returns null if the packageName cannot be found (or if
	 * there was a certificate exception).
	 */
	public static X509Certificate getCertForApp(String packageName) {
		try {
			// Create packMgr, if needed
			if (packMgr == null) {
				packMgr = DynamixService.getAndroidContext().getPackageManager();
			}
			// Create certFactory, if needed
			if (certFactory == null) {
				certFactory = CertificateFactory.getInstance("X509");
			}
			PackageInfo packageInfo = packMgr.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			Signature[] signatures = packageInfo.signatures;
			// signatures[0] is a DER encoded X.509 certificate
			byte[] cert = signatures[0].toByteArray();
			InputStream input = new ByteArrayInputStream(cert);
			X509Certificate c = (X509Certificate) certFactory.generateCertificate(input);
			return c;
		} catch (NameNotFoundException e) {
			Log.w(TAG, "Package not found for " + packageName + " - " + e);
		} catch (CertificateException e) {
			Log.w(TAG, "Certificate exception for " + packageName + " - " + e);
		}
		return null;
	}

	/**
	 * Returns the RunningAppProcessInfo bound to the incoming socket. Returns null if the socket is not bound to any
	 * apps.
	 */
	public static RunningAppProcessInfo getAppProcessForSocket(Socket socket) {
		List<UidPortMapping> mappings = new ArrayList<WebUtils.UidPortMapping>();
		// Check for connection type
		if (socket.getInetAddress() instanceof java.net.Inet4Address) {
			// Map all IPV4 connections
			mappings.addAll(getUidPortMappings("/proc/net/tcp"));
		} else {
			// Map all IPV6 connections
			mappings.addAll(getUidPortMappings("/proc/net/tcp6"));
		}
		// Create the local activity manager, if needed
		if (actMgr == null) {
			// Grab the ActivityManager
			actMgr = (ActivityManager) DynamixService.getAndroidContext().getSystemService(Context.ACTIVITY_SERVICE);
		}
		/*
		 * Loop through the list of running processes, looking for a UID matching one of the cached connections.
		 */
		List<RunningAppProcessInfo> l = actMgr.getRunningAppProcesses();
		for (RunningAppProcessInfo app : l) {
			for (UidPortMapping connection : mappings) {
				if (connection.uid == app.uid)
					return app;
			}
		}
		Log.w(TAG, "Could not find RunningAppProcessInfo for socket " + socket);
		return null;
	}

	/*
	 * Struct-like helper class that maps a locally bound network port to an app UID.
	 */
	private static class UidPortMapping {
		private int uid; // The uid
		private int port; // The uid's locally bound port

		UidPortMapping(int uid, int port) {
			this.uid = uid;
			this.port = port;
		}
	}

	/*
	 * Helper method that obtains UidPortMappings from the incoming path. The path must point to a valid TCP socket
	 * table, such as "/proc/net/tcp" or "/proc/net/tcp6". For a description of the table format, see
	 * http://linuxdevcenter.com/pub/a/linux/2000/11/16/LinuxAdmin.html
	 */
	private static List<UidPortMapping> getUidPortMappings(String path) {
		// Create a return List of mappings
		List<UidPortMapping> mappings = new ArrayList<WebUtils.UidPortMapping>();
		try {
			// Try to 'cat' the path
			java.lang.Process proc = Runtime.getRuntime().exec(new String[] { "cat", path });
			// Create a reader for the proc's input stream
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()), 8192);
			String line = null;
			// Parse each line of the file
			while ((line = reader.readLine()) != null) {
				// Make sure we're not parsing a header
				if (!line.contains("local_address")) {
					// Replace multiple spaces with single spaces
					String clean = line.trim().replaceAll(" +", " ");
					// Split the clean string into tokens
					String[] tokens = clean.split(" ");
					// Token 1 is the 'local_address'
					String[] localAdd = tokens[1].split(":");
					// Token 7 is the 'uid'
					String uid = tokens[7];
					// Add the UidPortMapping
					mappings.add(new UidPortMapping(Integer.parseInt(uid), Integer.parseInt(localAdd[1], 16)));
				}
			}
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		return mappings;
	}

	/**
	 * Utility that serializes plug-in info into a format suitable for passing to web clients.
	 */
	// public static String serializeContextPluginInfo(ContextPluginInformation plugin) throws Exception {
	// return mapper.writeValueAsString(plugin);
	// }
	/**
	 * Utility that serializes a plug-in info list into a format suitable for passing to web clients.
	 */
	// public static String serializeContextPluginInfo(List<ContextPluginInformation> plugins) throws Exception {
	// return mapper.writeValueAsString(plugins);
	// }
	/**
	 * Utility that serializes context support info into a format suitable for passing to web clients.
	 */
	// public static String serializeContextSupportInfo(ContextSupportInfo supportInfo) throws Exception {
	// return mapper.writeValueAsString(supportInfo);
	// }
	/**
	 * Utility that serializes a context support info list into a format suitable for passing to web clients.
	 */
	// public static String serializeContextSupportInfo(List<ContextSupportInfo> supportInfoList) throws Exception {
	// return mapper.writeValueAsString(supportInfoList);
	// }
	/**
	 * Utility that serializes the incoming Object into a format suitable for passing to web clients. Defaults to JSON
	 * encoding format.
	 */
	public static String serializeObject(Object value) throws Exception {
		return mapper.writeValueAsString(value);
	}

	/**
	 * Utility that serializes the incoming Object into a format suitable for passing to web clients.
	 */
	public static String serializeObject(Object value, PluginConstants format) throws Exception {
		return mapper.writeValueAsString(value);
	}
}
