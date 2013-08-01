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

import org.ambientdynamix.security.PrivacyPolicy;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * User interface providing a detailed overview of a specific PrivacyPolicy.
 * 
 * @see PrivacyPolicy
 * @author Darren Carlson
 */
public class PrivacyDetailsActivity extends Activity {
	private final String TAG = this.getClass().getSimpleName();
	// Private data
	private TextView nameText;
	private TextView statusText;
	private TextView description;
	private ImageView icon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.privacy_details);
		nameText = (TextView) findViewById(R.id.privacy_policy_name);
		statusText = (TextView) findViewById(R.id.privacy_policy_status);
		description = (TextView) findViewById(R.id.privacy_policy_description);
		icon = (ImageView) findViewById(R.id.icon);
		icon.setImageResource(R.drawable.profile);
		/*
		 * Check the incoming intent for the serialized PrivacyPolicy to display.
		 */
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			PrivacyPolicy policy = (PrivacyPolicy) extras.getSerializable("policy");
			nameText.setText(policy.getName());
			statusText.setText(policy.getClass().toString());
			description.setText(policy.getDescription());
		} else {
			Log.e(TAG, "Missing Bundle!");
		}
	}
}