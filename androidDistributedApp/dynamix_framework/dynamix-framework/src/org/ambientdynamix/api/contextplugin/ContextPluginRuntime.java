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

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.contextplugin.PluginConstants.LogPriority;
import org.ambientdynamix.api.contextplugin.security.SecuredContext;

import android.content.Context;
import android.util.Log;

/**
 * Base class for various ContextPluginRuntimes, which represents entities that performs the actual work of context
 * modeling within the Dynamix Framework. Each ContextPluginRuntime operate in conjunction with an associated
 * ContextPlugin that provides meta-data describing the plugin's various attributes. There are various types of
 * ContextPluginRuntime sub-classes, each with different runtime behavior. Please see the developer documentation for
 * details.
 * 
 * @see ContextPlugin
 * @see AutoContextPluginRuntime
 * @see ReactiveContextPluginRuntime
 * @see AutoReactiveContextPluginRuntime
 * @author Darren Carlson
 */
public abstract class ContextPluginRuntime {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private IPluginEventHandler eventHandler;
	private IPluginFacade pluginFacade;
	private ContextPlugin parentPlugin;
	private UUID sessionId;
	private String statusMessage;
	private Class<IContextPluginInteractionViewFactory> acquisitionViewFactory;
	private Class<IContextPluginConfigurationViewFactory> settingsViewFactory;

	/**
	 * Creates a ContextPluginRuntime.
	 * 
	 * @see PluginState
	 */
	public ContextPluginRuntime() {
	}

	/**
	 * Adds the specified IPluginContextListener as a listener for this ContextPluginRuntime's ContextEvents.
	 * 
	 * @see ContextEvent
	 * @see IPluginContextListener
	 */
	public final void addContextListener(IPluginContextListener listener) {
		// Delegates behavior to the injected IPluginEventHandler
		if (eventHandler != null)
			this.eventHandler.addContextListener(listener);
	}

	/**
	 * Clears the status message associated with this runtime.
	 */
	public final void clearStatusMessage() {
		this.statusMessage = "";
	}

	/**
	 * Returns the acquisition Activity Class (if set), or null.
	 */
	public final Class<IContextPluginInteractionViewFactory> getAcquisitionViewFactory() {
		return acquisitionViewFactory;
	}

	/**
	 * Returns this ContextPluginRuntime's associated IPluginFacade.
	 * 
	 * @see IPluginFacade
	 */
	public final IPluginFacade getPluginFacade() {
		return pluginFacade;
	}

	/**
	 * Returns this ContextPluginRuntime's event handler.
	 * 
	 * @see IPluginEventHandler
	 */
	public final IPluginEventHandler getEventHandler() {
		return this.eventHandler;
	}

	/**
	 * Returns the ContextPlugin parent of this ContextPluginRuntime
	 */
	public final ContextPlugin getParentPlugin() {
		return parentPlugin;
	}

	/**
	 * Utility method that returns the SecuredContext for this runtime.
	 * 
	 * @see SecuredContext
	 */
	public final Context getSecuredContext() {
		return getPluginFacade().getSecuredContext(getSessionId());
	}

	/**
	 * Returns the UUID session id that uniquely identifies a ContextPluginRuntime to the Dynamix Framework during
	 * runtime. This UUID may change each time Dynamix starts.
	 */
	public final UUID getSessionId() {
		return this.sessionId;
	}

	/**
	 * Returns the settings Activity (if set), or null.
	 */
	public final Class<IContextPluginConfigurationViewFactory> getSettingsViewFactory() {
		return settingsViewFactory;
	}

	/**
	 * Returns a status message associated with this runtime.
	 */
	public final String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * Removes the specified IPluginContextListener as a listener for this ContextPluginRuntime's ContextEvents.
	 * 
	 * @see ContextEvent
	 * @see IPluginContextListener
	 */
	public final void removeContextListener(IPluginContextListener listener) {
		// Delegates behavior to the injected IPluginEventHandler
		if (eventHandler != null)
			eventHandler.removeContextListener(listener);
	}

	/**
	 * Sends a logging event. Logging events are retained by the Dynamix Framework and not passed to applications.
	 * 
	 * @param priority
	 *            The priority of the message.
	 * @param logMessage
	 *            The message content.
	 */
	public final void sendLoggingEvent(LogPriority priority, String logMessage) {
		ContextInfoSet dataSet = ContextInfoSet.createLoggingContextInfoSet(priority, logMessage);
		if (getEventHandler() != null)
			getEventHandler().sendEvent(this, dataSet);
		else
			Log.w(TAG, "Can't sendContextEvent because the event handler was null");
	}

	/**
	 * Sets the acquisition Activity Class.
	 */
	public final void setAcquisitionViewFactory(Class<IContextPluginInteractionViewFactory> acquisitionViewFactory) {
		this.acquisitionViewFactory = acquisitionViewFactory;
	}

	/**
	 * Sets this ContextPluginRuntime's parent ContextPlugin.
	 */
	public final void setParentPlugin(ContextPlugin parentPlugin) {
		this.parentPlugin = parentPlugin;
	}

	/**
	 * Sets this runtime's PowerScheme to the incoming value. Note that runtimes must be capable of dynamically
	 * adjusting their power consumption to match new PowerScheme values (which may change over time).
	 * 
	 * @param scheme
	 *            The new PowerScheme to apply.
	 */
	public abstract void setPowerScheme(PowerScheme scheme) throws Exception;

	/**
	 * Sets the settings Activity.
	 */
	public final void setSettingsViewFactory(Class<IContextPluginConfigurationViewFactory> settingsViewFactory) {
		this.settingsViewFactory = settingsViewFactory;
	}

	/**
	 * Sets a status message associated with this runtime.
	 */
	public final void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	/**
	 * Called once when the ContextPluginRuntime is first initialized. The implementing subclass should acquire the
	 * resources necessary to run. If initialization is unsuccessful, the plug-ins should throw an exception and release
	 * any acquired resources.
	 */
	public abstract void init(PowerScheme powerScheme, ContextPluginSettings settings) throws Exception;

	/**
	 * Called by the Dynamix Context Manager to start (or prepare to start) context sensing or acting operations.
	 */
	public abstract void start() throws Exception;

	/**
	 * Called by the Dynamix Context Manager to stop context sensing or acting operations; however, any acquired
	 * resources should be maintained, since start may be called again.
	 */
	public abstract void stop() throws Exception;

	/**
	 * Stops the runtime (if necessary) and then releases all acquired resources in preparation for garbage collection.
	 * Once this method has been called, it may not be re-started and will be reclaimed by garbage collection sometime
	 * in the indefinite future.
	 */
	public abstract void destroy() throws Exception;

	/**
	 * Called when new ContextPluginSettings are available for the runtime. When this method is called, the
	 * runtime should immediately change its runtime behavior to match the new settings (if started).
	 * 
	 * @param settings
	 *            The new ContextPluginSettings to apply.
	 */
	public abstract void updateSettings(ContextPluginSettings settings) throws Exception;

	/**
	 * Sets the associated IPluginFacade.
	 * 
	 * @see IPluginFacade
	 */
	protected final void setPluginFacade(IPluginFacade androidFacade) {
		this.pluginFacade = androidFacade;
	}

	/**
	 * Sets this ContextPluginRuntime's event handler.
	 * 
	 * @see IPluginEventHandler
	 */
	protected final void setEventHandler(IPluginEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	/**
	 * Sets the UUID session id that uniquely identifies a ContextPluginRuntime to the Dynamix Framework during runtime.
	 * This UUID may change each time Dynamix starts.
	 */
	protected final void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}
}