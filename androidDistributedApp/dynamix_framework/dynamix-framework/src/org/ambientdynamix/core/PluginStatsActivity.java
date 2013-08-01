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
import java.util.Vector;

import org.ambientdynamix.api.contextplugin.ContextInfoSet;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.PluginConstants.EventType;
import org.ambientdynamix.event.PluginStatsEvent;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * User interface that shows plug-in statistics.
 * 
 * @author Darren Carlson
 */
public class PluginStatsActivity extends ListActivity {
	// Private data
	private static PluginStatsActivity activity;
	private final String TAG = this.getClass().getSimpleName();
	private ListView eventList = null;
	private ContextPluginAdapter adapter = null;
	private final Handler uiHandler = new Handler();
	private ContextPlugin plugin;
	private TextView description = null;

	// Refreshes the UI
	public static void refreshData() {
		if (activity != null)
			activity.uiHandler.post(new Runnable() {
				@Override
				public void run() {
					activity.refreshList();
				}
			});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plugin_stats);
		eventList = getListView();
		eventList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// Grab the clicked info
				final PluginStatsEvent event = (PluginStatsEvent) eventList.getItemAtPosition(position);
				// Create a dialog
				AlertDialog.Builder ad = new AlertDialog.Builder(PluginStatsActivity.this);
				ad.setIcon(R.drawable.icon);
				ad.setTitle("Event Details");
				ad.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
					}
				});
				ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						// OK, go back to Main menu
					}
				});
				// Create out customized view
				// http://stackoverflow.com/questions/1564867/adding-a-vertical-scrollbar-to-an-alertdialog-in-android
				View v = LayoutInflater.from(PluginStatsActivity.this).inflate(R.layout.scrollable_alert, null);
				LinearLayout layout = (LinearLayout) v.findViewById(R.id.stats_details_layout);
				if (event.hasError()) {
					TextView contextType = new TextView(PluginStatsActivity.this);
					contextType.setText("Error: " + event.getErrorMessage());
					layout.addView(contextType, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT));
				} else if (event.getEvent().getContextInfoSet().getEventType() == EventType.LOGGING) {
					TextView contextType = new TextView(PluginStatsActivity.this);
					contextType.setText("Log Message: " + event.getEvent().getContextInfoSet().getLogMessage());
					layout.addView(contextType, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT));
				} else {
					ContextInfoSet info = event.getEvent().getContextInfoSet();
					TextView contextType = new TextView(PluginStatsActivity.this);
					contextType.setText("Context Type: " + info.getContextType());
					layout.addView(contextType, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT));
					TextView timeStamp = new TextView(PluginStatsActivity.this);
					timeStamp.setText("Timestamp: " + info.getTimeStamp().toGMTString());
					layout.addView(timeStamp, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT));

					TextView size = new TextView(PluginStatsActivity.this);
					if (event.getEvent().getTotalIContextInfoBytes() < 1024) {
						size.setText("Event Size: " + (event.getEvent().getTotalIContextInfoBytes()) + " bytes");
					} else
						size.setText("Event Size: " + (event.getEvent().getTotalIContextInfoBytes() / 1024) + " kB");

					layout.addView(size, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT));

					TextView expires = new TextView(PluginStatsActivity.this);
					if (info.expires())
						expires.setText("Expires: " + info.getExpireTime().toLocaleString());
					else
						expires.setText("Expires: Does not expire.");
					layout.addView(expires, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT));
					/*
					 * Setup context details by limiting the text to 3450 characters. Note that a TextView cannot hold
					 * more than 3450 characters, otherwise the view will automatically default to a single line.
					 */
					TextView details = new TextView(PluginStatsActivity.this);
					String content = "No string-based representation available";
					if (info.getSecuredContextInfo().get(0).getContextInfo().getStringRepresentationFormats() != null) {
						String[] formatArray = new String[info.getSecuredContextInfo().get(0).getContextInfo()
								.getStringRepresentationFormats().size()];
						info.getSecuredContextInfo().get(0).getContextInfo().getStringRepresentationFormats()
								.toArray(formatArray);
						if (formatArray.length > 0) {
							for (String format : formatArray) {
								content = "Format: "
										+ format
										+ " = "
										+ info.getSecuredContextInfo().get(0).getContextInfo()
												.getStringRepresentation(format) + " ";
							}
							if (content != null && content.length() > 3450) {
								content = content.substring(0, 3450);
							}
						}
					}
					details.setText(content);
					layout.addView(details, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT));
				}
				ad.setView(v);
				ad.show();
			}
		});
		TextView nameText = (TextView) findViewById(R.id.plug_name);
		TextView statusText = (TextView) findViewById(R.id.plug_status);
		description = (TextView) findViewById(R.id.plug_stats_summary);
		ImageView icon = (ImageView) findViewById(R.id.icon);
		plugin = (ContextPlugin) getIntent().getSerializableExtra("plug");
		if (plugin != null) {
			nameText.setText(plugin.getName());
			statusText.setText(plugin.getVersionInfo().toString());
			if (plugin.isEnabled()) {
				icon.setImageResource(R.drawable.plugin_enabled);
			} else {
				icon.setImageResource(R.drawable.plugin_disabled);
			}
		}
		activity = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, 1, 1, "Refresh");
		menu.add(1, 2, 2, "Clear");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == 1) {
			refreshList();
		}
		if (item.getItemId() == 2) {
			DynamixService.clearPluginStats(plugin);
			refreshList();
		}
		return true;
	}

	@Override
	protected void onResume() {
		if (adapter != null) {
			adapter.clear();
			adapter.notifyDataSetChanged();
		}
		refreshList();
		super.onResume();
	}

	private void refreshList() {
		PluginStats stats = DynamixService.getPluginStats(plugin);
		if (stats != null) {
			description.setText("Total events: " + stats.getTotalEvents());
			//Log.i(TAG, "Got PluginStats for: " + stats.getPlug());
			List<PluginStatsEvent> statList = new Vector<PluginStatsEvent>();
			statList.addAll(stats.getPastEvents());
			adapter = new ContextPluginAdapter(this, R.layout.iconless_row, statList);
			adapter.setNotifyOnChange(true);
			eventList.setAdapter(adapter);
		}
	}

	/**
	 * Local class used as a datasource for PluginStatsEvent entities. This class extends a typed Generic ArrayAdapter
	 * and overrides getView in order to update the UI state.
	 * 
	 * @author Darren Carlson
	 */
	private class ContextPluginAdapter extends ArrayAdapter<PluginStatsEvent> {
		public ContextPluginAdapter(Context context, int textViewResourceId, List<PluginStatsEvent> updates) {
			super(context, textViewResourceId, updates);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.iconless_row, null);
			}
			final PluginStatsEvent event = getItem(position);
			if (event != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				tt.setText(event.getEvent().getContextInfoSet().getTimeStamp().toLocaleString());
				if (event.hasError()) {
					bt.setText("Error");
				} else if (event.getEvent().getContextInfoSet().getEventType() == EventType.LOGGING) {
					bt.setText("Log Message: " + event.getEvent().getContextInfoSet().getLogMessage());
				} else {
					bt.setText(event.getEvent().getContextType());
				}
			}
			return v;
		}
	}
}
