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

import org.ambientdynamix.api.contextplugin.ContextPlugin;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Manages a plug-ins Looper thread.
 * 
 * @author Darren Carlson
 */
public class PluginLooperThread extends Thread {
	// Private data
	private final String TAG = getClass().getSimpleName(); 
	private ContextPlugin plug;
	private Looper looper;
	// Public data
	public Handler handler;

	/**
	 * Creates a PluginLooperThread for the specified ContextPlugin.
	 */
	public PluginLooperThread(ContextPlugin plug) {
		this.plug = plug;
	}

	/**
	 * Returns the ContextPlugin.
	 */
	public ContextPlugin getContextPlugin() {
		return this.plug;
	}

	/**
	 * Returns the ContextPlugin's Looper.
	 */
	public Looper getLooper() {
		return looper;
	}

	/**
	 * Kills the thread.
	 */
	public void kill() {
		/*
		 * Thread kill ideas. Injecting an exception using a debugger:
		 * http://www.rhcedan.com/2010/06/22/killing-a-java-thread/ Use a timeout:
		 * http://www.devinprogress.info/2011/03/run-tasks-in-parallel-and-set-your-own.html - In combo with reflection
		 * to modify the timeout length?
		 */
		Log.w(TAG, "Trying to Kill Looper!");
		handler.postAtFrontOfQueue(new Runnable() {
			@Override
			public void run() {
				looper.quit();
				Log.w(TAG, "Throwing runtime exception");
				throw new RuntimeException();
			}
		});
	}

	/**
	 * Attempts to stop the thread by calling quit on the underlying Looper.
	 */
	public void quit() {
		if (isAlive())
			handler.postAtFrontOfQueue(new Runnable() {
				@Override
				public void run() {
					looper.quit();
				}
			});
	}

	/**
	 * Run method that is called when this PluginLooperThread is started. Exceptions are handled by the caller (probably
	 * the ContextManager's startPlugin method).
	 */
	public void run() {
		Looper.prepare();
		handler = new Handler();
		looper = Looper.myLooper();
		//looper = Looper.myLooper();
		Log.d(TAG, "Starting looper: " +looper + " for plug-in: " + plug);
		Looper.loop();
		Log.d(TAG, "Exiting looper: " + looper + " for plug-in: " + plug);
	}
}
