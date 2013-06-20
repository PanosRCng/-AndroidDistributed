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
package org.ambientdynamix.contextplugins.batteryTemperaturePlugin;

import java.util.HashSet;
import java.util.Set;

import org.ambientdynamix.api.application.IContextInfo;

import android.os.Parcel;
import android.os.Parcelable;

class BatteryTemperaturePluginInfo implements IContextInfo, IBatteryTemperaturePluginInfo {
	/**
	 * Required CREATOR field that generates instances of this Parcelable class from a Parcel.
	 * 
	 * @see http://developer.android.com/reference/android/os/Parcelable.Creator.html
	 */
	public static Parcelable.Creator<BatteryTemperaturePluginInfo> CREATOR = new Parcelable.Creator<BatteryTemperaturePluginInfo>() {
		/**
		 * Create a new instance of the Parcelable class, instantiating it from the given Parcel whose data had
		 * previously been written by Parcelable.writeToParcel().
		 */
		public BatteryTemperaturePluginInfo createFromParcel(Parcel in) {
			return new BatteryTemperaturePluginInfo(in);
		}

		/**
		 * Create a new array of the Parcelable class.
		 */
		public BatteryTemperaturePluginInfo[] newArray(int size) {
			return new BatteryTemperaturePluginInfo[size];
		}
	};
	// Public static variable for our supported context type
	public static String CONTEXT_TYPE = "org.ambientdynamix.contextplugins.batteryTemperaturePlugin";
	private String state;
	private int temperature = 0;
	
	@Override
	public Set<String> getStringRepresentationFormats() {
		Set<String> formats = new HashSet<String>();
		formats.add("text/plain");
		formats.add("dynamix/web");
		return formats;
	}

	@Override
	public String getStringRepresentation(String format)
	{
		if (format.equalsIgnoreCase("text/plain"))
		{	
				return CONTEXT_TYPE + "=" + getState();
		}
		else if (format.equalsIgnoreCase("dynamix/web"))
		{
			return CONTEXT_TYPE + "=" + getState();
		}
		else
		{
			// Format not supported, so return an empty string
			return "";
		}
	}

	@Override
	public String getImplementingClassname() {
		return this.getClass().getName();
	}

	@Override
	public String getContextType() {
		return CONTEXT_TYPE;
	}

	@Override
	public String getState()
	{
		return this.state;
	}
	
	@Override
	public void setState(String state)
	{
		this.state = state;
	}
	
	public BatteryTemperaturePluginInfo(int temperature) {
		this.temperature = temperature;
	}

	@Override
	public int getTemperature()
	{
		return temperature;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	};

	/**
	 * Used by Parcelable when sending (serializing) data over IPC.
	 */
	public void writeToParcel(Parcel out, int flags)
	{
		out.writeInt(this.temperature);
		out.writeString(this.state);
	}

	/**
	 * Used by the Parcelable.Creator when reconstructing (deserializing) data sent over IPC.
	 */
	private BatteryTemperaturePluginInfo(final Parcel in)
	{
		this.temperature = in.readInt();
		this.state = in.readString();
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