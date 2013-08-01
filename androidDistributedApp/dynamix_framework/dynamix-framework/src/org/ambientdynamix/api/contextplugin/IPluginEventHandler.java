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

/**
 * Interface that allows clients to register and unregister for receiving SecuredEvents from a ContextPluginRuntime.
 * 
 * @author Darren Carlson
 */
public interface IPluginEventHandler {
	/**
	 * Registers the specified IPluginContextListener to receive events.
	 */
	public void addContextListener(IPluginContextListener listener);

	/**
	 * Unregisters the specified IPluginContextListener from event reception.
	 */
	public void removeContextListener(IPluginContextListener listener);

	/**
	 * Sends an error message in response to a specific requestId.
	 * 
	 * @param sender
	 *            The event source.
	 * @param requestId
	 *            The original requestId.
	 * @param errorMessage
	 *            The error message.
	 */
	public void sendError(ContextPluginRuntime sender, UUID requestId, String errorMessage, int errorCode);

	/**
	 * Sends the specified ContextDataSet to registered listeners using the specified ContextPluginRuntime as the event
	 * sender.
	 */
	public void sendEvent(ContextPluginRuntime sender, ContextInfoSet dataSet);
}