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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Details of a context support registration.
 * @author Darren Carlson
 *
 */
public class ContextSupportInfo implements Parcelable {
	/**
	 * Static Parcelable.Creator required to reconstruct a the object from an incoming Parcel.
	 */
	public static final Parcelable.Creator<ContextSupportInfo> CREATOR = new Parcelable.Creator<ContextSupportInfo>() {
		public ContextSupportInfo createFromParcel(Parcel in) {
			return new ContextSupportInfo(in);
		}

		public ContextSupportInfo[] newArray(int size) {
			return new ContextSupportInfo[size];
		}
	};
	// Private data
	private String contextType;
	private String supportId;
	private ContextPluginInformation plugin;

	/**
	 * Creates a ContextSupportInfo.
	 * @param supportId The context support's id.
	 * @param plugin The plug-in serving the context support registration.
	 * @param contextType The context type string of the context support registration.
	 */
	public ContextSupportInfo(String supportId, ContextPluginInformation plugin, String contextType) {
		this.contextType = contextType;
		this.supportId = supportId;
		this.plugin = plugin;
	}

	/**
	 * Returns the context type of the context support registration.
	 */
	public String getContextType() {
		return contextType;
	}
	/**
	 * Returns the id of this context support registration.
	 */
	public String getSupportId() {
		return supportId;
	}

	/**
	 * Returns the plug-in servicing this context support registration.
	 */
	public ContextPluginInformation getPlugin() {
		return plugin;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	private ContextSupportInfo(Parcel in) {
		this.contextType = in.readString();
		this.supportId = in.readString();
		this.plugin = in.readParcelable(getClass().getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(contextType);
		out.writeString(supportId);
		out.writeParcelable(plugin, flags);
	}
}
