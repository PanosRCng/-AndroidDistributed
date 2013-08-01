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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.event.PluginStatsEvent;
import org.ambientdynamix.event.SourcedContextInfoSet;

import android.util.Log;

/**
 * Manages statistics for plug-ins (e.g., total events and event history).
 * 
 * @author Darren Carlson
 */
public class PluginStats {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private ArrayBlockingQueue<PluginStatsEvent> queue;
	private int totalEvents;
	private ContextPlugin plug;

	/**
	 * Creates a PluginStats.
	 * 
	 * @param plug
	 *            The plug-in to be monitored.
	 * @param queueCapacity
	 *            The queue capacity for event history.
	 */
	public PluginStats(ContextPlugin plug, int queueCapacity) {
		this.plug = plug;
		queue = new ArrayBlockingQueue<PluginStatsEvent>(queueCapacity);
		if (FrameworkConstants.DEBUG)
			Log.v(TAG, "Created PluginStats for: " + plug);
	}

	/**
	 * Clears all collected stats for the plug-in.
	 */
	public void clear() {
		synchronized (queue) {
			this.queue.clear();
			this.totalEvents = 0;
		}
	}

	/**
	 * Returns a List of past plug-in events (and errors) that have been collected for the plug-in.
	 */
	public List<PluginStatsEvent> getPastEvents() {
		synchronized (queue) {
			PluginStatsEvent[] info = new PluginStatsEvent[queue.size()];
			List<PluginStatsEvent> returnList = new ArrayList<PluginStatsEvent>();
			Collections.addAll(returnList, queue.toArray(info));
			return returnList;
		}
	}

	/**
	 * Returns the ContextPlugin being monitored.
	 */
	public ContextPlugin getPlug() {
		return plug;
	}

	/**
	 * Returns the total number of events sent by the plug-in since monitoring started (or was cleared).
	 */
	public int getTotalEvents() {
		return totalEvents;
	}

	/**
	 * Collects statistics on failed context scans.
	 */
	public void handleContextScanFailed(String errorMessage) {
		synchronized (queue) {
			if (!queue.offer(new PluginStatsEvent(errorMessage))) {
				queue.remove();
				handleContextScanFailed(errorMessage);
			} else {
				totalEvents++;
				if (FrameworkConstants.DEBUG)
					Log.d(TAG, "PluginStats collected an error with event count " + totalEvents + " and queue size "
							+ queue.size());
				PluginStatsActivity.refreshData();
			}
		}
	}

	public void handlePluginContextEvent(SourcedContextInfoSet sourcedSet) {
		if (sourcedSet.getTotalIContextInfoBytes() <= 51200) {
			synchronized (queue) {
				if (!queue.offer(new PluginStatsEvent(sourcedSet))) {
					queue.remove();
					handlePluginContextEvent(sourcedSet);
				} else {
					totalEvents++;
					if (FrameworkConstants.DEBUG)
						Log.d(TAG, "PluginStats collected: " + sourcedSet.getContextType() + " with event count"
								+ totalEvents + " and queue size " + queue.size());
					PluginStatsActivity.refreshData();
				}
			}
		} else {
			handleContextScanFailed("No data available since event exceeded 51200 bytes");
		}
	}
	/**
	 * Collects statistics on incoming plug-in events.
	 */
	// @Override
	// public synchronized void onPluginContextEvent(final UUID sessionId, final ContextInfoSet infoSet) {
	// Thread t = new Thread(new Runnable() {
	// @Override
	// public void run() {
	// if (infoSet != null && infoSet.getSecuredContextInfo() != null && infoSet.getSecuredContextInfo().size() > 0) {
	// // We have to be careful about storing corrupt ContextInfoSet data, or the stat Activity will crash
	// SourcedContextInfoSet sourcedSet = new SourcedContextInfoSet(infoSet, plug);
	// if(sourced)
	// synchronized (queue) {
	// if (!queue.offer(new PluginStatsEvent(sourcedSet))) {
	// queue.remove();
	// onPluginContextEvent(sessionId, infoSet);
	// }
	// else {
	// totalEvents++;
	// Log.d(TAG, "PluginStats collected: " + infoSet.getContextType() + " with event count" + totalEvents
	// + " and queue size " + queue.size());
	// PluginStatsActivity.refreshData();
	// }
	// }
	// }
	// }
	// });
	// t.setDaemon(true);
	// t.start();
	//
	// }
}
