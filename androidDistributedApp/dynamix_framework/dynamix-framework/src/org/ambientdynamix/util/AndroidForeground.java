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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.util.Log;

/**
 * Utility class for starting and stopping foreground services. This is used to keep the DynamixService alive during
 * background processing.
 * 
 * @author Darren Carlson
 */
public class AndroidForeground {
	/*
	 * See
	 * http://developer.android.com/reference/android/app/Service.html#startForeground%28int,%20android.app.Notification
	 * %29
	 */
	// Static data
	public final static String TAG = AndroidForeground.class.getSimpleName();
	public static final Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class };
	public static final Class[] mStopForegroundSignature = new Class[] { boolean.class };
	// Private data
	private NotificationManager mNM;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	/**
	 * Creates an AndroidForeground.
	 */
	public AndroidForeground(NotificationManager mNM, Method mStartForeground, Method mStopForeground) {
		this.mNM = mNM;
		this.mStartForeground = mStartForeground;
		this.mStopForeground = mStopForeground;
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older APIs if it is not available.
	 */
	public void startForegroundCompat(int id, Notification notification, Service s) {
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(id);
			mStartForegroundArgs[1] = notification;
			try {
				mStartForeground.invoke(s, mStartForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w(TAG, "Unable to invoke startForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w(TAG, "Unable to invoke startForeground", e);
			}
			return;
		}
		// Fall back on the old API.
		s.setForeground(true);
		mNM.notify(id, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older APIs if it is not available.
	 */
	public void stopForegroundCompat(int id, Service s) {
		// Log.w(TAG, "!!! Running stopForegroundCompat !!! ");
		// If we have the new stopForeground API, then use it.
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			try {
				mStopForeground.invoke(s, mStopForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w(TAG, "Unable to invoke stopForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w(TAG, "Unable to invoke stopForeground", e);
			}
			return;
		}
		// Fall back on the old API. Note to cancel BEFORE changing the
		// foreground state, since we could be killed at that point.
		mNM.cancel(id);
		s.setForeground(false);
	}
}