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
package org.ambientdynamix.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.ambientdynamix.api.application.AppConstants.PluginInstallStatus;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.IContextPluginRuntimeFactory;
import org.ambientdynamix.update.contextplugin.IContextPluginInstallListener;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Manages the underlying OSGi framework on behalf of the Dynamix Framework. This class is adapted from the FelixService
 * class developed within the Music Middleware project, which is released under an LGPL license.
 * 
 * @author Darren Carlson
 */
class OSGIManager implements FrameworkListener, ServiceListener, BundleListener {
	public final static String TAG = OSGIManager.class.getSimpleName();
	private static final LinkedBlockingQueue<BundleInstaller> queue = new LinkedBlockingQueue<BundleInstaller>();
	private static final Map<BundleInstallerWorker, Thread> installWorkers = new HashMap<BundleInstallerWorker, Thread>();
	private Context androidContext;
	private DynamixService service;
	/*
	 * Mapping of Bundles to their associated IContextPluginRuntimeFactory entity
	 */
	private Map<ContextPlugin, IContextPluginRuntimeFactory> factoryCache = new ConcurrentHashMap<ContextPlugin, IContextPluginRuntimeFactory>();
	/**
	 * Our local OSGi Framework
	 */
	protected static Framework osgiFramework;
	/*
	 * Optional system property to specify the work directory
	 */
	public final static String DEPLOYMENT_DIR_PROPERTY = "deployment.dir";
	/*
	 * Directory for the OSGi framework (subdirectory of the DEPLOYMENT_DIRECTORY)
	 */
	public final static String OSGI_DIR = "felix";
	/*
	 * Directory for the OSGi cache (subdirectory of the OSGI_DIR)
	 */
	public final static String OSGI_CACHE_DIR = "felix-cache";
	/*
	 * Filename where the version code of the application is installed. This version code is used to check if a new
	 * version of the application is installed (in that case, the OSGi cache directory must be removed and reinstalled
	 * all the bundles)
	 */
	public final static String VERSIONCODE_FILE = "versionCode";

	/**
	 * Creates an OSGIManager based on the incoming androidContext
	 * 
	 * @param androidContext
	 *            The Android Context of the DynamixService.
	 */
	public OSGIManager(Context androidContext) {
		this.androidContext = androidContext;
	}

	/**
	 * Utility method that replaces the existing Manifest of the incoming bundleJar with the replacementManifest. This
	 * is used during Bundle verification to create a temporary version of the Bundle that can be tested without causing
	 * problems.
	 * 
	 * @param bundleJar
	 *            The target Bundle JAR
	 * @param replacementManifest
	 *            The Bundle to inject into the bundleJar
	 * @throws IOException
	 */
	public static void updateManifest(File bundleJar, Manifest replacementManifest) throws IOException {
		Log.v(TAG, "updateManifest for: " + bundleJar);
		// Create tmp paths and files
		String tmpPath = bundleJar.getParent() + "/" + UUID.randomUUID().toString() + ".tmp";
		File tempFile = new File(tmpPath);
		String manifestPath = bundleJar.getParent() + "/" + UUID.randomUUID().toString() + ".mf";
		File manifestFile = new File(manifestPath);
		replacementManifest.write(new FileOutputStream(manifestPath));
		// delete it, otherwise you cannot rename your existing zip to it.
		tempFile.delete();
		boolean renameOk = bundleJar.renameTo(tempFile);
		if (!renameOk) {
			throw new RuntimeException("could not rename the file " + bundleJar.getAbsolutePath() + " to "
					+ tempFile.getAbsolutePath());
		}
		byte[] buf = new byte[DynamixService.getConfig().getDefaultBufferSize()];
		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(bundleJar));
		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			// Copy everything except for the manifest
			if (!name.equalsIgnoreCase("META-INF/MANIFEST.MF")) {
				// Add ZIP entry to the output stream.
				out.putNextEntry(new ZipEntry(entry.getName()));
				int len;
				while ((len = zin.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}
		// Close the input stream
		zin.close();
		// Create and InputStream from the manifestFile
		InputStream in = new FileInputStream(manifestFile);
		// Add the new manifest entry to the output stream.
		out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		// Complete the entry
		out.closeEntry();
		in.close();
		// Complete the ZIP file
		out.close();
		// Delete all tmp files
		tempFile.delete();
		manifestFile.delete();
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		Log.v(TAG, "bundleChanged: " + event.toString());
	}

	/**
	 * Event handler for OSGi FrameworkEvents.
	 * 
	 * @see FrameworkEvent
	 */
	public void frameworkEvent(FrameworkEvent event) {
		Log.v(TAG, "FrameworkEvent: " + event.toString());
		switch (event.getType()) {
		case FrameworkEvent.STARTED:
			BundleContext bc = osgiFramework.getBundleContext();
			try {
				// Security Method 1
				// securityService = (DynamixSecurityService) bc.getService(bc
				// .getServiceReference(DynamixSecurityService.class.getName()));
				// Security Method 2
				// securityService = new DynamixSecurityService(androidContext, osgiFramework.getBundleContext());
				// securityService.addManagmentAllPermission();
			} catch (Exception e) {
				Log.e(TAG, "Could not get DynamixSecurityService: " + e.toString());
			}
			listAllOSGiServices();
			listBundles();
			DynamixService.onOSGiFrameworkStarted();
			break;
		case FrameworkEvent.STOPPED:
			DynamixService.onOSGiFrameworkStopped();
			break;
		case FrameworkEvent.ERROR:
			DynamixService.onOSGiFrameworkError();
			break;
		}
	}

	/**
	 * Returns the previously cached IContextPluginRuntimeFactory for the specified ContextPlugin, or null if the
	 * factory was not found.
	 */
	public IContextPluginRuntimeFactory getContextPluginRuntimeFactory(ContextPlugin plug) {
		return factoryCache.get(plug);
	}

	/**
	 * Initialize the OSGIManager using a background Thread. Once OSGi is initialized, a FrameworkListener event will be
	 * passed from the OSGi Framework to our local 'frameworkEvent' method, which handles callbacks. This method is
	 * asynchronous.
	 */
	public void init() {
		startInstallerWorkers();
		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "Starting the OSGi framework...");
				// Get the directory for the application data and for storing the OSGi framework
				String applicationDataDir = getApplicationDataDir();
				String osgiDeploymentDir = applicationDataDir.concat(File.separator).concat(OSGI_DIR);
				Log.d(TAG, "The deployment directory for the Felix framework is: ".concat(osgiDeploymentDir));
				// Check if the .apk version code is already installed
				boolean isSameVersionCode = isSameVersionCode();
				// Check if it is required to (re)deploy the OSGi framework
				if (!isSameVersionCode || !new File(osgiDeploymentDir).exists()) {
					/*
					 * It is required to unzip the OSGi bundles. If the OSGi deployment dir already exists, remove it
					 * (to avoid conflicts between different versions)
					 */
					if (new File(osgiDeploymentDir).exists())
						new File(osgiDeploymentDir).delete();
					long initial_time = System.currentTimeMillis();
					unzipFile(androidContext.getResources().openRawResource(R.raw.felix), applicationDataDir);
					Log.v(TAG, "Unzipped in: " + (System.currentTimeMillis() - initial_time) / 1000 + " seconds.");
					// Replace the relative paths of the OSGi bundles in the
					// configuration file
					parseFelixConfigurationFile(osgiDeploymentDir);
					System.setProperty(Main.CONFIG_PROPERTIES_PROP,
							"file://".concat(osgiDeploymentDir).concat("/conf/parsed_config.properties"));
					// Save the version code
					if (!isSameVersionCode)
						saveVersionCode();
				} else {
					// The framework was already installed, so only restart the bundles
					System.setProperty(Main.CONFIG_PROPERTIES_PROP,
							"file://".concat(osgiDeploymentDir).concat("/conf/restart.properties"));
				}
				/*
				 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				 * Perhaps the .properties files are not being read because the properties are not working properly
				 * under Felix 4.x. For example, the logger and config admin services are NOT starting, which is
				 * suspicious. If this is correct, the root export path is also not working. Thus, even if the plug-in
				 * has proper permissions, the APIs will not resolve by wiring. A fix may include setting these
				 * properties in code using the 'configProps' below.
				 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				 */
				// Launch the OSGi framework
				// System.setProperty(Main.SYSTEM_PROPERTIES_PROP,
				// "file://".concat(osgiDeploymentDir).concat("/conf/system.properties"));
				// System.setProperty("java.security.policy",
				// "file://".concat(osgiDeploymentDir).concat("/conf/all.policy"));
				// Log.e(TAG, "Security Policy Path: " +
				// "file://".concat(osgiDeploymentDir).concat("/conf/all.policy"));
				/*
				 * I'm getting a class cast exception in Permission.java ( Seems to be a bug:
				 * https://issues.apache.org/jira/browse/FELIX-3101
				 */
				// Load system properties
				Main.loadSystemProperties();
				// Setup framework config
				Properties configProps = Main.loadConfigProperties();
				// Map configProps = new HashMap();
				configProps.put(Constants.FRAMEWORK_STORAGE, OSGI_CACHE_DIR);
				configProps.put("felix.auto.deploy.dir", osgiDeploymentDir + "/bundle");
				configProps.put(BundleCache.CACHE_ROOTDIR_PROP, osgiDeploymentDir);
				/*
				 * Security configuration options.
				 */
				// String ksPath = osgiDeploymentDir.concat("/conf/dynamix.ks");
				// Log.i(TAG, "KeyStore: " + ksPath + " exists: " + new File(ksPath).exists() );
				// configProps.put(Constants.FRAMEWORK_TRUST_REPOSITORIES, ksPath);
				// configProps.setProperty(SecurityConstants.KEYSTORE_FILE_PROP, ksPath);
				// configProps.setProperty(SecurityConstants.KEYSTORE_TYPE_PROP, "JKS");
				// configProps.setProperty(SecurityConstants.KEYSTORE_PASS_PROP, "");
				// configProps.put(SecurityConstants.ENABLE_PERMISSIONADMIN_PROP, "false");
				/*
				 * Load Framework Security by using the felix system bundle activators property. We need to handle
				 * loading security this way because we can't use auto-install. This is because Dalvik loads a
				 * PathClassLoader as its system classloader by default, but Felix needs a URLClassLoader loader to load
				 * the security bundle (ExtensionManager.java).
				 * http://www.mail-archive.com/users@felix.apache.org/msg10521.html -
				 * https://issues.apache.org/jira/browse/FELIX-2780 (note that there is a workaround patch) - Apparently
				 * fixed: https://issues.apache.org/jira/browse/FELIX-2877 -
				 * http://stackoverflow.com/questions/5796012/java-automatic-custom-classloader (inject) -
				 * http://felix.apache.org/site/apache-felix-framework-configuration-properties.html Felix folder layout
				 * - http://felix.apache.org/site/apache-felix-framework-bundle-cache.html Check secure version of
				 * Felix: http://sfelix.gforge.inria.fr/ Structure of signed bundle:
				 * https://www.owasp.org/index.php/Protecting_code_archives_with_digital_signatures
				 * #Bundle_Signature_Generation
				 */
				/*
				 * felix.systembundle.activators example: http://www.simexplorer.org/wiki/DevDoc/Netigso
				 */
				// List<BundleActivator> activators = new ArrayList<BundleActivator>();
				// activators.add(new org.apache.felix.framework.SecurityActivator());
				// activators.add(new DynamixSecurityActivator());
				// configProps.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators);
				try {
					long initial_time = System.currentTimeMillis();
					// Create an instance of the framework.
					FrameworkFactory factory = new org.apache.felix.framework.FrameworkFactory();
					osgiFramework = factory.newFramework(configProps);
					// Initialize the framework, but don't start it yet.
					osgiFramework.init();
					// Add ourselves as a FrameworkListener
					osgiFramework.getBundleContext().addFrameworkListener(OSGIManager.this);
					osgiFramework.getBundleContext().addBundleListener(OSGIManager.this);
					// Create a filtered event listener for IContextPluginRuntimeFactory services
					// TODO: Remove this since we handle service registrations manually?
					// String filter = "(" + Constants.OBJECTCLASS + "=" + IContextPluginRuntimeFactory.class.getName()
					// + ")";
					// osgiFramework.getBundleContext().addServiceListener(OSGIManager.this, filter);
					String filter = "(" + Constants.OBJECTCLASS + "=" + IContextPluginRuntimeFactory.class.getName()
							+ ")";
					Log.d(TAG, "Registering service listener: " + filter);
					osgiFramework.getBundleContext().addServiceListener(OSGIManager.this, filter);
					// Use the system bundle context to process the auto-deploy and auto-install/auto-start properties.
					AutoProcessor.process(configProps, osgiFramework.getBundleContext());
					// Start the framework.
					osgiFramework.start();
					Log.i(TAG, "OSGi framework started in: " + (System.currentTimeMillis() - initial_time) / 1000
							+ " seconds");
				} catch (Throwable t) {
					Log.w(TAG, "The OSGi framework could not be started", t);
				}
			}
		}.start();
	}

	/**
	 * Installs the OSGi Bundle for the specified ContextPlugin. If the Bundle is already installed for the
	 * ContextPlugin, this method operates synchronously, returning true immediately. However, if the OSGi Bundle is not
	 * installed, this method operates asynchronously, launching a threaded BundleInstaller and returning false. If a
	 * BundleInstaller is launched, the OSGi Framework will call the 'serviceChanged' method when the process is
	 * complete.
	 * 
	 * @return True id the plugin was already installed; false if not (meaning the OSGiManager will try to install it)
	 */
	public boolean installBundle(ContextPlugin plug, IContextPluginInstallListener listener) {
		if (isActive()) {
			if (plug != null) {
				synchronized (queue) {
					if (plug.isInstalled()) {
						Log.w(TAG, "Plugin is already installed: " + plug);
						return true;
					} else {
						// Create a BundleInstaller
						BundleInstaller installer = new BundleInstaller(plug, listener);
						Log.d(TAG, "Checking for existing installer for: " + plug);
						if (!queue.contains(installer)) {
							// Bundle was not found in our set of active installers either, so start installing it
							Log.d(TAG, "Installing Bundle for ContextPlugin: " + plug);
							/*
							 * Set the install status to INSTALLING on the current thread so addContextSupport works
							 * properly.
							 */
							plug.setInstallStatus(PluginInstallStatus.INSTALLING);
							// Add the installer to the install queue
							queue.add(installer);
						} else
							Log.w(TAG,
									"Plugin already installing: " + plug + " with install status: "
											+ plug.getInstallStatus());
					}
				}
			} else
				Log.e(TAG, "ContextPlugin was null in installBundle");
		}
		return false;
	}

	/**
	 * Returns true if the factory is already installed for the plug-in; false otherwise.
	 */
	public boolean isBundleInstalled(ContextPlugin plug) {
		return factoryCache.containsKey(plug);
	}

	/**
	 * Event handler for OSGi ServiceEvents.
	 * 
	 * @see ServiceEvent
	 */
	public void serviceChanged(ServiceEvent event) {
		Log.v(TAG, "serviceChanged for: " + event.getServiceReference().getBundle().getSymbolicName() + " type: "
				+ event.getType());
		// Handle service registrations (we are going this now in startPluginBundle)
		if (event.getType() == ServiceEvent.REGISTERED) {
			// Cache the IContextPluginRuntimeFactory instances locally for each Bundle
			// Bundle b = event.getServiceReference().getBundle();
			// if (b != null) {
			// try {
			// // Make the factory using the Bundle
			// IContextPluginRuntimeFactory factory = (IContextPluginRuntimeFactory) event.getServiceReference()
			// .getBundle().getBundleContext().getService(event.getServiceReference());
			//
			// // Cache the factory
			// factoryCache.put(b, factory);
			// }
			// catch (Exception e) {
			// Log.w(TAG, "Error creating IContextPluginRuntimeFactory: " + e.toString());
			// }
			// }
			// else Log.w(TAG, "event.getServiceReference().getBundle() was NULL");
		}
		/*
		 * Hand the ServiceEvent to the DynamixService for further processing. Note that DynamixService may be null if
		 * it's not yet booted, so we check for null first.
		 */
		if (service != null)
			service.handleServiceEvent(event);
	}

	/**
	 * Sets the reference to the DynamixService. This is necessary for performing callbacks.
	 * 
	 * @param service
	 *            The current DynamixService object.
	 */
	public void setService(DynamixService service) {
		this.service = service;
	}

	/**
	 * Starts the OSGi Bundle associated with the specified ContextPlugin.
	 * 
	 * @param plug
	 *            The ContextPlugin's bundle to start. Returns true if the bundle was started; false, otherwise.
	 */
	public boolean startPluginBundle(ContextPlugin plug) {
		if (isActive()) {
			if (plug.isInstalled()) {
				Log.d(TAG, "Trying to start OSGi bundle for " + plug);
				// Access the OSGi Bundle, using the bundle id stored in the ContextPlugin
				Bundle b = osgiFramework.getBundleContext().getBundle(plug.getBundleId());
				// Check if we got a Bundle (meaning that it was installed in the osgi framework)
				if (b != null) {
					try {
						Log.d(TAG, "Beginning start with OSGi bundle state: " + b.getState());
						/*
						 * Links: http://java.dzone.com/articles/osgi-security-fly
						 * http://java.sun.com/developer/onlineTraining/Programming/JDCBook/appA.html
						 * http://publib.boulder.ibm
						 * .com/infocenter/wasinfo/fep/index.jsp?topic=/com.ibm.websphere.nd.multiplatform
						 * .doc/info/ae/ae/rsec_clientpolicy.html Good:
						 * http://publib.boulder.ibm.com/infocenter/wasinfo/v7r0
						 * /index.jsp?topic=/com.ibm.websphere.osgifep.multiplatform.doc/topics/ca_java2sec.html
						 * http://www.knopflerfish.org/snapshots_trunk/current_trunk/docs/osgi_with_security.html
						 */
						// if(securityService != null)
						// securityService.lockDownBundle(b);
					} catch (Exception e1) {
						Log.w(TAG, "Could not updatePermissions: " + e1.toString());
					}
					// TODO: Update security here
					/*
					 * Security issues: - When Dalvik loads, it uses a PathClassLoader by as its system classloader by
					 * default - Felix needs a URLClassLoader loader to load the security bundle (ExtensionManager.java)
					 * - This is a problem. Solutions: Maybe having the Felix classes only referenced by a Bundle -
					 * meaning that it will be given a URLClassLoader by the OSGi Framework. This would mean probably
					 * shifting the entire Dynamix Framework into the OSGi world. - This might not work, though, because
					 * Android still needs to start the OSGi framework itself, and, thus, will become the root
					 * classloader. Links: - http://www.mail-archive.com/users@felix.apache.org/msg10521.html -
					 * https://issues.apache.org/jira/browse/FELIX-2780 (note that there is a workaround patch) -
					 * Apparently fixed: https://issues.apache.org/jira/browse/FELIX-2877 -
					 * http://stackoverflow.com/questions/5796012/java-automatic-custom-classloader (inject) -
					 * http://felix.apache.org/site/apache-felix-framework-configuration-properties.html
					 */
					// Security Notes:
					// http://felix.apache.org/site/apache-felix-framework-security.html
					// http://www.apacheserver.net/Osgi-security-at184394.htm
					// http://www.apacheserver.net/OSGi-iPojo-Messaging-and-Security-at234221.htm
					// http://markmail.org/message/wlhgw6o6buvi3jwy#query:Felix%20Framework%20Security+page:1+mid:s2jn5zdhrm6nqbxn+state:results
					// Good Keystore and cert manager:
					// http://portecle.sourceforge.net/
					// Launch using: java -jar portecle.jar
					// http://osdir.com/ml/users-felix-apache/2010-06/msg00162.html
					// http://felix.apache.org/site/presentations.data/Building%20Secure%20OSGi%20Applications.pdf
					// http://www.mail-archive.com/users@felix.apache.org/msg02631.html (help
					// enabling security with policies)
					// http://felix.apache.org/site/apache-felix-framework-security.html
					// http://felix.apache.org/site/apache-felix-framework-usage-documentation.html
					// Custom policy:
					// http://www.mail-archive.com/users@felix.apache.org/msg05522.html
					/*
					 * Ok, there appears to be an issue with the version of both the security framework, plus the felix
					 * framework itself Basically, we need the 3.1.0 Snapshot felix with the 1.3.0 snapshot security
					 * see: http://www.mail-archive.com/users@felix.apache.org/msg07967.html Related, there is some OSGi
					 * in action sample code on security here: http://osgi-in-action.googlecode.com/svn/trunk
					 * /chapter14/combined-example /org.foo.policy/src/org/foo/policy/Activator.java
					 */
					Map<?, ?> m = b.getSignerCertificates(Bundle.SIGNERS_ALL);
					Log.d(TAG, "Checking X509Certificates: Size is " + m.size());
					for (Object cert : m.keySet()) {
						Log.d(TAG, "Cert: " + cert);
						if (cert instanceof X509Certificate) {
							X509Certificate tmp = (X509Certificate) cert;
							Log.d(TAG, "Cert: " + tmp);
						}
					}
					// Make sure the Bundle is ACTIVE
					if (b.getState() == Bundle.ACTIVE) {
						Log.d(TAG, "Bundle was already ACTIVE, no need to start it.");
						if (registerFactory(plug, b)) {
							Log.d(TAG, "Started Bundle for: " + plug);
							return true;
						}
					} else {
						// Bundle was not ACTIVE, so start it
						try {
							Log.d(TAG, "Bundle was not ACTIVE - starting it...");
							b.start();
							// Wait for the Bundle to become active (this might not be needed if 'b.start()' is blocking
							// -
							// not sure).
							int bundleStartCount = 0;
							while (b.getState() != Bundle.ACTIVE) {
								try {
									Log.d(TAG, "Waiting for Bundle to finish starting for: " + plug);
									Thread.sleep(250);
								} catch (InterruptedException e) {
								}
								if (bundleStartCount++ > 12)
									throw new Exception("Bundle start timeout for: " + plug);
							}
							Log.d(TAG, "Started Bundle: " + b + ", which now has state: " + b.getState());
							// Bundle has started, so register its factory.
							if (registerFactory(plug, b)) {
								Log.d(TAG, "Started Bundle for: " + plug);
								return true;
							}
						} catch (Exception e) {
							Log.e(TAG, "startPluginBundle Exception: " + e);
						}
					}
				} else
					Log.e(TAG, "Bundle not found for ContextPlugin: " + plug);
			} else
				Log.w(TAG, "Cannot start " + plug + " because it's not installed!");
		}
		// If we fall through to here, there was a problem starting the bundle, so return false.
		return false;
	}

	/**
	 * Stops the OSGi Framework and clears the factory and installer caches.
	 */
	public void stopFramework() {
		Log.d(TAG, "Stopping OSGi Framework");
		if (osgiFramework != null) {
			try {
				queue.clear();
				stopInstallerWorkers();
				factoryCache.clear();
				osgiFramework.stop();
				osgiFramework = null;
			} catch (BundleException e) {
				e.printStackTrace();
				Log.e(TAG, "stopFramework error: " + e.toString());
			}
		}
	}

	/**
	 * Stops the OSGi Bundle associated with the specified ContextPlugin.
	 * 
	 * @param plug
	 *            The ContextPlugin's bundle to stop. Returns true if the bundle was stopped; false, otherwise.
	 */
	public boolean stopPluginBundle(ContextPlugin plug) {
		Log.d(TAG, "Trying to stop OSGi bundle: " + plug.getBundleId());
		if (isActive()) {
			if (plug.isInstalled()) {
				// Access the OSGi Bundle, using the bundle id stored in the ContextPlugin
				Bundle b = osgiFramework.getBundleContext().getBundle(plug.getBundleId());
				if (b != null) {
					try {
						// Stop the bundle, which automatically stops any associated services.
						b.stop();
						// Remove the plug-in's factory
						synchronized (factoryCache) {
							factoryCache.remove(plug);
						}
						// Remove any installed security for the plug-in
						// securityService.noPerms(b);
						Log.d(TAG, "Stopped OSGi Bundle for ContextPlugin: " + plug);
						return true;
					} catch (BundleException e) {
						Log.e(TAG, "stopPluginBundle exception: " + e.toString());
					}
				} else {
					Log.w(TAG, "Bundle not found for ContextPlugin: " + plug);
				}
			} else {
				Log.w(TAG, "Cannot stop " + plug + " because it's not installed!");
				return true;
			}
		}
		return false;
	}

	/**
	 * Uninstalls a previously installed OSGi Bundle for the specified ContextPlugin.
	 * 
	 * @param plug
	 *            The ContextPlugin's Bundle to uninstall. Returns true if the Bundle was uninstalled; false, otherwise.
	 */
	public boolean uninstallBundle(ContextPlugin plug) {
		Log.d(TAG, "uninstallBundle for " + plug + " with bundleID: " + plug.getBundleId());
		if (isActive()) {
			// Access the OSGi Bundle, using the bundle id stored in the ContextPlugin
			Bundle b = osgiFramework.getBundleContext().getBundle(plug.getBundleId());
			// If the Bundle is not null, uninstall it
			if (b != null) {
				// Stop the plugin, which removes the factoryCache reference
				stopPluginBundle(plug);
				// Uninstall the plug-in's Bundle
				try {
					// Uninstall the Bundle
					b.uninstall();
					// Update the plug-in's state
					plug.setInstallStatus(PluginInstallStatus.NOT_INSTALLED);
					return true;
				} catch (BundleException e) {
					Log.e(TAG, "Bundle Uninstall Error: " + e.toString());
				}
			} else {
				Log.w(TAG, "uninstallPlugin could not find Bundle for: " + plug);
			}
		}
		return false;
	}

	/**
	 * Updates an existing plug-in with a new plug-in while maintaining the existing settings, if present.
	 * 
	 * @param originalPlug
	 *            The plug-in to update
	 * @param originalSettings
	 *            The existing plug-ins settings (or null, if there are none)
	 * @param newPlug
	 *            The new plug-in
	 * @param listener
	 *            The listener to send update progress to
	 * @return True if the update process was started; false otherwise
	 */
	public boolean updatePluginBundle(ContextPlugin originalPlug, ContextPluginSettings originalSettings,
			ContextPlugin newPlug, IContextPluginInstallListener listener) {
		Log.d(TAG, "updatePluginBundle for " + originalPlug);
		if (isActive()) {
			if (newPlug != null) {
				synchronized (queue) {
					if (newPlug.isInstalled()) {
						Log.w(TAG, "Plugin is already installed: " + newPlug);
						return true;
					} else {
						BundleInstaller installer = new BundleInstaller(originalPlug, newPlug, originalSettings,
								listener);
						if (!queue.contains(installer)) {
							// Bundle was not found in our set of active installers either, so start installing it
							Log.d(TAG, "Updating " + originalPlug + " with " + newPlug);
							/*
							 * Set the install status to INSTALLING on the current thread so addContextSupport installs
							 * work properly.
							 */
							newPlug.setInstallStatus(PluginInstallStatus.INSTALLING);
							// Add the installer to the queue
							queue.add(installer);
							return true;
						} else
							Log.w(TAG, "Plugin already updating: " + originalPlug);
					}
				}
			} else
				Log.e(TAG, "ContextPlugin was null in updatePluginBundle");
		}
		return false;
	}

	/**
	 * Cancels a previously started plug-in installation or update.
	 * 
	 * @param plug
	 *            The plug-in to cancel the installation for (in case of updates, specify the original plug-in).
	 * @return True if the installation or update was canceled; false otherwise.
	 */
	protected boolean cancelInstallation(ContextPlugin plug) {
		Log.d(TAG, "Cancel installation requested for: " + plug);
		// Check the queue for any pending installers
		synchronized (queue) {
			for (BundleInstaller bi : queue) {
				if (bi.originalPlug.equals(plug)) {
					Log.d(TAG, "Cancelling queued installer for: " + plug);
					queue.remove(bi);
					return true;
				}
			}
		}
		synchronized (installWorkers) {
			// Check for the installer in the current installer workers
			for (BundleInstallerWorker worker : installWorkers.keySet()) {
				if (worker.isInstalling() && worker.currentInstaller.newPlug.equals(plug)) {
					Log.d(TAG, "Cancelling running installer for: " + plug);
					worker.cancelCurrentInstall();
					return true;
				}
			}
		}
		Log.w(TAG, "Could not find an installer to cancel for: " + plug);
		return false;
	}

	/**
	 * Returns the ClassLoader for the specified ContextPlugin.
	 */
	ClassLoader getContextPluginClassLoader(ContextPlugin plug) {
		IContextPluginRuntimeFactory factory = factoryCache.get(plug);
		if (factory != null) {
			return factory.getClass().getClassLoader();
		} else
			Log.w(TAG, "getContextPluginClassLoader could not find factory for: " + plug);
		return null;
	}

	/**
	 * Get the directory for the application data
	 * 
	 * @return
	 */
	private String getApplicationDataDir() {
		String applicationDataDir = null;
		try {
			// Get the directory if the system property is set
			applicationDataDir = System.getProperty(DEPLOYMENT_DIR_PROPERTY);
			if (applicationDataDir == null)
				// Take the default directory
				applicationDataDir = androidContext.getFilesDir().getAbsolutePath();
		} catch (Throwable t) {
			Log.w(TAG, "Error getting the directory for the application data", t);
		}
		return applicationDataDir;
	}

	/**
	 * Get the versionCode from the AndroidManifest file of the Android application
	 * 
	 * @return
	 */
	private int getVersionCodeFromManifest() {
		int versionCode = -1;
		try {
			PackageInfo packageInfo = androidContext.getPackageManager().getPackageInfo(
					androidContext.getPackageName(), PackageManager.GET_META_DATA);
			versionCode = packageInfo.versionCode;
		} catch (Throwable t) {
			Log.w(TAG, "Error getting the version code from the AndroidManifest XML file", t);
		}
		return versionCode;
	}

	/**
	 * Check if it is not installed a new version of the application by comparing the versionCode in the
	 * AndroidManifest.xml file and the value in the versionCode file in the data directory of the application
	 * 
	 * @return
	 */
	private boolean isSameVersionCode() {
		boolean isSaveVersionCode = false;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = androidContext.openFileInput(VERSIONCODE_FILE);
			byte[] buffer = new byte[128];
			int count;
			String savedVersionCodeString = "";
			while ((count = fileInputStream.read(buffer)) != -1)
				savedVersionCodeString += new String(buffer, 0, count);
			int savedVersionCode = Integer.parseInt(savedVersionCodeString);
			int manifestVersionCode = getVersionCodeFromManifest();
			if (manifestVersionCode != -1 && manifestVersionCode == savedVersionCode)
				isSaveVersionCode = true;
		} catch (Throwable t) {
			// Error reading the saved version code (perhaps it is the first
			// installation of the application)
			Log.w(TAG, "The version code is not saved yet. Is it the first installation?");
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (Throwable th) {
					Log.w(TAG, "The versionCode file reader could not be closed", th);
				}
			}
		}
		return isSaveVersionCode;
	}

	/**
	 * Utility method that outputs all installed OSGi services to the Log. See:
	 * http://www.osgi.org/javadoc/r4v42/org/osgi /framework/BundleContext.html#getServiceReferences
	 * %28java.lang.String,%20java.lang.String%29
	 */
	private void listAllOSGiServices() {
		Log.d(TAG, "Listing installed OSGi services");
		ServiceReference[] srs;
		try {
			srs = osgiFramework.getBundleContext().getAllServiceReferences(null, null);
			for (ServiceReference sr : srs) {
				Log.d(TAG, "Service: " + sr);
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Utility method that outputs all installed OSGi Bundles to the Log.
	 */
	private void listBundles() {
		Log.d(TAG, "Listing installed OSGi bundles");
		Bundle[] bdls;
		bdls = osgiFramework.getBundleContext().getBundles();
		for (Bundle b : bdls) {
			Log.d(TAG, "Bundle: " + b + " / State: " + b.getState());
		}
	}

	/**
	 * Generate a new configuration file where the relative paths to the OSGi bundles are replaced by absolute patch
	 * considering the felixDeploymentDir. This is just a hack for Felix which does not permit to specify the directory
	 * where the OSGi bundles are stores as other OSGi frameworks actually do.
	 * 
	 * @param felixDeploymentDir
	 * @return
	 */
	private boolean parseFelixConfigurationFile(String felixDeploymentDir) {
		boolean isParsed = false;
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		try {
			// Paths to the original and parsed configuration files
			String originalConfigFilePath = felixDeploymentDir.concat("/conf/config.properties");
			String parsedConfigFilePath = felixDeploymentDir.concat("/conf/parsed_config.properties");
			// Open a stream to read the original configuration file
			bufferedReader = new BufferedReader(new FileReader(originalConfigFilePath));
			bufferedWriter = new BufferedWriter(new FileWriter(parsedConfigFilePath));
			Pattern pattern = Pattern.compile("file:bundle/");
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				line = matcher.replaceAll("file:".concat(felixDeploymentDir).concat("/bundle/"));
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (Throwable th) {
					Log.w(TAG, "The buffered reader for the original configuration file could not be closed", th);
				}
			if (bufferedWriter != null)
				try {
					bufferedWriter.close();
				} catch (Throwable th) {
					Log.w(TAG, "The buffered writer for the parsed configuration file could not be closed", th);
				}
		}
		return isParsed;
	}

	/**
	 * Creates and registers the IContextPluginRuntimeFactory for the specified ContextPlugin using the Bundle.
	 * 
	 * @return True if registration succeeds; false otherwise.
	 */
	private boolean registerFactory(ContextPlugin plug, Bundle b) {
		Log.d(TAG, "registerFactory for: " + plug);
		synchronized (factoryCache) {
			if (!factoryCache.containsKey(plug)) {
				// Make sure the bundle is not starting or active
				if (b.getState() == Bundle.ACTIVE) {
					try {
						Log.d(TAG, "Trying to create IContextPluginRuntimeFactory for: " + plug);
						// Grab the factoryClass from the Bundle using the plugin's specified runtime factory class
						Class<?> factoryClass = b.loadClass(plug.getRuntimeFactoryClass());
						// Create a new instance of the IContextPluginRuntimeFactory using the factoryClass
						IContextPluginRuntimeFactory factory = (IContextPluginRuntimeFactory) factoryClass
								.newInstance();
						Log.d(TAG, "Created IContextPluginRuntimeFactory: " + factory + " for " + plug);
						// Make sure the factory is of the right type
						if (factory instanceof IContextPluginRuntimeFactory) {
							// Cache the factory
							factoryCache.put(plug, factory);
							Log.d(TAG, "IContextPluginRuntimeFactory registered for: " + plug);
							return true;
						} else
							Log.w(TAG,
									"registerFactory found a factoryClass that was not a IContextPluginRuntimeFactory: "
											+ factory);
					} catch (Exception e) {
						Log.e(TAG, "registerFactory Exception: " + e);
					}
				} else {
					// We return true here because we are starting or have started
					Log.w(TAG, "Bundle must be ACTIVE for factory registration. Bundle state was: " + b.getState());
				}
			} else {
				Log.d(TAG, "Factory already registered for: " + plug);
				return true;
			}
			// If we fall through to here, factory registration failed, so return false.
			return false;
		}
	}

	/**
	 * Save the AndroidManifest.xml version code in the versionCode file
	 */
	private void saveVersionCode() {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = androidContext.openFileOutput(VERSIONCODE_FILE, Context.MODE_PRIVATE);
			int versionCode = getVersionCodeFromManifest();
			String versionCodeString = String.valueOf(versionCode);
			fileOutputStream.write(versionCodeString.getBytes());
			Log.d(TAG, "The current version code  is ".concat(versionCodeString));
		} catch (Throwable t) {
			Log.w(TAG, "The version code could not be saved", t);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Throwable th) {
					Log.w(TAG, "The versionCode file writer could not be closed", th);
				}
			}
		}
	}

	/**
	 * Utility method that starts the installer worker threads.
	 */
	void startInstallerWorkers() {
		stopInstallerWorkers();
		Log.d(TAG, "Starting installer workers");
		synchronized (installWorkers) {
			for (int i = 0; i < DynamixService.getConfig().getInstallerWorkersCount(); i++) {
				BundleInstallerWorker worker = new BundleInstallerWorker(queue);
				Thread t = new Thread(worker);
				installWorkers.put(worker, t);
				t.setDaemon(true);
				t.setPriority(Thread.MIN_PRIORITY);
				t.start();
			}
		}
	}

	/**
	 * Utility method that stops all installer worker threads.
	 */
	void stopInstallerWorkers() {
		Log.d(TAG, "Stopping installer workers");
		synchronized (installWorkers) {
			for (BundleInstallerWorker worker : installWorkers.keySet()) {
				worker.stopWorker();
			}
			installWorkers.clear();
		}
	}

	/**
	 * Unzip an input stream into a directory
	 * 
	 * @param inputStream
	 *            Input stream corresponding to the zip file
	 * @param directory
	 *            Directory to store the unzipped content
	 */
	private void unzipFile(InputStream inputStream, String directory) {
		try {
			Log.v(TAG, "Unzip the OSGi framework to: " + directory);
			JarInputStream jarInputStream = new JarInputStream(inputStream);
			JarEntry jarEntry;
			byte[] buffer = new byte[DynamixService.getConfig().getDefaultBufferSize()];
			while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
				File jarEntryFile = new File(directory + File.separator + jarEntry.getName());
				if (jarEntry.isDirectory()) {
					jarEntryFile.mkdirs();
					continue;
				}
				FileOutputStream fos = new FileOutputStream(jarEntryFile);
				while (true) {
					int read = jarInputStream.read(buffer);
					if (read == -1)
						break;
					fos.write(buffer, 0, read);
				}
				fos.close();
			}
			jarInputStream.close();
		} catch (Throwable t) {
			Log.w(TAG, "Error unzipping the zip file", t);
		}
	}

	/**
	 * Utility method that verifies the integrity of the specified Bundle. It turns out to be more difficult than it
	 * seems to verify that a Bundle is not corrupt on Android. This is because the OSGi framework will happily load a
	 * Bundle that appears to have the proper structure (given a proper manifest), even if the Bundle is missing
	 * classes.dex. If this happens, the old Bundle will be permanently removed and the corrupt bundle will be used from
	 * now on, generating errors every time it's touched. To check for Bundle integrity, we fully load the bundle into
	 * OSGi (making sure to create a sample runtime) by creating a temporary version of the bundle JAR with
	 * 
	 * @param plug
	 *            The plug-in whose Bundle we should verify
	 * @param bundleURL
	 *            The url of the Bundle on the file-system
	 * @throws Exception
	 */
	private void verifyBundle(ContextPlugin plug, String bundleURL) throws Exception {
		Log.d(TAG, "Verifying Bundle for: " + plug);
		String bundleFilePath = bundleURL.replace("file://", "");
		JarFile originalJar = new JarFile(bundleFilePath);
		Manifest originalManifest = originalJar.getManifest();
		JarFile tmpJar = new JarFile(bundleFilePath);
		Manifest tmpManifest = tmpJar.getManifest();
		/*
		 * Replace the current 'Bundle-SymbolicName' in the 'tmpManifest' with a random UUID string so that name of the
		 * Bundle doesn't clash when OSGi loads it for verification.
		 */
		Attributes attribs = tmpManifest.getMainAttributes();
		for (Object o : attribs.keySet()) {
			Attributes.Name name = (Attributes.Name) o;
			if (name.toString().equalsIgnoreCase("Bundle-SymbolicName")) {
				attribs.put(name, UUID.randomUUID().toString());
				break;
			}
		}
		// Update the manifest of the incoming bundle using the modified tmpManifest
		updateManifest(new File(bundleFilePath), tmpManifest);
		// Now, verify the bundle by loading it into the OSGi framework and extracting the RuntimeFactoryClass
		Log.d(TAG, "Test loading verification Bundle into OSGi for: " + plug);
		Bundle verify = null;
		try {
			verify = osgiFramework.getBundleContext().installBundle(bundleURL);
			if (verify != null) {
				Log.d(TAG, "Starting verification Bundle for: " + plug);
				verify.start();
				while (verify.getState() != Bundle.ACTIVE) {
					try {
						Log.d(TAG, "Waiting for Bundle to finish starting for: " + verify);
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
				// Verify that we can extract the Bundle's factoryClass - throws exceptions if not
				Class<?> factoryClass = verify.loadClass(plug.getRuntimeFactoryClass());
				// Uninstall the verification bundle
				verify.uninstall();
				verify = null;
				// Since the bundle is valid, replace its manifest with the originalManifest so it can be loaded
				// properly
				updateManifest(new File(bundleFilePath), originalManifest);
				Log.d(TAG, "Bundle is valid for: " + plug);
			}
		} catch (Exception e) {
			Log.e(TAG, "Bundle verification error: " + e.toString() + " for " + plug);
			throw e;
		} finally {
			// Clean up
			if (verify != null) {
				try {
					verify.uninstall();
				} catch (Exception e1) {
					Log.e(TAG, "Bundle verification uninstall exception: " + e1 + " for " + plug);
				}
			}
		}
	}

	/**
	 * Returns true if the underlying OSGi Framework is active; false otherwise
	 * 
	 * @return
	 */
	private boolean isActive() {
		if (osgiFramework != null && osgiFramework.getState() == Bundle.ACTIVE)
			return true;
		else {
			Log.w(TAG, "OSGIManager is not started!");
			return false;
		}
	}

	/**
	 * Local class used for installing OSGi Bundles on a dedicated Thread.
	 * 
	 * @author Darren Carlson
	 */
	private class BundleInstaller {
		private ContextPlugin newPlug;
		private IContextPluginInstallListener listener;
		private boolean done = false;
		private ContextPlugin originalPlug;
		private ContextPluginSettings originalSettings;
		private boolean update = false;

		/**
		 * Creates a BundleInstaller for UPDATING the specified ContextPlugin.
		 * 
		 * @param originalPlug
		 *            The plug-in to update.
		 * @param newPlug
		 *            The new plug-in that should replace the originalPlug.
		 * @param originalSettings
		 *            The originalPlug's settings (or null).
		 * @param listener
		 *            The listener that should receive updates.
		 */
		public BundleInstaller(ContextPlugin originalPlug, ContextPlugin newPlug,
				ContextPluginSettings originalSettings, IContextPluginInstallListener listener) {
			update = true;
			this.originalPlug = originalPlug;
			this.newPlug = newPlug;
			this.originalSettings = originalSettings;
			this.listener = listener;
		}

		/**
		 * Creates a BundleInstaller for INSTALLING the specified ContextPlugin.
		 * 
		 * @param newPlug
		 *            The plug-ins whose Bundle we should install.
		 * @param listener
		 *            The listener that should receive updates.
		 */
		public BundleInstaller(ContextPlugin newPlug, IContextPluginInstallListener listener) {
			this.newPlug = newPlug;
			this.listener = listener;
			update = false;
		}

		/**
		 * Cancels a running install process
		 */
		public synchronized void cancelInstall() {
			this.done = true;
		}

		@Override
		public boolean equals(Object candidate) {
			// First determine if they are the same object reference
			if (this == candidate)
				return true;
			// Make sure they are the same class
			if (candidate == null || candidate.getClass() != getClass())
				return false;
			// They are the same class... now check if their id's are the same
			BundleInstaller other = (BundleInstaller) candidate;
			if (update)
				return this.originalPlug.equals(other.originalPlug) ? true : false;
			else
				return this.newPlug.equals(other.newPlug) ? true : false;
		}

		@Override
		public int hashCode() {
			int result = 17;
			if (update)
				result = 31 * result + this.originalPlug.hashCode();
			else
				result = 31 * result + this.newPlug.hashCode();
			return result;
		}

		/**
		 * Runs the BundleInstaller.
		 */
		public void run() {
			// Notify session listeners
			SessionManager.notifyAllNewContextPluginInstalling(newPlug);
			Log.d(TAG, "BundleInstaller installing: " + newPlug);
			Log.d(TAG, "BundleInstaller installing from: " + newPlug.getInstallUrl());
			try {
				if (listener != null) {
					listener.onInstallStarted(newPlug);
					listener.onInstallProgress(newPlug, 5);
				}
			} catch (Exception e2) {
				Log.w(TAG, "Error updating listener: " + listener);
			}
			// Notify session listeners
			SessionManager.notifyContextPluginInstallProgress(newPlug, 5);
			if (!newPlug.isInstalled()) {
				// Setup initial state
				newPlug.setInstallStatus(PluginInstallStatus.INSTALLING);
				done = false;
				Bundle b = null;
				// If we're installing, uninstall any existing Bundle first
				if (!update)
					uninstallBundle(newPlug.getId());
				// Download newPlug's Bundle JAR
				String path = null;
				try {
					int count;
					InputStream input = null;
					long lengnthOfFile = 0;
					/*
					 * We need to use an InputStream for local files because of URL security issues on Android >= 4
					 */
					if (newPlug.getInstallUrl().contains("file:")) {
						File tmp = new File(newPlug.getInstallUrl().replace("file:", ""));
						input = new FileInputStream(tmp);
						lengnthOfFile = tmp.length();
					} else {
						URL url = new URL(newPlug.getInstallUrl());
						URLConnection conexion = url.openConnection();
						conexion.connect();
						lengnthOfFile = conexion.getContentLength();
						input = new BufferedInputStream(url.openStream());
					}
					// Use a tmp file in the cache dir during the download
					path = androidContext.getCacheDir() + "/" + UUID.randomUUID().toString() + ".jar";
					OutputStream output = new FileOutputStream(path);
					byte data[] = new byte[DynamixService.getConfig().getDefaultBufferSize()];
					long total = 0;
					int nextUpdate = 0;
					while (!done && (count = input.read(data)) != -1) {
						total += count;
						int progress = (int) (total * 80 / lengnthOfFile);
						if (progress >= nextUpdate) {
							nextUpdate = progress + 10;
							// Update the listener
							try {
								if (progress > 5) {
									// Update the session listeners
									SessionManager.notifyContextPluginInstallProgress(newPlug, progress);
									if (listener != null) {
										// Update dedicated listener
										listener.onInstallProgress(newPlug, progress);
									}
								}
							} catch (Exception e) {
								Log.w(TAG, "Error updating listener: " + listener);
							}
						}
						output.write(data, 0, count);
					}
					// Close all streams
					output.flush();
					output.close();
					input.close();
					Log.d(TAG, "Bundle downloaded for: " + newPlug);
					if (!done) {
						// Handle new installs
						if (!update) {
							// Update the dedicated listener
							try {
								if (listener != null)
									listener.onInstallProgress(newPlug, 90);
							} catch (Exception e1) {
								Log.w(TAG, "Error updating listener: " + listener);
							}
							// Update the session listeners
							SessionManager.notifyContextPluginInstallProgress(newPlug, 90);
							Log.i(TAG, "Performing OSGi Bundle installation for: " + newPlug);
							// Install the Bundle using the osgi framework
							b = osgiFramework.getBundleContext().installBundle("file://" + path);
							if (b != null) {
								// Update the dedicated listener
								try {
									if (listener != null)
										listener.onInstallProgress(newPlug, 95);
								} catch (Exception e1) {
									Log.w(TAG, "Error updating listener: " + listener);
								}
								// Update the session listeners
								SessionManager.notifyContextPluginInstallProgress(newPlug, 95);
								Log.d(TAG, "Validating OSGi Bundle for: " + newPlug);
								// Verify proper Bundle naming for Dynamix
								if (b.getSymbolicName().equalsIgnoreCase(newPlug.getId())) {
									Log.d(TAG, "Bundle ID and Symbolic Name match for: " + newPlug);
									// Start the plug-in's Bundle - this will throw exceptions if Bundle dependencies
									// are not found
									b.start();
									// Wait for the Bundle to become active (this might not be needed if 'b.start()' is
									// blocking - not sure).
									int bundleStartCount = 0;
									while (b.getState() != Bundle.ACTIVE) {
										try {
											Log.d(TAG, "Waiting for Bundle to finish starting for: " + newPlug);
											Thread.sleep(250);
										} catch (InterruptedException e) {
										}
										if (bundleStartCount++ > 12)
											throw new Exception("Bundle start timeout for: " + newPlug);
									}
									// Create a tmp factory using the bundle to test for classes.dex
									Class<?> factoryClass;
									try {
										factoryClass = b.loadClass(newPlug.getRuntimeFactoryClass());
									} catch (Exception e1) {
										throw new Exception("Could not create factory class for: " + newPlug);
									}
									// Create a new instance of the IContextPluginRuntimeFactory using the factoryClass
									IContextPluginRuntimeFactory factory = (IContextPluginRuntimeFactory) factoryClass
											.newInstance();
									Log.d(TAG, "Bundle is valid for: " + newPlug);
									// Provide the bundleID to the plugin
									newPlug.setBundleId(b.getBundleId());
									// Set installed state
									newPlug.setInstallStatus(PluginInstallStatus.INSTALLED);
									// If Dynamix is started, continue to install...
									if (DynamixService.isFrameworkStarted()) {
										// If the plug-in requires configuration, make sure to register its factory
										if (newPlug.requiresConfiguration()) {
											if (!registerFactory(newPlug, b))
												throw new BundleException("Could not register factory for: " + newPlug);
										} else {
											// Since the plug-in does not require configuration, simply start it
											if (!startPluginBundle(newPlug))
												throw new BundleException("Bundle failed to start!");
										}
										Log.i(TAG,
												"Bundle successfully installed: " + newPlug + " with bundleID "
														+ b.getBundleId() + " / from: " + newPlug.getInstallUrl());
									} else
										Log.d(TAG, "Dynamix was not running after installing: " + newPlug);
									try {
										// Update the dedicated listener
										if (listener != null) {
											listener.onInstallProgress(newPlug, 100);
											listener.onInstallComplete(newPlug);
										}
									} catch (Exception e) {
										Log.w(TAG, "Error updating listener: " + listener);
									}
									// Update the session listeners
									SessionManager.notifyContextPluginInstallProgress(newPlug, 100);
									// Call back the DynamixService with the good news
									DynamixService.handleBundleInstalled(newPlug);
								} else
									throw new BundleException("Plug-in id did not match the Bundle's symbolic name: "
											+ b.getSymbolicName());
							} else
								throw new BundleException("Could not install bundle into the osgi framework!");
						} else {
							// Handle updates
							for (Bundle b2 : osgiFramework.getBundleContext().getBundles()) {
								if (b2.getSymbolicName().equalsIgnoreCase(originalPlug.getId()))
									try {
										File tmp = new File(path);
										// Update the dedicated listener
										try {
											if (listener != null)
												listener.onInstallProgress(newPlug, 80);
										} catch (Exception e) {
											Log.w(TAG, "Error updating listener: " + listener);
										}
										// Update the session listeners
										SessionManager.notifyContextPluginInstallProgress(newPlug, 80);
										// Verify the Bundle
										verifyBundle(newPlug, "file://" + path);
										Log.i(TAG, "Installing Bundle for: " + originalPlug);
										FileInputStream updateStream = new FileInputStream(tmp);
										b2.update(updateStream);
										updateStream.close();
										// Update the dedicated listener
										try {
											if (listener != null)
												listener.onInstallProgress(newPlug, 90);
										} catch (Exception e) {
											Log.w(TAG, "Error updating listener: " + listener);
										}
										// Update the session listeners
										SessionManager.notifyContextPluginInstallProgress(newPlug, 90);
										// Provide the bundleID to the plugin
										newPlug.setBundleId(b2.getBundleId());
										newPlug.setInstallStatus(PluginInstallStatus.INSTALLED);
										if (DynamixService.isFrameworkStarted()) {
											if (!registerFactory(newPlug, b2))
												throw new Exception("Could not registerFactory for: " + newPlug);
										} else
											Log.d(TAG, "Dynamix was not running after updating: " + originalPlug);
										Log.i(TAG, "Bundle successfully updated: " + originalPlug + " with " + newPlug
												+ " from: " + newPlug.getInstallUrl());
										// Update the dedicated listener
										try {
											if (listener != null) {
												listener.onInstallProgress(newPlug, 100);
												listener.onInstallComplete(newPlug);
											}
										} catch (Exception e) {
											Log.w(TAG, "Error updating listener: " + listener);
										}
										// Update the session listeners
										SessionManager.notifyContextPluginInstallProgress(newPlug, 100);
										// Call back the DynamixService with the good news
										DynamixService.handleBundleUpdated(originalPlug, originalSettings, newPlug);
									} catch (Exception e1) {
										/*
										 * We want to catch any exceptions here because OSGi should automatically revert
										 * to the original Bundle if the update fails. If we throw exceptions here, the
										 * cleanup code below will uninstall our Bundle, which is not what we want in
										 * this case. In case of problems we want to keep using the old Bundle.
										 */
										Log.e(TAG, "Updating Bundle Exception: " + e1);
										// Set error state on the plug-in
										newPlug.setInstallStatus(PluginInstallStatus.ERROR);
										// TODO:? DynamixService.handleBundleUpdateFailed(originalPlug);
										// Update the dedicated listener
										try {
											if (listener != null)
												listener.onInstallFailed(newPlug, e1.toString());
										} catch (Exception e) {
											Log.w(TAG, "Error updating listener: " + listener);
										}
										// Update the session listeners
										SessionManager.notifyAllContextPluginInstallFailed(newPlug, e1.toString());
									}
							}
						}
					} else {
						// throw new Exception("Install was cancelled for: " + newPlug);
						Log.w(TAG, "Install was cancelled for: " + newPlug);
						newPlug.setInstallStatus(PluginInstallStatus.NOT_INSTALLED);
					}
				} catch (Exception e) {
					// Clean up the mess
					Log.e(TAG, "Bundle Install Error: " + e);
					newPlug.setInstallStatus(PluginInstallStatus.ERROR);
					// Notify all apps that the plug-in install failed
					SessionManager.notifyAllContextPluginInstallFailed(newPlug, e.toString());
					// Update the listener too
					try {
						if (listener != null)
							listener.onInstallFailed(newPlug, "Bundle Install Error: " + e.toString());
					} catch (Exception e2) {
						Log.w(TAG, "Error updating listener: " + listener);
					}
					// If we actually got a Bundle, uninstall it...
					if (b != null) {
						try {
							b.uninstall();
						} catch (Exception e1) {
							Log.e(TAG, "Uninstall exception: " + e1);
						}
					}
					// Call back the DynamixService with the bad news
					DynamixService.handleBundleInstallError(newPlug);
				} finally {
					try {
						// Clean up temp file
						File f = new File(path);
						if (f.exists()) {
							Log.v(TAG, "Deleting temp file: " + f.getAbsolutePath() + " with result: " + f.delete());
						} else {
							Log.w(TAG, "Could not find temp file: " + path);
						}
					} catch (Exception e) {
						Log.w(TAG, "Warning during temp file deletion: " + e);
					}
					newPlug = null;
				}
			} else {
				Log.w(TAG, "BundleInstaller found that its plug-in was already installed: " + newPlug);
				DynamixService.handleBundleInstalled(newPlug);
			}
		}

		/*
		 * Utility for uninstalling the Bundle associated to the plugId.
		 */
		private void uninstallBundle(String plugId) {
			for (Bundle b : osgiFramework.getBundleContext().getBundles()) {
				if (b.getSymbolicName().equalsIgnoreCase(plugId))
					try {
						Log.d(TAG, "Uninstalling existing Plug-in Bundle for: " + plugId);
						b.uninstall();
					} catch (BundleException e1) {
						Log.e(TAG, "Uninstall bundle exception: " + e1);
					}
			}
		}
	}

	/**
	 * Utility class for handling a bundle installer worker.
	 * 
	 * @author Darren Carlson
	 */
	private class BundleInstallerWorker implements Runnable {
		private boolean done = false;
		LinkedBlockingQueue<BundleInstaller> queue;
		BundleInstaller currentInstaller = null;
		private boolean installing = false;

		BundleInstallerWorker(LinkedBlockingQueue<BundleInstaller> queue) {
			this.queue = queue;
		}

		public void cancelCurrentInstall() {
			if (currentInstaller != null)
				currentInstaller.cancelInstall();
		}

		public boolean isInstalling() {
			return installing;
		}

		@Override
		public void run() {
			Log.d(TAG, "BundleInstallerWorker started");
			done = false;
			while (!done) {
				ContextPlugin plug = null;
				try {
					// Try to grab an installed (the queue will block until something is inserted)
					currentInstaller = queue.take();
					plug = currentInstaller.originalPlug;
					installing = true;
					// Run the installer
					currentInstaller.run();
				} catch (InterruptedException e) {
					// Log.w(TAG, "BundleInstallerWorker interrupted for: " + plug);
				} finally {
					installing = false;
				}
			}
			Log.d(TAG, "BundleInstallerWorker stopped");
		}

		public void stopWorker() {
			done = true;
			cancelCurrentInstall();
			installWorkers.get(this).interrupt();
		}
	}
}
