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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Used as a data-source for DynamixApplication entities (typically within a ListView). This class extends a typed
 * Generic ArrayAdapter and overrides getView in order to update the UI state.
 * 
 * @author Darren Carlson
 */
class DynamixApplicationAdapter extends ArrayAdapter<DynamixApplication> {
	private ArrayList<DynamixApplication> apps;
	private boolean pending;

	/**
	 * Public constructor. This class displays different icons depending on the value of 'pending', which refers to
	 * either pending applications (pending == true) or authorized applications (pending == false).
	 */
	public DynamixApplicationAdapter(Context context, int textViewResourceId, ArrayList<DynamixApplication> apps,
			boolean pending) {
		super(context, textViewResourceId, apps);
		this.apps = apps;
		this.pending = pending;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.icon_row, null);
		}
		DynamixApplication app = apps.get(position);
		if (app != null) {
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			ImageView icon = (ImageView) v.findViewById(R.id.icon);
			if (tt != null) {
				tt.setText(app.getName());
			}
			if (bt != null) {
				bt.setText(R.string.app_pending_message);
			}
			if (pending) {
				// If we're showing pending applications, use the alert icon
				icon.setImageResource(R.drawable.alert);
			} else {
				/*
				 * If we're showing authorized applications, show proper icons depending on the app's blocked and
				 * connected states.
				 */
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
		}
		return v;
	}
}
