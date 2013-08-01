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
import java.util.Timer;
import java.util.TimerTask;

import org.ambientdynamix.data.DynamixPreferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.ToggleButton;

/**
 * Home user interface, which shows the current authorized Dynamix applications along with their status. This UI also
 * provides a toggle button for activating/deactivating the Dynamix Framework.
 * 
 * @author Darren Carlson
 */
public class HomeActivity extends ListActivity {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private static final int ENABLE_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int ACTIVITY_EDIT = 1;
	private static HomeActivity activity;
	//private static HomeActivity me;
	private DynamixApplicationAdapter adapter;
	private ListView appList = null;
	private Timer refresher;
	private final Handler uiHandler = new Handler();
	private ToggleButton togglebutton = null;
	// Create runnable for updating the UI
	final Runnable updateList = new Runnable() {
		public void run() {
			if (adapter != null)
				adapter.notifyDataSetChanged();
		}
	};

	// Refreshes the UI
	public static void refreshData() {
		if (activity != null)
			activity.uiHandler.post(new Runnable() {
				@Override
				public void run() {
					activity.refresh();
				}
			});
	}

	/**
	 * Static method that allows callers to change the enabled state of the Dynamix Enable/Disable button. Note that
	 * this method is only cosmetic, meaning that it does not actually call methods on the DynamixService.
	 * 
	 * @param active
	 */
	public static void setActiveState(boolean active) {
		if (activity != null) {
			if (activity.togglebutton != null)
				activity.togglebutton.setChecked(active);
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		AlertDialog.Builder builder = null;
		final DynamixApplication app = (DynamixApplication) appList.getItemAtPosition(info.position);
		switch (item.getItemId()) {
		case ENABLE_ID:
			// Present "Are You Sure" dialog box
			builder = new AlertDialog.Builder(this);
			builder.setMessage(app.isEnabled() ? "Block " + app.getName() + "?" : "Unblock " + app.getName() + "?")
					.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							app.setEnabled(!app.isEnabled());
							adapter.notifyDataSetChanged();
							DynamixService.changeApplicationEnabled(app, app.isEnabled());
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			builder.create().show();
			return true;
		case DELETE_ID:
			// Present "Are You Sure" dialog box
			builder = new AlertDialog.Builder(this);
			builder.setMessage("Remove " + app.getName() + "?").setCancelable(false)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							adapter.remove(app);
							DynamixService.revokeSecurityAuthorization(app);
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			builder.create().show();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		// Set our static reference
		activity = this;
		setContentView(R.layout.home_tab);
		appList = getListView();
		appList.setClickable(true);
		// Set an OnItemClickListener on the appList to support editing the
		// applications
		appList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				editApplication((DynamixApplication) appList.getItemAtPosition(position));
			}
		});

		// Setup the Dynamix Enable/Disable button
		togglebutton = (ToggleButton) findViewById(R.id.DynamixActiveToggle);
		togglebutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (togglebutton.isChecked()) {
					DynamixService.startFramework();
				} else {
					
					DynamixService.stopFramework();
				}
			}
		});
		// Setup an state refresh timer, which periodically updates application
		// state in the appList
		refresher = new Timer(true);
		TimerTask t = new TimerTask() {
			@Override
			public void run() {
				uiHandler.post(updateList);
			}
		};
		refresher.scheduleAtFixedRate(t, 0, 1000);
		registerForContextMenu(appList);
		
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(R.string.app_list_contextmenu_title);
		menu.add(0, ENABLE_ID, 0, R.string.app_contextmenu_block);
		menu.add(0, DELETE_ID, 0, R.string.app_contextmenu_remove);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == Activity.RESULT_OK) {
			Bundle extras = intent.getExtras();
			switch (requestCode) {
			case ACTIVITY_EDIT:
				// Access the serialized app coming in from the Intent's Bundle
				// extra
				DynamixApplication app = (DynamixApplication) extras.getSerializable("app");
				// Update the DynamixService with the updated application
				DynamixService.updateApplication(app);
				refresh();
				break;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		refresh();
	}



	/**
	 * Edit the application by creating an intent to launch the ApplicationSettingsActivity, making sure to send along
	 * the application as a Bundle extra.
	 * 
	 * @param app
	 */
	private void editApplication(DynamixApplication app) {
		Bundle bundle = new Bundle();
		bundle.putBoolean("pending", false);
		bundle.putSerializable("app", app);
		Intent i = new Intent(this, ContextFirewallActivity.class);
		i.putExtras(bundle);
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	private void refresh() {
		if (DynamixService.isFrameworkInitialized()) {
			// Setup toggle button with proper state
			boolean dynamixEnabled = DynamixPreferences.isDynamixEnabled(this);
			togglebutton.setChecked(dynamixEnabled);
			// If Dynamix is enabled, but the DynamixService is not running, then call startFramework
			if (dynamixEnabled && !DynamixService.isFrameworkStarted()) {
				DynamixService.startFramework();
			}
			// Load the registered application List box
			this.adapter = new DynamixApplicationAdapter(this, R.layout.icon_row, new ArrayList<DynamixApplication>(
					DynamixService.SettingsManager.getAuthorizedApplications()), false);
			this.adapter.setNotifyOnChange(true);
			appList.setAdapter(this.adapter);
		}
	}
}