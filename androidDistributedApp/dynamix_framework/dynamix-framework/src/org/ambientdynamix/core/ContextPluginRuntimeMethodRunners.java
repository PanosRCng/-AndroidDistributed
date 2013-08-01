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

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ambientdynamix.api.contextplugin.AutoContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ReactiveContextPluginRuntime;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Set of utilities for launching threaded method runners for various ContextPluginRuntime implementations.
 * 
 * @author Darren Carlson
 */
abstract class ContextPluginRuntimeMethodRunners {
	// Private data
	private final static String TAG = ContextPluginRuntimeMethodRunners.class.getSimpleName();
	/**
	 * Static ThreadPoolExecutor that submit Callable tasks, which can throw checked exceptions.
	 */
	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 60, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>()) {
		/*
		 * See: http://stackoverflow.com/questions/4492273/catching-thread-exceptions-from-java-executorservice
		 */
		@Override
		public <T> Future<T> submit(final Callable<T> task) {
			Callable<T> wrappedTask = new Callable<T>() {
				@Override
				public T call() throws Exception {
					try {
						return task.call();
					} catch (Exception e) {
						Log.w(TAG, "Exception: " + e);
						return null;
					}
				}

				@Override
				protected void finalize() throws Throwable {
					super.finalize();
				}
			};
			// Throwable t = null;
			// Runnable r = new Runnable() {
			// @Override
			// public void run() {
			// Log.w(TAG, "Running exception handler");
			// }
			// };
			// super.afterExecute(r, t);
			// if (t == null && r instanceof Future<?>) {
			// try {
			// Future<?> future = (Future<?>) r;
			// if (future.isDone())
			// future.get();
			// } catch (CancellationException ce) {
			// Log.w(TAG, "CancellationException: " + ce);
			// t = ce;
			// } catch (ExecutionException ee) {
			// Log.w(TAG, "ExecutionException: " + ee);
			// t = ee.getCause();
			// } catch (InterruptedException ie) {
			// Log.w(TAG, "InterruptedException: " + ie);
			// Thread.currentThread().interrupt(); // ignore/reset
			// }
			// }
			return super.submit(wrappedTask);
		}
	};

	protected void afterExecute(Runnable r, Throwable t) {
	}

	/**
	 * Static utility method for launching Daemon threads.
	 * 
	 * @param runner
	 *            The Callable to run.
	 * @param threadPriority
	 *            The thread priority.
	 * @return Returns the launched Future.
	 */
	public static Future<?> launchThread(Callable<?> runner, int threadPriority) {
//		final Future<?> future = executor.submit(runner);
//		Thread catcher = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					future.get();
//				} catch (ExecutionException ex) {
//					ex.getCause().printStackTrace();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});
//		catcher.setDaemon(true);
//		catcher.start();
//		return future;
		return executor.submit(runner);
		
		
	}

	/**
	 * Callable that provides functionality for performing a manual context scan on a dedicated thread
	 * 
	 * @see AutoContextPluginRuntime
	 * @author Darren Carlson
	 */
	public static class DoManualContextScan implements Callable<Object> {
		private final String TAG = this.getClass().getSimpleName();
		private AutoContextPluginRuntime target;

		public DoManualContextScan(AutoContextPluginRuntime target) {
			this.target = target;
		}

		@Override
		public Object call() throws Exception {
			try {
				target.doManualContextScan();
			} catch (Exception e) {
				Log.w(TAG, "DoManualContextScan exception for " + target + " | Exception was: " + e.toString());
			}
			return null;
		}
	}

	/**
	 * Callable that provides functionality for performing a context scan request on a dedicated thread
	 * 
	 * @see ReactiveContextPluginRuntime
	 * @author Darren Carlson
	 */
	public static class HandleContextRequest implements Callable<Object> {
		private ContextManager mgr;
		private ReactiveContextPluginRuntime target;
		private UUID requestId;
		private String contextInfoType;
		private Bundle scanConfig;

		public HandleContextRequest(ContextManager mgr, ReactiveContextPluginRuntime target, UUID requestId,
				String contextInfoType, Bundle scanConfig) {
			this.mgr = mgr;
			this.target = target;
			this.requestId = requestId;
			this.contextInfoType = contextInfoType;
			this.scanConfig = scanConfig;
		}

		@Override
		public Object call() throws Exception {
			try {
				if (scanConfig == null)
					target.handleContextRequest(requestId, contextInfoType);
				else
					target.handleConfiguredContextRequest(requestId, contextInfoType, scanConfig);
			} catch (Exception e) {
				Log.w(TAG, "Exception during HandleContextRequest: " + e.toString());
			}
			return null;
		}
	}

	/**
	 * Callable that provides functionality for starting context scanning on a dedicated thread
	 * 
	 * @see ContextPluginRuntime
	 * @author Darren Carlson
	 */
	public static class StartContextScanning implements Callable<Object> {
		private final String TAG = this.getClass().getSimpleName();
		private ContextPluginRuntime target;
		private Handler handler;
		private Looper looper;

		public StartContextScanning(ContextPluginRuntime target) {
			this.target = target;
		}

		/*
		 * Refactored to use Callable, since Runnable cannot throw checked exceptions.
		 * http://download.oracle.com/javase/6/docs/api/java/util/concurrent/Callable.html See:
		 * http://stackoverflow.com/questions/1369204/how-to-throw-a-checked-exception-from-a-java-thread (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public Object call() throws Exception {
			try {
				looper = Looper.myLooper();
				Looper.prepare();
				handler = new Handler();
				handler.postDelayed((new Runnable() {
					@Override
					public void run() {
						try {
							target.start();
						} catch (Exception e) {
							Log.e(TAG, "Context Scanning Looper Exception: " + e);
							/*
							 * TODO: Deactivate plug-in here?
							 */
						}
					}
				}), 500);
				Looper.loop();
			} catch (Exception e) {
				Log.w(TAG, "Start exception for " + target + " | Exception was: " + e.toString());
				handler.post(new Runnable() {
					@Override
					public void run() {
						looper.quit();
					}
				});
			}
			return null;
		}
	}
}