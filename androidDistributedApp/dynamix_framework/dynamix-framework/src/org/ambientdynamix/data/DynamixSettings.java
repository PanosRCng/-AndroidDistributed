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

import java.util.List;
import java.util.Vector;

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.core.DynamixApplication;
import org.ambientdynamix.update.contextplugin.DiscoveredContextPlugin;

/**
 * Internal storage object for user-based settings related to the Dynamix Framework. This class is intended to be
 * persisted by an ISettingsManager. It uses thread-safe Vectors internally to store Lists of objects; however, to
 * maintain proper state, all interactions with DynamixSettings should be handled through the ISettingsManager. To help
 * ensure this relationship, this class is not public.
 * 
 * @author Darren Carlson
 */
public class DynamixSettings {
	/**
	 * Indicates whether or not Dynamix is enabled.
	 */
	private boolean enabled;
	/**
	 * Indicates whether or not Dynamix exited (closed) cleanly. Default to true for the first run.
	 */
	private Boolean cleanExit = true;
	/**
	 * Indicates whether or not Dynamix should auto-start when Android starts.
	 */
	private Boolean autoStart = true;
	/**
	 * The current power scheme.
	 */
	private PowerScheme scheme = PowerScheme.BALANCED;
	/**
	 * List of all applications that are authorized to receive ContextEvents.
	 */
	private List<DynamixApplication> authorizedApplications = new Vector<DynamixApplication>();
	/**
	 * List of all applications that have requested Dynamix Service but are not yet authorized to receive ContextEvents.
	 */
	private List<DynamixApplication> pendingApplications = new Vector<DynamixApplication>();
	/**
	 * List of all applications that have been denied Dynamix service and will not receive ContextEvents.
	 */
	private List<DynamixApplication> deniedApplications = new Vector<DynamixApplication>();
	/**
	 * List of all installed ContextPlugins.
	 */
	private List<ContextPlugin> installedContextPlugins = new Vector<ContextPlugin>();
	/**
	 * List of all pending DiscoveredContextPlugins.
	 */
	private List<DiscoveredContextPlugin> pendingContextPlugins = new Vector<DiscoveredContextPlugin>();

	/**
	 * Empty constructor to support DB4o searches.
	 */
	public DynamixSettings() {
	}

	/**
	 * Returns the list of authorized applications.
	 */
	public List<DynamixApplication> getAuthorizedApplications() {
		return this.authorizedApplications;
	}

	/**
	 * Returns a List of the installed ContextPlugins.
	 */
	public List<ContextPlugin> getInstalledContextPlugins() {
		return installedContextPlugins;
	}

	/**
	 * Returns the List of pending DiscoveredContextPlugins.
	 */
	public List<DiscoveredContextPlugin> getPendingContextPlugins() {
		return pendingContextPlugins;
	}

	/**
	 * Returns the List of denied applications.
	 */
	public List<DynamixApplication> getDeniedApplications() {
		return deniedApplications;
	}

	/**
	 * Returns the list of pending applications.
	 */
	public List<DynamixApplication> getPendingApplications() {
		return pendingApplications;
	}

	/**
	 * Returns the current PowerScheme.
	 */
	public PowerScheme getPowerScheme() {
		return scheme;
	}

	/**
	 * @return True if Dynamix had a clean exit; false otherwise.
	 */
	public Boolean hadCleanExit() {
		return cleanExit;
	}

	/**
	 * Returns true if Dynamix should auto-start when Android boots; false, otherwise.
	 */
	public boolean isAutoStart() {
		return autoStart;
	}

	/**
	 * Checks if the Dynamix Framework is enabled.
	 */
	public synchronized boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Sets if Dynamix should auto-start when Android boots.
	 * 
	 * @param autoStart
	 *            true if Dynamix should auto-start when Android boots; false otherwise.
	 */
	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

	/**
	 * Sets if Dynamix had a clean exit.
	 * 
	 * @param cleanExit
	 */
	public void setCleanExit(Boolean cleanExit) {
		this.cleanExit = cleanExit;
	}

	/**
	 * Sets the List of ContextPluginUpdates
	 * 
	 * @param pendingContextPlugins
	 *            The List of ContextPluginUpdates
	 */
	public void setContextPluginUpdates(List<DiscoveredContextPlugin> contextPluginUpdates) {
		this.pendingContextPlugins = contextPluginUpdates;
	}

	/**
	 * Sets the enabled state of the Dynamix Framework.
	 */
	public synchronized void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Sets the current PowerScheme.
	 */
	public void setPowerScheme(PowerScheme scheme) {
		this.scheme = scheme;
	}
}