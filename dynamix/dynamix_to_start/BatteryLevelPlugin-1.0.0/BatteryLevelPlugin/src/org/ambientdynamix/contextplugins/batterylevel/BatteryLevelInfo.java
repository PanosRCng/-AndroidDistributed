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
package org.ambientdynamix.contextplugins.batterylevel;

import java.util.HashSet;
import java.util.Set;

import org.ambientdynamix.api.application.IContextInfo;

import android.os.Parcel;
import android.os.Parcelable;

class BatteryLevelInfo implements IContextInfo, IBatteryLevelInfo {
	/**
	 * Required CREATOR field that generates instances of this Parcelable class from a Parcel.
	 * 
	 * @see http://developer.android.com/reference/android/os/Parcelable.Creator.html
	 */
	public static Parcelable.Creator<BatteryLevelInfo> CREATOR = new Parcelable.Creator<BatteryLevelInfo>() {
		/**
		 * Create a new instance of the Parcelable class, instantiating it from the given Parcel whose data had
		 * previously been written by Parcelable.writeToParcel().
		 */
		public BatteryLevelInfo createFromParcel(Parcel in) {
			return new BatteryLevelInfo(in);
		}

		/**
		 * Create a new array of the Parcelable class.
		 */
		public BatteryLevelInfo[] newArray(int size) {
			return new BatteryLevelInfo[size];
		}
	};
	// Public static variable for our supported context type
	public static String CONTEXT_TYPE = "org.ambientdynamix.contextplugins.batterylevel";
	// private variable for battery level (as a %)
	private double batteryLevel;

	@Override
	public Set<String> getStringRepresentationFormats() {
		Set<String> formats = new HashSet<String>();
		formats.add("text/plain");
		formats.add("dynamix/web");
		return formats;
	}

	@Override
	public String getStringRepresentation(String format) {
		if (format.equalsIgnoreCase("text/plain"))
			return "Battery level: " + batteryLevel;
		else if (format.equalsIgnoreCase("dynamix/web"))
			return "batteryLevel=" + getBatteryLevel();
		else
			// Format not supported, so return an empty string
			return "";
	}

	@Override
	public String getImplementingClassname() {
		return this.getClass().getName();
	}

	@Override
	public String getContextType() {
		return CONTEXT_TYPE;
	}

	/**
	 * Createa a BatteryLevelInfo
	 * 
	 * @param batteryLevel
	 *            The device's detected battery level as a percentage of 100.
	 */
	public BatteryLevelInfo(double batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	/**
	 * Returns the device's detected battery level as a percentage of 100.
	 */
	@Override
	public double getBatteryLevel() {
		return batteryLevel;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	};

	/**
	 * Used by Parcelable when sending (serializing) data over IPC.
	 */
	public void writeToParcel(Parcel out, int flags) {
		out.writeDouble(this.batteryLevel);
	}

	/**
	 * Used by the Parcelable.Creator when reconstructing (deserializing) data sent over IPC.
	 */
	private BatteryLevelInfo(final Parcel in) {
		this.batteryLevel = in.readDouble();
	}

	/**
	 * Default implementation that returns 0.
	 * 
	 * @return 0
	 */
	@Override
	public int describeContents() {
		return 0;
	}
}