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
package org.ambientdynamix.security;

import java.io.IOException;
import java.io.Serializable;

/**
 * Experimental killable thread. Unused.
 * 
 * @author Darren Carlson
 * 
 */
public class KillableThread extends Thread implements Serializable {
	private final String TAG = this.getClass().getSimpleName();
	private boolean kill;
	private Thread myThread;

	public KillableThread(Runnable r) {
		super(r);
	}

	// Call run hsing post
	// quit the looper
	public void kill() throws Exception {
		this.kill = true;
		if (myThread != null) {
			super.start();
		}
		// ByteArrayOutputStream fos = null;
		// ObjectOutputStream out = null;
		// fos = new ByteArrayOutputStream();
		// out = new ObjectOutputStream(fos);
		// out.writeObject(this);
		// out.close();
		// Log.i(TAG, "Calling kill on Handler: " + h);
		/*
		 * This will never post because the previous task never exits...
		 */
		// h.post(new Runnable() {
		// @Override
		// public void run() {
		// Log.e(TAG, "Sending RuntimeException");
		// throw new RuntimeException();
		// }
		// });
		// kill = true;
		// if(threadLooper != null)
		// threadLooper.quit();
		// sleep(long millis, int nanos)
		// Class[] signature = new Class[] { long.class, int.class };
		//
		// try {
		// Method m = getClass().getDeclaredMethod(
		// "sleep", signature);
		// m.invoke(10000000, 1);
		//
		// } catch (NoSuchMethodException ex) {
		// // Ignore. Just interested in the method's existence.
		// }
		// catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// catch (InvocationTargetException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		kill = false;
		myThread = Thread.currentThread();
		// Thread t = new Thread(new Runnable() {
		// @Override
		// public void run() {
		// boolean done = false;
		// while (!done) {
		// if (kill) {
		// Log.e(TAG, "Kill requested, sending RuntimeException");
		// done = true;
		//
		// throw new RuntimeException();
		// }
		// else
		// try {
		// Thread.sleep(1000);
		// }
		// catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }
		// });
		// t.setDaemon(true);
		// t.start();
		super.run();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		throw new IOException("Thread killer strikes");
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		throw new IOException("Thread killer strikes");
	}
}
