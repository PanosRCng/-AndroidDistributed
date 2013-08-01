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

/**
 * Constants for the Plug-in API.
 * 
 * @author Darren Carlson
 */
public final class PluginConstants {
	// Singleton constructor
	private PluginConstants() {
	}
	
	public enum WEB_ENCODING{
		NONE,
		JSON;
	}

	// Supported formats for auto web encoding
	public static final String NO_WEB_ENCODING = "NO_WEB_ENCODING";
	public static final String JSON_WEB_ENCODING = "application/json";

	/**
	 * Event type constants
	 */
	public enum EventType {
		BROADCAST, UNICAST, LOGGING
	}

	/**
	 * Logging priority constants
	 */
	public enum LogPriority {
		ASSERT, VERBOSE, DEBUG, INFO, WARN, ERROR
	}

	/**
	 * Device platform constants
	 */
	public enum PLATFORM {
		ANDROID("android");
		private String name;
		private static PLATFORM[] platforms = new PLATFORM[] { ANDROID };

		PLATFORM(String name) {
			this.name = name;
		}

		public static PLATFORM getPlatformFromString(String platformString) throws Exception {
			if (platformString != null) {
				for (PLATFORM p : platforms) {
					if (p.name.compareToIgnoreCase(platformString.trim()) == 0)
						return p;
				}
			} else
				throw new Exception("Platform string cannot be null");
			throw new Exception("Could not find PLATFORM for string: " + platformString);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Update priority values.
	 */
	public enum UpdatePriority {
		OPTIONAL, NORMAL, IMPORTANT, CRITICAL, MANDATORY
	}
}
