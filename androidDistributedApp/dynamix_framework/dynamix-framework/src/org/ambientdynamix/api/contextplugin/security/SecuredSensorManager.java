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
package org.ambientdynamix.api.contextplugin.security;

import java.util.List;
import java.util.Vector;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * The Android SensorManager is potentially dangerous because (by default) it sends events using the application's main
 * thread. This means that a plug-in could crash Dynamix if it causes an exception in the onSensorChanged event, etc. To
 * counteract this problem the SecuredSensorManager provides a custom HandlerThread and Handler, which are constructed
 * using the plug-in's own Looper, allowing exceptions to be caught by Dynamix.
 * 
 * @author Darren Carlson
 */
public class SecuredSensorManager {
	// Private data
	private final String TAG = getClass().getSimpleName();
	private SensorManager mgr;
	private Handler handler;
	private Looper plugLooper;
	private Vector<SensorEventListener> listeners = new Vector<SensorEventListener>();

	/**
	 * Creates a SecuredSensorManager using the incoming SensorManager and plug-in Looper.
	 */
	SecuredSensorManager(SensorManager mgr, Looper plugLooper) {
		this.mgr = mgr;
		this.plugLooper = plugLooper;
		handler = new Handler(plugLooper);
	}

	/**
	 * Returns the plug-in's Looper.
	 */
	public Looper getPlugLooper() {
		return this.plugLooper;
	}

	/**
	 * Returns a List of sensors for the given type from the underlying SensorManager.
	 * 
	 * @param type
	 *            The type of sensor to find.
	 */
	public List<Sensor> getSensorList(int type) {
		return mgr.getSensorList(type);
	}
	
	/**
	 * Returns the Sensor for the given type from the underlying SensorManager;
	 * @param type The type of sensor to find
	 * @return
	 */
	public Sensor getDefaultSensor(int type){
		return mgr.getDefaultSensor(type);
	}

	/**
	 * Registers the SensorEventListener to receive events from the underlying SensorManager.
	 * 
	 * @param listener
	 *            The listener to register.
	 * @param sensor
	 *            The sensor to register.
	 * @param rate
	 *            The sensor rate
	 * @return True if the listener was registered; false otherwise.
	 */
	public boolean registerSensorListener(SensorEventListener listener, Sensor sensor, int rate) {
		boolean success = mgr.registerListener(listener, sensor, rate, handler);
		//Log.e(TAG, "SSM registerSensorListener for " + listener + " with success " + success);
		/*
		 * Maintain a list of listeners so we can handle 'removeAllListeners', if needed
		 */
		synchronized (listeners) {
			if (success)
				if (!listeners.contains(listener))
					listeners.add(listener);
				else
					Log.w(TAG, "Already contained listener: " + listener);
		}
		return success;
	}

	/**
	 * Removes all previously registered SensorEventListener from the underlying SensorManager.
	 */
	public void removeAllListeners() {
		synchronized (listeners) {
			for (SensorEventListener listener : listeners)
				mgr.unregisterListener(listener);
		}
	}

	/**
	 * Unregisters the SensorEventListener from receiving events from the underlying SensorManager.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void unregisterListener(SensorEventListener listener) {
		//Log.e(TAG, "SSM unregisterListener for " + listener);
		mgr.unregisterListener(listener);
		listeners.remove(listener);
	}

	/**
	 * Unregisters the SensorEventListener from receiving events from the underlying SensorManager for the specific
	 * sensor.
	 * 
	 * @param listener
	 *            The listener to remove.
	 * @param sensor
	 *            The sensor to remove the listener from.
	 */
	public void unregisterListener(SensorEventListener listener, Sensor sensor) {
		mgr.unregisterListener(listener, sensor);
		listeners.remove(listener);
	}
}