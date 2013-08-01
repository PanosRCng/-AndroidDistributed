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

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * An ArrayAdapter that supports an "empty message" when the adapter has no elements. This allows ListViews to indicate
 * that they are empty, instead of showing a blank list.
 * 
 * @author Darren Carlson
 */
public abstract class EmptyListSupportAdapter<T> extends ArrayAdapter<T> {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private String emptyTitle;
	private String emptyMessage;

	public EmptyListSupportAdapter(Context context, int resource, int textViewResourceId, List<T> objects,
			String emptyTitle, String emptyMessage) {
		super(context, resource, textViewResourceId, objects);
		this.emptyTitle = emptyTitle;
		this.emptyMessage = emptyMessage;
	}

	public EmptyListSupportAdapter(Context context, int resource, int textViewResourceId, String emptyTitle,
			String emptyMessage) {
		super(context, resource, textViewResourceId);
		this.emptyTitle = emptyTitle;
		this.emptyMessage = emptyMessage;
	}

	public EmptyListSupportAdapter(Context context, int resource, int textViewResourceId, T[] objects,
			String emptyTitle, String emptyMessage) {
		super(context, resource, textViewResourceId, objects);
		this.emptyTitle = emptyTitle;
		this.emptyMessage = emptyMessage;
	}

	public EmptyListSupportAdapter(Context context, int textViewResourceId, List<T> objects, String emptyTitle,
			String emptyMessage) {
		super(context, textViewResourceId, objects);
		this.emptyTitle = emptyTitle;
		this.emptyMessage = emptyMessage;
	}

	public EmptyListSupportAdapter(Context context, int textViewResourceId, String emptyTitle, String emptyMessage) {
		super(context, textViewResourceId);
		this.emptyTitle = emptyTitle;
		this.emptyMessage = emptyMessage;
	}

	public EmptyListSupportAdapter(Context context, int textViewResourceId, T[] objects, String emptyTitle,
			String emptyMessage) {
		super(context, textViewResourceId, objects);
		this.emptyTitle = emptyTitle;
		this.emptyMessage = emptyMessage;
	}

	/**
	 * If the underlying list is empty, we return 1; otherwise, we return the total number of elements in our list.
	 */
	@Override
	public int getCount() {
		if (isListEmpty())
			return 1;
		else
			return super.getCount();
	}

	public String getEmptyMessage() {
		return emptyMessage;
	}

	public String getEmptyTitle() {
		return emptyTitle;
	}

	/**
	 * Always returns false because this Adapter always has at least one item. If the underlying list is empty, then
	 * that item is an blank item.
	 */
	@Override
	public boolean isEmpty() {
		return false;
	}

	/**
	 * Returns false if the underlying list is empty, which prevents the user from tapping the blank item. Returns true
	 * if we have data in our list.
	 */
	@Override
	public boolean isEnabled(int position) {
		if (isListEmpty())
			return false;
		else
			return super.isEnabled(position);
	}

	/**
	 * Returns true if the underlying list is empty; false otherwise.
	 */
	public boolean isListEmpty() {
		return super.getCount() == 0 ? true : false;
	}
}
