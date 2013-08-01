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
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

/**
 * User interface for reviewing applications that still require authorization before they are able to use Dynamix
 * Framework services (these are known as pending applications). This Activity is automatically focused if the user
 * clicks on a Dynamix Pending Application Notification in the Android notification tray (handled by DynamixActivity).
 * 
 * @author Darren Carlson
 */
public class PendingApplicationActivity extends ListActivity {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private static final int ACTIVITY_EDIT = 1;
	private List<DynamixApplication> pendingApps = null;
	private DynamixApplicationAdapter adapter;
	private ListView pendingAppList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pending_app_tab);
		pendingAppList = getListView();
		pendingAppList.setClickable(true);
		pendingAppList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				editApplication((DynamixApplication) pendingAppList.getItemAtPosition(position));
			}
		});
		registerForContextMenu(pendingAppList);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(R.string.app_list_contextmenu_title);
		menu.add(0, Menu.FIRST, 0, R.string.app_contextmenu_remove);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		AlertDialog.Builder builder = null;
		final DynamixApplication app = (DynamixApplication) pendingAppList.getItemAtPosition(info.position);
		// Present "Are You Sure" dialog box
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Remove " + app.getName() + "?").setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						adapter.remove(app);
						DynamixService.removePendingApplication(app);
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.create().show();
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == Activity.RESULT_OK) {
			Bundle extras = intent.getExtras();
			switch (requestCode) {
			case ACTIVITY_EDIT:
				DynamixApplication app = (DynamixApplication) extras.getSerializable("app");
				if (DynamixService.authorizeApplication(app)) {
					adapter.remove(app);
					adapter.notifyDataSetChanged();
					Toast.makeText(this, "Application Approved!", Toast.LENGTH_SHORT).show();
					if (adapter.isEmpty()) {
						// Set home tab if there are no more applications to
						// authorize
						BaseActivity.activateTab(0);
					}
				}
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		pendingApps = new ArrayList<DynamixApplication>(DynamixService.SettingsManager.getPendingApplications());
		this.adapter = new DynamixApplicationAdapter(this, R.layout.icon_row, new ArrayList<DynamixApplication>(
				pendingApps), true);
		pendingAppList.setAdapter(this.adapter);
	}

	private void editApplication(DynamixApplication app) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("app", app);
		bundle.putBoolean("pending", true);
		Intent i = new Intent(this, ContextFirewallActivity.class);
		i.putExtras(bundle);
		startActivityForResult(i, ACTIVITY_EDIT);
	}
}