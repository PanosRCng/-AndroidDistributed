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


/**
 * Represents an event collected by PluginStats.
 * 
 * @author Darren Carlson
 */
public class PluginStatsEvent {
	// Private data
	private boolean error = false;
	private String errorMessage = "No error";
	private SourcedContextInfoSet event;
	private Date timeStamp = new Date();

	/**
	 * Creates a PluginStatsEvent that represents a ContextInfoSet.
	 * 
	 * @param event
	 *            The ContextInfoSet to wrap.
	 */
	public PluginStatsEvent(SourcedContextInfoSet event) {
		this.event = event;
	}

	/**
	 * Creates a PluginStatsEvent that represents an error.
	 * 
	 * @param errorMessage
	 *            The error message to display.
	 */
	public PluginStatsEvent(String errorMessage) {
		this.error = true;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the error message, if this PluginStatsEvent is representing an error.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Returns the time-stamp of this PluginStatsEvent.
	 */
	public Date getErrorTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * Returns the ContextInfoSet represented by this PluginStatsEvent, or null if this PluginStatsEvent represents an
	 * error.
	 */
	public SourcedContextInfoSet getEvent() {
		return event;
	}

	/**
	 * Returns true if this PluginStatsEvent represents an error; false if it represents a ContextInfoSet.
	 */
	public boolean hasError() {
		return error;
	}
}