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

import java.io.Serializable;
import java.util.UUID;

/**
 * The IContextPluginRuntimeFactory is used in conjunction with extensions of the ContextPluginRuntimeFactory class to
 * provide an OSGi-based service for creating a concrete ContextPluginRuntime for a given ContextPlugin.
 * 
 * @see ContextPluginRuntimeFactory
 * @author Darren Carlson
 */
public interface IContextPluginRuntimeFactory extends Serializable {
	/**
	 * Makes a concrete ContextPluginRuntime using the incoming ContextPlugin as the parent.
	 * 
	 * @param parentPlugin
	 *            The parent ContextPlugin
	 * @param facade
	 *            The IPluginFacade facade
	 * @param handler
	 *            The IPluginEventHandler used for event management
	 * @param sessionId
	 *            The globally unique session id for ContextPluginRuntime during the current Dynamix Framework run
	 *            Returns a ContextPluginRuntime, or null if the runtime cannot be created
	 */
	public ContextPluginRuntime makeContextPluginRuntime(ContextPlugin parentPlugin, IPluginFacade facade,
			IPluginEventHandler handler, UUID sessionId) throws Exception;
}