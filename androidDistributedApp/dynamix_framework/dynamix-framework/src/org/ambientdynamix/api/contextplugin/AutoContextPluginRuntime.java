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
package org.ambientdynamix.api.contextplugin;

import java.util.List;
import java.util.Vector;

import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.util.Log;

/**
 * Base class for autonomous ContextPluginRuntime implementations, which perform continuous context acquisition and
 * modeling of a specific set of context information types, broadcasting IContextInfo results to all Dynamix
 * applications holding associated context support (and appropriate security credentials). In this regard, an
 * AutoContextPluginRuntime can be understood to "push" context events to clients without requiring Applications
 * to specifically request a context scan. Rather, they operate independently in the background, modeling and
 * broadcasting context information as deemed appropriate by the developer of the AutoContextPluginRuntime, and in
 * accordance with the current power scheme.
 * <p>
 * Note: Each ContextPluginRuntime operate in conjunction with an associated ContextPlugin, which provides meta-data
 * describing the plugin's name, description, version, supported fidelity levels, etc. There are various types of
 * ContextPluginRuntimes, each with different runtime behavior. Currently, the available types are
 * AutoContextPluginRuntime, ReactiveContextPluginRuntime and AutoReactiveContextPluginRuntime. Please see the
 * documentation accompanying these classes for details.
 * 
 * @see ContextPlugin
 * @see ContextPluginRuntime
 * @see ReactiveContextPluginRuntime
 * @see AutoReactiveContextPluginRuntime
 * @author Darren Carlson
 */
public abstract class AutoContextPluginRuntime extends ContextPluginRuntime {
	private final String TAG = this.getClass().getSimpleName();

	/**
	 * Perform a single context scan when the runtime is STARTED and the PowerScheme is set to MANUAL. Once the
	 * single scan is complete, if the PowerScheme remains MANUAL, the ContextPluginRuntime should stop scanning
	 * context and wait until 'doManualContextScan' is called again or another power scheme is chosen using
	 * 'setPowerScheme'.
	 */
	public abstract void doManualContextScan();

	/**
	 * Called by subclasses in order to send non-expiring context events to the Dynamix Framework
	 * 
	 * @param data
	 *            The List of SecuredContextData to send
	 */
	public final void sendContextEvent(List<SecuredContextInfo> data) {
		// Create a BROADCAST ContextDataSet
		ContextInfoSet dataSet = ContextInfoSet.createBroadcastContextInfoSet(data);
		if (dataSet != null) {
			// Delegates behavior to the injected IPluginEventHandler
			if (super.getEventHandler() != null)
				super.getEventHandler().sendEvent(this, dataSet);
			else
				Log.w(TAG, "Can't sendContextEvent because the event handler was null");
		} else
			Log.w(TAG, "Can't sendContextEvent because createBroadcastContextDataSet returned null");
	}

	/**
	 * Called by subclasses in order to send expiring context events to the Dynamix Framework
	 * 
	 * @param data
	 *            The List of SecuredContextData to send
	 * @param expireMills
	 *            The length of time the SecuredContextData are valid (in milliseconds)
	 */
	public final void sendContextEvent(List<SecuredContextInfo> data, int expireMills) {
		// Create a BROADCAST ContextDataSet
		ContextInfoSet dataSet = ContextInfoSet.createBroadcastContextInfoSet(data, expireMills);
		if (dataSet != null) {
			// Delegates behavior to the injected IPluginEventHandler
			if (super.getEventHandler() != null)
				super.getEventHandler().sendEvent(this, dataSet);
			else
				Log.w(TAG, "Can't sendContextEvent because the event handler was null");
		} else
			Log.w(TAG, "Can't sendContextEvent because createBroadcastContextDataSet returned null");
	}

	/**
	 * Called by subclasses in order to send a non-expiring context event to the Dynamix Framework
	 * 
	 * @param data
	 *            The SecuredContextData to send
	 */
	public final void sendContextEvent(SecuredContextInfo data) {
		List<SecuredContextInfo> events = new Vector<SecuredContextInfo>();
		events.add(data);
		this.sendContextEvent(events);
	}

	/**
	 * Called by subclasses in order to send an expiring context event to the Dynamix Framework
	 * 
	 * @param data
	 *            The SecuredContextData to send
	 * @param expireMills
	 *            The length of time the SecuredContextData is valid (in milliseconds)
	 */
	public final void sendContextEvent(SecuredContextInfo data, int expireMills) {
		List<SecuredContextInfo> events = new Vector<SecuredContextInfo>();
		events.add(data);
		this.sendContextEvent(events, expireMills);
	}
}