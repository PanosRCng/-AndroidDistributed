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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.core.UpdateManager.IContextPluginUpdateListener;
import org.ambientdynamix.data.ContextPluginAdapter;
import org.ambientdynamix.event.PluginDiscoveryResult;
import org.ambientdynamix.update.contextplugin.IContextPluginConnector;
import org.ambientdynamix.update.contextplugin.IContextPluginInstallListener;
import org.ambientdynamix.util.SeparatedListAdapter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * User interface that provides an overview of the ContextPlugin updates. Also provides facilities for scanning for new
 * updates and performing specific updates.
 * 
 * @see UpdateManager
 * @author Darren Carlson
 */
public class UpdatesActivity extends ListActivity implements IContextPluginInstallListener,
		IContextPluginUpdateListener {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private static UpdatesActivity activity;
	private Map<PluginDiscoveryResult, Integer> installables = new Hashtable<PluginDiscoveryResult, Integer>();
	private SeparatedListAdapter adapter;
	private ListView plugList = null;
	private final Handler uiHandler = new Handler();
	private ProgressDialog updateProgress = null;
	final String CONTEXT_PLUG_SECTION = "Context Plug-in Updates";
	final String FRAMEWORK_UPDATE_SECTION = "Dynamix Framework Updates";
	private static final int CANCEL_ID = Menu.FIRST + 1;

	public static UpdatesActivity getInstance() {
		return activity;
	}

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

	@Override
	public void onInstallComplete(ContextPlugin plug) {
		Log.i(TAG, "installComplete for " + plug);
		PluginDiscoveryResult r = findUpdate(plug);
		synchronized (installables) {
			installables.remove(r);
			removeUpdate(r);
			if (installables.isEmpty()) {
				BaseActivity.activateTab(BaseActivity.PLUGINS_TAB_ID);
			}
		}
	}

	@Override
	public void onInstallFailed(ContextPlugin plug, String message) {
		Log.w(TAG, "installFailed for " + plug + " with message: " + message);
		PluginDiscoveryResult r = findUpdate(plug);
		if (r != null)
			installables.remove(r);
		refreshList();
		toast(message, Toast.LENGTH_LONG);
	};

	@Override
	public void onInstallProgress(ContextPlugin plug, int percentComplete) {
		// Log.i(TAG, "installProgress " + percentComplete + " for " + plug);
		// We only update the installable if it's still in the list (another event may have removed it i.e. completed)
		PluginDiscoveryResult up = findUpdate(plug);
		if (up != null) {
			installables.put(up, percentComplete);
			refreshList();
		}
	}

	@Override
	public void onInstallStarted(ContextPlugin plug) {
		// TODO Auto-generated method stub
		Log.i(TAG, "installStarted for " + plug);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		Log.e(TAG, "onContextItemSelected for: " + item.getItemId());
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Object test = plugList.getItemAtPosition(info.position);
		if (test instanceof ContextPlugin || test instanceof PluginDiscoveryResult) {
			ContextPlugin tmp = null;
			if (test instanceof ContextPlugin) {
				tmp = (ContextPlugin) plugList.getItemAtPosition(info.position);
			} else {
				PluginDiscoveryResult update = (PluginDiscoveryResult) plugList.getItemAtPosition(info.position);
				tmp = update.getDiscoveredPlugin().getContextPlugin();
			}
			final ContextPlugin plug = tmp;
			switch (item.getItemId()) {
			case CANCEL_ID:
				Log.e(TAG, "Calling cancel for: " + plug);
				DynamixService.cancelInstallation(plug);
				return true;
			}
		}
		Log.e(TAG, "Not caught by switch for: " + item.getItemId() + " | " + CANCEL_ID);
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.updates_tab);
		plugList = getListView();
		plugList.setClickable(true);
		// create our list and custom adapter
		adapter = new SeparatedListAdapter(this);
		adapter.addSection(CONTEXT_PLUG_SECTION, new ContextPluginAdapter(this, R.layout.installable_row,
				(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),
				new ArrayList<PluginDiscoveryResult>(), installables, true, "No Available Updates",
				"Tap 'Find Updates' to search for new updates."));
		ContextPluginAdapter sectionAdapter = getContextPluginAdapter();
		sectionAdapter.setNotifyOnChange(true);
		plugList.setAdapter(adapter);
		/*
		 * Setup the OnItemClickListener for the plugList ListView. When clicked, edit the plugin using the
		 * PluginDetailsActivity.
		 */
		plugList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Bundle bundle = new Bundle();
				Log.w(TAG, "getItemViewType: " + adapter.getItemViewType(position));
				if (adapter.getItemViewType(position) == 1) {
					PluginDiscoveryResult update = (PluginDiscoveryResult) adapter.getItem(position);
					bundle.putSerializable("update", update);
					Intent i = new Intent(UpdatesActivity.this, UpdateDetailsActivity.class);
					i.putExtras(bundle);
					startActivity(i);
				}
			}
		});
		/*
		 * Setup the update plugins button.
		 */
		Button btnCheckUpdates = (Button) findViewById(R.id.btn_update_scan);
		btnCheckUpdates.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				DynamixService.checkForContextPluginUpdates(UpdatesActivity.this);
			}
		});
		/*
		 * Setup the update plugins button.
		 */
		Button btnUpdatePlugins = (Button) findViewById(R.id.btn_do_updates);
		btnUpdatePlugins.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				DynamixService.installContextPluginUpdates(installables.keySet(), UpdatesActivity.this);
			}
		});
		registerForContextMenu(plugList);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(R.string.plug_list_context_menu_title);
		menu.add(0, CANCEL_ID, 0, R.string.cancel_install_label);
	}

	@Override
	public void onUpdateCancelled() {
		if (updateProgress != null)
			updateProgress.dismiss();
	}

	@Override
	public void onUpdateComplete(List<PluginDiscoveryResult> incomingUpdates,
			Map<IContextPluginConnector, String> errors) {
		if (updateProgress != null)
			updateProgress.dismiss();
		if (errors != null && errors.size() > 0) {
			String messageBuilder = "";
			for (IContextPluginConnector ps : errors.keySet()) {
				messageBuilder = messageBuilder + ps + ": " + errors.get(ps) + " ";
			}
			final String finalMessage = messageBuilder;
			uiHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					AlertDialog.Builder builder = new AlertDialog.Builder(UpdatesActivity.this);
					builder.setTitle("Update Problems");
					builder.setMessage(finalMessage);
					builder.setNeutralButton("Ok", null);
					builder.create().show();
				}
			});
		}
		refresh();
	}

	@Override
	public void onUpdateStarted() {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				updateProgress = ProgressDialog.show(UpdatesActivity.this, "Checking for Updates", "Please wait...",
						false, true, new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								Log.w(TAG, "onCancel called for dialog: " + dialog);
								updateProgress.dismiss();
								UpdateManager.cancelContextPluginUpdate();
							}
						});
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (installables.isEmpty()) {
			refresh();
		} else {
			// We've still got possible installs selected or running
		}
	}

	private PluginDiscoveryResult findUpdate(ContextPlugin plug) {
		if (installables != null)
			for (PluginDiscoveryResult tmp : installables.keySet()) {
				if (tmp.getDiscoveredPlugin().getContextPlugin().equals(plug)) {
					return tmp;
				}
			}
		return null;
	}

	private ContextPluginAdapter getContextPluginAdapter() {
		return (ContextPluginAdapter) adapter.sections.get(CONTEXT_PLUG_SECTION);
	}

	/**
	 * Refreshes the underlying data-source
	 */
	private void refresh() {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				ContextPluginAdapter sectionAdapter = getContextPluginAdapter();
				sectionAdapter.clear();
				for (PluginDiscoveryResult update : UpdateManager.getFilteredContextPluginUpdates())
					sectionAdapter.add(update);
				adapter.notifyDataSetChanged();
			}
		});
	}

	private void refreshList() {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}

	private void removeUpdate(final PluginDiscoveryResult update) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				ContextPluginAdapter sectionAdapter = getContextPluginAdapter();
				int position = sectionAdapter.getPosition(update);
				Log.d(TAG, "Removing UpdateResult: " + update + " at position: " + position);
				sectionAdapter.remove(update);
				adapter.notifyDataSetChanged();
			}
		});
	}

	private void toast(final String message, final int duration) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "TOASTING: " + message);
				Toast.makeText(UpdatesActivity.this, message, duration).show();
			}
		});
	}

	@Override
	public void onUpdateError(String message) {
		Log.w(TAG, "onUpdateError: " + message);
		
	}
}