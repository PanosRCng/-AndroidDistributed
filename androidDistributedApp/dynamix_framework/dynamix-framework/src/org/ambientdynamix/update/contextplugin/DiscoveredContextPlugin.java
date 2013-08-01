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
package org.ambientdynamix.update.contextplugin;

import java.io.Serializable;

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.PluginConstants.UpdatePriority;

/**
 * Provides information regarding a discovered ContextPlugin. This class is used to represent both new plug-ins and
 * plug-in updates.
 * 
 * @author Darren Carlson
 */
public class DiscoveredContextPlugin implements Serializable {
	private static final long serialVersionUID = -3008156585439407905L;
	// Private data
	private ContextPlugin plugin;
	private String updateMessage;
	private UpdatePriority priority;
	private boolean hasError = false;
	private String errorMessage = "";

	/**
	 * Creates a newly DiscoveredContextPlugin with no message and UpdatePriority.NORMAL.
	 * 
	 * @param plugin
	 *            The discovered plug-in.
	 * @param pluginSource
	 *            The source of the plug-in.
	 */
	public DiscoveredContextPlugin(ContextPlugin plugin) {
		this.plugin = plugin;
		this.updateMessage = "";
		this.priority = UpdatePriority.NORMAL;
	}

	/**
	 * Creates a DiscoveredContextPlugin with an update message.
	 * 
	 * @param plugin
	 *            The discovered plug-in.
	 * @param updateMessage
	 *            A message associated with the update.
	 * @param priority
	 *            A UpdatePriority associated with the update.
	 */
	public DiscoveredContextPlugin(ContextPlugin plugin, String updateMessage,
			UpdatePriority priority) {
		this.plugin = plugin;

		this.priority = priority;
		this.updateMessage = updateMessage;
	}

	/**
	 * Creates a DiscoveredContextPlugin with an error state.
	 * 
	 * @param errorMessage
	 *            The error message.
	 */
	public DiscoveredContextPlugin(String errorMessage) {
		this.hasError = true;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the discovered ContextPlugin.
	 */
	public ContextPlugin getContextPlugin() {
		return plugin;
	}

	/**
	 * Returns the error message associated with this DiscoveredContextPlugin.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Returns the update message associated with the DiscoveredContextPlugin.
	 */
	public String getUpdateMessage() {
		return updateMessage;
	}

	/**
	 * Returns the UpdatePriority associated with the DiscoveredContextPlugin.
	 */
	public UpdatePriority getPriority() {
		return priority;
	}

	/**
	 * Returns true if this DiscoveredContextPlugin encountered an error; false otherwise.
	 */
	public boolean hasError() {
		return hasError;
	}

	/**
	 * Sets the discovered ContextPlugin.
	 */
	public void setDiscoveredPlugin(ContextPlugin plug) {
		this.plugin = plug;
	}

	/**
	 * Sets the message associated with the DiscoveredContextPlugin.
	 */
	public void setMessage(String updateMessage) {
		this.updateMessage = updateMessage;
	}

	/**
	 * Sets the UpdatePriority associated with the DiscoveredContextPlugin.
	 */
	public void setPriority(UpdatePriority priority) {
		this.priority = priority;
	}

	@Override
	public String toString() {
		return "DiscoveredContextPlugin for: " + plugin;
	}
}