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
 * BootUpReceiver provides the logic to auto-start Dynamix at the completion of the Android boot process (if requested).
 * BootUpReceiver is registered in AndroidManifest.xml to receive 'android.intent.action.BOOT_COMPLETED' events.
 * 
 * @author Darren Carlson
 */
public class BootUpReceiver extends BroadcastReceiver {
	private final String TAG = this.getClass().getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean restart = intent.getBooleanExtra("restart", false);
		// Start if Dynamix is enabled, auto-start is true and the DynamixService is not running
		if (DynamixPreferences.isDynamixEnabled(context) && (restart || DynamixPreferences.autoStartDynamix(context))
				&& !DynamixService.isFrameworkStarted()) {
			Log.i(TAG, "BootUpReceiver is launching Dynamix");
			// Use the incoming context to start the Dynamix service
			context.startService(new Intent(context, DynamixService.class));
		} else
			Log.i(TAG, "BootUpReceiver is NOT launching Dynamix");
	}
}