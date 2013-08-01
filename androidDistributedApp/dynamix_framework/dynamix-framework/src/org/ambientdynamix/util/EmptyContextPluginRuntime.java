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
package org.ambientdynamix.util;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;

/**
 * A generic ContextPluginRuntime used internally by the Dynamix Framework as a place-holder for ContextPlugins that are
 * being installed. This ContextPluginRuntime does nothing except for handle state.
 * 
 * @author Darren Carlson
 */
public class EmptyContextPluginRuntime extends ContextPluginRuntime {
	// Priate data
	private final String TAG = this.getClass().getSimpleName();

	@Override
	public void destroy() {
		// Do nothing
	}

	@Override
	public void init(PowerScheme powerScheme, ContextPluginSettings settings) {
		// Do nothing
	}

	@Override
	public void setPowerScheme(PowerScheme scheme) {
		// Do nothing
	}

	@Override
	public void start() {
		// Do nothing
	}

	@Override
	public void stop() {
		// Do nothing
	}

	@Override
	public void updateSettings(ContextPluginSettings arg0) {
		// Do nothing
	}
}