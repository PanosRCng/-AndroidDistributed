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
package org.ambientdynamix.data;

import java.util.ArrayList;
import java.util.Map;

import org.ambientdynamix.core.R;
import org.ambientdynamix.event.PluginDiscoveryResult;
import org.ambientdynamix.util.EmptyListSupportAdapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Local class used as a data-source for UpdateResults. This class extends a typed Generic ArrayAdapter and overrides
 * getView in order to update the UI state.
 * 
 * @author Darren Carlson
 */
public class ContextPluginAdapter extends EmptyListSupportAdapter<PluginDiscoveryResult> {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private Map<PluginDiscoveryResult, Integer> installables;
	private LayoutInflater inflator;
	private boolean showAsUpdate;

	/**
	 * Creates a ContextPluginAdapter.
	 * 
	 * @param context
	 *            The Android context.
	 * @param textViewResourceId
	 *            The text view resource id to manage.
	 * @param inflator
	 *            The Android LayoutInflater.
	 * @param updates
	 *            An ArrayList of UpdateResults.
	 * @param installables
	 *            A Map of UpdateResults to their install completion percentage (e.g., 10 = 10%).
	 * @param showAsUpdate
	 *            True if the adapter should be configured for updates; false to configure the adapter for
	 *            installations.
	 * @param emptyTitle
	 *            The message to display in the title of an empty adapter.
	 * @param emptyMessage
	 *            The message to display in the body of an empty adapter.
	 */
	public ContextPluginAdapter(Context context, int textViewResourceId, LayoutInflater inflator,
			ArrayList<PluginDiscoveryResult> updates, Map<PluginDiscoveryResult, Integer> installables,
			boolean showAsUpdate, String emptyTitle, String emptyMessage) {
		super(context, textViewResourceId, updates, emptyTitle, emptyMessage);
		this.installables = installables;
		this.inflator = inflator;
		this.showAsUpdate = showAsUpdate;
	}

	/**
	 * Returns the total number of UpdateResults.
	 */
	public int getInstallableCount() {
		return this.installables.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (super.isListEmpty()) {
			v = inflator.inflate(R.layout.iconless_row, null);
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			tt.setText(getEmptyTitle());
			bt.setText(getEmptyMessage());
			return v;
		} else  {
			v = inflator.inflate(R.layout.installable_row, null);
			/*
			 * This issue is that the position is -1 here, which is not a valid index
			 */
			final PluginDiscoveryResult update = getItem(position);
			if (update != null) {
				Integer percentComplete = installables.get(update);
				final TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView sum = (TextView) v.findViewById(R.id.summary);
				ProgressBar progress = (ProgressBar) v.findViewById(R.id.progress);
				CheckedTextView checked = (CheckedTextView) v.findViewById(R.id.checkedTextView);
				if (progress == null || checked == null)
					return v;
				if (installables.containsKey(update) && installables.get(update) >= 0) {
					progress.setVisibility(View.VISIBLE);
					progress.setProgress(percentComplete);
				} else
					progress.setVisibility(View.GONE);
				checked.setChecked(installables.containsKey(update));
				checked.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.i(TAG, "View clicekd " + v);
						CheckedTextView tv = (CheckedTextView) v;
						if (!tv.isChecked()) {
							installables.put(update, -1);
							tv.setChecked(true);
						} else {
							installables.remove(update);
							tv.setChecked(false);
						}
						ContextPluginAdapter.this.notifyDataSetChanged();
					}
				});
				if (tt != null) {
					tt.setText(update.getDiscoveredPlugin().getContextPlugin().getName());
				}
				if (sum != null) {
					if (showAsUpdate) {
						StringBuilder updateText = new StringBuilder();
						updateText.append(update.getTargetPlugin().getVersionInfo() + " to "
								+ update.getDiscoveredPlugin().getContextPlugin().getVersionInfo() + "\n");
						updateText.append(update.getDiscoveredPlugin().getPriority().toString() + " Update");
						sum.setText(updateText);
					} else
						sum.setText(update.getDiscoveredPlugin().getContextPlugin().getDescription());
				}
			}
		}
		return v;
	}
}