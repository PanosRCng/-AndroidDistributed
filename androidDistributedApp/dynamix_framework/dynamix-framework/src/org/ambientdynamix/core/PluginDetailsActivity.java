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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.util.DescriptiveIcon;
import org.ambientdynamix.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * User interface showing the details of a specific ContextPlugin, such as its name, description and supported fidelity
 * levels.
 * 
 * @see ContextPlugin
 * @see PrivacyRiskLevel
 * @author Darren Carlson
 */
public class PluginDetailsActivity extends Activity {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private TextView nameText;
	private TextView statusText;
	private TextView description;
	private ListView supportedLevelList;
	private PluginDetailsAdapter adapter;
	private ImageView icon;
	ContextPlugin plug;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plugin_details);
		nameText = (TextView) findViewById(R.id.plug_name);
		statusText = (TextView) findViewById(R.id.plug_status);
		description = (TextView) findViewById(R.id.plug_description);
		description.setMovementMethod(new ScrollingMovementMethod());
		supportedLevelList = (ListView) findViewById(R.id.fidelityList);
		icon = (ImageView) findViewById(R.id.icon);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == Activity.RESULT_OK) {
			this.setIntent(intent);
		}
	}

	@Override
	protected void onResume() {
		// Extract the serialized ContextPlugin from the incoming intent
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			final ContextPlugin plugin = (ContextPlugin) extras.getSerializable("plug");
			nameText.setText(plugin.getName());
			statusText.setText(plugin.getVersionInfo().toString());
			description.setText(plugin.getDescription());
			DescriptiveIcon di = Utils.getDescriptiveIcon(plugin);
			icon.setImageResource(di.getIconResId());
			description.setText(plugin.getDescription());
			/*
			 * For some reason, after the bundle is deserialized, the input TreeMap comes out a HashMap. Dump the
			 * hashmap into a TreeMap to make sure our PrivacyRiskLevel sorting is correct
			 */
			Map<PrivacyRiskLevel, String> sortedMap = new TreeMap(plugin.getSupportedPrivacyRiskLevels());
			ArrayList<FidelityDescription> descriptions = new ArrayList<FidelityDescription>();
			for (PrivacyRiskLevel l : sortedMap.keySet()) {
				descriptions.add(new FidelityDescription(l, plugin.getSupportedPrivacyRiskLevels().get(l)));
			}
			this.adapter = new PluginDetailsAdapter(this, R.layout.fidelity_description_row, descriptions);
			supportedLevelList.setAdapter(this.adapter);
			Button btnConfigureContextPlugin = (Button) findViewById(R.id.btn_configure_context_plugin);
			Button btnSeePluginStats = (Button) findViewById(R.id.btn_see_plugin_stats);
			if (plugin.isInstalled()) {
				btnSeePluginStats.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						bundle.putSerializable("plug", plugin);
						Intent i = new Intent(PluginDetailsActivity.this, PluginStatsActivity.class);
						i.putExtras(bundle);
						startActivity(i);
					}
				});
				// Setup configure button: btn_configure_context_plugin
				if (plugin.hasConfigurationView()) {
					if (DynamixService.isFrameworkStarted()) {
						btnConfigureContextPlugin.setEnabled(true);
						btnConfigureContextPlugin.setText("Configure Plug-in");
					} else {
						btnConfigureContextPlugin.setEnabled(false);
						btnConfigureContextPlugin.setText("Configuration Offline");
					}
				} else {
					btnConfigureContextPlugin.setText("No Configuration");
					btnConfigureContextPlugin.setEnabled(false);
				}
				btnConfigureContextPlugin.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						Result r = DynamixService.openContextPluginConfigurationForFramework(plugin.getId());
						// TODO : Toast message on fail?
//						if(!r.wasSuccessful())
//							Utils.
					}
				});
			} else {
				btnConfigureContextPlugin.setVisibility(View.GONE);
				btnSeePluginStats.setVisibility(View.GONE);
			}
		} else {
			Log.e(TAG, "Missing Bundle!");
		}
		super.onResume();
	}

	/*
	 * Struct like inner class that is used for populating the PluginDetailsAdapter
	 */
	private class FidelityDescription {
		public PrivacyRiskLevel level;
		public String description;

		public FidelityDescription(PrivacyRiskLevel level, String description) {
			this.level = level;
			this.description = description;
		}
	}

	/**
	 * Local class used as a datasource for FidelityDescription entities. This class extends a typed Generic
	 * ArrayAdapter and overrides getView in order to update the UI state.
	 * 
	 * @author Darren Carlson
	 */
	private class PluginDetailsAdapter extends ArrayAdapter<FidelityDescription> {
		public PluginDetailsAdapter(Context context, int textViewResourceId, ArrayList<FidelityDescription> descriptions) {
			super(context, textViewResourceId, descriptions);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.fidelity_description_row, null);
			}
			FidelityDescription desc = this.getItem(position);
			if (desc != null) {
				TextView tt = (TextView) v.findViewById(R.id.fidelity);
				TextView bt = (TextView) v.findViewById(R.id.fidelity_description);
				if (tt != null) {
					tt.setText(desc.level.toString().substring(0, desc.level.toString().indexOf(" ")));
				}
				if (bt != null) {
					bt.setText(desc.description);
				}
			}
			return v;
		}
	}
}
