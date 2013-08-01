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
package org.ambientdynamix.api.application;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Provides generic versioning information using a 'major', 'minor', 'micro' scheme. The major version number indicate
 * non-backwards compatible version changes. The minor version number indicates backwards compatible changes. The micro
 * version number indicates bug fixes.
 * 
 * @see <a href="http://semver.org/">Semantic Versioning</a>
 * 
 * @author Darren Carlson
 */
public final class VersionInfo implements Parcelable, Comparable<VersionInfo>, Serializable {
	private static final long serialVersionUID = -4833433156542436517L;
	private final static String TAG = VersionInfo.class.getSimpleName();
	/**
	 * Static Parcelable Creator required to reconstruct a the object from an incoming Parcel
	 */
	public static final Parcelable.Creator<VersionInfo> CREATOR = new Parcelable.Creator<VersionInfo>() {
		public VersionInfo createFromParcel(Parcel in) {
			return new VersionInfo(in);
		}

		public VersionInfo[] newArray(int size) {
			return new VersionInfo[size];
		}
	};
	// Private data
	private int major = 0, minor = 0, micro = 0;

	/**
	 * Creates a VersionInfo from integer values according to standard major, minor, and micro version designations.
	 */
	public VersionInfo(int major, int minor, int micro) {
		this.major = major;
		this.minor = minor;
		this.micro = micro;
	}

	/*
	 * Private constructor (required for the static Parcelable.Creator method)
	 */
	private VersionInfo(Parcel in) {
		this.major = in.readInt();
		this.minor = in.readInt();
		this.micro = in.readInt();
	}

	/**
	 * Parses the incoming string to produce a VersionInfo. The string must be in the form "<major>.<minor>.<micro>".
	 * trailing values are assumed to be zero if they are not provided, meaning that 2 would be interpreted as 2.0.0 and
	 * 3.5 would be interpretated 3.5.0.
	 * 
	 * @param versionString
	 *            The string to interpret
	 * @return A valid VersionInfo; or null if the versionString is malformed
	 */
	public static VersionInfo createVersionInfo(String versionString) {
		try {
			int minor = 0;
			int micro = 0;
			String[] values = versionString.split("\\.");
			int major = Integer.parseInt(values[0]);
			if (values.length > 1)
				minor = Integer.parseInt(values[1]);
			if (values.length > 2)
				micro = Integer.parseInt(values[2]);
			return new VersionInfo(major, minor, micro);
		} catch (NumberFormatException e) {
			Log.w(TAG, e.getMessage());
		}
		return null;
	}

	@Override
	public int compareTo(VersionInfo other) {
		if (this.major > other.major)
			return 1;
		else if (this.major < other.major)
			return -1;
		else {
			// Same major version - try comparing minor version
			if (this.minor > other.minor)
				return 1;
			else if (this.minor < other.minor)
				return -1;
			else {
				// Same minor version - try comparing micro version
				if (this.micro > other.micro)
					return 1;
				else if (this.micro < other.micro)
					return -1;
				else {
					// Same version down to the micro version
					return 0;
				}
			}
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		// Deep compare
		VersionInfo other = (VersionInfo) candidate;
		if (other.major == this.major && other.minor == this.minor && other.micro == this.micro)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + new Integer(major).hashCode() + new Integer(minor).hashCode()
				+ new Integer(micro).hashCode();
		return result;
	}

	/**
	 * Returns the major version number; indicates non-backwards compatible version changes.
	 * 
	 * @see <a href="http://semver.org/">Semantic Versioning</a>
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * Returns the minor version number; increments denote backwards compatible changes.
	 * 
	 * @see <a href="http://semver.org/">Semantic Versioning</a>
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * Returns the micro version number; indicates bug fixes.
	 * 
	 * @see <a href="http://semver.org/">Semantic Versioning</a>
	 */
	public int getMicro() {
		return micro;
	}
	
	/**
	 * Used to provide JavaBean access to full string value (mostly for JSON encoding).
	 * @return A string version of the version, including major, minor and micro.
	 */
	public String getValue(){
		return this.toString();
	}

	@Override
	public String toString() {
		return String.valueOf(major) + "." + String.valueOf(minor) + "." + String.valueOf(micro);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(major);
		dest.writeInt(minor);
		dest.writeInt(micro);
	}
}