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
package org.ambientdynamix.contextplugins.myExperimentPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ambientdynamix.api.application.IContextInfo;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

class ExperimentPluginInfo implements IContextInfo, IExperimentPluginInfo
{
	/**
	 * Required CREATOR field that generates instances of this Parcelable class from a Parcel.
	 * 
	 * @see http://developer.android.com/reference/android/os/Parcelable.Creator.html
	 */
	public static Parcelable.Creator<ExperimentPluginInfo> CREATOR = new Parcelable.Creator<ExperimentPluginInfo>()
	{
		/**
		 * Create a new instance of the Parcelable class, instantiating it from the given Parcel whose data had
		 * previously been written by Parcelable.writeToParcel().
		 */
		public ExperimentPluginInfo createFromParcel(Parcel in) {
			return new ExperimentPluginInfo(in);
		}

		/**
		 * Create a new array of the Parcelable class.
		 */
		public ExperimentPluginInfo[] newArray(int size) {
			return new ExperimentPluginInfo[size];
		}
	};

	public String CONTEXT_TYPE = "";
	private String state = "started";
	private Bundle data;
	List<String> dependencies = new ArrayList<String>();
		
	@Override
	public Set<String> getStringRepresentationFormats()
	{
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
	public void setContextType(String contextType)
	{
		this.CONTEXT_TYPE = contextType;
	}

	public ExperimentPluginInfo(String state)
	{
		this.state = state;
	}
	
	@Override
	public String getState() {
		return state;
	}

	@Override
	public Bundle getData()
	{
		return this.data;
	}
	
	@Override
	public void setData(Bundle data)
	{
		this.data = data;
	}
	
	@Override
	public List<String> getDependencies()
	{
		return dependencies;
	}
	
	@Override
	public void setDependencies(List<String> dependencies)
	{
		this.dependencies = dependencies;
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
		out.writeString(this.CONTEXT_TYPE);
		out.writeStringList(this.dependencies);
		out.writeString(this.state);
		out.writeBundle(this.data);
	}


	private ExperimentPluginInfo(final Parcel in)
	{
		this.CONTEXT_TYPE = in.readString();
		in.readStringList(this.dependencies);
		this.state = in.readString();
		this.data = in.readBundle();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

}