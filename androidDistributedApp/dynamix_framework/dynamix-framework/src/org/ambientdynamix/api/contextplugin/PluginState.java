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

/**
 * Represents the various plug-in runtime states.
 * 
 * @see ContextPluginRuntime
 * @author Darren Carlson
 */
public final class PluginState implements Serializable {
	private static final long serialVersionUID = 4181339602328973444L;
	/**
	 * New and uninitialized.
	 */
	public static final PluginState NEW = new PluginState("NEW");
	/**
	 * Initializing.
	 */
	public static final PluginState INITIALIZING = new PluginState("INITIALIZING");
	/**
	 * Initialized and stopped (all necessary resources have been acquired).
	 */
	public static final PluginState INITIALIZED = new PluginState("INITIALIZED");
	/**
	 * Starting.
	 */
	public static final PluginState STARTING = new PluginState("STARTING");
	/**
	 * Started and running.
	 */
	public static final PluginState STARTED = new PluginState("STARTED");
	/**
	 * Stopping.
	 */
	public static final PluginState STOPPING = new PluginState("STOPPING");
	/**
	 * Plugin is ready for garbage collection (all acquired resources have been released).
	 */
	public static final PluginState DESTROYED = new PluginState("DESTROYED");
	/**
	 * Plugin has entered an error state and is NOT running (all acquired resources have been released).
	 */
	public static final PluginState ERROR = new PluginState("ERROR");
	// Private variables
	private String name;

	// Private constructor to ensure singleton
	private PluginState(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		PluginState other = (PluginState) candidate;
		return other.name.equals(this.name) ? true : false;
	}

	// HashCode Example: http://www.javafaq.nu/java-example-code-175.html
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + this.name.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
