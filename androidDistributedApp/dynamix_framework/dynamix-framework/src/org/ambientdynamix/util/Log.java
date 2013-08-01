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

/**
 * Simply configurable wrapper for android.util.Log
 * @author Darren Carlson
 *
 */
public class Log {
	/*
	 * http://stackoverflow.com/questions/5553146/disable-logcat-output-completely-in-release-android-app
	 * 
	 * You can use ProGuard to remove completely any lines where a return value is not used, by telling ProGuard to assume that there will be no problems.
	 * The following proguard.cfg chunk instructs to remove Log.d, Log.v and Log.i calls.

		-assumenosideeffects class android.util.Log {
		    public static *** d(...);
		    public static *** v(...);
		    public static *** i(...);
		}
		
	The end result is that these log lines are not in your release apk, and therefore any user with logcat won't see d/v/i logs.

	 */
	static final boolean LOG = false;

	public static void i(String tag, String string) {
		if (LOG)
			android.util.Log.i(tag, string);
	}

	public static void e(String tag, String string) {
		if (LOG)
			android.util.Log.e(tag, string);
	}

	public static void d(String tag, String string) {
		if (LOG)
			android.util.Log.d(tag, string);
	}

	public static void v(String tag, String string) {
		if (LOG)
			android.util.Log.v(tag, string);
	}

	public static void w(String tag, String string) {
		if (LOG)
			android.util.Log.w(tag, string);
	}
}
