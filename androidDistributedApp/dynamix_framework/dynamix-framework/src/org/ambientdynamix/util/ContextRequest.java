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
package org.ambientdynamix.util;

import java.util.Date;

import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.core.DynamixApplication;

/**
 * Represents a context request for a specific DynamixApplication and IDynamixListener.
 * 
 * @author Darren Carlson
 */
public class ContextRequest {
	// Private data
	private DynamixApplication app;
	private IDynamixListener listener;
	private Date createdTime;
	private ContextPlugin plugin;

	/**
	 * Creates a ContextRequest for the specified DynamixApplication and IDynamixListener.
	 */
	public ContextRequest(DynamixApplication app, IDynamixListener listener, ContextPlugin plugin) {
		this.app = app;
		this.listener = listener;
		this.createdTime = new Date();
		this.plugin = plugin;
	}

	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		ContextRequest other = (ContextRequest) candidate;
		return this.app.equals(other.app) && listener.asBinder().equals(other.listener.asBinder()) && this.plugin.equals(other.plugin) ? true : false;
	}

	/**
	 * Returns the DynamixApplication that made the request.
	 */
	public DynamixApplication getApp() {
		return app;
	}

	/**
	 * Returns the IDynamixListener that made the request.
	 */
	public IDynamixListener getListener() {
		return listener;
	}
	
	/**
	 * Returns the time that this context request was created
	 */
	public Date getCreatedTime(){
		return this.createdTime;
	}
	
	/**
	 * Returns the Context Plug-in handling this context request.
	 */
	public ContextPlugin getPlugin(){
		return this.plugin;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + app.hashCode() + listener.hashCode() + plugin.hashCode();
		return result;
	}
}
