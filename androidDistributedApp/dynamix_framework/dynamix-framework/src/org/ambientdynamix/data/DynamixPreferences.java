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

import org.ambientdynamix.core.DynamixService;
import org.ambientdynamix.core.FrameworkConstants;
import org.ambientdynamix.util.RepositoryInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class for managing Dynamix preferences. Provides utility methods that allow simplified access to preference settings.
 * 
 * @author Darren Carlson
 */
public class DynamixPreferences {
	// Preference Keys
	public static final String DYNAMIX_ENABLED = "DYNAMIX_ENABLED";
	public static final String AUTO_START_DYNAMIX = "AUTO_START_DYNAMIX";
	public static final String WEB_CONNECTOR = "WEB_CONNECTOR";
	public static final String VIBRATION_ALERTS = "VIBRATION_ALERTS";
	public static final String AUDIBLE_ALERTS = "AUDIBLE_ALERTS";
	public static final String BACKGROUND_MODE = "BACKGROUND_MODE";
	public static final String LOCAL_CONTEXT_PLUGIN_DISCOVERY = "local_discovery";
	public static final String DYNAMIX_PLUGIN_DISCOVERY_ENABLED = "DYNAMIX_PLUGIN_DISCOVERY_ENABLED";
	public static final String EXTERNAL_PLUGIN_DISCOVERY_ENABLED = "EXTERNAL_PLUGIN_DISCOVERY_ENABLED";
	public static final String AUTO_CONTEXT_PLUGIN_UPDATES = "AUTO_CONTEXT_PLUGIN_UPDATES";
	public static final String CONTEXT_PLUGIN_UPDATE_INTERVAL = "CONTEXT_PLUGIN_UPDATE_INTERVAL";
	public static final String AUTO_CONTEXT_PLUGIN_INSTALL = "AUTO_CONTEXT_PLUGIN_INSTALL";
	public static final String AUTO_APP_UNINSTALL = "AUTO_APP_UNINSTALL";
	public static final String ACCEPT_SELF_SIGNED_CERTS = "ACCEPT_SELF_SIGNED_CERTS";
	public static final String PRIMARY_CONTEXT_PLUGIN_REPO_PATH = "PRIMARY_CONTEXT_PLUGIN_REPO_PATH";
	public static final String EXTERNAL_CONTEXT_PLUGIN_REPO_PATH = "EXTERNAL_CONTEXT_PLUGIN_REPO_PATH";
	public static final String LOCAL_CONTEXT_PLUGIN_REPO_PATH = "LOCAL_CONTEXT_PLUGIN_REPO_PATH";
	
	public static final String USE_WIFI_NETWORK_ONLY = "USE_WIFI_NETWORK_ONLY";
	public static final String CERT_COLLECT = "CERT_COLLECT";

	// Singleton constructor
	private DynamixPreferences() {
	}
	
	public static boolean collectCerts(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getBoolean(CERT_COLLECT, false);
	}

	public static boolean useWifiNetworkOnly(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getBoolean(USE_WIFI_NETWORK_ONLY, true);
	}
	
	public static boolean audibleAlertsEnabled(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getBoolean(AUDIBLE_ALERTS, false);
	}

	public static boolean autoAppUninstall(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getBoolean(AUTO_APP_UNINSTALL, true);
	}

	public static boolean autoContextPluginInstallEnabled(Context c) {
		if (DynamixService.isEmbedded())
			return DynamixService.getConfig().allowAutoContextPluginInstall();
		else {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
			return sharedPrefs.getBoolean(AUTO_CONTEXT_PLUGIN_INSTALL, DynamixService.getConfig()
					.allowAutoContextPluginInstall());
		}
	}

	public static boolean autoContextPluginUpdateCheck(Context c) {
		if (DynamixService.isEmbedded()) {
			return DynamixService.getConfig().isAutoContextPluginUpdateCheckEnabled();
		} else {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
			return sharedPrefs.getBoolean(AUTO_CONTEXT_PLUGIN_UPDATES, false);
		}
	}

	public static boolean autoStartDynamix(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getBoolean(AUTO_START_DYNAMIX, false);
	}

	public static boolean backgroundModeEnabled(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getBoolean(BACKGROUND_MODE, false);
	}

	public static int getContextPluginUpdateInterval(Context c) {
		if (DynamixService.isEmbedded()) {
			return DynamixService.getConfig().getContextPluginUpdateCheckInterval();
		} else {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
			return Integer.parseInt(sharedPrefs.getString(CONTEXT_PLUGIN_UPDATE_INTERVAL, "60000"));
		}
	}

	public static String getExternalDiscoveryPath(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getString(EXTERNAL_CONTEXT_PLUGIN_REPO_PATH, "http://");
	}

	public static String getLocalContextPluginDiscoveryPath(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getString(LOCAL_CONTEXT_PLUGIN_REPO_PATH, "/dynamix/");
	}

	public static String getNetworkContextPluginDiscoveryPath(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		RepositoryInfo server = DynamixService.getConfig().getPrimaryContextPluginRepo();
		return sharedPrefs.getString(PRIMARY_CONTEXT_PLUGIN_REPO_PATH, server.getUrl());
	}

	public static boolean isDynamixEnabled(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getBoolean(DYNAMIX_ENABLED, false);
	}

	public static boolean isDynamixRepositoryEnabled(Context c) {
		if (DynamixService.isEmbedded())
			return true;
		else {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
			return sharedPrefs.getBoolean(DYNAMIX_PLUGIN_DISCOVERY_ENABLED, true);
		}
	}

	public static boolean isExternalRepositoryEnabled(Context c) {
		if (DynamixService.isEmbedded())
			return true;
		else {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
			return sharedPrefs.getBoolean(EXTERNAL_PLUGIN_DISCOVERY_ENABLED, false);
		}
	}

	public static boolean localContextPluginDiscoveryEnabled(Context c) {
		if (DynamixService.isEmbedded())
			return true;
		else {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
			return sharedPrefs.getBoolean(LOCAL_CONTEXT_PLUGIN_DISCOVERY, DynamixService.getConfig()
					.isLocalContextPluginDiscoveryEnabled());
		}
	}

	/**
	 * Resets all preferences to their defaults.
	 * 
	 * @param c
	 */
	public static void setDefaults(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		// Clear the existing SharedPreferences
		if (sharedPrefs != null) {
			SharedPreferences.Editor e = sharedPrefs.edit();
			e.clear();
			e.commit();
		}
		// Restore the default framework power scheme
		DynamixService.setNewPowerScheme(FrameworkConstants.DEFAULT_POWER_SCHEME);
	}

	public static void setDynamixEnabledState(Context c, boolean enabledState) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putBoolean(DYNAMIX_ENABLED, enabledState);
		prefsEditor.commit();
	}

	public static boolean vibrationAlertsEnabled(Context c) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getBoolean(VIBRATION_ALERTS, false);
	}
}