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

import java.util.Calendar;
import java.util.Date;

import org.ambientdynamix.api.application.IDynamixListener;

/**
 * Foundation class for command-based event sending encapsulation based on the command pattern. By abstracting event
 * sending in this way, we are able to simplify the process of sending events to registered Dynamix applications using
 * the ContextManager. IEventCommands can be sent to a filterable list of Dynamix applications through a single
 * ContextManager method named 'sendEventCommand'. The IEventCommand provides pre-processing, event sending and post
 * processing logic.
 * 
 * @author Darren Carlson
 * @see ContextManager
 * @see EventCommand
 */
abstract class IEventCommand {
	private Calendar deliveryTime;
	private boolean sendDelay;

	/**
	 * Creates an IEventCommand with no send delay
	 */
	public IEventCommand() {
		setSendDelayMills(0);
	}

	/**
	 * Creates an IEventCommand with with a send delay of > 0ms
	 */
	public IEventCommand(int sendDelayMills) {
		setSendDelayMills(sendDelayMills);
	}

	public boolean deliveryDelayElapsed() {
		if (hasSendDelay()) {
			Date now = new Date();
			return now.after(deliveryTime.getTime());
		}
		return true;
	}

	public Date getDeliveryTime() {
		return deliveryTime.getTime();
	}

	/**
	 * Returns true if sending this command should be delayed; false if this command should be sent immediately.
	 */
	public boolean hasSendDelay() {
		return sendDelay;
	}

	/**
	 * Called after command processing ends
	 * 
	 * @param mgr
	 *            The ContextManager
	 */
	public abstract void postProcess();

	/**
	 * Called before command processing begins
	 * 
	 * @param mgr
	 *            The ContextManager
	 */
	public abstract void preProcess();

	/**
	 * The command method used to call specific methods on the incoming DynamixSession and/or IDynamixListener.
	 * 
	 * @param mgr
	 *            The ContextManager
	 */
	public abstract void processCommand(DynamixSession app, IDynamixListener listener) throws Exception;

	public void setSendDelayMills(int sendDelayMills) {
		deliveryTime = Calendar.getInstance();
		if (sendDelayMills > 0) {
			deliveryTime.add(Calendar.MILLISECOND, sendDelayMills);
			sendDelay = true;
		} else
			sendDelay = false;
	}
}