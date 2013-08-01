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

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.event.PluginDiscoveryResult;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * User interface listing and controlling updates.
 * 
 * @author Darren Carlson
 */
public class UpdateDetailsActivity extends Activity {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private TextView nameText;
	private TextView statusText;
	private TextView priorityDesc;
	private WebView updateDesc;
	private TextView updateTarget;
	private ImageView icon;
	private PluginDiscoveryResult update;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_details);
		nameText = (TextView) findViewById(R.id.plug_name);
		statusText = (TextView) findViewById(R.id.plug_status);
		updateTarget = (TextView) findViewById(R.id.update_target_description);
		priorityDesc = (TextView) findViewById(R.id.update_priority_description);
		updateDesc = (WebView) findViewById(R.id.update_description);
		icon = (ImageView) findViewById(R.id.icon);
		icon.setImageResource(R.drawable.plugin_update);
		/*
		 * Check the incoming intent for the serialized PrivacyPolicy to display.
		 */
		update = (PluginDiscoveryResult) getIntent().getSerializableExtra("update");
		if (update != null) {
			ContextPlugin oldPlug = update.getTargetPlugin();
			ContextPlugin newPlug = update.getDiscoveredPlugin().getContextPlugin();
			nameText.setText(newPlug.getName());
			statusText.setText(newPlug.getVersionInfo().toString());
			updateTarget.setText("Updates " + update.getTargetPlugin().getVersionInfo() + " with "
					+ update.getDiscoveredPlugin().getContextPlugin().getVersionInfo());
			priorityDesc.setText(update.getDiscoveredPlugin().getPriority().toString());
			updateDesc.setBackgroundColor(Color.BLACK);
			updateDesc.loadData(update.getDiscoveredPlugin().getUpdateMessage(), "text/html", "utf-8");
		} else
			Log.w(TAG, "Missing UpdateResult");
		// Setup the button
		Button btnViewPlug = (Button) findViewById(R.id.btn_see_plugin_details);
		btnViewPlug.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (update != null) {
					Bundle bundle = new Bundle();
					bundle.putSerializable("plug", update.getDiscoveredPlugin().getContextPlugin());
					Intent i = new Intent(UpdateDetailsActivity.this, PluginDetailsActivity.class);
					i.putExtras(bundle);
					startActivity(i);
				}
			}
		});
	}
}
