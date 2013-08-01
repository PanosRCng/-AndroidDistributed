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
package org.ambientdynamix.event;

import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.ambientdynamix.api.contextplugin.ContextInfoSet;
import org.ambientdynamix.api.contextplugin.ContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.IPluginContextListener;
import org.ambientdynamix.api.contextplugin.IPluginEventHandler;
import org.ambientdynamix.api.contextplugin.PluginConstants.EventType;
import org.ambientdynamix.util.Utils;

import android.util.Log;

/**
 * Simple implementation of the IPluginEventHandler interface. Uses a thread-safe Vector for maintaining the list of
 * listeners. Automatically creates a snapshot of the current listener list before sending events.
 * 
 * @author Darren Carlson
 */
public class SimpleEventHandler implements IPluginEventHandler {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private List<IPluginContextListener> listeners = new Vector<IPluginContextListener>();

	/**
	 * Adds a IPluginContextListener if it has not already been added.
	 */
	public void addContextListener(IPluginContextListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Removes a previously added IPluginContextListener.
	 */
	public void removeContextListener(IPluginContextListener listener) {
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendError(final ContextPluginRuntime sender, final UUID responsetId, final String errorMessage,
			final int errorCode) {
		if (sender != null) {
			if (responsetId != null) {
				List<IPluginContextListener> snapshot = new Vector<IPluginContextListener>(listeners);
				for (final IPluginContextListener l : snapshot) {
					Utils.dispatch(new Runnable() {
						@Override
						public void run() {
							l.onContextRequestFailed(sender.getSessionId(), responsetId, errorMessage, errorCode);
						}
					});
				}
				snapshot.clear();
				snapshot = null;
			} else
				Log.w(TAG, "responsetId was null... aborting sendError");
		} else
			Log.w(TAG, "Sender was null... aborting sendError");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendEvent(final ContextPluginRuntime sender, final ContextInfoSet infoSet) {
		if (sender != null) {
			if (infoSet != null) {
				// Ensure that multicase events have a responseId
				if (infoSet.getEventType() == EventType.UNICAST && infoSet.getResponseId() == null) {
					Log.w(TAG, "responseId was null for multicast event... aborting sendEvent");
				} else {
					List<IPluginContextListener> snapshot = new Vector<IPluginContextListener>(listeners);
					for (final IPluginContextListener l : snapshot) {
						Utils.dispatch(new Runnable() {
							@Override
							public void run() {
								l.onPluginContextEvent(sender.getSessionId(), infoSet);
							}
						});
					}
					snapshot.clear();
					snapshot = null;
				}
			} else
				Log.w(TAG, "InfoSet was null... aborting sendEvent");
		} else
			Log.w(TAG, "Sender was null... aborting sendEvent");
	}
}