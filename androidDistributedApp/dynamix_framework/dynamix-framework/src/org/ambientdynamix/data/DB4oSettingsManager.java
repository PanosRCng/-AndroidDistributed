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
package org.ambientdynamix.data;

import java.io.File;
import java.util.List;

import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.Permission;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.core.DynamixApplication;
import org.ambientdynamix.security.PluginPrivacySettings;
import org.ambientdynamix.security.PrivacyPolicy;
import org.ambientdynamix.update.contextplugin.DiscoveredContextPlugin;
import org.ambientdynamix.util.RepositoryInfo;
import org.ambientdynamix.util.Utils;

import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;

/**
 * Object-based database implementation of the ISettingsManager based on the DB4o database engine. This SettingsManager
 * provides settings caching so that read intensive operations are more efficient.
 * 
 * @see DynamixSettings
 * @see ISettingsManager
 * @author Darren Carlson
 */
public class DB4oSettingsManager implements ISettingsManager {
	// Private data
	private static final String TAG = DB4oSettingsManager.class.getSimpleName();
	private ObjectContainer _db = null;
	private volatile DynamixSettings settingsCache;
	private final boolean CACHE_ENABLED = true;

	/**
	 * {@inheritDoc}
	 */
	public synchronized void addAuthorizedApplication(DynamixApplication app) {
		Log.d(TAG, "addAuthorizedApplication for: " + app);
		DynamixSettings settings = getCachedSettings();
		if (!settings.getAuthorizedApplications().contains(app)) {
			settings.getAuthorizedApplications().add(app);
			// Store the updated settings
			storeSettings(settings);
			// Update privacy policies
			updateAllApplicationPrivacyPolicies();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addClassLoader(ClassLoader loader) {
		// Unused currently
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean addContextPlugin(ContextPlugin plugin) {
		if (Utils.validateContextPlugin(plugin)) {
			DynamixSettings settings = getCachedSettings();
			// Check if the plugin was already registered
			if (!settings.getInstalledContextPlugins().contains(plugin)) {
				// The plugin is new, so add it to our ContextPlugins List
				settings.getInstalledContextPlugins().add(plugin);
				// Store the updated settings
				storeSettings(settings);
				// Update privacy policies
				updateAllApplicationPrivacyPolicies();
				Log.d(TAG, "addContextPlugin added: " + plugin);
				return true;
			} else
				Log.w(TAG, "During addContextPlugin, settings already contained: " + plugin);
		} else
			Log.w(TAG, "addContextPlugin found invalid plugin: " + plugin);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void addDeniedApplication(DynamixApplication deniedApplication) {
		if (deniedApplication != null) {
			DynamixSettings settings = getCachedSettings();
			settings.getDeniedApplications().add(deniedApplication);
			storeSettings(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean addPendingApplication(DynamixApplication app) {
		DynamixSettings settings = getCachedSettings();
		// Check if this application already exists
		int index = settings.getPendingApplications().indexOf(app);
		if (index == -1) {
			// It's not registered, so complete the assembly
			for (ContextPlugin plug : settings.getInstalledContextPlugins()) {
				app.configurePrivacySettingsForContextPlugin(plug);
			}
			settings.getPendingApplications().add(app);
			// Store the updated settings
			storeSettings(settings);
			return true;
		} else
			Log.w(TAG, "addPendingApplication found previously installed app with the same id: " + app);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean authorizePendingApplication(DynamixApplication app) {
		Log.d(TAG, "authorizeApplication for: " + app);
		DynamixSettings settings = getCachedSettings();
		if (settings.getPendingApplications().contains(app)) {
			// Remove the app from the pending list
			if (settings.getPendingApplications().remove(app)) {
				// Add the app to the authorized list, if the list does not already contain it
				if (!settings.getAuthorizedApplications().contains(app)) {
					settings.getAuthorizedApplications().add(app);
					// Store the updated settings
					storeSettings(settings);
					// Update privacy policies
					updateAllApplicationPrivacyPolicies();
					return true;
				} else
					Log.w(TAG, "During authorizeApplication, RegisteredApplications already contained: " + app);
			} else
				Log.w(TAG, "During authorizeApplication, PendingApplications did NOT contain: " + app);
		} else
			Log.w(TAG, "Application not pending!");
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean checkApplicationAuthorized(int appID) {
		DynamixSettings settings = getCachedSettings();
		for (DynamixApplication app : settings.getAuthorizedApplications()) {
			if (app.getAppID() == appID)
				return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean checkApplicationPending(int appID) {
		DynamixSettings settings = getCachedSettings();
		for (DynamixApplication app : settings.getPendingApplications()) {
			if (app.getAppID() == appID)
				return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void clearSettings() {
		Log.d(TAG, "clearSettings");
		if (isDatabaseOpen()) {
			// Clear the settings from the database
			DynamixSettings proto = new DynamixSettings();
			ObjectSet<DynamixSettings> result = _db.queryByExample(proto);
			while (result.hasNext()) {
				Object deleteMe = result.next();
				Log.d(TAG, "Deleting settings: " + deleteMe);
				_db.delete(deleteMe);
				_db.commit();
			}
			// Clear the settingsCache
			this.settingsCache = null;
		} else
			Log.w(TAG, "clearSettings encountered a closed database. Database was: " + _db);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void closeDatabase() {
		if (_db != null) {
			while (!_db.close())
				;
			_db = null;
			Log.i(TAG, "Settings database is closed.");
		} else {
			Log.e(TAG, "Settings was NULL!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized DynamixApplication getDynamixApplication(int appID) {
		DynamixSettings settings = getCachedSettings();
		for (DynamixApplication app : settings.getAuthorizedApplications()) {
			if (app.getAppID() == appID) {
				return app;
			}
		}
		for (DynamixApplication app : settings.getPendingApplications()) {
			if (app.getAppID() == appID) {
				return app;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized DynamixApplication getAuthorizedApplication(int appID) {
		DynamixSettings settings = getCachedSettings();
		for (DynamixApplication app : settings.getAuthorizedApplications()) {
			if (app.getAppID() == appID) {
				return app;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DynamixApplication> getAuthorizedApplications() {
		return Utils.getSortedAppList(getCachedSettings().getAuthorizedApplications());
	}

	/**
	 * {@inheritDoc}
	 */
	public ContextPlugin getContextPlugin(String id) {
		DynamixSettings settings = getCachedSettings();
		for (ContextPlugin plug : settings.getInstalledContextPlugins()) {
			if (plug.getId().equalsIgnoreCase(id))
				return plug;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ContextPlugin> getInstalledContextPlugins() {
		return Utils.getSortedContextPluginList(getCachedSettings().getInstalledContextPlugins());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized ContextPluginSettings getContextPluginSettings(final ContextPlugin plug) {
		Log.d(TAG, "getInstalledContextPluginsettings for: " + plug);
		// Make sure we got a database (i.e. _db != null)
		if (isDatabaseOpen()) {
			DynamixSettings settings = getCachedSettings();
			// Find the ContextPlugin and return its settings
			int index = settings.getInstalledContextPlugins().indexOf(plug);
			if (index != -1) {
				return settings.getInstalledContextPlugins().get(index).getContextPluginSettings();
			} else
				Log.w(TAG, "getInstalledContextPluginsettings could not find ContextPlugin: " + plug);
		} else
			throw new RuntimeException("Database was closed after open... this is bad: " + _db);
		Log.w(TAG, "No ContextPluginSettings found in database for: " + plug);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DiscoveredContextPlugin> getPendingContextPlugins() {
		return Utils.getSortedDiscoveredPluginList(getCachedSettings().getPendingContextPlugins());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DynamixApplication> getDeniedApplications() {
		return Utils.getSortedAppList(getCachedSettings().getDeniedApplications());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DynamixApplication> getPendingApplications() {
		return Utils.getSortedAppList(getCachedSettings().getPendingApplications());
	}

	/**
	 * {@inheritDoc}
	 */
	public PowerScheme getPowerScheme() {
		return getCachedSettings().getPowerScheme();
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized DynamixSettings getSettings() {
		// Make sure we have a database
		if (isDatabaseOpen()) {
			// Perform a DB4o queryByExample for the single settings object in the database.
			DynamixSettings proto = new DynamixSettings();
			Log.i(TAG, "Constructing query using DynamixSettings: " + proto);
			// ObjectSet<DynamixSettings> result = _db.queryByExample(proto);
			ObjectSet<DynamixSettings> result = _db.queryByExample(DynamixSettings.class);
			/*
			 * Query query=_db.query(); query.constrain(DynamixSettings.class); ObjectSet<DynamixSettings>
			 * result=query.execute();
			 */
			// Warn if there are more than 1 settings entity
			if (result.size() > 1)
				Log.w(TAG, "More than 1 settings object Database");
			// If we got a result, return it.
			if (result.hasNext()) {
				DynamixSettings s = result.next();
				Log.v(TAG, "getSettings is returning: " + s);
				//_db.activate(s, Integer.MAX_VALUE);
				return s;
			} else {
				Log.e(TAG, "Settings NOT FOUND in database!");
			}
		} else
			Log.e(TAG, "Database was closed after open... this is bad: " + _db);
		Log.e(TAG, "Returning new settings");
		/*
		 * TODO: If we return new settings, we need to reset Dynamix to a default state, otherwise there may be plug-ins
		 * installed that won't be reflected by the settings object.
		 */
		return new DynamixSettings();
	}

	/**
	 * Returns True if Dynamix had a clean exit; false otherwise.
	 */
	public synchronized Boolean hadCleanExit() {
		if (isDatabaseOpen()) {
			DynamixSettings settings = getCachedSettings();
			return settings.hadCleanExit();
		} else
			throw new RuntimeException("Database was closed after open... this is bad: " + _db);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void openDatabase(String path) throws Exception {
		Log.i(TAG, "Opening database using: " + path + " on thread: " + Thread.currentThread());
		if (path != null) {
			/*
			 * Open the DB4o database and setup object handling (e.g. updateDepth, cascadeOnDelete, etc.)
			 */
			try {
				if (!isDatabaseOpen()) {
					Log.i(TAG, "Database was closed... opening");
					// Setup database
					EmbeddedConfiguration c1 = Db4oEmbedded.newConfiguration();
					c1.common().objectClass(DynamixSettings.class).cascadeOnActivate(true);
					c1.common().objectClass(DynamixSettings.class).cascadeOnUpdate(true);
					c1.common().objectClass(DynamixSettings.class).cascadeOnDelete(true);
					c1.common().objectClass(PrivacyRiskLevel.class).cascadeOnActivate(true);
					c1.common().objectClass(PrivacyRiskLevel.class).cascadeOnUpdate(true);
					c1.common().objectClass(PrivacyRiskLevel.class).cascadeOnDelete(true);
					c1.common().objectClass(VersionInfo.class).cascadeOnActivate(true);
					c1.common().objectClass(VersionInfo.class).cascadeOnUpdate(true);
					c1.common().objectClass(VersionInfo.class).cascadeOnDelete(true);
					c1.common().objectClass(DiscoveredContextPlugin.class).cascadeOnActivate(true);
					c1.common().objectClass(DiscoveredContextPlugin.class).cascadeOnUpdate(true);
					c1.common().objectClass(DiscoveredContextPlugin.class).cascadeOnDelete(true);
					c1.common().objectClass(ContextPlugin.class).cascadeOnActivate(true);
					c1.common().objectClass(ContextPlugin.class).cascadeOnUpdate(true);
					c1.common().objectClass(ContextPlugin.class).cascadeOnDelete(true);
					c1.common().objectClass(PrivacyPolicy.class).cascadeOnActivate(true);
					c1.common().objectClass(PrivacyPolicy.class).cascadeOnUpdate(true);
					c1.common().objectClass(PrivacyPolicy.class).cascadeOnDelete(true);
					c1.common().objectClass(PluginPrivacySettings.class).cascadeOnActivate(true);
					c1.common().objectClass(PluginPrivacySettings.class).cascadeOnUpdate(true);
					c1.common().objectClass(PluginPrivacySettings.class).cascadeOnDelete(true);
					c1.common().objectClass(DynamixApplication.class).cascadeOnActivate(true);
					c1.common().objectClass(DynamixApplication.class).cascadeOnUpdate(true);
					c1.common().objectClass(DynamixApplication.class).cascadeOnDelete(true);
					/*
					 * After changing to DB4o 8.x, we're getting NPEs with the Permission class. Values are null.
					 */
					c1.common().objectClass(Permission.class).cascadeOnActivate(true);
					c1.common().objectClass(Permission.class).cascadeOnUpdate(true);
					c1.common().objectClass(Permission.class).cascadeOnDelete(true);
					c1.common().objectClass(RepositoryInfo.class).cascadeOnActivate(true);
					c1.common().objectClass(RepositoryInfo.class).cascadeOnUpdate(true);
					c1.common().objectClass(RepositoryInfo.class).cascadeOnDelete(true);
					c1.common().objectClass(DynamixSettings.class)
					.minimumActivationDepth(Integer.MAX_VALUE);
					c1.common().activationDepth(Integer.MAX_VALUE);
					// Create the database file, plus path (if needed)
					File dbFile = new File(path);
					if (!dbFile.exists()) {
						File parent = new File(dbFile.getParent());
						parent.mkdirs();
						dbFile.createNewFile();
					}
					Log.i(TAG, "Opening database using path: " + dbFile);
					_db = Db4oEmbedded.openFile(c1, dbFile.getCanonicalPath());
				}
				Log.i(TAG, "Database is open!");
			} catch (Exception e) {
				closeDatabase();
				Log.e(TAG, e.getMessage());
				throw e;
			}
		} else
			throw new Exception("Database string was null");
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean removeApplication(DynamixApplication app) {
		DynamixSettings settings = getCachedSettings();
		// Check authorized applications
		if (settings.getAuthorizedApplications().contains(app)) {
			boolean result = settings.getAuthorizedApplications().remove(app);
			// Store the updated settings
			storeSettings(settings);
			return result;
		}
		// Check pending applications
		if (settings.getPendingApplications().contains(app)) {
			boolean result = settings.getPendingApplications().remove(app);
			// Store the updated settings
			storeSettings(settings);
			return result;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeClassLoader(ClassLoader loader) {
		// Unused
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean removeContextPlugin(ContextPlugin plug) {
		if (Utils.validateContextPlugin(plug)) {
			DynamixSettings settings = getCachedSettings();
			// Try to remove the plugin
			if (settings.getInstalledContextPlugins().remove(plug)) {
				// Remove privacy policies from each app for this ContextPlugin
				for (DynamixApplication app : settings.getAuthorizedApplications()) {
					app.removePluginPrivacySettings(plug);
				}
				// Same for PendingApplications
				for (DynamixApplication app : settings.getPendingApplications()) {
					app.removePluginPrivacySettings(plug);
				}
				Log.d(TAG, "Removed: " + plug);
				// Remove its IContextPluginSettings settings too
				removeContextPluginSettings(plug);
				// Store the updated settings
				storeSettings(settings);
				return true;
			} else
				Log.w(TAG, "Could not remove " + plug);
		}
		Log.w(TAG, "removeContextPlugin failed for: " + plug);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean removeContextPluginSettings(final ContextPlugin plug) {
		Log.d(TAG, "removeContextPluginSettings for: " + plug);
		// Make sure we got a database (i.e. _db != null)
		if (isDatabaseOpen()) {
			DynamixSettings settings = getCachedSettings();
			// Find the ContextPlugin and return its settings
			int index = settings.getInstalledContextPlugins().indexOf(plug);
			if (index != -1) {
				settings.getInstalledContextPlugins().get(index).setContextPluginSettings(null);
				// Store the updated settings
				storeSettings(settings);
				return true;
			} else
				Log.i(TAG, "No settings found for ContextPlugin: " + plug);
		} else
			throw new RuntimeException("Database was closed after open... this is bad: " + _db);
		Log.i(TAG, "No ContextPluginSettingsMapping found in database for: " + plug);
		return false;
	}

	@Override
	public synchronized boolean replaceContextPlugin(ContextPlugin originalPlugin, ContextPlugin newPlugin) {
		if (Utils.validateContextPlugin(newPlugin)) {
			DynamixSettings settings = getCachedSettings();
			if (settings.getInstalledContextPlugins().contains(originalPlugin)) {
				Log.d(TAG, "replaceContextPlugin for: " + originalPlugin);
				int index = settings.getInstalledContextPlugins().indexOf(originalPlugin);
				if (index != -1) {
					settings.getInstalledContextPlugins().set(index, newPlugin);
					// Store the updated settings
					storeSettings(settings);
					// Update privacy policies
					updateAllApplicationPrivacyPolicies();
					return true;
				} else
					Log.w(TAG, "replaceContextPlugin could not find: " + originalPlugin);
			} else
				Log.w(TAG, "replaceContextPlugin could not find: " + originalPlugin);
		} else
			Log.w(TAG, "replaceContextPlugin failed for: " + originalPlugin);
		return false;
	}

	/**
	 * Sets if Dynamix had a clean exit.
	 * 
	 * @param cleanExit
	 */
	public synchronized void setCleanExit(Boolean cleanExit) {
		DynamixSettings settings = getCachedSettings();
		settings.setCleanExit(cleanExit);
		storeSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setContextPluginUpdates(List<DiscoveredContextPlugin> updates) {
		DynamixSettings settings = getCachedSettings();
		settings.setContextPluginUpdates(updates);
		storeSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setPowerScheme(PowerScheme newScheme) {
		if (newScheme != null) {
			DynamixSettings settings = getCachedSettings();
			settings.setPowerScheme(newScheme);
			storeSettings(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean storeContextPluginSettings(ContextPlugin plug, ContextPluginSettings plugSettings) {
		Log.d(TAG, "storeContextPluginSettings for: " + plug);
		// Make sure we have a database
		if (isDatabaseOpen()) {
			DynamixSettings settings = getCachedSettings();
			// Find the ContextPlugin and return its settings
			int index = settings.getInstalledContextPlugins().indexOf(plug);
			if (index != -1) {
				settings.getInstalledContextPlugins().get(index).setContextPluginSettings(plugSettings);
				return true;
			} else
				Log.w(TAG, "storeContextPluginSettings could not find ContextPlugin: " + plug);
		} else
			throw new RuntimeException("Database was closed after open... this is bad: " + _db);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean updateAllApplicationPrivacyPolicies() {
		DynamixSettings settings = getCachedSettings();
		for (ContextPlugin plugin : settings.getInstalledContextPlugins()) {
			// Update each RegisteredApplication with the plugin (creates privacy policies)
			for (DynamixApplication app : settings.getAuthorizedApplications()) {
				app.configurePrivacySettingsForContextPlugin(plugin);
			}
			// Same for PendingApplications
			for (DynamixApplication app : settings.getPendingApplications()) {
				app.configurePrivacySettingsForContextPlugin(plugin);
			}
		}
		// Store the updated settings
		storeSettings(settings);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean updateApplication(DynamixApplication app) {
		Log.d(TAG, "updateApplication for " + app);
		DynamixSettings settings = getCachedSettings();
		if (settings.getAuthorizedApplications().contains(app)) {
			int index = settings.getAuthorizedApplications().indexOf(app);
			if (index != -1) {
				settings.getAuthorizedApplications().set(index, app);
				// Store the updated settings
				storeSettings(settings);
				updateAllApplicationPrivacyPolicies();
				return true;
			}
		} else if (settings.getPendingApplications().contains(app)) {
			int index = settings.getPendingApplications().indexOf(app);
			if (index != -1) {
				settings.getPendingApplications().set(index, app);
				// Store the updated settings
				storeSettings(settings);
				updateAllApplicationPrivacyPolicies();
				return true;
			}
		} else
			Log.w(TAG, "Could not update: " + app);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean updateContextPlugin(ContextPlugin plug) {
		if (Utils.validateContextPlugin(plug)) {
			DynamixSettings settings = getCachedSettings();
			if (settings.getInstalledContextPlugins().contains(plug)) {
				Log.d(TAG, "updateContextPlugin for: " + plug);
				int index = settings.getInstalledContextPlugins().indexOf(plug);
				if (index != -1) {
					settings.getInstalledContextPlugins().set(index, plug);
					// Store the updated settings
					storeSettings(settings);
					// Update privacy policies
					updateAllApplicationPrivacyPolicies();
					return true;
				} else
					Log.w(TAG, "updateContextPlugin could not find: " + plug);
			} else
				Log.w(TAG, "updateContextPlugin could not find: " + plug);
		} else
			Log.w(TAG, "updateContextPlugin failed for: " + plug);
		return false;
	}

	/*
	 * Returns the current settings object from the database, caching it locally for future reads.
	 */
	private synchronized DynamixSettings getCachedSettings() {
		if (CACHE_ENABLED) {
			if (settingsCache == null) {
				settingsCache = getSettings();
			}
			return settingsCache;
		} else {
			return getSettings();
		}
	}

	/**
	 * Checks if the database is open.
	 */
	private boolean isDatabaseOpen() {
		return (_db != null && !_db.ext().isClosed());
	}

	/**
	 * Stores the incoming settings and updates the local settings cache.
	 */
	private synchronized boolean storeSettings(DynamixSettings settings) {
		Log.d(TAG, "Beginning storeSettings for thread: " + Thread.currentThread());
		if (isDatabaseOpen()) {
			if (settings != null) {
				try {
					// Then store the new settings in the database
					_db.store(settings);
					_db.commit();
					// Finally, make sure to cache the new settings
					this.settingsCache = settings;
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					Log.w(TAG, "Exception during storeSettings: " + e.getMessage());
				}
			} else
				Log.w(TAG, "storeSettings received null settings");
		} else
			Log.w(TAG, "storeSettings encountered a closed database. Database was: " + _db);
		return false;
	}
}