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

import java.util.List;

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.security.PluginPrivacySettings;
import org.ambientdynamix.security.PrivacyPolicy;
import org.ambientdynamix.util.DescriptiveIcon;
import org.ambientdynamix.util.EmptyListSupportAdapter;
import org.ambientdynamix.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * User interface for adjusting Context Firewall settings for an Application.
 * 
 * @author Darren Carlson
 */
public class ContextFirewallActivity extends Activity {
	private final String TAG = this.getClass().getSimpleName();
	// Private data
	private DynamixApplication app = null;
	private TextView nameText;
	private TextView appStatus;
	private ListView plugList;
	private ImageView icon;
	private Spinner policySpinner;
	private TextView policyDescription;
	private boolean pending = false;
	private PluginSettingsAdapter pluginSettingsAdapter;
	private ArrayAdapter<PrivacyPolicy> privacyPolicyAdapter;

	/**
	 * Called when a potentially custom PrivacyRiskLevel ContextItem is selected from the Plugin Settings List context
	 * menu. This means that a new PrivacyPolicy has been chosen and the app should be updated.
	 */
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		// Get the selected PluginPrivacySettings from the UI
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		PluginPrivacySettings policy = (PluginPrivacySettings) pluginSettingsAdapter.getItem(info.position);
		/*
		 * Get the PrivacyRiskLevel for the selected MenuItem's item id. Note that PrivacyRisk.getLevelForID returns
		 * null if the item id is -1 (i.e. default level as set in 'onCreateContextMenu'). Null means that we should set
		 * the default PrivacyRisk.
		 */
		PrivacyRiskLevel level = PrivacyRiskLevel.getLevelForID(item.getItemId());
		if (level != null) {
			policy.overrideMaxPrivacyRisk(level);
		} else {
			// Default PrivacyRiskLevel requested
			policy.setDefaultMaxPrivacyRisk();
		}
		pluginSettingsAdapter.notifyDataSetChanged();
		return super.onContextItemSelected(item);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_app);
		setTitle("Dynamix Context Firewall");
		// Setup the finished editing button
		Button btnFinishedEdit = (Button) findViewById(R.id.btn_finished_app_edit);
		// Extract the serialized application from the incoming intent
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			// Extract basic information from the serialized application
			app = (DynamixApplication) extras.getSerializable("app");
			if (app != null) {
				nameText = (TextView) findViewById(R.id.app_edit_name);
				nameText.setText(app.getName());
				plugList = (ListView) findViewById(R.id.plug_settings_list);
				appStatus = (TextView) findViewById(R.id.app_edit_status);
				icon = (ImageView) findViewById(R.id.icon);
				policyDescription = (TextView) findViewById(R.id.privacy_policy_description);
				/*
				 * See if this application is 'pending', meaning that it has not yet been authorized. Setup its icon
				 * with the appropriate status.
				 */
				pending = extras.getBoolean("pending");
				if (pending) {
					btnFinishedEdit.setText(R.string.btn_finished_app_pending);
					appStatus.setText(app.getStatusString());
					icon.setImageResource(R.drawable.alert);
				} else {
					btnFinishedEdit.setText(R.string.btn_finished_app_edit);
					appStatus.setText(app.getStatusString());
					if (app.isEnabled()) {
						if (DynamixService.checkConnected(app)) {
							icon.setImageResource(R.drawable.app_connected);
						} else {
							icon.setImageResource(R.drawable.app_disconnected);
						}
					} else {
						icon.setImageResource(R.drawable.app_blocked);
					}
				}
				// Load the privacy policy settings into the policySpinner
				policySpinner = (Spinner) findViewById(R.id.privacy_policy_spinner);
				ImageView privacy_icon = (ImageView) findViewById(R.id.privacy_icon);
				privacy_icon.setImageResource(R.drawable.profile);
				/*
				 * Dynamically load available PrivacyPolicies into the privacyPolicyAdapter, which is used in
				 * conjunction with the policySpinner.
				 */
				privacyPolicyAdapter = new ArrayAdapter<PrivacyPolicy>(this, android.R.layout.simple_spinner_item,
						DynamixService.getPrivacyPolicies());
				privacyPolicyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				policySpinner.setAdapter(privacyPolicyAdapter);
				// Select the app's privacy policy in the policySpinner
				int position = privacyPolicyAdapter.getPosition(app.getPrivacyPolicy());
				policySpinner.setSelection(position, false);
				policyDescription.setText(app.getPrivacyPolicy().getDescription());
				/*
				 * Setup the policySpinner's OnItemSelectedListener Note: Can't use 'setOnItemClickListener' with a
				 * Spinner
				 */
				policySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
						handlePrivacyPolicyChange((PrivacyPolicy) policySpinner.getSelectedItem());
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
				/*
				 * Setup the OnItemClickListener for the plugList ListView. When clicked, show the plugin using the
				 * PluginDetailsActivity.
				 */
				plugList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
						ContextPlugin plug = ((PluginPrivacySettings) plugList.getItemAtPosition(position)).getPlugin();
						showPlugin(plug);
					}
				});
				registerForContextMenu(plugList);
			} else
				BaseActivity.activateTab(0);
			/*
			 * http://stackoverflow.com/questions/1246613/android-list-with-grayed-out-items
			 */
		}
		// Setup the finished editing button's OnClickListener. This calls back 'onActivityResult' in the HomeActivity
		btnFinishedEdit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Bundle bundle = new Bundle();
				bundle.putSerializable("app", app);
				Intent mIntent = new Intent();
				mIntent.putExtras(bundle);
				setResult(RESULT_OK, mIntent);
				finish();
			}
		});
		registerForContextMenu(plugList);
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(R.string.app_settings_contextmenu_fidelity);
		menu.add(0, -1, 0, "Policy Default");
		menu.add(0, PrivacyRiskLevel.NONE.getID(), PrivacyRiskLevel.NONE.getID(), "Blocked");
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		ContextPlugin plug = ((PluginPrivacySettings) plugList.getItemAtPosition(info.position)).getPlugin();
		for (PrivacyRiskLevel l : plug.getSupportedPrivacyRiskLevels().keySet()) {
			menu.add(0, l.getID(), l.getID(), l.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onResume() {
		super.onResume();
		pluginSettingsAdapter = new PluginSettingsAdapter(this, R.layout.icon_row, app.getPluginPrivacySettings(),
				"No Context Plug-ins Installed", "");
		plugList.setAdapter(this.pluginSettingsAdapter);
	}

	/**
	 * Handle PrivacyPolicy changes.
	 */
	private void handlePrivacyPolicyChange(final PrivacyPolicy newPolicy) {
		boolean alertUser = false;
		for (PluginPrivacySettings settings : app.getPluginPrivacySettings()) {
			if (settings.isCustom()) {
				alertUser = true;
				break;
			}
		}
		// Since there are custom policies, we need to alert the user before making changes.
		if (alertUser) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Override Custom Settings?").setCancelable(false)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							app.setPrivacyPolicy(newPolicy, true);
							pluginSettingsAdapter.notifyDataSetChanged();
							policyDescription.setText(newPolicy.getDescription());
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							app.setPrivacyPolicy(newPolicy, false);
							pluginSettingsAdapter.notifyDataSetChanged();
							dialog.cancel();
						}
					});
			builder.create().show();
		} else {
			app.setPrivacyPolicy(newPolicy, true);
			pluginSettingsAdapter.notifyDataSetChanged();
			policyDescription.setText(newPolicy.getDescription());
		}
	}

	/**
	 * Show the specified ContextPlugin using the PluginDetailsActivity.
	 */
	private void showPlugin(ContextPlugin plug) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("plug", plug);
		Intent i = new Intent(this, PluginDetailsActivity.class);
		i.putExtras(bundle);
		startActivity(i);
	}

	/**
	 * Local class used as a datasource for PluginPrivacySettings. This class extends a typed Generic ArrayAdapter and
	 * overrides getView in order to update the UI state.
	 * 
	 * @author Darren Carlson
	 */
	private class PluginSettingsAdapter extends EmptyListSupportAdapter<PluginPrivacySettings> {
		public PluginSettingsAdapter(Context context, int textViewResourceId, List<PluginPrivacySettings> settings,
				String emptyTitle, String emptyMessage) {
			super(context, textViewResourceId, settings, emptyTitle, emptyMessage);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = convertView;
			if (super.isListEmpty()) {
				v = inflator.inflate(R.layout.iconless_row, null);
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				tt.setText(getEmptyTitle());
				bt.setText(getEmptyMessage());
				return v;
			} else {
				v = inflator.inflate(R.layout.icon_row, null);
				PluginPrivacySettings setting = this.getItem(position);
				if (setting != null) {
					TextView tt = (TextView) v.findViewById(R.id.toptext);
					TextView bt = (TextView) v.findViewById(R.id.bottomtext);
					ImageView icon = (ImageView) v.findViewById(R.id.icon);
					if (tt != null) {
						tt.setText(setting.getPlugin().getName());
					}
					if (bt != null) {
						if (setting.isCustom()) {
							bt.setText(setting.getMaxPrivacyRisk().toString() + " (Custom)");
						} else {
							bt.setText(setting.getMaxPrivacyRisk().toString() + " (Policy)");
						}
					}
					DescriptiveIcon di = Utils.getDescriptiveIcon(setting.getPlugin());
					icon.setImageResource(di.getIconResId());
				}
				return v;
			}
		}
	}
}