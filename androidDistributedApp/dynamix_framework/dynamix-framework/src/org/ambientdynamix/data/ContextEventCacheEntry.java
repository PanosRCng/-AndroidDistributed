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

import java.sql.Timestamp;

import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.event.SourcedContextInfoSet;

/**
 * A cache event entry containing a ContextPlugin source, an SourcedContextDataSet and a Timestamp indicating when the
 * SourcedContextDataSet was cached.
 * 
 * @author Darren Carlson
 */
public class ContextEventCacheEntry {
	// Private data
	private ContextPlugin source;
	private SourcedContextInfoSet dataSet;
	private Timestamp cachedTime;
	private IDynamixListener targetListener;

	/**
	 * Creates a ContextEventCacheEntry.
	 * 
	 * @param source
	 *            The ContextPlugin event source
	 * @param targetListener
	 *            The target event listener
	 * @param eventSet
	 *            The SourcedContextDataSet to cache
	 * @param cachedTime
	 *            The time the SourcedContextDataSet was cached, as a Timestamp
	 */
	public ContextEventCacheEntry(ContextPlugin source, IDynamixListener targetListener,
			SourcedContextInfoSet eventSet, Timestamp cachedTime) {
		this(source, eventSet, cachedTime);
		this.targetListener = targetListener;
	}

	/**
	 * Creates a ContextEventCacheEntry.
	 * 
	 * @param source
	 *            The ContextPlugin event source
	 * @param eventSet
	 *            The SourcedContextDataSet to cache
	 * @param cachedTime
	 *            The time the SourcedContextDataSet was cached, as a Timestamp
	 */
	public ContextEventCacheEntry(ContextPlugin source, SourcedContextInfoSet eventSet, Timestamp cachedTime) {
		this.source = source;
		this.dataSet = eventSet;
		this.cachedTime = cachedTime;
	}

	/**
	 * Returns the cached time as a Timestamp.
	 */
	public Timestamp getCachedTime() {
		return cachedTime;
	}

	/**
	 * Returns the ContextPlugin source.
	 */
	public ContextPlugin getEventSource() {
		return this.source;
	}

	/**
	 * Returns the SourcedContextDataSet.
	 */
	public SourcedContextInfoSet getSourcedContextEventSet() {
		return this.dataSet;
	}

	/**
	 * Returns the target app for this cached event, or null if there is no target app.
	 */
	public IDynamixListener getTargetListener() {
		return targetListener;
	}

	/**
	 * Returns true if this cached event has a target listener.
	 */
	public boolean hasTargetListener() {
		return targetListener != null ? true : false;
	}

	/**
	 * Sets the cached time as a Timestamp.
	 */
	public void setCachedTime(Timestamp cachedTime) {
		this.cachedTime = cachedTime;
	}

	/**
	 * Sets the ContextPlugin source.
	 */
	public void setEventSource(ContextPlugin source) {
		this.source = source;
	}

	/**
	 * Sets the SourcedContextDataSet.
	 */
	public void setSourcedContextEventSet(SourcedContextInfoSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public String toString() {
		return "ContextEventCacheEntry from: " + source;
	}
}