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

import org.ambientdynamix.api.contextplugin.ContextPlugin;

/**
 * Event listener interface for plug-in installations.
 * 
 * @author Darren Carlson
 */
public interface IContextPluginInstallListener {
	/**
	 * Raised when the install is complete.
	 */
	public void onInstallComplete(ContextPlugin plug);

	/**
	 * Raised if the install fails.
	 */
	public void onInstallFailed(ContextPlugin plug, String message);

	/**
	 * Raised when there is installation progress.
	 */
	public void onInstallProgress(ContextPlugin plug, int percentComplete);

	/**
	 * Raised when the install is starts.
	 */
	public void onInstallStarted(ContextPlugin plug);
}
