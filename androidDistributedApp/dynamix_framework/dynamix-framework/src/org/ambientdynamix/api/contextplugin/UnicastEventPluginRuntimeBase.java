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
import java.util.UUID;
import java.util.Vector;

import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.util.Log;

/**
 * Base class for all context plug-in runtimes requiring unicast event support.
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
 * @see InteractiveContextPluginRuntime
 * @see AutoReactiveContextPluginRuntime
 * @author Darren Carlson
 */
public abstract class UnicastEventPluginRuntimeBase extends ContextPluginRuntime {
	private final String TAG = this.getClass().getSimpleName();

	/**
	 * Called by subclasses in order to send non-expiring context events to the Dynamix Framework
	 * 
	 * @param responseId
	 *            The UUID of the originating request
	 * @param data
	 *            The List of SecuredContextData to send
	 */
	public final void sendContextEvent(UUID responseId, List<SecuredContextInfo> data) {
		// Create a UNICAST ContextDataSet
		ContextInfoSet dataSet = ContextInfoSet.createUnicastContextInfoSet(data, responseId);
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
	 * @param responseId
	 *            The UUID of the originating request
	 * @param data
	 *            The List of SecuredContextData to send
	 * @param expireMills
	 *            The length of time the SecuredContextData are valid (in milliseconds)
	 */
	public final void sendContextEvent(UUID responseId, List<SecuredContextInfo> data, int expireMills) {
		// Create a UNICAST ContextDataSet
		ContextInfoSet dataSet = ContextInfoSet.createUnicastContextInfoSet(data, responseId, expireMills);
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
	 * @param responseId
	 *            The UUID of the originating request
	 * @param data
	 *            The SecuredContextData to send
	 */
	public final void sendContextEvent(UUID responseId, SecuredContextInfo data) {
		List<SecuredContextInfo> events = new Vector<SecuredContextInfo>();
		events.add(data);
		this.sendContextEvent(responseId, events);
	}

	/**
	 * Called by subclasses in order to send an expiring context event to the Dynamix Framework
	 * 
	 * @param responseId
	 *            The UUID of the originating request
	 * @param data
	 *            The SecuredContextData to send
	 * @param expireMills
	 *            The length of time the SecuredContextData is valid (in milliseconds)
	 */
	public final void sendContextEvent(UUID responseId, SecuredContextInfo data, int expireMills) {
		List<SecuredContextInfo> events = new Vector<SecuredContextInfo>();
		events.add(data);
		this.sendContextEvent(responseId, events, expireMills);
	}

	/**
	 * Called by subclasses to send context scan errors.
	 * 
	 * @param requestId
	 *            The UUID of the originating request.
	 * @param errorMessage
	 *            The error message
	 * @param errorCode
	 *            The error code of the failure.
	 */
	@Deprecated
	public final void sendContextScanError(UUID requestId, String errorMessage, int errorCode) {
		// Delegates behavior to the injected IPluginEventHandler
		if (super.getEventHandler() != null)
			super.getEventHandler().sendError(this, requestId, errorMessage, errorCode);
		else
			Log.w(TAG, "Can't sendContextScanError because the event handler was null");
	}

	/**
	 * Called by subclasses to send context request errors.
	 * 
	 * @param requestId
	 *            The UUID of the originating request.
	 * @param errorMessage
	 *            The error message.
	 * @param errorCode
	 *            The error code of the failure.
	 */
	public final void sendContextRequestError(UUID requestId, String errorMessage, int errorCode) {
		// Delegates behavior to the injected IPluginEventHandler
		if (super.getEventHandler() != null)
			super.getEventHandler().sendError(this, requestId, errorMessage, errorCode);
		else
			Log.w(TAG, "Can't sendContextRequestError because the event handler was null");
	}
}
