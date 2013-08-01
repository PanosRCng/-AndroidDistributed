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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.security.Permission;
import org.ambientdynamix.security.BlockedPrivacyPolicy;
import org.ambientdynamix.security.PluginPrivacySettings;
import org.ambientdynamix.security.PrivacyPolicy;
import org.ambientdynamix.util.Utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * DynamixApplication represents the state of an 'external' application (running in a separate process and address
 * space), which interacts with the Dynamix Framework via its AIDL-based application API. Interactions between external
 * applications and Dynamix are mediated by Dynamix's API security model, which uses Android's kernel-based process
 * identification along with security policies to determine the type and fidelity of context information provisioned.
 * Dynamix users can precisely control the type and fidelity of context information provisioned to applications through
 * the set of configurable PluginPrivacySettings associated with each DynamixApplication.
 * 
 * @see PrivacyPolicy
 * @see PluginPrivacySettings
 * @author Darren Carlson
 */
public class DynamixApplication implements Serializable {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private static final long serialVersionUID = 7240238549835623638L;
	private static int count = 0;
	private int appID;
	private String packageName;
	private int versionCode;
	private String versionName;
	private String appName;
	private String appDescription;
	private boolean enabled = true;
	private boolean mayInstallPlugins = false;
	private PrivacyPolicy privacyPolicy;
	private Date lastContact;
	private List<PluginPrivacySettings> pluginPrivacySettings = new Vector<PluginPrivacySettings>();
	private List<Permission> permissions = new Vector<Permission>();
	private boolean admin = false;
	private boolean webApp = false;

	/**
	 * Empty constructor required for DB4o.
	 */
	public DynamixApplication() {
	}

	/**
	 * Creates a DynamixApplication from the web application's URL. Web app identification is based on the incoming
	 * URL's hostname + port + path. For specifically named resources, the id includes the name of the resource listed
	 * on the path (e.g., ambientdynamix.org/example/index.htm). For unnamed resources, the id always includes the final
	 * trailing slash (e.g., ambientdynamix.org/example/).
	 * 
	 * @param webAppUrl
	 * @throws MalformedURLException
	 */
	public DynamixApplication(int appId, String webAppUrl) throws MalformedURLException {
		this.appID = appId;
		this.appName = webAppUrl;
		this.webApp = true;
		// Set a default BlockedPrivacyPolicy
		this.privacyPolicy = new BlockedPrivacyPolicy();
		Log.v(TAG, "Created: " + this);
	}

	/**
	 * Creates a DynamixApplication based on Android ApplicationInfo.
	 */
	public DynamixApplication(PackageManager pm, PackageInfo pkg, ApplicationInfo info) {
		/*
		 * Tips on getting detailed App Info (icon, etc.):
		 * http://android.git.kernel.org/?p=platform/packages/apps/Settings
		 * .git;a=blob;f=src/com/android/settings/InstalledAppDetails .java;h=d05014bfc027dc7e280de2eeb07f80207cefd32c
		 * ;hb=68b8069862314a26dbacd28d13dd4c6bea8b6141
		 */
		this.appID = info.uid;
		this.appName = info.loadLabel(pm).toString();
		this.appDescription = "Directory: " + info.sourceDir;
		this.packageName = info.packageName;
		this.versionCode = pkg.versionCode;
		this.versionName = pkg.versionName;
		this.webApp = false;
		/*
		 * TODO: Introduce permission introspection here pkg.permissions returns PermissionInfo[] Maintain the
		 * PermissionInfo[] in this class Note: This is only filled in if the flag GET_PERMISSIONS was set.
		 */
		// Set a default BlockedPrivacyPolicy
		this.privacyPolicy = new BlockedPrivacyPolicy();
		Log.v(TAG, "Created: " + this);
	}

	/**
	 * Configures the privacy settings for the incoming ContextPlugin based on the installed PrivacyPolicy.
	 */
	public synchronized void configurePrivacySettingsForContextPlugin(ContextPlugin plug) {
		// Log.v(TAG, "configurePrivacySettingsForContextPlugin for " + plug);
		if (Utils.validateContextPlugin(plug)) {
			boolean found = false;
			synchronized (pluginPrivacySettings) {
				// Check if our policies already contain a mapping for the incoming plugin
				for (PluginPrivacySettings policy : pluginPrivacySettings) {
					if (policy.getPlugin().equals(plug)) {
						// Log.v(TAG, "configurePrivacySettingsForContextPlugin found existing policy for: " + plug);
						found = true;
						break;
					}
				}
				if (!found) {
					// Log.v(TAG, "configurePrivacySettingsForContextPlugin creating a new policy for: " + plug);
					pluginPrivacySettings.add(new PluginPrivacySettings(plug, privacyPolicy));
				}
			}
		} else
			Log.e(TAG, "configurePrivacySettingsForContextPlugin failed to validate plugin: " + plug);
	}

	@Override
	public boolean equals(Object candidate) {
		// First determine if they are the same object reference
		if (this == candidate)
			return true;
		// Make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		// Ok, they are the same class... check if their id's are the same
		DynamixApplication other = (DynamixApplication) candidate;
		return other.getAppID() == this.getAppID() ? true : false;
	}

	/**
	 * Returns the unique identifier of this application (currently based on the Android process UID).
	 */
	public int getAppID() {
		return this.appID;
	}

	/**
	 * Returns the description of the application as a string.
	 */
	public String getDescription() {
		return this.appDescription;
	}

	/**
	 * Returns the 'friendly' name of the application as a string.
	 */
	public String getName() {
		return this.appName;
	}

	/**
	 * Returns a List of type Permission for the application
	 */
	public List<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * Returns a read-only list of the PluginPrivacySettings associated with this application.
	 */
	public List<PluginPrivacySettings> getPluginPrivacySettings() {
		return Collections.unmodifiableList(pluginPrivacySettings);
	}

	/**
	 * Returns the current PrivacyPolicy.
	 */
	public PrivacyPolicy getPrivacyPolicy() {
		return this.privacyPolicy;
	}

	/**
	 * Returns a status message for this application intended for display.
	 */
	public String getStatusString() {
		return "ID: " + this.getAppID();
	}

	// HashCode Example: http://www.javafaq.nu/java-example-code-175.html
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + this.appID;
		return result;
	}

	/**
	 * Checks if the application is connected, meaning that it has been 'pinged' recently. This indicates whether or not
	 * the application is active. Returns true if connected; false, otherwise.
	 */
	public boolean isConnected() {
		if (lastContact != null) {
			Date now = new Date();
			long nowMills = now.getTime();
			long lastMills = lastContact.getTime();
			if (nowMills - lastMills < DynamixService.getConfig().getAppInactivityTimeoutMills())
				return true;
		}
		return false;
	}

	/**
	 * Returns true if this application enabled; false otherwise
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Returns whether or not this app may install context plugins
	 */
	public boolean mayInstallPlugins() {
		return mayInstallPlugins;
	}

	/**
	 * Returns true if this is an admin app (full permissions); false otherwise;
	 */
	public boolean isAdmin() {
		return admin;
	}

	/**
	 * Set true if this is an admin app (full permissions); false otherwise;
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * Returns true if this app is a web application (i.e., a web page); false otherwise.
	 */
	public boolean isWebApp() {
		return webApp;
	}

	/**
	 * Set true if this app is a web application (i.e., a web page); false otherwise.
	 */
	public void setWebApp(boolean webApp) {
		this.webApp = webApp;
	}

	/**
	 * Update our last contact time to the current system time.
	 */
	public void pingConnected() {
		lastContact = new Date();
	}

	/**
	 * Removes the PluginPrivacySettings for the specified ContextPlugin.
	 */
	public synchronized PluginPrivacySettings removePluginPrivacySettings(ContextPlugin plug) {
		//Log.d(TAG, "removePluginPrivacySettings for ContextPlugin: " + plug);
		PluginPrivacySettings removeMe = null;
		synchronized (pluginPrivacySettings) {
			Iterator<PluginPrivacySettings> iter = pluginPrivacySettings.iterator();
			while (iter.hasNext()) {
				PluginPrivacySettings p = iter.next();
				if (p.getPlugin().equals(plug)) {
					removeMe = p;
					iter.remove();
					break;
				}
			}
		}
		if (removeMe == null)
			Log.w(TAG, "removePluginPrivacySettings could not find privacy policy for: " + plug);
		return removeMe;
	}

	/**
	 * Sets whether this application is enabled or not
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Sets whether or not this app may install context plugins
	 */
	public void setMayInstallPlugins(boolean mayInstallPlugins) {
		this.mayInstallPlugins = mayInstallPlugins;
	}

	/**
	 * Sets a List of type Permission for the application
	 */
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}

	/**
	 * Sets the PrivacyPolicy, overriding custom PrivacyRiskLevel settings if overrideCustomPolicy is set to true.
	 */
	public synchronized void setPrivacyPolicy(PrivacyPolicy newPolicy, boolean setDefaultMaxPrivacyRisk) {
		if (newPolicy != null) {
			this.privacyPolicy = newPolicy;
			synchronized (pluginPrivacySettings) {
				for (PluginPrivacySettings ps : pluginPrivacySettings) {
					// We always set the policy, even though the actual level may not be changed by the update...
					ps.setPrivacyPolicy(privacyPolicy);
					if (setDefaultMaxPrivacyRisk) {
						// Reset to the default PrivacyRiskLevel for the new privacyPolicy
						ps.setDefaultMaxPrivacyRisk();
					}
				}
			}
		} else
			Log.w(TAG, "setPrivacyPolicy received null policy");
	}

	@Override
	public String toString() {
		return this.appName + " / ID: " + this.appID;
	}
}