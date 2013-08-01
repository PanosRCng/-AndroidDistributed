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
package org.ambientdynamix.event;

import java.io.Serializable;

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.update.contextplugin.DiscoveredContextPlugin;

/**
 * Represents a plug-in discovered from an update check. The plug-in may be new or an update.
 * 
 * @author Darren Carlson
 */
public class PluginDiscoveryResult implements Serializable {
	// Private data
	private static final long serialVersionUID = -2336124008345879711L;
	private DiscoveredContextPlugin discoveredPlug;
	private ContextPlugin updateTarget;

	/**
	 * Creates an PluginDiscoveryResult that represents a new ContextPlugin that has not been previously installed.
	 * 
	 * @param discoveredPlug
	 *            The DiscoveredContextPlugin.
	 */
	public PluginDiscoveryResult(DiscoveredContextPlugin discoveredPlug) {
		this.discoveredPlug = discoveredPlug;
	}

	/**
	 * Creates an PluginDiscoveryResult that represents an update for an existing ContextPlugin.
	 * 
	 * @param discoveredPlug
	 *            The DiscoveredContextPlugin.
	 * @param updateTarget
	 *            The ContextPlugin to be updated.
	 */
	public PluginDiscoveryResult(DiscoveredContextPlugin discoveredPlug, ContextPlugin updateTarget) {
		this.discoveredPlug = discoveredPlug;
		this.updateTarget = updateTarget;
	}

	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		// Cast the incoming candidate to a PluginDiscoveryResult
		PluginDiscoveryResult other = (PluginDiscoveryResult) candidate;
		// Check if the candidate has an update target
		if (!hasUpdateTarget()) {
			// Check equality for new plug-ins
			if (other.hasUpdateTarget())
				// If we have an update target, we are not equal (since the candidate has one)
				return false;
			else
				// Check for equality of the discovered plug-ins
				return other.getDiscoveredPlugin().getContextPlugin()
						.equals(this.getDiscoveredPlugin().getContextPlugin());
		} else {
			// Check equality for updates
			if (!other.hasUpdateTarget())
				// If the candidate has an update target, we are not equal (since we have one)
				return false;
			else {
				// Check if the candidate's discovered plug-in is equal to ours
				if (other.getDiscoveredPlugin().getContextPlugin()
						.equals(this.getDiscoveredPlugin().getContextPlugin()))
					// Finally, check if the candidate's target plug-in is equal to ours
					return other.getTargetPlugin().equals(getTargetPlugin());
				else
					return false;
			}
		}
	}

	/**
	 * Returns the DiscoveredContextPlugin.
	 */
	public DiscoveredContextPlugin getDiscoveredPlugin() {
		return discoveredPlug;
	}

	/**
	 * Returns the target ContextPlugin, or null if this PluginDiscoveryResult represents a new ContextPlugin. Check by
	 * calling 'hasUpdateTarget()'.
	 */
	public ContextPlugin getTargetPlugin() {
		return updateTarget;
	}

	@Override
	public int hashCode() {
		return discoveredPlug.getContextPlugin().hashCode();
	}

	/**
	 * Returns true if this PluginDiscoveryResult is an update of an existing plug-in; false otherwise.
	 */
	public boolean hasUpdateTarget() {
		return (updateTarget != null) ? true : false;
	}

	/**
	 * Sets the DiscoveredContextPlugin.
	 */
	public void setContextPluginUpdate(DiscoveredContextPlugin discoveredPlug) {
		this.discoveredPlug = discoveredPlug;
	}

	/**
	 * Sets the update target ContextPlugin.
	 */
	public void setUpdateTarget(ContextPlugin target) {
		this.updateTarget = target;
	}

	@Override
	public String toString() {
		if (hasUpdateTarget())
			return "PluginDiscoveryResult: " + discoveredPlug.getContextPlugin() + " | Updates " + updateTarget;
		else
			return "PluginDiscoveryResult: " + discoveredPlug.getContextPlugin() + " | New Plugin";
	}
}