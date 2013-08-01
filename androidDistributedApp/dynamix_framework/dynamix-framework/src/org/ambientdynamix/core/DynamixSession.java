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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.api.contextplugin.ContextPlugin;

import android.os.IBinder;
import android.util.Log;

/**
 * Represents a session for a specific DynamixApplicaiton.
 * 
 * @author Darren Carlson
 */
class DynamixSession implements Serializable {
	// Private data
	private static final long serialVersionUID = -2174448383460352412L;
	private final String TAG = this.getClass().getSimpleName();
	private int processId;
	private UUID sessionId;
	private DynamixApplication app;
	private Map<IBinder, IDynamixListener> binderMap = new ConcurrentHashMap<IBinder, IDynamixListener>();
	private Map<IBinder, UUID> listenerMap = new ConcurrentHashMap<IBinder, UUID>();
	private Map<IBinder, List<ContextSupport>> contextSupportMap = new ConcurrentHashMap<IBinder, List<ContextSupport>>();

	/**
	 * Creates a DynamixSession using the processId
	 */
	public DynamixSession(int processId) {
		this.processId = processId;
	}

	/**
	 * Refreshes the app using the Dynamix database, which updates the app's in memory details, such as privacy policies
	 * for plug-ins.
	 */
	public synchronized void refreshApp() {
		if(isSessionOpen()){
			DynamixApplication refreshed = DynamixService.getDynamixApplicationByUid(app.getAppID());
			if (refreshed != null) {
				this.app = refreshed;
			} else
				Log.w(TAG, "Could not refresh app: " + app);
		}
	}

	/**
	 * Adds the specified listener to the session.
	 * 
	 * @param listener
	 *            The listener to add.
	 * @param notify
	 *            True if the listener should be notified of the addition; false otherwise.
	 * @return The listener's id
	 */
	public synchronized String addDynamixListener(IDynamixListener listener, boolean notify) {
		Log.d(TAG, "addDynamixListener for: " + listener.asBinder());
		if (!listenerMap.containsKey(listener.asBinder())) {
			UUID listenerId = UUID.randomUUID();
			listenerMap.put(listener.asBinder(), listenerId);
			binderMap.put(listener.asBinder(), listener);
			// Notify the requester that the listener was added
			if (notify)
				SessionManager.notifyDynamixListenerAdded(listener, listenerId.toString());
			return listenerId.toString();
		} else {
			// The listener is already there
			Log.d(TAG, "addDynamixListener found existing listener for: " + listener.asBinder());
			if (notify)
				SessionManager.notifyDynamixListenerAdded(listener, listenerMap.get(listener.asBinder()).toString());
			return listenerMap.get(listener.asBinder()).toString();
		}
	}

	/**
	 * Adds a ContextSupport for the specified listener.
	 * 
	 * @return True if the context support was added; false otherwise.
	 */
	public synchronized boolean addContextSupport(IDynamixListener listener, ContextSupport contextSupport) {
		Log.d(TAG, "addContextSupport for " + contextSupport);
		if (listener != null) {
			if (contextSupport != null) {
				// Ensure that the listener has already been registered
				if (listenerMap.containsKey(listener.asBinder())) {
					// Handle existing support
					if (contextSupportMap.keySet().contains(listener.asBinder())) {
						List<ContextSupport> supportList = contextSupportMap.get(listener.asBinder());
						/*
						 * Check to see if the listener already has context support.
						 */
						boolean hasSupport = false;
						for (ContextSupport existing : supportList) {
							// Check existing context type against the requested support's context type
							if (existing.getContextType().equalsIgnoreCase(contextSupport.getContextType()))
								// Check existing plug-in against the requested support's plug-in
								if (existing.getContextPlugin().equals(contextSupport.getContextPlugin())) {
									// Found existing support
									hasSupport = true;
									break;
								}
						}
						if (!hasSupport)
							return supportList.add(contextSupport);
						else {
							Log.d(TAG, "Context support was already added for: " + listener);
							return true;
						}
					} else {
						// No support yet, so set it up
						List<ContextSupport> supportList = new Vector<ContextSupport>();
						supportList.add(contextSupport);
						contextSupportMap.put(listener.asBinder(), supportList);
						Log.d(TAG, "Added context support for: " + listener);
						return true;
					}
				} else
					Log.w(TAG,
							"addContextSupport called, but listener is not registered to receive events: "
									+ listener.asBinder());
			} else
				Log.w(TAG, "addContextSupport received null contextSupport");
		} else
			Log.w(TAG, "addContextSupport received null listener");
		return false;
	}

	/**
	 * Closes the session, removing the app's context support and cleaning up state.
	 * 
	 * @param notify
	 *            True if the app should be notified of the session close; false otherwise.
	 */
	public synchronized void closeSession(boolean notify) {
		Log.d(TAG, "Closing session... ");
		if (isSessionOpen()) {
			/*
			 * Make sure that the contextSupportMap is empty. Note that the contextSupportMap should be empty, since the
			 * ContextManager is typically called first during a session close operation so that it can remove context
			 * support resources. During this process, the ContextManager also calls removeContextSupport
			 */
			if (!contextSupportMap.isEmpty()) {
				Log.w(TAG, "contextSupportMap was not empty during session close!");
				for (List<ContextSupport> subs : contextSupportMap.values())
					for (ContextSupport sub : subs)
						Log.w(TAG, "Remaining context support " + sub);
				Log.d(TAG, "Removing context support...");
				removeAllContextSupport(notify);
				contextSupportMap.clear();
			}
			Log.w(TAG, "TODO: Cancelling outstanding requests...");
			Log.w(TAG, "TODO: Closing any context acquisition interfaces...");
			Log.d(TAG, "Session closed for app: " + app);
			// Handle notification
			if (notify)
				SessionManager.notifySessionClosed(app);
			// Setting the app to null closes the session
			this.app = null;
			// Null out the sessionId, too
			this.sessionId = null;
		} else {
			Log.d(TAG, "Session was already closed.");
		}
	}

	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		DynamixSession other = (DynamixSession) candidate;
		return this.app.equals(other.app) ? true : false;
	}

	/**
	 * Returns the application
	 */
	public DynamixApplication getApp() {
		return app;
	}

	/**
	 * Returns all context support registrations for the session.
	 */
	public List<ContextSupport> getAllContextSupport() {
		List<ContextSupport> subs = new ArrayList<ContextSupport>();
		synchronized (contextSupportMap) {
			for (List<ContextSupport> l : contextSupportMap.values())
				subs.addAll(l);
		}
		return subs;
	}

	/**
	 * Returns a list of the specified listener's context support.
	 */
	public List<ContextSupport> getContextSupport(IDynamixListener listener) {
		Vector<ContextSupport> returnList = new Vector<ContextSupport>();
		List<ContextSupport> subs = contextSupportMap.get(listener.asBinder());
		// Need a null check otherwise Vector 'addAll' throws NPEs
		if (subs != null)
			returnList.addAll(subs);
		return returnList;
	}

	/**
	 * Returns a list of registered context support matching the specified contextType string.
	 */
	public synchronized List<ContextSupport> getContextSupport(String contextType) {
		List<ContextSupport> returnList = new Vector<ContextSupport>();
		for (List<ContextSupport> subs : contextSupportMap.values())
			for (ContextSupport sub : subs)
				if (sub.getContextType().equalsIgnoreCase(contextType))
					returnList.add(sub);
		return returnList;
	}

	/**
	 * Returns the ContextSupport associated with the incoming ContextSupportInfo.
	 */
	public synchronized ContextSupport getContextSupport(ContextSupportInfo supportInfo) {
		for (List<ContextSupport> subList : contextSupportMap.values()) {
			for (ContextSupport sub : subList) {
				if (sub.getSupportId().equalsIgnoreCase(supportInfo.getSupportId()))
					return sub;
			}
		}
		return null;
	}

	/**
	 * Returns the listener registered with the specified listenerId, or null if the listener is not found.
	 */
	public synchronized IDynamixListener getDynamixListener(UUID listenerId) {
		if (listenerId != null) {
			for (IBinder l : listenerMap.keySet()) {
				UUID test = listenerMap.get(l);
				if (test != null) {
					if (listenerId.equals(test)) {
						return binderMap.get(l);
					}
				}
			}
		} else
			Log.w(TAG, "getDynamixListener received null listenerId");
		return null;
	}

	/**
	 * Returns the listenerId for the specified IDynamixListener, or null if the listener is not found.
	 */
	public synchronized String getDynamixListenerId(IDynamixListener listener) {
		UUID id = listenerMap.get(listener.asBinder());
		if (id != null)
			return id.toString();
		else
			return null;
	}

	/**
	 * Returns the registered listeners for this session.
	 */
	public synchronized List<IDynamixListener> getDynamixListeners() {
		return new Vector<IDynamixListener>(binderMap.values());
	}

	/**
	 * Returns the process id for this session
	 */
	public int getProcessId() {
		return this.processId;
	}

	/**
	 * Returns the session identifier as a UUID
	 */
	public UUID getSessionId() {
		return sessionId;
	}

	/**
	 * Returns true if the listener has a context support of the specified type.
	 * 
	 * @param listener
	 *            The listener to check.
	 * @param contextType
	 *            The context type to check.
	 */
	public synchronized boolean hasContextSupport(IDynamixListener listener, String contextType) {
		List<ContextSupport> installedSupport = getContextSupport(listener);
		for (ContextSupport support : installedSupport) {
			if (support.getContextType().equalsIgnoreCase(contextType))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + this.app.hashCode();
		return result;
	}

	/**
	 * Returns true if the specified listener is registered with the session; false otherwise.
	 */
	public boolean isDynamixListenerRegistered(IDynamixListener listener) {
		return listenerMap.containsKey(listener.asBinder());
	}

	/**
	 * Returns true if the session is open; false otherwise
	 */
	public boolean isSessionOpen() {
		return app != null;
	}

	/**
	 * Opens the session for the specified app. This registers the app with the session and establishes a unique
	 * sessionId for this session.
	 */
	public synchronized void openSession(DynamixApplication app) {
		if (app == null)
			throw new RuntimeException("DynamixApplication cannot be null");
		if (this.app != null)
			Log.w(TAG, "Open session replacing existing app: " + this.app);
		this.app = app;
		this.sessionId = UUID.randomUUID();
		Log.i(TAG, this.app + " had its session opened with sessionId: " + sessionId);
	}

	/**
	 * Removes the listener from the session.
	 * 
	 * @param listener
	 *            The listener to remove
	 * @return True if the listener was removed; false otherwise.
	 */
	public synchronized boolean removeDynamixListener(IDynamixListener listener) {
		Log.d(TAG, "removeDynamixListener for: " + listener);
		if (listener != null) {
			// Remove the listener
			if (listenerMap.containsKey(listener.asBinder())) {
				Log.i(TAG, this + " is removing listener: " + listener);
				listenerMap.remove(listener.asBinder());
				binderMap.remove(listener.asBinder());
				return true;
			} else
				Log.v(TAG, this + " did not find listener: " + listener + " to remove");
		}
		return false;
	}

	/**
	 * Removes all context support for all registered listeners.
	 * 
	 * @param notify
	 *            True if each listener should be notified; false otherwise.
	 */
	public synchronized void removeAllContextSupport(boolean notify) {
		// Create a snapshot of the support registrations
		List<ContextSupport> supportSnapshot = new Vector<ContextSupport>();
		for (IBinder listener : contextSupportMap.keySet()) {
			supportSnapshot.addAll(contextSupportMap.get(listener));
		}
		for (ContextSupport sub : supportSnapshot)
			removeContextSupport(sub.getDynamixListener(), sub, notify);
	}

	/**
	 * Removes the listener's context support and any associated cached events.
	 * 
	 * @param listener
	 *            The listener to remove context support from.
	 * @param notify
	 *            True if we should notify the listener; false otherwise.
	 */
	public synchronized void removeContextSupport(IDynamixListener listener, boolean notify) {
		Log.d(TAG, "removeContextSupport for: " + listener);
		List<ContextSupport> removeList = new ArrayList<ContextSupport>();
		List<ContextSupport> supportSnapshot = contextSupportMap.get(listener.asBinder());
		if (supportSnapshot != null) {
			for (ContextSupport support : supportSnapshot)
				removeList.add(support);
		} else
			Log.d(TAG, this + " did not find listener: " + listener + " to remove");
		// Remove each support registration
		for (ContextSupport support : removeList)
			removeContextSupport(support.getDynamixListener(), support, notify);
	}

	/**
	 * Removes all context support registrations from the specified plug-in.
	 * 
	 * @param plug
	 *            The plug-in to remove context support for.
	 * @param notify
	 *            True if we should notify the listener; false otherwise.
	 */
	public synchronized void removeContextSupportFromPlugin(ContextPlugin plug, boolean notify) {
		Log.d(TAG, "removeContextSupportForPlugin for: " + plug);
		List<ContextSupport> removeList = new ArrayList<ContextSupport>();
		// Create a snapshot of all support registrations
		Collection<List<ContextSupport>> allSupportRegistrations = contextSupportMap.values();
		// Go through all support registrations, storing those that match the plug-in
		if (allSupportRegistrations != null) {
			for (List<ContextSupport> supportList : allSupportRegistrations) {
				for (ContextSupport sub : supportList) {
					if (sub.getContextPlugin().equals(plug))
						removeList.add(sub);
				}
			}
		}
		// Remove the necessary support registrations
		for (ContextSupport support : removeList)
			removeContextSupport(support.getDynamixListener(), support, notify);
	}

	/**
	 * Removes a specific context support from the listener.
	 * 
	 * @param notify
	 *            True if the listener should be notified; false otherwise.
	 */
	public synchronized Result removeContextSupport(IDynamixListener listener, ContextSupport contextSupport,
			boolean notify) {
		String errorMessage = null;
		int errorCode = 0;
		if (listener != null) {
			if (contextSupport != null) {
				Log.d(TAG, "removeContextSupport " + contextSupport + " for listener: " + listener.asBinder());
				synchronized (contextSupportMap) {
					if (contextSupportMap.keySet().contains(listener.asBinder())) {
						List<ContextSupport> contextSupportRegistrations = contextSupportMap.get(listener.asBinder());
						if (contextSupportRegistrations.contains(contextSupport)) {
							// Remove the support from the listener
							if (contextSupportRegistrations.remove(contextSupport)) {
								// Remove the associated context events from the cache
								DynamixService.removeCachedContextEvents(listener, contextSupport.getContextType());
								// Notify if necessary
								if (notify)
									SessionManager.notifyContextSupportRemoved(
											contextSupport.getDynamixApplication(), contextSupport.getDynamixListener(),
											contextSupport.getContextSupportInfo());
								return new Result();
							} else {
								errorCode = ErrorCodes.DYNAMIX_FRAMEWORK_ERROR;
								errorMessage = "Could not remove support: " + contextSupport;
								Log.w(TAG, "Could not remove support: " + contextSupport);
							}
						} else {
							errorCode = ErrorCodes.NO_CONTEXT_SUPPORT;
							errorMessage = "Context support not present for: " + listener;
							Log.w(TAG, "Context support not present for: " + listener);
						}
					} else {
						errorCode = ErrorCodes.MISSING_PARAMETERS;
						errorMessage = "removeContextSupport called for non-listener: " + listener;
						Log.w(TAG, "removeContextSupport called for non-listener: " + listener);
					}
				}
			} else {
				errorCode = ErrorCodes.MISSING_PARAMETERS;
				errorMessage = "removeContextSupport received null contextSupport";
				Log.w(TAG, "removeContextSupport received null contextSupport");
			}
		} else {
			errorCode = ErrorCodes.MISSING_PARAMETERS;
			errorMessage = "removeContextSupport received null listener";
			Log.w(TAG, "removeContextSupport received null listener");
		}
		return new Result(errorMessage, errorCode);
	}

	/**
	 * Sets the session identifier as a UUID
	 */
	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String toString() {
		return "DynamixSession for app = " + app;
	}

	/**
	 * Update's the application for this session.
	 */
	public void updateApp(DynamixApplication app) {
		this.app = app;
	}
}