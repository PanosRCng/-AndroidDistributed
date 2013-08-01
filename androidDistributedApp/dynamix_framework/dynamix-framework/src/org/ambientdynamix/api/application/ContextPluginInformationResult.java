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

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Result class that provides a List of ContextPluginInformation.
 * @author Darren Carlson
 *
 */
public class ContextPluginInformationResult extends Result implements Parcelable {
	
	/**
	 * Static Parcelable.Creator required to reconstruct a the object from an incoming Parcel.
	 */
	public static final Parcelable.Creator<ContextPluginInformationResult> CREATOR = new Parcelable.Creator<ContextPluginInformationResult>() {
		public ContextPluginInformationResult createFromParcel(Parcel in) {
			return new ContextPluginInformationResult(in);
		}

		public ContextPluginInformationResult[] newArray(int size) {
			return new ContextPluginInformationResult[size];
		}
	};
	// Private data
	private List<ContextPluginInformation> plugInfo = new ArrayList<ContextPluginInformation>();


	/**
	 * Creates a successful result using the incoming ContextPluginInformation.
	 * @param plugInfo The ContextPluginInformation associated with this Result.
	 */
	public ContextPluginInformationResult(ContextPluginInformation plugInfo) {
		this.plugInfo.add(plugInfo);
	}
	
	/**
	 * Creates a successful result using the incoming List of ContextPluginInformation.
	 * @param plugInfo The ContextPluginInformation associated with this Result.
	 */
	public ContextPluginInformationResult(List<ContextPluginInformation> plugInfo) {
		this.plugInfo = plugInfo;
	}

	/**
	 * Constructor that creates a Result with status "success = false".
	 * 
	 * @param errorMessage
	 *            The message associated with the error.
	 * @param errorCode
	 *            The status code associated with the error. @see org,ambientdynamix.api.application.ErrorCodes
	 */
	public ContextPluginInformationResult(String errorMessage, int errorCode){
		super(errorMessage, errorCode);
	}

	/**
	 * Returns the ContextPluginInformation associated with this Result.
	 */
	public List<ContextPluginInformation> getContextPluginInformation() {
		return plugInfo;
	}

	private ContextPluginInformationResult(Parcel in) {
		super(in);
		in.readList(plugInfo, getClass().getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeList(plugInfo);
	}
}
