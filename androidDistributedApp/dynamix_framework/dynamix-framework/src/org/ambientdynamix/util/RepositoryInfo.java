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

import java.io.Serializable;

import android.util.Log;

/**
 * Generalized model of a repository, including alias, url and type.
 * 
 * @author Darren Carlson
 */
public class RepositoryInfo implements Serializable {
	public static String SIMPLE_FILE_SOURCE = "SIMPLE_FILE_SOURCE";
	public static String SIMPLE_NETWORK_SOURCE = "SIMPLE_NETWORK_SOURCE";
	public static String NEXUS_LUCENE_SOURCE = "NEXUS_LUCENE_SOURCE";
	public static String NEXUS_INDEX_SOURCE = "NEXUS_INDEX_SOURCE";
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private String alias;
	private String url;
	private String type;

	/**
	 * Creates a default RepositoryInfo
	 */
	public RepositoryInfo() {
	}

	/**
	 * Creates a RepositoryInfo with all available options.
	 */
	public RepositoryInfo(String alias, String url, String type) {
		this.alias = alias;
		this.url = url;
		this.type = type;
	}

	/**
	 * Returns the type of the server.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of the server.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the alias of the server (i.e. a descriptive server name used for display to the user).
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Returns the URL of the server.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the alias of the server (i.e. a descriptive server name used for display to the user).
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Sets the URL of the server.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Returns true if the RepositoryInfo contains no errors; false otherwise.
	 */
	public boolean validateServerInfo() {
		if (alias != null && alias.length() > 0) {
			if (url != null) {
				// We're simply returning true here, since urls may be local
				return true;
				// try {
				// URL test = new URL(url);
				// return true;
				// } catch (MalformedURLException e) {
				// Log.w(TAG, "Invalid URL: " + url);
				// }
			} else
				Log.w(TAG, "Missing URL");
		} else
			Log.w(TAG, "Missing Alias");
		return false;
	}

	@Override
	public boolean equals(Object candidate) {
		// Check for null
		if (candidate == null)
			return false;
		// Determine if they are the same object reference
		if (this == candidate)
			return true;
		// Make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		// Make sure the id's and version numbers are the same
		RepositoryInfo other = (RepositoryInfo) candidate;
		if (other.getUrl().equalsIgnoreCase(this.getUrl()))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + getUrl().hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getUrl();
	}
}