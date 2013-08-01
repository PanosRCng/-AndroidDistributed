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
 * Represents a notification that can be displayed in the Android status tray and managed by the Dynamix Framework. 
 * AndroidNotifications target a specific tabID, allowing the correct tab to be focused in the UI when the notification
 * is clicked by the user.
 * 
 * @author Darren Carlson
 */
import java.io.Serializable;

import android.content.Intent;

public class AndroidNotification implements Serializable {
	// Private data
	private static final long serialVersionUID = -4636784405006579473L;
	// For some reason, low ids are not clearable... starting higher
	private static int id = 1337;
	private int iconID;
	private String description;
	private int notificationID;
	private Type type;
	private int tabID;
	
	public enum Type{
		PENDING_APP, PENDING_PLUGIN, PLUGIN_UPDATE, FRAMEWORK_UPDATE, PLUGIN_ALERT
	}

	/**
	 * Creates a new AndroidNotification.
	 * 
	 * @param tabID
	 *            The tab identifier for the notification.
	 * @param type
	 *            The NotificationType.
	 * @param iconID
	 *            An icon identifier.
	 * @param description
	 *            A description of the notification.
	 */
	public AndroidNotification(int tabID, Type type, int iconID, String description) {
		this.notificationID = id++;
		this.iconID = iconID;
		this.type = type;
		this.description = description;
		this.tabID = tabID;
	}

	public AndroidNotification(int tabID, Type type, int iconID, String description,
			Intent launchMe) {
		this.notificationID = id++;
		this.iconID = iconID;
		this.type = type;
		this.description = description;
		this.tabID = tabID;
	}

	/**
	 * Returns the notification description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the icon id.
	 */
	public int getIconID() {
		return iconID;
	}

	/**
	 * Returns the tab id.
	 */
	public int getTabID() {
		return tabID;
	}

	/**
	 * Returns the type of notification.
	 */
	public Type getType() {
		return type;
	}
	
	public int getNotificationId(){
		return this.notificationID;
	}

	@Override
	public String toString() {
		return "AndroidNotification: ID = " + this.notificationID;
	}
	
//	@Override
//	public boolean equals(Object candidate) {
//		// First determine if they are the same object reference
//		if (this == candidate)
//			return true;
//		// Make sure they are the same class
//		if (candidate == null || candidate.getClass() != getClass())
//			return false;
//		// Ok, they are the same class... check if their id's are the same
//		DynamixApplication other = (DynamixApplication) candidate;
//		return other.getAppID() == this.getAppID() ? true : false;
//	}
}