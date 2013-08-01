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
import java.util.Vector;

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.core.UpdateManager.IContextPluginUpdateListener;
import org.ambientdynamix.data.ContextPluginAdapter;
import org.ambientdynamix.event.PluginDiscoveryResult;
import org.ambientdynamix.update.contextplugin.IContextPluginConnector;
import org.ambientdynamix.update.contextplugin.IContextPluginInstallListener;
import org.ambientdynamix.util.DescriptiveIcon;
import org.ambientdynamix.util.EmptyListSupportAdapter;
import org.ambientdynamix.util.SeparatedListAdapter;
import org.ambientdynamix.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * User interface that provides an overview of the ContextPlugins installed in the framework. Provides facilities for
 * enabling/disabling plugins as well as uninstalling plugins.
 * 
 * @see ContextPlugin
 * @author Darren Carlson
 */
public class PluginsActivity extends ListActivity implements IContextPluginInstallListener,
		IContextPluginUpdateListener {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private static final int ACTIVITY_EDIT = 1;
	private SeparatedListAdapter adapter;
	private ListView plugList = null;
	private static final int ENABLE_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int CANCEL_ID = Menu.FIRST + 3;
	private final Handler uiHandler = new Handler();
	private static PluginsActivity activity;
	final String INSTALLED_PLUGS_SECTION = "Installed Context Plug-ins";
	final String NEW_PLUGS_SECTION = "Available Context Plug-ins";
	private Map<PluginDiscoveryResult, Integer> installables = new Hashtable<PluginDiscoveryResult, Integer>();
	private ProgressDialog updateProgress = null;
	private InstalledContextPluginAdapter installedAdapter;
	private ContextPluginAdapter newPlugsAdapter;

	public static PluginsActivity getInstance() {
		return activity;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
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
		installables.remove(r);
		removeUpdate(r);
		refreshList();
		if (newPlugsAdapter.getInstallableCount() == 0) {
			scrollTo(0);
		}
	}

	@Override
	public void onInstallFailed(ContextPlugin plug, String message) {
		Log.i(TAG, "installFailed for " + plug + " with message: " + message);
		PluginDiscoveryResult r = findUpdate(plug);
		if (r != null)
			installables.remove(r);
		BaseActivity.toast(message, Toast.LENGTH_LONG);
		if (newPlugsAdapter.getInstallableCount() == 0)
			refreshList();
	};

	@Override
	public void onInstallProgress(ContextPlugin plug, int percentComplete) {
		Log.d(TAG, "installProgress " + percentComplete + " for " + plug);
		// We only update the installable if it's still in the list (another event may have removed it i.e. completed)
		PluginDiscoveryResult up = findUpdate(plug);
		if (up != null) {
			installables.put(up, percentComplete);
			refreshList();
		}
	}

	@Override
	public synchronized void onInstallStarted(ContextPlugin plug) {
		Log.i(TAG, "installStarted for " + plug);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		// Log.e(TAG, "onContextItemSelected for: " + item.getItemId());
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		AlertDialog.Builder builder = null;
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
			case ENABLE_ID:
				// Present "Are You Sure" dialog box
				builder = new AlertDialog.Builder(this);
				builder.setMessage(
						plug.isEnabled() ? "Disable " + plug.getName() + "?" : "Enable " + plug.getName() + "?")
						.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								boolean toggleValue = !plug.isEnabled();
								plug.setEnabled(toggleValue);
								/*
								 * This needs to re-init the plug-in before starting it because the plug-in had been
								 * destroyed.
								 */
								if (toggleValue) {
									// DynamixService.updateContextPluginValues(plug, true)
									DynamixService.reInitializePlugin(plug);
								} else
									DynamixService.updateContextPluginValues(plug, true);
								adapter.notifyDataSetChanged();
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
				builder.setMessage("Remove " + plug.getName() + "?").setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (DynamixService.uninstallPlugin(plug, true)) {
									Adapter a = adapter.getAdapterForSection(INSTALLED_PLUGS_SECTION);
									if (a instanceof ArrayAdapter) {
										((ArrayAdapter) a).remove(plug);
									}
								}
								refresh();
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				builder.create().show();
				return true;
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
		setContentView(R.layout.plugin_tab);
		activity = this;
		plugList = getListView();
		plugList.setClickable(true);
		// create our list and custom adapter
		adapter = new SeparatedListAdapter(this);
		installedAdapter = new InstalledContextPluginAdapter(this, R.layout.icon_row, new ArrayList<ContextPlugin>(),
				"No Context Plug-ins Installed", "");
		installedAdapter.setNotifyOnChange(true);
		adapter.addSection(INSTALLED_PLUGS_SECTION, installedAdapter);
		newPlugsAdapter = new ContextPluginAdapter(this, R.layout.installable_row,
				(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),
				new ArrayList<PluginDiscoveryResult>(), installables, false, "No Available Context Plug-ins",
				"Tap 'Find Plug-ins' to search for compatible Context Plug-ins.");
		newPlugsAdapter.setNotifyOnChange(true);
		adapter.addSection(NEW_PLUGS_SECTION, newPlugsAdapter);
		plugList.setAdapter(this.adapter);
		/*
		 * Setup the OnItemClickListener for the plugList ListView. When clicked, edit the plugin using the
		 * PluginDetailsActivity.
		 */
		plugList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				ContextPlugin plug = null;
				Object item = plugList.getItemAtPosition(position);
				if (item instanceof ContextPlugin)
					plug = (ContextPlugin) item;
				if (item instanceof PluginDiscoveryResult)
					plug = ((PluginDiscoveryResult) item).getDiscoveredPlugin().getContextPlugin();
				// Log.i(TAG, "onItemClick: " + item + " at position: " + installedAdapter.getPosition(item));
				if (plug != null)
					editPlugin(plug);
			}
		});
		/*
		 * Setup the update plugins button.
		 */
		Button btnFindPlugs = (Button) findViewById(R.id.btn_find_plugs);
		btnFindPlugs.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				DynamixService.checkForNewContextPlugins(PluginsActivity.this);
			}
		});
		/*
		 * Setup the update plugins button.
		 */
		Button btnInstallPlugs = (Button) findViewById(R.id.btn_install_plugs);
		btnInstallPlugs.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// Send the selected updates to Dynamix for installation
				List<ContextPlugin> plugs = new Vector<ContextPlugin>();
				for (PluginDiscoveryResult ur : installables.keySet()) {
					plugs.add(ur.getDiscoveredPlugin().getContextPlugin());
				}
				DynamixService.installPlugins(Utils.getSortedContextPluginList(plugs), PluginsActivity.this);
			}
		});
		registerForContextMenu(plugList);
		refresh();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		/*
		 * Make sure that we only show the context menu for installed context plug-ins.
		 */
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		int newPlugStart = adapter.getPositionForSection(NEW_PLUGS_SECTION);
		if (info.position < newPlugStart) {
			menu.setHeaderTitle(R.string.plug_list_context_menu_title);
			menu.add(0, ENABLE_ID, 0, R.string.enable_disable_label);
			menu.add(0, DELETE_ID, 0, R.string.plug_contextmenu_remove);
		} else {
			menu.setHeaderTitle(R.string.plug_list_context_menu_title);
			menu.add(0, CANCEL_ID, 0, R.string.cancel_install_label);
		}
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
					AlertDialog.Builder builder = new AlertDialog.Builder(PluginsActivity.this);
					builder.setTitle("Update Problems");
					builder.setMessage(finalMessage);
					builder.setNeutralButton("Ok", null);
					builder.create().show();
				}
			});
		}
		refresh();
		if (!newPlugsAdapter.isEmpty()) {
			scrollTo(PluginsActivity.this.adapter.getPositionForSection(NEW_PLUGS_SECTION));
		}
	}

	@Override
	public void onUpdateStarted() {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				updateProgress = ProgressDialog.show(PluginsActivity.this, "Checking for new Context Plug-ins",
						"Please wait...", false, true, new DialogInterface.OnCancelListener() {
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		// Setup Change Settings
		MenuItem item1 = menu.add(1, Menu.FIRST, Menu.NONE, "Change Settings");
		item1.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				startActivity(new Intent(PluginsActivity.this, DynamixPreferenceActivity.class));
				return true;
			}
		});
		// Setup Change Settings
		MenuItem item2 = menu.add(1, Menu.FIRST + 1, Menu.NONE, "Remove all Plug-ins");
		item2.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				// Present "Are You Sure" dialog box
				AlertDialog.Builder builder = new AlertDialog.Builder(PluginsActivity.this);
				builder.setMessage("Remove all Context Plug-ins?").setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								removeAllPlugs();
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				builder.create().show();
				return true;
			}
		});
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == Activity.RESULT_OK) {
			// Not handled at present
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	/*
	 * Edit the specified ContextPlugin using the PluginDetailsActivity.
	 */
	private void editPlugin(ContextPlugin plug) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("plug", plug);
		Intent i = new Intent(this, PluginDetailsActivity.class);
		i.putExtras(bundle);
		startActivityForResult(i, ACTIVITY_EDIT);
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

	/**
	 * Refresh the underlying data-source
	 */
	private void refresh() {
		if (newPlugsAdapter != null) {
			newPlugsAdapter.clear();
			for (PluginDiscoveryResult update : UpdateManager.getNewContextPlugins())
				newPlugsAdapter.add(update);
		}
		if (installedAdapter != null) {
			installedAdapter.clear();
			for (ContextPlugin plug : DynamixService.SettingsManager.getInstalledContextPlugins()) {
				if (plug.isInstalled())
					installedAdapter.add(plug);
				else {
					// Plug-in is registered in the settings, but not installed
					installedAdapter.add(plug);
				}
			}
		}
		refreshList();
	}

	private void refreshList() {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}

	private void removeAllPlugs() {
		List<ContextPlugin> plugs = DynamixService.getInstalledContextPlugins();
		for (ContextPlugin plug : plugs) {
			DynamixService.uninstallPlugin(plug, true);
		}
		refresh();
		scrollTo(0);
	}

	private void removeUpdate(final PluginDiscoveryResult update) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				int position = newPlugsAdapter.getPosition(update);
				// Log.i(TAG, "Removing UpdateResult: " + update + " at position: " + position);
				newPlugsAdapter.remove(update);
				adapter.notifyDataSetChanged();
			}
		});
	}

	private void scrollTo(final int position) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "Scrolling to: " + position);
				// plugList.scrollTo(0, position);
				plugList.setSelection(position);
				refresh();
			}
		});
	}

	/**
	 * Local class used as a data-source for ContextPlugins. This class extends a typed Generic ArrayAdapter and
	 * overrides getView in order to update the UI state.
	 * 
	 * @author Darren Carlson
	 */
	private class InstalledContextPluginAdapter extends EmptyListSupportAdapter<ContextPlugin> {
		public InstalledContextPluginAdapter(Context context, int textViewResourceId, List<ContextPlugin> plugs,
				String emptyTitle, String emptyMessage) {
			super(context, textViewResourceId, plugs, emptyTitle, emptyMessage);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (super.isListEmpty()) {
				View v = convertView;
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.iconless_row, null);
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				tt.setText(getEmptyTitle());
				bt.setText(getEmptyMessage());
				return v;
			} else {
				View v = convertView;
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.icon_row, null);
				ContextPlugin plug = this.getItem(position);
				if (plug != null) {
					TextView tt = (TextView) v.findViewById(R.id.toptext);
					TextView bt = (TextView) v.findViewById(R.id.bottomtext);
					ImageView icon = (ImageView) v.findViewById(R.id.icon);
					if (tt != null) {
						tt.setText(plug.getName());
					}
					DescriptiveIcon di = Utils.getDescriptiveIcon(plug);
					if (icon != null)
						icon.setImageResource(di.getIconResId());
					if (bt != null)
						bt.setText(di.getStatusText());
				} else
					Log.e(TAG, "Could not get ContextPlugin for position: " + position);
				return v;
			}
		}
	}

	@Override
	public void onUpdateError(String message) {
		Log.w(TAG, "onUpdateError: " + message);
	}
}