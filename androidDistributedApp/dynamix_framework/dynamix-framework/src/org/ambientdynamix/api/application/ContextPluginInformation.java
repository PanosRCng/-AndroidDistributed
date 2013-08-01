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

import java.util.List;
import java.util.Vector;

import org.ambientdynamix.api.application.AppConstants.ContextPluginType;
import org.ambientdynamix.api.application.AppConstants.PluginInstallStatus;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Provides immutable information about a ContextPlugin, including the plug-in's supported context types.
 * 
 * @author Darren Carlson
 */
public class ContextPluginInformation implements Parcelable {
	/**
	 * Static Parcelable.Creator required to reconstruct a the object from an incoming Parcel.
	 */
	public static final Parcelable.Creator<ContextPluginInformation> CREATOR = new Parcelable.Creator<ContextPluginInformation>() {
		public ContextPluginInformation createFromParcel(Parcel in) {
			return new ContextPluginInformation(in);
		}

		public ContextPluginInformation[] newArray(int size) {
			return new ContextPluginInformation[size];
		}
	};
	// Private variables
	private final String TAG = this.getClass().getSimpleName();
	private String pluginId;
	private String pluginName;
	private String pluginDescription;
	private VersionInfo version;
	private List<String> supportedContextTypes = new Vector<String>();
	private int statusInt;
	private int plugTypeInt;
	private String requiresConfiguration;
	private String isConfigured;
	private String isEnabled;

	/**
	 * Creates a new ContextPluginInformation.
	 * 
	 * @param pluginId
	 *            The ID of the plug-in.
	 * @param pluginName
	 *            The name of the plug-in.
	 * @param pluginDescription
	 *            The description of the plug-in.
	 * @param version
	 *            The version of the plug-in.
	 * @param userControlledContextAcquisition
	 *            Whether or not this plug-in requires user controlled context acquisition (e.g. through a GUI).
	 * @param supportedContextTypes
	 *            A list of supported context types.
	 * @param plugType
	 *            The plug-in type
	 */
	public ContextPluginInformation(String pluginId, String pluginName, String pluginDescription,
			VersionInfo versionInfo, List<String> supportedContextTypes, PluginInstallStatus status,
			ContextPluginType plugType, boolean requiresConfiguration, boolean isConfigured, boolean isEnabled) {
		if (pluginId == null || pluginName == null || pluginDescription == null || versionInfo == null
				|| supportedContextTypes == null)
			throw new RuntimeException("Null in ContextPluginInformation constructor!");
		this.pluginId = pluginId;
		this.pluginName = pluginName;
		this.pluginDescription = pluginDescription;
		this.version = versionInfo;
		this.requiresConfiguration = requiresConfiguration ? "true" : "false";
		this.isConfigured = isConfigured ? "true" : "false";
		this.isEnabled = isEnabled ? "true" : "false";
		if (supportedContextTypes != null)
			this.supportedContextTypes = supportedContextTypes;
		switch (status) {
		case INSTALLED:
			statusInt = 3;
			break;
		case INSTALLING:
			statusInt = 2;
			break;
		case NOT_INSTALLED:
			statusInt = 1;
			break;
		case ERROR:
			statusInt = 0;
			break;
		}
		switch (plugType) {
		case AUTO:
			plugTypeInt = 5;
			break;
		case REACTIVE:
			plugTypeInt = 4;
			break;
		case INTERACTIVE:
			plugTypeInt = 3;
			break;
		case AUTO_REACTIVE:
			plugTypeInt = 2;
			break;
		case AUTO_INTERACTIVE:
			plugTypeInt = 1;
			break;
		case AUTO_REACTIVE_INTERACTIVE:
			plugTypeInt = 0;
			break;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Returns the context plug-in's type.
	 * 
	 * @see AppConstants.ContextPluginType
	 */
	public ContextPluginType getContextPluginType() {
		switch (plugTypeInt) {
		case 5:
			return ContextPluginType.AUTO;
		case 4:
			return ContextPluginType.REACTIVE;
		case 3:
			return ContextPluginType.INTERACTIVE;
		case 2:
			return ContextPluginType.AUTO_REACTIVE;
		case 1:
			return ContextPluginType.AUTO_INTERACTIVE;
		case 0:
			return ContextPluginType.AUTO_REACTIVE_INTERACTIVE;
		}
		Log.e(TAG, "getContextPluginType had invalid status int: " + statusInt);
		return null;
	}

	/**
	 * Returns the plug-in's install status.
	 * 
	 * @see AppConstants.PluginInstallStatus
	 */
	public PluginInstallStatus getInstallStatus() {
		switch (statusInt) {
		case 3:
			return PluginInstallStatus.INSTALLED;
		case 2:
			return PluginInstallStatus.INSTALLING;
		case 1:
			return PluginInstallStatus.NOT_INSTALLED;
		case 0:
			return PluginInstallStatus.ERROR;
		}
		Log.e(TAG, "getInstallStatus had invalid status int: " + statusInt);
		return null;
	}

	/**
	 * Returns the description of the plugin.
	 */
	public String getPluginDescription() {
		return pluginDescription;
	}

	/**
	 * Returns the string-based id of the plugin.
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * Returns the name of the plugin.
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * Returns the list of supported context types.
	 */
	public List<String> getSupportedContextTypes() {
		return supportedContextTypes;
	}

	/**
	 * Returns the version of the plugin.
	 * 
	 * @see VersionInfo
	 */
	public VersionInfo getVersion() {
		return version;
	}

	/**
	 * Returns true if this plugin requires configuration; false otherwise.
	 */
	public boolean getRequiresConfiguration() {
		return this.requiresConfiguration.equalsIgnoreCase("true");
	}

	/**
	 * Returns true if this plugin is configured; false otherwise.
	 */
	public boolean isConfigured() {
		return this.isConfigured.equalsIgnoreCase("true");
	}

	/**
	 * Returns true if this plugin is enabled; false otherwise.
	 */
	public boolean isEnabled() {
		return this.isEnabled.equalsIgnoreCase("true");
	}

	@Override
	public String toString() {
		return this.pluginName + " " + this.version + " | " + this.pluginId;
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
		ContextPluginInformation other = (ContextPluginInformation) candidate;
		if (this.getPluginId().equalsIgnoreCase(other.getPluginId())
				&& this.getVersion().equals(other.getVersion()))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + getPluginId().hashCode() + getVersion().hashCode();
		return result;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.pluginId);
		dest.writeString(this.pluginName);
		dest.writeString(this.pluginDescription);
		dest.writeParcelable(version, flags);
		dest.writeStringList(this.supportedContextTypes);
		dest.writeInt(statusInt);
		dest.writeInt(plugTypeInt);
		dest.writeString(this.requiresConfiguration);
		dest.writeString(this.isConfigured);
		dest.writeString(this.isEnabled);
	}

	/*
	 * Private constructor (required for the static Parcelable.Creator method)
	 */
	private ContextPluginInformation(Parcel in) {
		this.pluginId = in.readString();
		this.pluginName = in.readString();
		this.pluginDescription = in.readString();
		this.version = in.readParcelable(getClass().getClassLoader());
		in.readStringList(this.supportedContextTypes);
		this.statusInt = in.readInt();
		this.plugTypeInt = in.readInt();
		this.requiresConfiguration = in.readString();
		this.isConfigured = in.readString();
		this.isEnabled = in.readString();
	}
}