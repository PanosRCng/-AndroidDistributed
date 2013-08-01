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
 * Represents an Android feature that is requested (or required) by a given plug-in.
 * 
 * @author Darren Carlson
 */
public class DynamixFeatureInfo implements Serializable {
	// Private data
	private static final long serialVersionUID = 1826414673043791340L;
	private String name;
	private boolean required;

	/**
	 * Creates a DynamixFeatureInfo with required = true
	 */
	public DynamixFeatureInfo() {
		required = true;
	}

	/**
	 * Creates a DynamixFeatureInfo
	 */
	public DynamixFeatureInfo(String name, boolean required) {
		this.name = name;
		this.required = required;
	}

	/**
	 * Returns the feature name as a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns true if this feature is required; false if it's optional.
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Sets the feature name as a String
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set true if this feature is required; false if it's optional.
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}
}
