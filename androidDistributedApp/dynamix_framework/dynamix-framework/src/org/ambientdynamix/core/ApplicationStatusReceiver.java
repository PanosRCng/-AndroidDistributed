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

import org.ambientdynamix.data.DynamixPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receives notifications from Android about application installs, uninstalls and updates. Removes context firewall
 * privacy profiles for uninstalled apps (if desired).
 * 
 * @author Darren Carlson
 */
public class ApplicationStatusReceiver extends BroadcastReceiver {
	private final String TAG = this.getClass().getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String action = intent.getAction();
			if (action.compareTo(Intent.ACTION_PACKAGE_REMOVED) == 0) {
				int uid = (Integer) intent.getExtras().get(Intent.EXTRA_UID);
				// If EXTRA_REPLACING is null, the app is being completely uninstalled
				if (intent.getExtras().get(Intent.EXTRA_REPLACING) == null) {
					// Remove the Dynamix app, if necessary
					if (DynamixPreferences.autoAppUninstall(context)) {
						// Check if there was an app registered under the incoming uid
						DynamixApplication app = DynamixService.getDynamixApplicationByUid(uid);
						if (app != null) {
							Log.i(TAG, "Android uninstalled: " + app);
							Log.i(TAG, "Removing: " + app);
							DynamixService.revokeSecurityAuthorization(app);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.w(TAG, "Exception during app uninstall: " + e.toString());
		}
	}
}