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
package org.ambientdynamix.update.contextplugin;

import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.PluginConstants.PLATFORM;

import android.util.Log;

/**
 * Shared utility class for plug-in updating. Primarily used for XML parsing.
 * 
 * @author Darren Carlson
 */
class UpdateUtils {
	// Private data
	private static final String TAG = UpdateUtils.class.getSimpleName();

	/**
	 * Utility method that is used to check if the ContextPlugin is compatible with the specified parameters.
	 * 
	 * @param plug
	 *            The ContextPlugin to check for compatibility with the host device.
	 * @param platform
	 *            The platform of the host device.
	 * @param platformVersion
	 *            The version of the host device's platform.
	 * @param frameworkVersion
	 *            The Dynamix framework version.
	 * @return
	 */
	static boolean checkCompatibility(ContextPlugin plug, PLATFORM platform, VersionInfo platformVersion,
			VersionInfo frameworkVersion) {
		// First, make sure the platform matches
		if (!(plug.getTargetPlatform() == platform)) {
			Log.w(TAG, "Incompatible plug-in platform... skipping");
			Log.w(TAG, "plugPlatform: " + plug.getTargetPlatform());
			Log.w(TAG, "platform: " + platform);
			return false;
		}
		// Make sure we have the minimum platform api level
		if (plug.getMinPlatformApiLevel().compareTo(platformVersion) > 0) {
			Log.w(TAG, "Incompatible plug-in platform version... skipping");
			Log.w(TAG, "plugMinPlatformVersion: " + plug.getMinPlatformApiLevel());
			Log.w(TAG, "platformVersion: " + platformVersion);
			return false;
		}
		// Make sure we're under the max platform api level
		if (plug.hasMaxPlatformApiLevel()) {
			int result = plug.getMaxPlatformApiLevel().compareTo(platformVersion);
			if (result < 0) {
				Log.w(TAG, "Incompatible plug-in platform version... skipping");
				Log.w(TAG, "plugMaxPlatformVersion: " + plug.getMaxPlatformApiLevel());
				Log.w(TAG, "platformVersion: " + platformVersion);
				return false;
			}
		}
		// Make sure we have the minimum Dynamix framework level
		if (plug.getMinFrameworkVersion().compareTo(frameworkVersion) > 0) {
			Log.w(TAG, "Incompatible plug-in framework version... skipping.");
			Log.w(TAG, "plugMinFrameworkVersion: " + plug.getMinFrameworkVersion());
			Log.w(TAG, "frameworkVersion: " + frameworkVersion);
			return false;
		}
		// Make sure we're not greater than the the maximum Dynamix framework level
		if (plug.hasMaxFrameworkVersion()) {
			int result = plug.getMaxFrameworkVersion().compareTo(frameworkVersion);
			if (result < 0) {
				Log.w(TAG, "Incompatible plug-in framework version... skipping.");
				Log.w(TAG, "plugMaxFrameworkVersion: " + plug.getMaxFrameworkVersion());
				Log.w(TAG, "frameworkVersion: " + frameworkVersion);
				return false;
			}
		}
		return true;
	}
}