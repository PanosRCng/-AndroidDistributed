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

import java.util.List;

import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.PluginConstants.PLATFORM;

/**
 * Base class for classes that are capable of extracting updates from a variety of file and network-based sources.
 * 
 * @author Darren Carlson
 */
public interface IContextPluginConnector {
	/**
	 * Cancels a previously started call to 'getContextPlugins'
	 */
	public abstract void cancel();

	/**
	 * Returns a list of ContextPluginUpdates using a class-specific mechanism.
	 * 
	 * @return The list of ContextPluginUpdates.
	 * @throws Exception
	 *             If the ContextPluginUpdates cannot be extracted.
	 */
	public abstract List<DiscoveredContextPlugin> getContextPlugins(PLATFORM platform, VersionInfo platformVersion,
			VersionInfo frameworkVersion) throws Exception;

	/*
	 * TODO: Implement listener interface and methods for events: start, progress, completed, failed.
	 */
}