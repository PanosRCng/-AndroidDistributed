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

import android.app.Activity;
import android.content.Intent;

/**
 * Dispatches Android events that require an Activity (e.g., NCF events).
 * 
 * @author Darren Carlson
 * 
 */
public class AndroidEventDispatcherActivity extends Activity {
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Dispatch NFC events
		if (getIntent().getAction().equalsIgnoreCase("android.nfc.action.NDEF_DISCOVERED")
				|| getIntent().getAction().equalsIgnoreCase("android.nfc.action.TAG_DISCOVERED")
				|| getIntent().getAction().equalsIgnoreCase("android.nfc.action.TECH_DISCOVERED")) {
			// Dispatch event to Dynamix
			DynamixService.dispatchNfcEvent(getIntent());
			// Close the Activity
			this.finish();
		}
	}
}
