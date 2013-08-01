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

import org.ambientdynamix.api.application.IStreamController;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * Monitors memory consumption during event streaming.
 * 
 * @author Darren Carlson
 */
public class StreamController implements IStreamController {
	private final String TAG = getClass().getSimpleName();
	boolean cancel;
	private boolean done;
	private float maxPercentOfTotal;
	private int checkPeriod;
	private ActivityManager activityManager;
	private long memoryThresholdBytes;
	private long totalMemory;

	public StreamController(Context context, int checkPeriod, float maxPercentOfTotal) {
		cancel = false;

		this.maxPercentOfTotal = maxPercentOfTotal;
		this.checkPeriod = checkPeriod;
		activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		// Log.i(TAG, "Total Memory: " + activityManager.getMemoryClass());
		totalMemory = Runtime.getRuntime().maxMemory(); // activityManager.getMemoryClass() * 1048576;
		memoryThresholdBytes = (long) (totalMemory * maxPercentOfTotal);
		// Log.d(TAG, "Created StreamController for device with " + totalMemory + " with a memory threshold of "
		// + memoryThresholdBytes);
	}

	@Override
	public boolean outOfMemory() {
		return cancel;
	}

	/*
	 * http://stackoverflow.com/questions/2298208/how-to-discover-memory-usage-of-my-application-in-android
	 * http://stackoverflow.com/questions/3118234/how-to-get-memory-usage-and-cpu-usage-in-android
	 * http://macgyverdev.blogspot.com/2011/11/android-track-down-memory-leaks.html
	 */
	public boolean isMemoryLimitReached() {
		// MemoryInfo mem = new ActivityManager.MemoryInfo();
		// activityManager..getMemoryInfo(mem);
		// Log.i(TAG, "Memory Heap size: " + Debug.getNativeHeapSize());
		// Log.i(TAG, "Memory Heap allocated: " + Debug.getNativeHeapAllocatedSize());
		// Log.i(TAG, "Memory Heap free: " + Debug.getNativeHeapFreeSize());
		// Log.i(TAG, "Memory Runtime available: " + Runtime.getRuntime().totalMemory());
		// Log.i(TAG, "Memory Runtime freeMemory: " + Runtime.getRuntime().freeMemory());
		// Log.i(TAG, "Memory Runtime maxMemory: " + Runtime.getRuntime().maxMemory());

		// Runtime.getRuntime().totalMemory() >
		// long available = Runtime.getRuntime().freeMemory();
		android.os.Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[] { android.os.Process
				.myPid() });
		long total = memInfo[0].getTotalPss() * 1024;
		Log.i(TAG, "Total memory: " + total + " threshold " + memoryThresholdBytes + " | Cancel: "
				+ (total >= memoryThresholdBytes));
		return (total >= memoryThresholdBytes);
		// long remainingMem = mem.availMem - mem.threshold;
		// Log.v(TAG, "StreamController memory check: " + remainingMem + " bytes remaining.");
		// return remainingMem < minMemoryThresholdBytes;
	}

	public void start() {
		done = false;
		if (isMemoryLimitReached()) {
			Log.e(TAG, "Memory limit reached on first pass!");
			cancel = true;
		} else {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					int count = 0;
					Log.v(TAG, "StreamController started!");
					while (!done) {
						Log.i(TAG, "Checking memory usage: " + count++);
						if (isMemoryLimitReached()) {
							Log.e(TAG, "Memory limit reached... canceling streaming!");
							stop();
							cancel = true;
						}
						try {
							Thread.sleep(checkPeriod);
						} catch (InterruptedException e) {
							Log.w(TAG, "StreamController interrupted!" + e);
							stop();
						}
					}
					Log.v(TAG, "StreamController stopped!");
				}
			});
			t.setDaemon(true);
			t.start();
		}
	}

	public void stop() {
		done = true;
	}
}