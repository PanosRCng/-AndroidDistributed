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
package org.ambientdynamix.api.application;

/**
 * Constants for the Dynamix Application API.
 * 
 * @author Darren Carlson
 */
public class AppConstants {
	// Private constructor to force singleton
	private AppConstants() {
	}

	/**
	 * Indicates the type of the Plugin
	 */
	public static enum ContextPluginType {
		AUTO, REACTIVE, INTERACTIVE, AUTO_REACTIVE, AUTO_INTERACTIVE, AUTO_REACTIVE_INTERACTIVE;
	}

	/**
	 * Indicates the installation status of the Plugin.
	 */
	public static enum PluginInstallStatus {
		INSTALLED, INSTALLING, PENDING_INSTALL, NOT_INSTALLED, ERROR
	}
}