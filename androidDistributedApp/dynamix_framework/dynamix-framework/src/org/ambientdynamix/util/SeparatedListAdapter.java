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
package org.ambientdynamix.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ambientdynamix.core.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

/**
 * SeparatedListAdapter written by Jeff Sharkey (Copyright 2008 Jeff Sharkey and released under the Apache 2.0 license).
 * 
 * @link 
 *       http://code.google.com/p/foursquared/source/browse/main/src/com/joelapenna/foursquared/widget/SeparatedListAdapter
 *       .java
 * @link http://www.jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
 * @author Jeff Sharkey
 */
public class SeparatedListAdapter extends BaseAdapter implements IObservableAdapter {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	public final Map<String, EmptyListSupportAdapter<?>> sections = new LinkedHashMap<String, EmptyListSupportAdapter<?>>();
	public final ArrayAdapter<String> headers;
	public final static int TYPE_SECTION_HEADER = 0;
	//public final static int EMPTY = -2;
	private Handler uiHandler = new Handler();
	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			uiHandler.post(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}
	};

	
	public SeparatedListAdapter(Context context) {
		super();
		headers = new ArrayAdapter<String>(context, R.layout.list_header);
	}

	public SeparatedListAdapter(Context context, int layoutId) {
		super();
		headers = new ArrayAdapter<String>(context, layoutId);
	}

	public synchronized void addSection(String section, EmptyListSupportAdapter<?> adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
		// Register an observer so we can call notifyDataSetChanged() when our
		// children adapters are modified, otherwise no change will be visible.
		adapter.registerDataSetObserver(mDataSetObserver);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	public synchronized void clear() {
		headers.clear();
		sections.clear();
		notifyDataSetInvalidated();
	}

	public synchronized Adapter getAdapterForSection(String section) {
		return sections.get(section);
	}

	@Override
	public synchronized int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : this.sections.values())
			total += adapter.getCount() + 1;
		return total;
	}

	public synchronized EmptyListSupportAdapter<?> getEmptySupportingAdapter(int position) {
		for (Object section : this.sections.keySet()) {
			EmptyListSupportAdapter<?> adapter = sections.get(section);
			int size = adapter.getCount() + 1;
			// check if position inside this section
			if (position == 0 || position < size)
				return adapter;
			// otherwise jump into next section
			position -= size;

		}
		return null;
	}

	@Override
	public synchronized Object getItem(int position) {
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;
			// check if position inside this section
			if (position == 0)
				return section;
			if (position < size)
				return adapter.getItem(position - 1);
			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	@Override
	public synchronized long getItemId(int position) {
		return position;
	}

	@Override
	public synchronized int getItemViewType(int position) {
		int type = 1;
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;
			// check if position inside this section
			if (position == 0)
				return TYPE_SECTION_HEADER;
			if (position < size)
				return type + adapter.getItemViewType(position - 1);
			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		//return -1;
		return 0;
	}

	public synchronized int getPositionForSection(String sectionName) {
		int count = 1;
		for (String section : this.sections.keySet()) {
			if (!section.equalsIgnoreCase(sectionName))
				count += sections.get(section).getCount();
			else
				break;
		}
		return count;
	}

	@Override
	public synchronized View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;
			if (position == 0) {
				/*
				 * Exception here: java.lang.IllegalStateException: ArrayAdapter requires the resource ID to be a
				 * TextView. Probably happens because the listview uses a caching mechanism and is filled with different
				 * values over time when scrolling the screen. Seems to happen when scrolling.
				 */
				//Log.i(TAG, "getView for " + sectionnum + " convertView " + convertView + " parent " + parent);
				try {
					View v = headers.getView(sectionnum, convertView, parent);
					// Focusable needs to be false: http://code.google.com/p/android/issues/detail?id=3414
					v.setFocusable(false);
					return v;
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
			if (position < size){
				View v = adapter.getView(position - 1, convertView, parent);
				// Focusable needs to be false: http://code.google.com/p/android/issues/detail?id=3414
				v.setFocusable(false);
				return v;
			}
			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	@Override
	public synchronized int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (Adapter adapter : this.sections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public synchronized boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public synchronized boolean isEnabled(int position) {
		int type = getItemViewType(position);
		if (type == TYPE_SECTION_HEADER){
			//Log.d(TAG, "Position " + position + " is a header and is disabled");
			return false;
		}
		else {
			// Get the adapter for the position, then ask the adapter if the item at position is enabled
			EmptyListSupportAdapter<?> adapter = getEmptySupportingAdapter(position);
			
			if (adapter != null){
				boolean enabled = adapter.isEnabled(position);
	
				//Log.d(TAG, "Position " + position + " is NOT a header and enabled is " + enabled);
				return enabled;
			}
			else
				Log.w(TAG, "Could not find adapter for position: " + position);
		}
		return true;
	}

	public synchronized void removeObserver() {
		// Notify all our children that they should release their observers too.
		for (Map.Entry<String, EmptyListSupportAdapter<?>> it : sections.entrySet()) {
			if (it.getValue() instanceof IObservableAdapter) {
				IObservableAdapter adapter = (IObservableAdapter) it.getValue();
				adapter.removeObserver();
			}
		}
	}
}
