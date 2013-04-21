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
package org.ambientdynamix.contextplugins.batterylevel;

import java.util.UUID;

import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.contextplugin.AutoReactiveContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * Example auto-reactive plug-in that detects the device's battery level. Provides both autonomous and reactive modes.
 * 
 * @author Darren Carlson
 * 
 */
public class BatteryLevelPluginRuntime extends AutoReactiveContextPluginRuntime {
	// Static logging TAG
	private final String TAG = this.getClass().getSimpleName();
	// Our secure context
	private Context context;
	// A BroadcastReceiver variable that is used to receive battery status updates from Android
	private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int rawlevel = intent.getIntExtra("level", -1);
			int scale = intent.getIntExtra("scale", -1);
			int level = -1;
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}
			BatteryLevelInfo info = new BatteryLevelInfo(level);
			sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
			Log.i(TAG, "Battery Level Event: " + level + "%");
		}
	};

	@Override
	public void init(PowerScheme powerScheme, ContextPluginSettings settings) throws Exception {
		// Set the power scheme
		this.setPowerScheme(powerScheme);
		// Store our secure context
		this.context = this.getSecuredContext();
	}

	@Override
	public void start() {
		/*
		 * When called, the plug-in should start scanning for context and/or handling context requests.
		 */
		// Register for battery level changed notifications
		context.registerReceiver(batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		Log.d(TAG, "Started!");
	}

	@Override
	public void stop() {
		/*
		 * At this point, the plug-in should stop scanning for context and/or handling context requests; however, we
		 * should retain resources needed to run again.
		 */
		// Unregister battery level changed notifications
		context.unregisterReceiver(batteryLevelReceiver);
		Log.d(TAG, "Stopped!");
	}

	@Override
	public void destroy() {
		/*
		 * At this point, the plug-in should stop and release any resources. Nothing to do in this case except for stop.
		 */
		this.stop();
		Log.d(TAG, "Destroyed!");
	}

	@Override
	public void handleContextRequest(UUID requestId, String contextType) {
		// Check for proper context type
		if (contextType.equalsIgnoreCase(BatteryLevelInfo.CONTEXT_TYPE)) {
			// Manually access the battery level with a null BroadcastReceiver
			Intent batteryIntent = context.registerReceiver(null,
					new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			int rawlevel = batteryIntent.getIntExtra("level", -1);
			double scale = batteryIntent.getIntExtra("scale", -1);
			double level = -1;
			if (rawlevel >= 0 && scale > 0) {
				level = rawlevel / scale;
				BatteryLevelInfo info = new BatteryLevelInfo(level);
				Log.i(TAG, "Battery Level Scan: " + level + "%");
				// Send the context event
				sendContextEvent(requestId, new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
			} else {
				sendContextScanError(requestId, "INTERNAL_PLUG_IN_ERROR", ErrorCodes.INTERNAL_PLUG_IN_ERROR);
			}
		} else {
			sendContextScanError(requestId, "NO_CONTEXT_SUPPORT for " + contextType, ErrorCodes.NO_CONTEXT_SUPPORT);
		}
	}

	@Override
	public void handleConfiguredContextRequest(UUID requestId, String contextType, Bundle config) {
		Log.w(TAG, "handleConfiguredContextRequest called, but we don't support configuration");
		// Drop the config and default to handleContextRequest
		handleContextRequest(requestId, contextType);
	}

	@Override
	public void updateSettings(ContextPluginSettings settings) {
		// Not supported
	}

	@Override
	public void setPowerScheme(PowerScheme scheme) {
		// Not supported
	}

	@Override
	public void doManualContextScan() {
		// Not supported
	}
}