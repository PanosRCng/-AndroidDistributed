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
package org.ambientdynamix.core;

import java.util.UUID;

import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.contextplugin.ContextPlugin;

/**
 * Represents context support that is associated with a Dynamix app and listener.
 * 
 * @author Darren Carlson
 */
class ContextSupport {
	// Private data
	private String contextType;
	private DynamixSession session;
	private IDynamixListener listener;
	private ContextPlugin plug;
	private String supportId;

	/**
	 * Creates a ContextSupport.
	 * 
	 * @param session
	 *            The DynamixSession
	 * @param listener
	 *            The listener to which this context support is bound.
	 * @param plug
	 *            The plug-in handling the context support.
	 * 
	 * @param contextType
	 *            The type of the context support.
	 */
	public ContextSupport(DynamixSession session, IDynamixListener listener, ContextPlugin plug, String contextType) {
		this.session = session;
		this.listener = listener;
		this.plug = plug;
		this.contextType = contextType;
		this.supportId = UUID.randomUUID().toString();
	}

	/**
	 * Returns the ContextPlugin associated with this context support.
	 */
	public ContextPlugin getContextPlugin() {
		return plug;
	}

	/**
	 * Returns the context type associated with this context support.
	 */
	public String getContextType() {
		return contextType;
	}

	/**
	 * Returns the DynamixApplication associated with this context support.
	 */
	public DynamixApplication getDynamixApplication() {
		return session.getApp();
	}

	/**
	 * Returns the IDynamixListener associated with this context support.
	 */
	public IDynamixListener getDynamixListener() {
		return this.listener;
	}

	/**
	 * Returns this context support's id.
	 */
	public String getSupportId() {
		return this.supportId;
	}

	/**
	 * Returns the ContextSupportInfo associated with this ContextSupport.
	 */
	public ContextSupportInfo getContextSupportInfo() {
		return new ContextSupportInfo(supportId.toString(), plug.getContextPluginInformation(), contextType);
	}

	// DynamixSession session, IDynamixListener listener, ContextPlugin plug, String contextType
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + listener.hashCode() + plug.hashCode() + contextType.hashCode()
				+ supportId.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ContextSupport: App = " + getDynamixApplication() + " | Binder = " + listener
				+ " | plug-in = " + getContextPlugin() + " | context type = " + getContextType()
				+ " | support id = " + getSupportId();
	}

	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		ContextSupport other = (ContextSupport) candidate;
		if (this.listener.asBinder().equals(other.getDynamixListener().asBinder())
				&& this.plug.equals(other.getContextPlugin())
				&& this.contextType.equalsIgnoreCase(other.getContextType())
				&& this.supportId.equalsIgnoreCase(other.getSupportId()))
			return true;
		else
			return false;
	}
}
