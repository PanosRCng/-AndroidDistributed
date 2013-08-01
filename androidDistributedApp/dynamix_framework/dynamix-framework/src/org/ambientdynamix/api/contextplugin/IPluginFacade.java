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

import java.util.UUID;

import android.content.Context;

/**
 * The IPluginFacade provides a mechanism whereby a ContextPluginRuntime can securely interact with Dynamix and
 * Android. ContextPluginRuntimes never interact with Android directly; rather, Android interaction is mediated through
 * implementations of the IPluginFacade, which check if the calling client is authorized to receive a given service,
 * register/unregister a particular BroadcastReceiver, etc. For example, obtaining a secured version of an Android
 * Context, which can be used to access accessing a particular Android service (e.g. Context.LOCATION_SERVICE).
 * 
 * @author Darren Carlson
 */
public interface IPluginFacade {
	/**
	 * Closes any configuration view associated with the Context Plugin.
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime.
	 */
	public void closeConfigurationView(UUID sessionID);

	/**
	 * Closes any context acquisition view associated with the Context Plugin.
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime.
	 */
	public void closeContextAcquisitionView(UUID sessionID);

	/**
	 * Returns the ContextPluginSettings persisted for the given ContextPluginRuntime in the Dynamix Framework.
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime Returns a ContextPlugin-specific
	 *            ContextPluginSettings object.
	 */
	public ContextPluginSettings getContextPluginSettings(UUID sessionID);

	/**
	 * Returns a secured version of the Android Context that is customized for the caller. The SecuredContext is
	 * configured by a set of Permissions, which allow only specific actions that have been granted by the user.
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime Returns a configured SecuredContext, or
	 *            null if the sessionID is incorrect
	 */
	public Context getSecuredContext(UUID sessionID);

	/**
	 * Returns the current PluginState for the ContextPluginRuntime
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime
	 * @return The state of the calling ContextPluginRuntime
	 */
	public PluginState getState(UUID sessionID);

	/**
	 * Sets the ContextPluginRuntime's associated ContextPlugin to the configured or unconfigured state.
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime.
	 * @param configured
	 *            True if the plugin is properly configured; false otherwise Returns true if the configuration status
	 *            was set; false otherwise.
	 */
	public boolean setPluginConfiguredStatus(UUID sessionID, boolean configured);

	/**
	 * Stores the ContextPluginSettings in the Dynamix Framework on behalf on the calling ContextPluginRuntime.
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime
	 * @param settings
	 *            The ContextPluginSettings to store Returns true if the settings were successfully stored; false
	 *            otherwise.
	 */
	public boolean storeContextPluginSettings(UUID sessionID, ContextPluginSettings settings);

	/**
	 * Adds a listener that will receive NFC events from Android
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime.
	 * @param listener
	 *            The listener that should receive the event (as an Intent)
	 * @return True if the listener was added; false otherwise.
	 */
	public boolean addNfcListener(UUID sessionID, NfcListener listener);

	/**
	 * Removes a previously added NfcListener
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime.
	 * @param listener
	 *            The NfcListener that should be removed.
	 * @return True if the listener was removed; false otherwise.
	 */
	public boolean removeNfcListener(UUID sessionID, NfcListener listener);

	/**
	 * Sends a PluginAlert to Dynamix, which will show it to the user if the plug-in has permission.
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime.
	 * @param alert
	 *            The PluginAlert
	 * @return True if the alert was sent; false otherwise.
	 */
	public boolean sendPluginAlert(UUID sessionID, PluginAlert alert);

	/**
	 * Removes the context request id from Dynamix, which helps Dynamix lower its memory usage. This functionality
	 * should be used if a plug-in receives a context request for an action that will not generate a context event.
	 * In this case, the plug-in should cancel the request id using this method.
	 * 
	 * @param sessionID
	 *            The unique session id of the calling ContextPluginRuntime.
	 * @param requestId
	 *            The context requestId to be cancelled.
	 * @return True if the request was cancelled; false otherwise.
	 */
	public boolean cancelContextRequestId(UUID sessionID, UUID requestId);
}