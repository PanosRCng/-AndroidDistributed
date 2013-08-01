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

/**
 * Utility class that associates descriptive text with an icon id.
 * 
 * @author Darren Carlson
 */
public class DescriptiveIcon {
	// Private data
	private int iconResId;
	private String statusText;

	/**
	 * Creates a DescriptiveIcon using the specified icon id and status text.
	 */
	public DescriptiveIcon(int iconResId, String statusText) {
		this.iconResId = iconResId;
		this.statusText = statusText;
	}

	/**
	 * Returns the icon's id.
	 */
	public int getIconResId() {
		return iconResId;
	}

	/**
	 * Returns the status text.
	 */
	public String getStatusText() {
		return statusText;
	}
}