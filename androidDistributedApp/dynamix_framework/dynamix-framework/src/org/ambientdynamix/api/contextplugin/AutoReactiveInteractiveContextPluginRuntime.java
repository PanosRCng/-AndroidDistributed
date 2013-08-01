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

import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.util.Log;

/**
 * Base class for AutoReactiveInteractiveContextPluginRuntime implementations, which combine the functionality of the
 * AutoContextPluginRuntime, ReactiveContextPluginRuntime and InteractiveContextPluginRuntime for ContextPluginRuntimes
 * wishing to provide all types of functionality. An AutoReactiveInteractiveContextPluginRuntime must provide runtime behavior
 * that matches both of the aforementioned interfaces simultaneously.
 * <p>
 * Note: Each ContextPluginRuntime operate in conjunction with an associated ContextPlugin, which provides meta-data
 * describing the plugin's name, description, version, supported fidelity levels, etc. There are various types of C
 * ontextPluginRuntimes, each with different runtime behavior. Currently, the available types are
 * AutoContextPluginRuntime, ReactiveContextPluginRuntime and AutoReactiveContextPluginRuntime. Please see the
 * documentation accompanying these classes for details.
 * 
 * @see ContextPlugin
 * @see ContextPluginRuntime
 * @see AutoContextPluginRuntime
 * @see ReactiveContextPluginRuntime
 * @see InteractiveContextPluginRuntime
 * @author Darren Carlson
 */
public abstract class AutoReactiveInteractiveContextPluginRuntime extends ReactiveContextPluginRuntime {
	private final String TAG = this.getClass().getSimpleName();

	/**
	 * Returns true of the plug-in provides a user interface for the specified context type; false otherwise. Note that
	 * this method will only be used to check context types supported by the plug-in. If this method returns false, it
	 * implies that the 'handleContextRequest' or 'handleConfiguredContextRequest' methods of the
	 * ReactiveContextPluginRuntime base class will be used to handle the context type.
	 * 
	 * @param contextType
	 *            The context type string to check.
	 * @return True of the plug-in provides a user interface for the specified context type; false otherwise.
	 */
	public abstract boolean hasUserInterfaceForContextType(String contextType);

	/**
	 * Returns the minimum PrivacyRiskLevel required by an application to launch the plug-in's context acquisition
	 * interface.
	 */
	public abstract PrivacyRiskLevel getMinLaunchLevel();

	/**
	 * Returns the desired screen orientation for the plug-in's interface. The return value should be a value from the
	 * 'ActivityInfo' screen orientation values e.g., 'ActivityInfo.SCREEN_ORIENTATION_PORTRAIT'
	 */
	public abstract int getScreenOrientation();

	/**
	 * Perform a single context scan when the runtime is STARTED and the PowerScheme is set to MANUAL. Once the single
	 * scan is complete, if the PowerScheme remains MANUAL, the ContextPluginRuntime should stop scanning context and
	 * wait until 'doManualContextScan' is called again or another power scheme is chosen using 'setPowerScheme'.
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
