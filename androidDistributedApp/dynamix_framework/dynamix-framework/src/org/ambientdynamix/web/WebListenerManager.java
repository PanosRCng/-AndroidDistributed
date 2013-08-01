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
package org.ambientdynamix.web;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ambientdynamix.core.WebListener;

import android.util.Log;

/**
 * Event management class for web listeners, which holds a linked queue of event commands to send.
 * 
 * @author Darren Carlson
 */
public class WebListenerManager<String> extends ConcurrentLinkedQueue<String> {
	// Private data
	private final java.lang.String TAG = this.getClass().getSimpleName();
	private Object lock = new Object();
	private WebListener listener;
	private Date lastAccess = new Date();
	private boolean dead = false;

	/**
	 * Creates a WebListenerManager.
	 * @param listener The WebListener to manage.
	 */
	public WebListenerManager(WebListener listener) {
		this.listener = listener;
		ping();
	}


	/**
	 * Returns the managed WebListener.
	 */
	public WebListener getListener() {
		return listener;
	}

	/**
	 * Sets the last access to the current time.
	 */
	public void ping() {
		lastAccess = new Date();
	}

	/**
	 * Set true if the WebListenerManager is dead (i.e., timed out); false otherwise.
	 */
	public void setDead(boolean dead) {
		this.dead = dead;
	}

	/**
	 * Returns true if the WebListenerManager is dead (i.e., timed out); false otherwise.
	 */
	public boolean isDead() {
		return dead;
	}

	/**
	 * Returns the time of the last ping.
	 */
	public Date getLastAccess() {
		return lastAccess;
	}

	/**
	 * Block and wait for an event.
	 * @param millis The time to wait (block) in milliseconds.
	 */
	public void waitForEvent(int millis) {
		synchronized (lock) {
			try {
				lock.wait(millis);
			} catch (InterruptedException e) {
				Log.i(TAG, " Interrupted ");
			}
		}
	}

	/**
	 * Adds the String command to sent to the web client to the queue, which notifies the wait lock.
	 */
	public boolean add(String command) {
		if (super.add(command)) {
			try {
				synchronized (lock) {
					lock.notify();
				}
			} catch (Exception e) {
				Log.w(TAG, e.toString());
			}
			return true;
		} else
			return false;
	}
}