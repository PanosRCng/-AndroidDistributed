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
package org.ambientdynamix.security;

import java.io.Serializable;

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;

/**
 * The abstract PrivacyPolicy class is the foundation for all concrete PrivacyPolicy entities installed in a Dynamix
 * Framework. A concrete PrivacyPolicy provides a set of mappings between a specific ContextPlugin and the maximum
 * PrivacyRiskLevel allowed to be sent to a given application for that ContextPlugin. PrivacyPolicy entities provide an
 * automated way to manage ContextPlugin privacy during runtime, when new ContextPlugins may be dynamically installed.
 * <p>
 * For example, a concrete PrivacyPolicy could be created that automatically sets the maximum allowed PrivacyRiskLevel
 * for all newly installed ContextPlugins to 'PrivacyRisk.NONE', so that no context information would be sent from the
 * ContextPlugin to applications without user intervention. In contrast, another PrivacyPolicy could be created that
 * automatically assigns 'PrivacyRisk.MAX' to all ContextPlugins that are digitally signed by a specific organization.
 * 
 * @see PrivacyRiskLevel
 * @see ContextPlugin
 * @author Darren Carlson
 */
public abstract class PrivacyPolicy implements Serializable {
	private static final long serialVersionUID = 1807370480828578982L;
	// Private data
	private String name;
	private String description;

	/**
	 * Creates a PrivacyPolicy using the specified name and description strings
	 */
	public PrivacyPolicy(String name, String description) {
		setName(name);
		setDescription(description);
	}

	/**
	 * This overrides equals() from java.lang.Object
	 */
	@Override
	public boolean equals(Object candidate) {
		// TODO: We should be careful here about testing for equality because we
		// simply rely on name
		if (candidate instanceof PrivacyPolicy) {
			PrivacyPolicy other = (PrivacyPolicy) candidate;
			return this.name.equalsIgnoreCase(other.name);
		}
		return false;
	}

	/**
	 * Returns a description of the PrivacyPolicy
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Abstract method that is implemented by concrete PrivacyPolicy classes. Used to get the max PrivacyRiskLevel for a
	 * specified ContextPlugin.
	 * 
	 * @param plugin
	 *            The ContextPlugin to determine a PrivacyRiskLevel for Returns a PrivacyRiskLevel for the specified
	 *            ContextPlugin
	 */
	public abstract PrivacyRiskLevel getMaxPrivacyRisk(ContextPlugin plugin);

	/**
	 * Returns the name of the PrivacyPolicy
	 */
	public String getName() {
		return name;
	}

	// HashCode Example: http://www.javafaq.nu/java-example-code-175.html
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + getName().hashCode();
		return result;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * Sets a description of the PrivacyPolicy
	 */
	protected void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the name of the PrivacyPolicy
	 */
	protected void setName(String name) {
		this.name = name;
	}
}