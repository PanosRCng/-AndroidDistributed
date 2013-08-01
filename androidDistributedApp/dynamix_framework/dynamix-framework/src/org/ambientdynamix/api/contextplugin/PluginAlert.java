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

/**
 * Alert sent from a plug-in to Dynamix.
 * 
 * @author Darren Carlson
 */
public final class PluginAlert {
	// Private data
	private String alertTitle;
	private String alertMessage;
	private String requestedSettingsActivity;

	/**
	 * Creates a PluginAlert without an Intent.
	 * 
	 * @param alertTitle
	 *            The title of the alert.
	 * @param alertMessage
	 *            The alert message.
	 */
	public PluginAlert(String alertTitle, String alertMessage) {
		this.alertTitle = alertTitle;
		this.alertMessage = alertMessage;
	}

	/**
	 * Creates a PluginAlert with an requested settings dialog to open.
	 * 
	 * @param alertTitle
	 *            The title of the alert.
	 * @param alertMessage
	 *            The alert message.
	 * @param requestedSettingsActivity
	 *            The settings to open.
	 */
	public PluginAlert(String alertTitle, String alertMessage, String requestedSettingsActivity) {
		this.alertTitle = alertTitle;
		this.alertMessage = alertMessage;
		this.requestedSettingsActivity = requestedSettingsActivity;
	}

	/**
	 * Returns the alert title.
	 */
	public String getAlertTitle() {
		return alertTitle;
	}

	/**
	 * Returns the alert message.
	 */
	public String getAlertMessage() {
		return alertMessage;
	}

	/**
	 * Returns the requested settings Activity (may be null).
	 */
	public String getRequestedSettingsActivity() {
		return requestedSettingsActivity;
	}
}
