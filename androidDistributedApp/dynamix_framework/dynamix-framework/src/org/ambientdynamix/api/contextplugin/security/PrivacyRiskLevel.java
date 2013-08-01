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
package org.ambientdynamix.api.contextplugin.security;

import java.io.Serializable;

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.IContextInfo;

import android.util.Log;

/**
 * The PrivacyRiskLevel class provides a mechanism for describing the privacy risk level of the IContextInfo present
 * within a ContextEvent. The notion of 'risk' is specific to a given context modeling domain and must be specified by
 * the context domain expert developing a given ContextPlugin and related ContextPluginRuntime. See the plug-in
 * development guide for details on properly mapping privacy risk levels.
 * 
 * @see ContextEvent
 * @see IContextInfo
 * @see SecuredContextInfo
 * @author Darren Carlson
 */
public final class PrivacyRiskLevel implements Comparable<PrivacyRiskLevel>, Serializable {
	private static final String TAG = PrivacyRiskLevel.class.getSimpleName();
	private static final long serialVersionUID = -8435741162469465365L;
	/** No context information given */
	public static final PrivacyRiskLevel NONE = new PrivacyRiskLevel(1, "NONE", "Blocked");
	/** Flag specifying Context information of low risk level */
	public static final PrivacyRiskLevel LOW = new PrivacyRiskLevel(2, "LOW", "Low Privacy Risk");
	// information given
	/** Flag specifying Context information of medium risk level */
	public static final PrivacyRiskLevel MEDIUM = new PrivacyRiskLevel(3, "MEDIUM", "Medium Privacy Risk");
	// given
	/** Flag specifying Context information of high risk level */
	public static final PrivacyRiskLevel HIGH = new PrivacyRiskLevel(4, "HIGH", "High Privacy Risk");
	/** Flag specifying Context information of maximum risk level */
	public static final PrivacyRiskLevel MAX = new PrivacyRiskLevel(5, "MAX", "Maximum Privacy Risk");
	// Private data
	private int id;
	private String name;
	private String friendlyName;
	private static PrivacyRiskLevel[] levels = new PrivacyRiskLevel[] { NONE, LOW, MEDIUM, HIGH, MAX };

	/*
	 * Singleton constructor
	 */
	private PrivacyRiskLevel(int sortOrder, String name, String friendlyName) {
		this.id = sortOrder;
		this.name = name;
		this.friendlyName = friendlyName;
	}

	/**
	 * Returns an ordered array of PrivacyRisk, arranged from low to high privacy risk level.
	 */
	public static PrivacyRiskLevel[] getAllPrivacyRiskLevels() {
		return levels;
	}

	/**
	 * Returns a PrivacyRiskLevel for the specified id.
	 * 
	 * @param id
	 *            A valid PrivacyRiskLevel integer id Returns a PrivacyRiskLevel for a given id, or null if no
	 *            PrivacyRiskLevel can be found for the id.
	 */
	public static PrivacyRiskLevel getLevelForID(int id) {
		for (PrivacyRiskLevel l : levels) {
			if (l.id == id)
				return l;
		}
		Log.w(TAG, "No privacy risk level found for id: " + id);
		return null;
	}

	/**
	 * Returns a PrivacyRiskLevel for the specified string. Allowed strings are: NONE, LOW, MEDIUM, HIGH, MAX.
	 * 
	 * @param level
	 *            The string version of the PrivacyRiskLevel Returns a PrivacyRiskLevel for a given string, or null if
	 *            no PrivacyRiskLevel can be found
	 */
	public static PrivacyRiskLevel getLevelForString(String level) {
		// MAX
		if (level != null) {
			level = level.trim();
			for (PrivacyRiskLevel l : levels) {
				if (l.name.equalsIgnoreCase(level))
					return l;
			}
		} else
			Log.w(TAG, "level was null!");
		Log.w(TAG, "No privacy risk level found for: " + level);
		return null;
	}

	@Override
	public int compareTo(PrivacyRiskLevel candidate) {
		if (candidate != null) {
			int o1 = this.getID();
			int o2 = candidate.getID();
			if (o1 > o2)
				return 1;
			else if (o1 < o2)
				return -1;
			else
				return 0;
		} else
			throw new RuntimeException("compareTo received NULL candidate");
	}

	/**
	 * This overrides equals() from java.lang.Object
	 */
	@Override
	public boolean equals(Object candidate) {
		// First determine if they are the same object reference
		if (this == candidate)
			return true;
		// Make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		PrivacyRiskLevel other = (PrivacyRiskLevel) candidate;
		return other.id == this.id ? true : false;
	}

	/**
	 * Returns the friendly name (displayable string) for this PrivacyRiskLevel
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * Returns the identifier for this PrivacyRiskLevel
	 */
	public int getID() {
		return id;
	}

	/**
	 * Returns the name of this PrivacyRiskLevel
	 */
	public String getName() {
		return name;
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
		return getFriendlyName();
	}
}