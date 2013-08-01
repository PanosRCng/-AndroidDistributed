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

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ambientdynamix.api.contextplugin.ContextInfoSet;
import org.ambientdynamix.api.contextplugin.ContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.IPluginContextListener;
import org.ambientdynamix.api.contextplugin.IPluginEventHandler;

/**
 * Experimental thread-pool implementation of the IPluginEventHandler interface. Currently not used.
 * 
 * @author Darren Carlson
 */
public class ThreadPoolEventHandler extends ThreadPoolExecutor implements IPluginEventHandler {
	// Private data
	private Collection<IPluginContextListener> _listeners = new ArrayList<IPluginContextListener>();

	public ThreadPoolEventHandler(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	public synchronized void addContextListener(IPluginContextListener listener) {
		_listeners.add(listener);
	}

	public synchronized void removeContextListener(IPluginContextListener listener) {
		_listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendError(ContextPluginRuntime sender, UUID requestId, String errorMessage, int errorCode) {
		Collection<IPluginContextListener> snapshot = new Vector<IPluginContextListener>(_listeners);
		for (IPluginContextListener l : snapshot) {
			this.execute(new ErrorRunner(sender, l, requestId, errorMessage, errorCode));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendEvent(ContextPluginRuntime sender, ContextInfoSet dataSet) {
		Collection<IPluginContextListener> snapshot = new Vector<IPluginContextListener>(_listeners);
		for (IPluginContextListener l : snapshot) {
			this.execute(new EventRunner(sender, l, dataSet));
		}
	}

	/**
	 * Internal Runnable class that is used for handling a single, threaded onContextScanError method call. Used in
	 * conjunction with the ThreadPoolEventHandler to send events to dependent clients.
	 * 
	 * @author Darren Carlson
	 */
	private class ErrorRunner implements Runnable {
		private ContextPluginRuntime source;
		private IPluginContextListener target;
		private UUID requestId;
		private String errorMessage;
		private int errorCode;

		public ErrorRunner(ContextPluginRuntime source, IPluginContextListener target, UUID requestId,
				String errorMessage, int errorCode) {
			this.source = source;
			this.target = target;
			this.requestId = requestId;
			this.errorMessage = errorMessage;
			this.errorCode = errorCode;
		}

		public void run() {
			target.onContextRequestFailed(source.getSessionId(), requestId, errorMessage, errorCode);
		}
	}

	/**
	 * Internal Runnable class that is used for handling a single, threaded handlePluginContextEvent method call. Used
	 * in conjunction with the ThreadPoolEventHandler to send events to dependent clients.
	 * 
	 * @author Darren Carlson
	 */
	private class EventRunner implements Runnable {
		private ContextPluginRuntime source;
		private IPluginContextListener target;
		private ContextInfoSet dataSet;

		public EventRunner(ContextPluginRuntime source, IPluginContextListener target, ContextInfoSet infoSet) {
			this.source = source;
			this.target = target;
			this.dataSet = infoSet;
		}

		public void run() {
			target.onPluginContextEvent(source.getSessionId(), dataSet);
		}
	}
}