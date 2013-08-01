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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.ResponseCache;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.PluginState;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.core.BaseActivity;
import org.ambientdynamix.core.DynamixApplication;
import org.ambientdynamix.core.DynamixService;
import org.ambientdynamix.core.R;
import org.ambientdynamix.security.PluginPrivacySettings;
import org.ambientdynamix.update.contextplugin.DiscoveredContextPlugin;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Static utility methods for the Dynamix Framework.
 * 
 * @author Darren Carlson
 */
public class Utils {
	// Private data
	private final static String TAG = Utils.class.getSimpleName();
	private static HostnameVerifier defaultHostnameVerifier;
	private static SSLSocketFactory defaultSSLSocketFactory;
	private static TrustManager[] defaultTrustManagers;
	private static SSLContext sc;

	// Singleton constructor
	private Utils() {
	}

	/**
	 * Downloads and creates a X509Certificate from the provided path.
	 */
	public static X509Certificate downloadCertificate(String path) throws IOException, CertificateException {
		URL url = new URL(path);
		InputStream is = new BufferedInputStream(url.openStream());
		CertificateFactory certFactory = CertificateFactory.getInstance("X509");
		X509Certificate cert = (X509Certificate) certFactory.generateCertificate(is);
		is.close();
		return cert;
	}

	/**
	 * Calculates the SHA-1 fingerprint of the incoming X509Certificate.
	 */
	public static String getFingerprint(X509Certificate cert) throws NoSuchAlgorithmException,
			CertificateEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] der = cert.getEncoded();
		md.update(der);
		byte[] digest = md.digest();
		return hexify(digest);
	}

	/**
	 * Converts the incoming byte array into a hex String.
	 */
	public static String hexify(byte bytes[]) {
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		StringBuffer buf = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; ++i) {
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
		}
		return buf.toString();
	}

	/**
	 * Returns true if the device is connected over WIFI; false otherwise.
	 */
	public static boolean isWifiConnected(Context c) {
		ConnectivityManager conMan = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	//	return mWifi.isConnected();
		return false;
	}

	/**
	 * Returns an identification url of the form <hostname>[:port]/<path>/[file]
	 * 
	 * @param rawUrl
	 * @return
	 * @throws MalformedURLException
	 */
	public static String getIdentificationUrl(String rawUrl, boolean onlyHostname) throws MalformedURLException {
		// Log.d(TAG, "Getting idURL for " + rawUrl);
		URL url = new URL(URLDecoder.decode(rawUrl));
		StringBuilder idUrl = new StringBuilder();
		// Add the hostname
		idUrl.append(url.getHost());
		// Add the port, if present
		if (url.getPort() != -1) {
			idUrl.append(":" + Integer.toString(url.getPort()));
			String port = Integer.toString(url.getPort());
		}
		// Add trailing slash for the hostname
		idUrl.append("/");
		if (!onlyHostname) {
			// Add the path, if present
			if (url.getPath() != null && url.getPath().length() > 0) {
				idUrl.append(url.getPath());
			}
		}
		// Remove any double slashes
		String finalUrl = idUrl.toString().replace("//", "/");
		// Log.d(TAG, "idURL for " + rawUrl + " is " + finalUrl);
		// Return final url of the path (minus any file)
		return finalUrl.substring(0, finalUrl.lastIndexOf('/') + 1);
	}

	/**
	 * Utility that dispatches the runnable using a daemon thread. Catches standard and uncaught exceptions.
	 * 
	 * @param runnable
	 */
	public static void dispatch(final Runnable runnable) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Exception e) {
					Log.w(TAG, "Exception during dispatch: " + e.toString());
				}
			}
		});
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				Log.w(TAG, "Uncaught exception during dispatch: " + ex.toString());
			}
		});
		t.start();
	}

	/**
	 * Returns the value for the specified argument from the url.
	 * 
	 * @param url
	 *            The URL to search.
	 * @param argument
	 *            The argument to extract.
	 * @return The value of the specified argument, or null if the argument cannot be found.
	 */
	public static String getArgumentValueFromUrl(String url, String argument) {
		try {
			// http://stackoverflow.com/questions/5902090/how-to-extract-parameters-from-a-given-url
			// Pattern p = Pattern.compile("r=([^&]+)");
			// Pattern p = Pattern.compile("(?<=repositoryId=).*?(?=&|$)");
			Pattern p = Pattern.compile(argument + "=([^&]+)");
			Matcher m = p.matcher(url);
			if (m.find()) {
				Log.i(TAG, "Found argument  " + argument + " in url " + url);
				return m.group(1);
			} else
				Log.i(TAG, "Could not find argument  " + argument + " in url " + url);
		} catch (PatternSyntaxException ex) {
			Log.w(TAG, "PatternSyntaxException: " + ex.toString());
		}
		return null;
	}

	/**
	 * Forces Dynamix to accept all self-signed certificates.
	 */
	public static void acceptAllSelfSignedSSLcertificates() {
		/*
		 * For details see: http://stackoverflow.com/questions/2642777/android-trusting-all-
		 * certificates-using-httpclient-over-https This idea may work:
		 * http://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
		 */
		Log.i(TAG, "Setting acceptAllSelfSignedSSLcertificates");
		initCertManagement();
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		} };
		HostnameVerifier hv = new HostnameVerifier() {
			/*
			 * Problem: This call appears to be cached after the first call (non-Javadoc)
			 * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
			 */
			@Override
			public boolean verify(String urlHostName, SSLSession session) {
				Log.w(TAG, "HostnameVerifier warning: URL Host: " + urlHostName + " vs. " + session.getPeerHost());
				return true;
			}
		};
		// Install the all-trusting trust manager
		try {
			// Re-initialize the SSLContext with the new values
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			sc.getServerSessionContext().setSessionTimeout(1);
			sc.getClientSessionContext().setSessionTimeout(1);
			sc.getServerSessionContext().setSessionCacheSize(1);
			sc.getClientSessionContext().setSessionCacheSize(1);
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			Log.i(TAG, "SSL Server Session Timeout is: " + sc.getServerSessionContext().getSessionTimeout());
			Log.i(TAG, "SSL Client Session Timeout is: " + sc.getClientSessionContext().getSessionTimeout());
		} catch (Exception e) {
			Log.e(TAG, "SSLContext Error: " + e.toString());
		}
	}

	/**
	 * Forces Dynamix to deny all self-signed certificates.
	 */
	public static void denyAllSelfSignedSSLcertificates() {
		Log.i(TAG, "Setting denyAllSelfSignedSSLcertificates");
		initCertManagement();
		try {
			Log.i(TAG, "Invalidating existing SSLSessions");
			Enumeration serverContext = sc.getServerSessionContext().getIds();
			while (serverContext.hasMoreElements()) {
				SSLSession session = sc.getServerSessionContext().getSession((byte[]) serverContext.nextElement());
				Log.i(TAG, "Invalidating server session: " + session);
				session.getSessionContext().setSessionTimeout(1);
				session.invalidate();
			}
			Enumeration clientContext = sc.getClientSessionContext().getIds();
			while (clientContext.hasMoreElements()) {
				SSLSession session = sc.getClientSessionContext().getSession((byte[]) clientContext.nextElement());
				Log.i(TAG, "Invalidating client session: " + session);
				session.getSessionContext().setSessionTimeout(1);
				session.invalidate();
			}
			// Re-initialize the SSLContext with the new values
			sc.init(null, getDefaultTrustManagers(), new java.security.SecureRandom());
			sc.getServerSessionContext().setSessionTimeout(1);
			sc.getClientSessionContext().setSessionTimeout(1);
			sc.getServerSessionContext().setSessionCacheSize(1);
			sc.getClientSessionContext().setSessionCacheSize(1);
			HttpsURLConnection.setDefaultHostnameVerifier(defaultHostnameVerifier);
			HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
			Authenticator.setDefault(null);
			CookieHandler.setDefault(null);
			ResponseCache.setDefault(null);
			System.gc();
		} catch (Exception e) {
			Log.e(TAG, "SSLContext Error: " + e.toString());
		}
	}

	/**
	 * Returns the VersionInfo associated with the current Android platform.
	 */
	public static VersionInfo getAndroidVersionInfo() {
		/*
		 * http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
		 * http://stackoverflow.com/questions/3423754/retrieving-android-api-version-programmatically
		 */
		String sdkString = Build.VERSION.SDK;
		int deprecated_sdk = Integer.parseInt(sdkString);
		if (deprecated_sdk < 4) {
			switch (deprecated_sdk) {
			case 1:
				return new VersionInfo(1, 0, 0);
			case 2:
				return new VersionInfo(1, 1, 0);
			case 3:
				return new VersionInfo(1, 5, 0);
			default:
				throw new RuntimeException("Unsupported Android SDK: " + Build.VERSION.SDK);
			}
		} else {
			switch (Build.VERSION.SDK_INT) {
			case 4:
				return new VersionInfo(1, 6, 0);
			case 5:
				return new VersionInfo(2, 0, 0);
			case 6:
				return new VersionInfo(2, 0, 1);
			case 7:
				return new VersionInfo(2, 1, 0);
			case 8:
				return new VersionInfo(2, 2, 0);
			case 9:
				return new VersionInfo(2, 3, 0);
			case 10:
				return new VersionInfo(2, 3, 3);
			case 11:
				return new VersionInfo(3, 0, 0);
			case 12:
				return new VersionInfo(3, 1, 0);
			case 13:
				return new VersionInfo(3, 2, 0);
			case 14:
				return new VersionInfo(4, 0, 0);
			default: {
				Log.w(TAG, "Android version is above our highest known version: " + Build.VERSION.SDK);
				return new VersionInfo(4, 0, 0);
			}
			}
		}
	}

	/**
	 * Returns the primary data directory path, including the trailing slash.
	 * 
	 * @param c
	 *            The Android Context of the Dynamix Framework instance.
	 */
	public static String getDataDirectoryPath(Context c) {
		return c.getDir("data", Context.MODE_PRIVATE).getPath() + "/";
	}

	/**
	 * Creates a DescriptiveIcon for the specified ContextPlugin.
	 */
	public static DescriptiveIcon getDescriptiveIcon(ContextPlugin plug) {
		if (plug.isEnabled()) {
			if (!plug.isInstalled()) {
				return new DescriptiveIcon(R.drawable.plugin_disabled, "Install pending...");
			} else if (!plug.isConfigured()) {
				return new DescriptiveIcon(R.drawable.plugin_disabled, "Not configured. Tap to configure.");
			} else {
				return new DescriptiveIcon(R.drawable.plugin_enabled, "Enabled. Tap and hold to disable or uninstall.");
			}
		} else {
			return new DescriptiveIcon(R.drawable.plugin_blocked, "Disabled. Tap and hold to enable.");
		}
	}

	/**
	 * General utility for getting an Enum from a String.
	 * 
	 * @param <T>
	 * @param c
	 *            The Enum Class
	 * @param string
	 *            The String of the Enum value to get
	 * @return The Enum value indicated by the String; or null if the value can't be found
	 *         http://stackoverflow.com/questions/604424/java-enum-converting-string -to-enum
	 */
	public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) throws Exception {
		if (c != null && string != null) {
			try {
				return Enum.valueOf(c, string.trim().toUpperCase());
			} catch (IllegalArgumentException ex) {
				throw new Exception("Cannot get string " + string + " from enum " + c);
			}
		}
		else throw new Exception("getEnumFromString encoutered null in either string " + string + " or class " + c);
	}

	/**
	 * Recursively walk a directory tree and return a List of all Files found; the List is sorted using
	 * File.compareTo().
	 * 
	 * @param aStartingDir
	 *            is a valid directory, which can be read.
	 */
	static public List<File> getFileListing(String path) throws FileNotFoundException {
		File aStartingDir = new File(path);
		validateDirectory(aStartingDir);
		List<File> result = getFileListingNoSort(aStartingDir);
		Collections.sort(result);
		return result;
	}

	/**
	 * http://nex-otaku-en.blogspot.com/2010/12/android-put-listview-in-scrollview.html
	 * 
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren2(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}
		int totalHeight = 0;
		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	/**
	 * Displays the incoming message to the user along options to close or restart Dynamix.
	 * 
	 * @param context
	 *            The Context of the caller
	 * @param message
	 *            The message to display
	 * @param exit
	 *            If true, Dynamix is closed; if false, Dynamix is restarted.
	 */
	public static void showGlobalAlert(final Activity context, String message, final boolean exit) {
		Log.i(TAG, "showGlobalAlert: " + message);
		if (context != null && !DynamixService.isEmbedded()) {
			try {
				AlertDialog dialog = new AlertDialog.Builder(context).create();
				dialog.setMessage(message);
				if (exit) {
					dialog.setButton("Close Dynamix", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							System.exit(0);
						}
					});
					dialog.setButton2("Restart Dynamix", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
							mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 3000,
									PendingIntent.getActivity(context, 0, new Intent(context, BaseActivity.class), 0));
							System.exit(2);
						}
					});
				} else {
					dialog.setButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				}
				dialog.show();
			} catch (Exception e) {
				Log.w(TAG, "Could not show alert: " + e);
			}
		} else
			Log.w(TAG, "Cannot show dialog, no Activity context provided");
	}

	/**
	 * Scans the incoming ContextPlugin for configuration errors, outputing descriptive log messages as needed.
	 * 
	 * @param plug
	 *            The ContextPlugin to validate Returns true if the ContextPlugin successfully validates (i.e. contained
	 *            no errors); false otherwise
	 */
	public static boolean validateContextPlugin(ContextPlugin plug) {
		// Log.d(TAG, "validateContextPlugin for: " + plug);
		if (plug != null) {
			if (plug.getId() != null && plug.getId().length() > 0) {
				if (plug.getName() != null && plug.getName().length() > 0) {
					if (plug.getSupportedContextTypes() == null || !plug.getSupportedContextTypes().isEmpty()) {
						if (plug.getSupportedPrivacyRiskLevels() == null
								|| !plug.getSupportedPrivacyRiskLevels().isEmpty()) {
							return true;
						} else
							Log.w(TAG, "ContextPlugin must have at least one supported privacy risk level");
					} else
						Log.w(TAG, "ContextPlugin must have at least one supported context type");
				} else
					Log.w(TAG, "ContextPlugin must have a name");
			} else
				Log.w(TAG, "ContextPlugin must have an id");
		} else
			Log.w(TAG, "ContextPlugin must not be null");
		Log.w(TAG, "ContextPlugin was INVALID: " + plug);
		return false;
	}

	/**
	 * Utility for initializing certificate management. This method also initializes the SSLContext on the first call.
	 */
	private static void initCertManagement() {
		if (sc == null) {
			try {
				/*
				 * Note that we need TLS on Android (not SSL)
				 */
				sc = SSLContext.getInstance("TLS");
				sc.init(null, getDefaultTrustManagers(), new java.security.SecureRandom());
				sc.getServerSessionContext().setSessionTimeout(1);
				sc.getClientSessionContext().setSessionTimeout(1);
				sc.getServerSessionContext().setSessionCacheSize(1);
				sc.getClientSessionContext().setSessionCacheSize(1);
			} catch (Exception e) {
				Log.e(TAG, "Could not get SSLContext: " + e);
			}
		}
		if (defaultHostnameVerifier == null) {
			defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
		}
		if (defaultSSLSocketFactory == null) {
			defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
		}
		if (defaultTrustManagers == null) {
			try {
				TrustManagerFactory factory = TrustManagerFactory
						.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				factory.init(KeyStore.getInstance(KeyStore.getDefaultType()));
				defaultTrustManagers = factory.getTrustManagers();
			} catch (Exception e1) {
				Log.w(TAG, "Could not get default trust managers: " + e1);
			}
		}
	}

	/**
	 * Returns the default TrustManagers.
	 */
	private static TrustManager[] getDefaultTrustManagers() {
		TrustManager[] tManagers = null;
		try {
			KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);
			tManagers = tmf.getTrustManagers();
		} catch (Exception e2) {
			Log.e(TAG, "Could not access default TrustManagers: " + e2);
		}
		return tManagers;
	}

	/**
	 * Utility method that returns the files recursively from a starting directory.
	 * 
	 * @param aStartingDir
	 *            The directory to start at.
	 */
	static private List<File> getFileListingNoSort(File aStartingDir) throws FileNotFoundException {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = aStartingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		for (File file : filesDirs) {
			result.add(file); // always add, even if directory
			if (!file.isFile()) {
				// must be a directory
				// recursive call!
				List<File> deeperList = getFileListingNoSort(file);
				result.addAll(deeperList);
			}
		}
		return result;
	}

	/**
	 * Validates the specified directory. Directory is valid if it exists, does not represent a file, and can be read.
	 */
	static private void validateDirectory(File aDirectory) throws FileNotFoundException {
		if (aDirectory == null) {
			throw new IllegalArgumentException("Directory should not be null.");
		}
		if (!aDirectory.exists()) {
			throw new FileNotFoundException("Directory does not exist: " + aDirectory);
		}
		if (!aDirectory.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: " + aDirectory);
		}
		if (!aDirectory.canRead()) {
			throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
		}
	}

	/**
	 * Returns the current CPU load for the specified process.
	 * 
	 * @param pid
	 *            The process to check
	 */
	public static float readCpuUsage(int pid) {
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/" + pid + "/stat", "r");
			String load = reader.readLine();
			String[] toks = load.split(" ");
			long idle1 = Long.parseLong(toks[5]);
			long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
					+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
			try {
				Thread.sleep(360);
			} catch (Exception e) {
			}
			reader.seek(0);
			load = reader.readLine();
			reader.close();
			toks = load.split(" ");
			long idle2 = Long.parseLong(toks[5]);
			long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
					+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
			return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
		} catch (IOException ex) {
			Log.w(TAG, "Could not read CPU Usage: " + ex);
		}
		return 0;
	}

	/**
	 * Returns a list of all running process (limit 1000).
	 */
	public static List<ActivityManager.RunningTaskInfo> getRunningProcesses(Context context) {
		ActivityManager mgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		return mgr.getRunningTasks(1000);
	}

	/**
	 * Returns a sorted List of DynamixApplication(s).
	 * 
	 * @param unsortedList
	 *            The List to sort.
	 */
	public static List<DynamixApplication> getSortedAppList(List<DynamixApplication> unsortedList) {
		List<DynamixApplicationSorter> sortList = new Vector<DynamixApplicationSorter>();
		for (DynamixApplication app : unsortedList)
			sortList.add(new DynamixApplicationSorter(app));
		Collections.<DynamixApplicationSorter> sort(sortList);
		List<DynamixApplication> returnList = new Vector<DynamixApplication>();
		for (DynamixApplicationSorter wrapper : sortList)
			returnList.add(wrapper.app);
		return returnList;
	}

	/**
	 * Returns a sorted List of ContextPlugin(s).
	 * 
	 * @param unsortedList
	 *            The List to sort.
	 */
	public static List<ContextPlugin> getSortedContextPluginList(List<ContextPlugin> unsortedList) {
		List<ContextPluginSorter> sortList = new Vector<ContextPluginSorter>();
		for (ContextPlugin plug : unsortedList)
			sortList.add(new ContextPluginSorter(plug));
		Collections.<ContextPluginSorter> sort(sortList);
		List<ContextPlugin> returnList = new Vector<ContextPlugin>();
		for (ContextPluginSorter wrapper : sortList)
			returnList.add(wrapper.plug);
		return returnList;
	}

	/**
	 * Returns a sorted List of DiscoveredContextPlugin(s).
	 * 
	 * @param unsortedList
	 *            The List to sort.
	 */
	public static List<DiscoveredContextPlugin> getSortedDiscoveredPluginList(List<DiscoveredContextPlugin> unsortedList) {
		List<ContextPluginUpdateSorter> sortList = new Vector<ContextPluginUpdateSorter>();
		for (DiscoveredContextPlugin update : unsortedList)
			sortList.add(new ContextPluginUpdateSorter(update));
		Collections.<ContextPluginUpdateSorter> sort(sortList);
		List<DiscoveredContextPlugin> returnList = new Vector<DiscoveredContextPlugin>();
		for (ContextPluginUpdateSorter wrapper : sortList)
			returnList.add(wrapper.update);
		return returnList;
	}

	/**
	 * Local class for comparing ContextPlugins.
	 */
	private static class ContextPluginSorter implements Comparable<ContextPluginSorter> {
		ContextPlugin plug;

		ContextPluginSorter(ContextPlugin plug) {
			this.plug = plug;
		}

		@Override
		public int compareTo(ContextPluginSorter candidate) {
			return plug.getName().compareTo(candidate.plug.getName());
		}
	}

	/**
	 * Local class for comparing DiscoveredPlugins.
	 */
	private static class ContextPluginUpdateSorter implements Comparable<ContextPluginUpdateSorter> {
		DiscoveredContextPlugin update;

		ContextPluginUpdateSorter(DiscoveredContextPlugin update) {
			this.update = update;
		}

		@Override
		public int compareTo(ContextPluginUpdateSorter candidate) {
			return update.getContextPlugin().getName().compareTo(candidate.update.getContextPlugin().getName());
		}
	}

	/**
	 * Local class for comparing DynamixApplications.
	 */
	private static class DynamixApplicationSorter implements Comparable<DynamixApplicationSorter> {
		DynamixApplication app;

		DynamixApplicationSorter(DynamixApplication app) {
			this.app = app;
		}

		@Override
		public int compareTo(DynamixApplicationSorter candidate) {
			return app.getName().compareTo(candidate.app.getName());
		}
	}

	/**
	 * Returns true if the specified runtime can launch a user interface; false otherwise.
	 * 
	 * @param app
	 *            The app wishing to launch the interface.
	 * @param wrapper
	 *            The target runtime wrapper.
	 * @param frameworkCall
	 *            True if this call was initiated by Dynamix; false otherweise
	 */
	public static boolean checkPluginInterfaceLaunchable(DynamixApplication app, ContextPluginRuntimeWrapper wrapper,
			boolean frameworkCall, boolean checkStarted) {
		ContextPlugin plug = wrapper.getContextPluginRuntime().getParentPlugin();
		if (plug != null) {
			// Make sure the plug-in is installed
			if (plug.isInstalled()) {
				// Make sure the plug-in is enabled
				if (plug.isEnabled()) {
					// Make sure the plug-in is started
					if (!checkStarted || wrapper.getState() == PluginState.STARTED) {
						// Check launch permission
						// Make sure the app has permission to launch the plug-in's UI
						if (frameworkCall || app.isAdmin()) {
							return true;
						} else {
							for (PluginPrivacySettings plugSet : app.getPluginPrivacySettings()) {
								if (plugSet.getPlugin().equals(plug)) {
									// We found the plug-in. Now check if the app is blocked from using the
									// plug-in
									if (plugSet.getMaxPrivacyRisk() != PrivacyRiskLevel.NONE) {
										// The app is not blocked from using the plug-in, so return true
										return true;
									} else
										Log.e(TAG, "App is blocked from accessing plug-in: " + plug);
								}
							}
						}
					} else
						Log.w(TAG, "Plug-in not started");
				} else
					Log.w(TAG, "Plugin is disabled " + plug);
			} else
				Log.w(TAG, "Plugin is not installed " + plug);
		} else
			Log.w(TAG, "Could not find parent plug-in for " + wrapper.getContextPluginRuntime());
		return false;
	}
}