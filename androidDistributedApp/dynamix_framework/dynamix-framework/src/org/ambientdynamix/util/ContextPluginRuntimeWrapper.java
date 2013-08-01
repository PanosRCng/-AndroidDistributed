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
import org.ambientdynamix.api.contextplugin.PluginState;

/**
 * Stateful wrapper for ContextPluginRuntimes.
 * 
 * @author Darren Carlson
 */
public class ContextPluginRuntimeWrapper {
	// Private data
	private volatile PluginState currentState;
	private volatile ContextPluginRuntime runtime;
	private volatile boolean executing;

	public ContextPluginRuntimeWrapper() {
		currentState = PluginState.NEW;
	}

	/**
	 * Creates a ContextPluginRuntimeWrapper using the specified ContextPluginRuntime and PluginState.
	 * 
	 * @param runtime
	 * @param initialState
	 */
	public ContextPluginRuntimeWrapper(ContextPluginRuntime runtime, PluginState initialState) {
		this.runtime = runtime;
		this.setState(initialState);
	}

	/**
	 * Returns true if the runtime is executing (started).
	 * 
	 * @return
	 */
	public boolean isExecuting() {
		return executing;
	}

	/**
	 * Set true if the runtime is executing (started); false otherwise.
	 * 
	 * @param executing
	 */
	public void setExecuting(boolean executing) {
		this.executing = executing;
	}

	/**
	 * Returns the wrapped ContextPluginRuntime.
	 */
	public ContextPluginRuntime getContextPluginRuntime() {
		return runtime;
	}

	/**
	 * Sets the ContextPluginRuntime.
	 */
	public void setContextPluginRuntime(ContextPluginRuntime runtime) {
		this.runtime = runtime;
	}

	/**
	 * Returns the current PluginState.
	 * 
	 * @see PluginState
	 */
	public final synchronized PluginState getState() {
		return this.currentState;
	}

	/**
	 * Sets the current PluginState.
	 * 
	 * @see PluginState
	 */
	public final synchronized void setState(PluginState newState) {
		if (newState == null)
			throw new RuntimeException("PluginState cannot be NULL!");
		else
			this.currentState = newState;
	}
}
