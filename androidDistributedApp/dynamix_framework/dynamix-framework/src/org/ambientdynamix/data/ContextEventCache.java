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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.core.FrameworkConstants;
import org.ambientdynamix.event.SourcedContextInfoSet;
import org.ambientdynamix.util.Utils;

import android.util.Log;

/**
 * A self-managing, threaded SecuredEvent cache that supports several configuration options. The ContextEventCache is
 * used to maintain a recent history of context events, along with their associated security information, for use in
 * re-provisioning context data on request by DynamixApplications.
 * 
 * @author Darren Carlson
 */
public class ContextEventCache implements Runnable {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private int maxCacheMills;
	private int cullInterval;
	private int maxCapacity;
	private volatile boolean running;
	private ArrayBlockingQueue<ContextEventCacheEntry> queue;

	/**
	 * Creates a new ContextEventCache.
	 * 
	 * @param maxCacheMills
	 *            : The maximum time (in milliseconds) a CacheEntry is allowed to stay in the cache.
	 * @param cullInterval
	 *            : How often (in milliseconds) to check for CacheEntry expiration.
	 * @param maxCapacity
	 *            : The maximum capacity of the cache. If exceeded, the cache will remove the oldest CacheEntry to make
	 *            room.
	 */
	public ContextEventCache(int maxCacheMills, int cullIntervalMills, int maxCapacity) {
		this.maxCacheMills = maxCacheMills;
		this.cullInterval = cullIntervalMills;
		this.maxCapacity = maxCapacity;
		queue = new ArrayBlockingQueue<ContextEventCacheEntry>(this.maxCapacity, true);
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#cacheEvent(org.ambientdynamix.api.contextplugin.ContextPlugin, org.ambientdynamix.event.SourcedContextInfoSet)
	 */
	
	public synchronized void cacheEvent(ContextPlugin source, SourcedContextInfoSet eventGroup) {
		if (FrameworkConstants.DEBUG)
			Log.v(TAG, "Caching SourcedContextInfoSet: " + eventGroup + " from " + source);

		if (eventGroup.getTotalIContextInfoBytes() <= 51200) {
			// Only cache if the event doesn't expire, or if the expiration time is longer than 0 milliseconds
			if (!eventGroup.expires() || eventGroup.getExireMills() > 0) {
				// Don't cache large events
				if (!queue.offer(new ContextEventCacheEntry(source, eventGroup, new Timestamp(new Date().getTime())))) {
					if (FrameworkConstants.DEBUG)
						Log.d(TAG, "Cache was full... removing an element to make space");
					queue.remove();
					cacheEvent(source, eventGroup);
				}
			}
		} else if (FrameworkConstants.DEBUG)
			Log.w(TAG, "Can't cache event of size: " + eventGroup.getTotalIContextInfoBytes());
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#cacheEvent(org.ambientdynamix.api.application.IDynamixListener, org.ambientdynamix.api.contextplugin.ContextPlugin, org.ambientdynamix.event.SourcedContextInfoSet)
	 */
	
	public synchronized void cacheEvent(IDynamixListener targetListener, ContextPlugin source,
			SourcedContextInfoSet eventGroup) {
		if (FrameworkConstants.DEBUG)
			Log.v(TAG, "Caching SourcedContextInfoSet: " + eventGroup + " from " + source + " for listener "
					+ targetListener);

		if (eventGroup.getTotalIContextInfoBytes() <= 51200) {
			// Only cache if the event doesn't expire, or if the expiration time is longer than 0 milliseconds
			if (!eventGroup.expires() || eventGroup.getExireMills() > 0) {
				if (!queue.offer(new ContextEventCacheEntry(source, targetListener, eventGroup, new Timestamp(
						new Date().getTime())))) {
					if (FrameworkConstants.DEBUG)
						Log.d(TAG, "Cache was full... removing an element to make space");
					queue.remove();
					cacheEvent(targetListener, source, eventGroup);
				}
			}
		} else if (FrameworkConstants.DEBUG)
			Log.w(TAG, "Can't cache event of size: " + eventGroup.getTotalIContextInfoBytes());
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#cacheEvents(org.ambientdynamix.api.contextplugin.ContextPlugin, java.util.List)
	 */
	
	public synchronized void cacheEvents(ContextPlugin source, List<SourcedContextInfoSet> eventGroups) {
		for (SourcedContextInfoSet event : eventGroups) {
			cacheEvent(source, event);
		}
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#getCachedEvents()
	 */
	
	public List<ContextEventCacheEntry> getCachedEvents() {
		return new Vector<ContextEventCacheEntry>(
				Arrays.asList(queue.toArray(new ContextEventCacheEntry[queue.size()])));
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#getCachedEvents(java.lang.String)
	 */
	
	public List<ContextEventCacheEntry> getCachedEvents(String contextType) {
		List<ContextEventCacheEntry> snapshot = getCachedEvents();
		for (ContextEventCacheEntry e : snapshot) {
			if (!e.getSourcedContextEventSet().getContextType().equals(contextType))
				snapshot.remove(e);
		}
		return snapshot;
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#removeContextEvents(org.ambientdynamix.api.contextplugin.ContextPlugin)
	 */
	
	public void removeContextEvents(ContextPlugin plug) {
		if (FrameworkConstants.DEBUG)
			Log.v(TAG, "Removing events for: " + plug);
		for (ContextEventCacheEntry event : queue) {
			if (event.getEventSource().equals(plug)) {
				if (FrameworkConstants.DEBUG)
					Log.v(TAG, "Removing: " + event);
				queue.remove(event);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#removeContextEvents(org.ambientdynamix.api.application.IDynamixListener, java.lang.String)
	 */
	
	public void removeContextEvents(IDynamixListener listener, String contextType) {
		if (FrameworkConstants.DEBUG)
			Log.v(TAG, "Removing events for: " + listener);
		for (ContextEventCacheEntry event : queue) {
			if (event.hasTargetListener() && event.getTargetListener().asBinder().equals(listener.asBinder())) {
				if (event.getSourcedContextEventSet().getContextType().equalsIgnoreCase(contextType)) {
					if (FrameworkConstants.DEBUG)
						Log.v(TAG, "Removing: " + event);
					queue.remove(event);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#run()
	 */
	
	public void run() {
		Log.i(TAG, "ContextEventCache is running!");
		while (running) {
			// Sleep for the cullInterval
			try {
				Thread.sleep(cullInterval);
			} catch (InterruptedException e1) {
				Log.e(TAG, e1.getMessage());
				e1.printStackTrace();
				running = false;
			}
			List<ContextEventCacheEntry> remove = new ArrayList<ContextEventCacheEntry>();
			// Check for expired ContextData elements
			Date now = new Date();
			for (ContextEventCacheEntry e : queue) {
				// Check for autoExpire
				if (now.getTime() - e.getCachedTime().getTime() > this.maxCacheMills) {
					remove.add(e);
					if (FrameworkConstants.DEBUG)
						Log.v(TAG, "CacheEntry " + e + " auto expired after " + maxCacheMills + "ms");
					break;
				}
				// Check if the EventGroup itself has expired
				if (e.getSourcedContextEventSet().expires()) {
					// Error check first
					if (e.getSourcedContextEventSet().getExpireTime() == null) {
						Log.w(TAG, "ContextEventSet had null ExpireTime: " + e.getSourcedContextEventSet());
					} else if (now.after(e.getSourcedContextEventSet().getExpireTime())) {
						if (FrameworkConstants.DEBUG)
							Log.v(TAG, "CacheEntry expired: " + e);
						remove.add(e);
					}
				}
			}
			// Remove out-dated cache entries
			for (ContextEventCacheEntry e : remove) {
				queue.remove(e);
			}
		}
		Log.d(TAG, "ContextEventCache has stopped!");
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#start()
	 */
	
	public synchronized void start() {
		if (!running) {
			Log.d(TAG, "Starting ContextEventCache...");
			running = true;
			Utils.dispatch(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.ambientdynamix.data.IContextEventCache#stop()
	 */
	
	public synchronized void stop() {
		if (running) {
			Log.d(TAG, "Stopping ContextEventCache...");
			running = false;
			queue.clear();
		}
	}
}