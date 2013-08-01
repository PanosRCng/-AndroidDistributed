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
package org.ambientdynamix.event;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ambientdynamix.api.contextplugin.ContextInfoSet;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.os.Parcel;
import android.util.Log;

/**
 * Adapter that adds event source ContextPluginInformation to a ContextInfoSet.
 * 
 * @author Darren Carlson
 */
public class SourcedContextInfoSet {
	// Private data
	private final String TAG = getClass().getSimpleName();
	private ContextPlugin eventSource;
	private ContextInfoSet ciSet;
	private Map<SecuredContextInfo, Integer> contextInfoMap;
	private int size;
	private boolean sizeCalculated;

	/**
	 * Creates a SourcedContextInfoSet.
	 * 
	 * @param infoSet
	 *            The ContextInfoSet
	 * @param eventSource
	 *            The ContextPluginInformation event source
	 * @throws Exception
	 */
	public SourcedContextInfoSet(ContextInfoSet infoSet, ContextPlugin eventSource, boolean calculateSize)
			throws Exception {
		this.ciSet = infoSet;
		this.eventSource = eventSource;
		this.contextInfoMap = new HashMap<SecuredContextInfo, Integer>();
		// Calculate the size of each IContextInfo
		for (SecuredContextInfo si : infoSet.getSecuredContextInfo()) {
			if (calculateSize) {
				Parcel p = Parcel.obtain();
				p.writeParcelable(si.getContextInfo(), 0);
				contextInfoMap.put(si, p.dataSize());
				size += p.dataSize();
				p.recycle();
			} else
				contextInfoMap.put(si, 0);
		}
	}

	public Set<SecuredContextInfo> getSecuredContextInfo() {
		return this.contextInfoMap.keySet();
	}

	public long getTotalIContextInfoBytes() {
		return size;
	}

	public int getSecuredContextInfoByteSize(SecuredContextInfo si) {
		if (contextInfoMap.containsKey(si))
			return contextInfoMap.get(si);
		else {
			Log.w(TAG, "SecuredContextInfo not found");
			return 0;
		}
	}

	/**
	 * Returns true if the underlying ContextInfoSet expires; false otherwise.
	 */
	public boolean expires() {
		return this.ciSet.expires();
	}

	/**
	 * Returns the ContextInfoSet.
	 */
	public ContextInfoSet getContextInfoSet() {
		return ciSet;
	}

	/**
	 * Returns the context type of the underlying IContextInfo entities (each will be of the same type).
	 */
	public String getContextType() {
		return this.ciSet.getContextType();
	}

	/**
	 * Returns the ContextPlugin that generated the event.
	 */
	public ContextPlugin getEventSource() {
		return this.eventSource;
	}

	/**
	 * Returns the expiration mills of the underlying ContextInfoSet.
	 */
	public int getExireMills() {
		return this.ciSet.getExpireMills();
	}

	/**
	 * Returns the expiration time of the underlying ContextInfoSet.
	 */
	public Date getExpireTime() {
		return this.ciSet.getExpireTime();
	}

	/**
	 * Returns the implementing class of the underlying ContextInfoSet.
	 */
	public String getImplementingClassname() {
		return this.ciSet.getImplementingClassname();
	}

	/**
	 * Returns the List of SecuredContextInfo from the underlying ContextInfoSet.
	 */
	public List<SecuredContextInfo> getSecureContextInfoList() {
		return this.ciSet.getSecuredContextInfo();
	}

	/**
	 * Returns the creation timestamp of the underlying ContextInfoSet.
	 */
	public Date getTimestamp() {
		return this.ciSet.getTimeStamp();
	}

	/**
	 * Sets the ContextInfoSet.
	 */
	public void setEventSet(ContextInfoSet eventSet) {
		this.ciSet = eventSet;
	}

	/**
	 * Sets the ContextPlugin that generated the event.
	 */
	public void setEventSource(ContextPlugin eventSource) {
		this.eventSource = eventSource;
	}
}