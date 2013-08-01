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
package org.ambientdynamix.api.application;

import android.os.Bundle;

/**
 * Factory class used for creating context support configurations.
 * @author Darren Carlson
 *
 */
public class ContextSupportConfig {
	public static final String REQUESTED_PLUGIN = "REQUESTED_PLUGIN";
	public static final String REQUESTED_PLUGIN_VERSION = "REQUESTED_PLUGIN_VERSION";
	public static final String CONTEXT_TYPE = "CONTEXT_TYPE";
	public static final String ALLOW_MULTIPLE_PLUGINS = "ALLOW_MULTIPLE_PLUGINS";

	// Singleton constructor
	private ContextSupportConfig() {
	}

	/**
	 * Creates a context support configuration that matches one or more plug-ins to the specified context type string. 
	 * @param contextType The context type to match.
	 * @param allowMultiplePlugins True if more than one plug-in may serve this support registration; false otherwise.
	 * @return The context support configuration Bundle.
	 */
	public static Bundle makeConfigBundle(String contextType, boolean allowMultiplePlugins) {
		Bundle b = new Bundle();
		b.putString(CONTEXT_TYPE, contextType);
		b.putBoolean(ALLOW_MULTIPLE_PLUGINS, allowMultiplePlugins);
		return b;
	}
	
	/**
	 * Creates a context support configuration for a specific plug-in and context type. 
	 * @param pluginId The plug-in that must handle the support request.
	 * @param contextType The requested context type (must match the specified plug-in's capabilities)
	 * @return The context support configuration Bundle.
	 */
	public static Bundle makeConfigBundle(String pluginId, String contextType) {
		Bundle b = new Bundle();
		b.putString(REQUESTED_PLUGIN, pluginId);
		b.putString(CONTEXT_TYPE, contextType);
		b.putBoolean(ALLOW_MULTIPLE_PLUGINS, false);
		return b;
	}
	
	/**
	 * Creates a context support configuration for a specific plug-in (including plug-in version) and context type. 
	 * @param pluginId The plug-in that must handle the support.
	 * @param plugVersion The requested plug-in version.
	 * @param contextType The requested context type (must match the specified plug-in's capabilities)
	 * @return The context support configuration Bundle.
	 */
	public static Bundle makeConfigBundle(String pluginId, VersionInfo plugVersion, String contextType) {
		Bundle b = new Bundle();
		b.putString(REQUESTED_PLUGIN, pluginId);
		b.putString(CONTEXT_TYPE, contextType);
		b.putBoolean(ALLOW_MULTIPLE_PLUGINS, false);
		b.putParcelable(REQUESTED_PLUGIN_VERSION, plugVersion);
		return b;
	}
}
