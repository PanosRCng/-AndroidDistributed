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
package org.ambientdynamix.contextplugins.WifiScanPlugin;

import java.util.HashSet;
import java.util.Set;

import org.ambientdynamix.api.application.IContextInfo;

import android.os.Parcel;
import android.os.Parcelable;

class WifiScanPluginInfo implements IContextInfo, IWifiScanPluginInfo {
	/**
	 * Required CREATOR field that generates instances of this Parcelable class from a Parcel.
	 * 
	 * @see http://developer.android.com/reference/android/os/Parcelable.Creator.html
	 */
	public static Parcelable.Creator<WifiScanPluginInfo> CREATOR = new Parcelable.Creator<WifiScanPluginInfo>() {
		/**
		 * Create a new instance of the Parcelable class, instantiating it from the given Parcel whose data had
		 * previously been written by Parcelable.writeToParcel().
		 */
		public WifiScanPluginInfo createFromParcel(Parcel in) {
			return new WifiScanPluginInfo(in);
		}

		/**
		 * Create a new array of the Parcelable class.
		 */
		public WifiScanPluginInfo[] newArray(int size) {
			return new WifiScanPluginInfo[size];
		}
	};
	// Public static variable for our supported context type
	public static String CONTEXT_TYPE = "org.ambientdynamix.contextplugins.WifiScanPlugin";
	private String state;
	private String bssid = "-1";
	
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
	
	public WifiScanPluginInfo(String bssid) {
		this.bssid = bssid;
	}

	@Override
	public String getScan()
	{
		return this.bssid;
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
		out.writeString(this.bssid);
		out.writeString(this.state);
	}

	/**
	 * Used by the Parcelable.Creator when reconstructing (deserializing) data sent over IPC.
	 */
	private WifiScanPluginInfo(final Parcel in)
	{
		this.bssid = in.readString();
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