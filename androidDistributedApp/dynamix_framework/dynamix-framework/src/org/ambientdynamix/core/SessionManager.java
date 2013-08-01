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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.application.IContextInfo;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.core.EventCommand.AwaitingSecurityAuthorization;
import org.ambientdynamix.core.EventCommand.CheckAppLiveliness;
import org.ambientdynamix.core.EventCommand.ContextPluginDiscoveryFinishedCommand;
import org.ambientdynamix.core.EventCommand.ContextPluginDiscoveryStartedCommand;
import org.ambientdynamix.core.EventCommand.ContextPluginErrorCommand;
import org.ambientdynamix.core.EventCommand.ContextPluginInstallFailed;
import org.ambientdynamix.core.EventCommand.ContextPluginInstallProgress;
import org.ambientdynamix.core.EventCommand.ContextPluginInstalled;
import org.ambientdynamix.core.EventCommand.ContextPluginInstalling;
import org.ambientdynamix.core.EventCommand.ContextPluginUninstalledCommand;
import org.ambientdynamix.core.EventCommand.ContextRequestFailed;
import org.ambientdynamix.core.EventCommand.ContextSupportAdded;
import org.ambientdynamix.core.EventCommand.ContextSupportRemoved;
import org.ambientdynamix.core.EventCommand.ContextTypeNotSupported;
import org.ambientdynamix.core.EventCommand.DynamixFrameworkActiveCommand;
import org.ambientdynamix.core.EventCommand.DynamixFrameworkInactiveCommand;
import org.ambientdynamix.core.EventCommand.InstallingContextSupport;
import org.ambientdynamix.core.EventCommand.ListenerAddedCommand;
import org.ambientdynamix.core.EventCommand.ListenerRemovedCommand;
import org.ambientdynamix.core.EventCommand.SecurityAuthorizationGranted;
import org.ambientdynamix.core.EventCommand.SecurityAuthorizationRevoked;
import org.ambientdynamix.core.EventCommand.SessionClosedCommand;
import org.ambientdynamix.core.EventCommand.SessionOpenedCommand;
import org.dynamicjava.api_bridge.ApiBridge;

import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteCallbackList;
import android.util.Log;

/**
 * Manages sessions for Dynamix applications. Includes remote communications facilities. Apps are assigned to one
 * session, but may have multiple listeners. Closing a session automatically removes all associated listeners.
 * 
 * @author Darren Carlson
 */
class SessionManager {
	// Private data
	private final static String TAG = SessionManager.class.getSimpleName();
	private static DynamixCallbackList<IDynamixListener> listeners = new DynamixCallbackList<IDynamixListener>();
	private static Handler broadcastHandler = new Handler();
	private static Map<Integer, DynamixSession> sessionMap = new ConcurrentHashMap<Integer, DynamixSession>();

	// Singleton constructor
	private SessionManager() {
	}

	/**
	 * Adds the IDynamixListener to the RemoteCallbackList and sessionMap. Note: For thread safety, all interactions
	 * with the RemoteCallbackList are handled using a single Handler.
	 * 
	 * @param listener
	 *            The IDynamixListener to add.
	 */
	protected static void addDynamixListener(final int processId, final IDynamixListener listener) {
		broadcastHandler.post(new Runnable() {
			public void run() {
				if (FrameworkConstants.DEBUG)
					Log.d(TAG, "SessionManager is adding listener: " + listener);
				if (listener != null) {
					// Register the listener
					listeners.register(listener);
					// Update the sessionMap
					synchronized (sessionMap) {
						// Update the sessionMap
						if (sessionMap.containsKey(processId)) {
							// Access the session, since it already exists
							DynamixSession session = sessionMap.get(processId);
							// Notify the listener
							session.addDynamixListener(listener, true);
						} else {
							// Create a new session, since we don't have one yet
							DynamixSession session = new DynamixSession(processId);
							sessionMap.put(processId, session);
							// Notify the listener
							session.addDynamixListener(listener, true);
						}
					}
				} else
					Log.w(TAG, "Listener was null in addDynamixListener");
			}
		});
	}

	/**
	 * Refreshes each session's app using the Dynamix database, which updates the app's in memory details, such as
	 * privacy policies for plug-ins.
	 */
	protected static void refreshApps() {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values()) {
				session.refreshApp();
			}
		}
	}

	/**
	 * Returns the collection of all sessions.
	 */
	protected static Collection<DynamixSession> getAllSessions() {
		return sessionMap.values();
	}

	/**
	 * Returns a DynamixSession for the specified DynamixApplication.
	 */
	protected static DynamixSession getSession(DynamixApplication app) {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values()) {
				if (session.isSessionOpen() && session.getApp().equals(app)) {
					// We found the session for the app, so return its listeners
					return session;
				}
			}
		}
		Log.w(TAG, "getSession could not find a session for: " + app);
		return null;
	}

	/**
	 * Returns all context support provided by the specified ContextPlugin.
	 */
	protected static List<ContextSupport> getAllContextSupport(ContextPlugin plug) {
		List<ContextSupport> subs = new ArrayList<ContextSupport>();
		synchronized (sessionMap) {
			for (DynamixSession s : sessionMap.values()) {
				for (ContextSupport sub : s.getAllContextSupport()) {
					if (sub.getContextPlugin().equals(plug)) {
						subs.add(sub);
					}
				}
			}
		}
		return subs;
	}

	/**
	 * Returns the number of context support registrations held by the specified plug-in.
	 */
	protected static int getContextSupportCount(ContextPlugin plug) {
		return getAllContextSupport(plug).size();
	}

	/**
	 * Returns a DynamixSession for the specified processId.
	 */
	protected static DynamixSession getSession(final int processId) {
		synchronized (sessionMap) {
			if (sessionMap.containsKey(processId)) {
				DynamixSession session = sessionMap.get(processId);
				return session;
			}
		}
		Log.w(TAG, "Could not find session for process id: " + processId);
		return null;
	}

	/**
	 * Returns a DynamixSession for the specified UUID.
	 */
	protected static DynamixSession getSession(UUID sessionId) {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values()) {
				if (session.getSessionId().equals(sessionId))
					return session;
			}
		}
		Log.w(TAG, "Could not find session for session UUID: " + sessionId);
		return null;
	}

	/**
	 * Returns true if the session is open for the processId; false otherwise.
	 */
	protected static boolean isSessionOpen(final int processId) {
		synchronized (sessionMap) {
			if (sessionMap.containsKey(processId)) {
				DynamixSession session = sessionMap.get(processId);
				return session.isSessionOpen();
			}
		}
		return false;
	}

	/**
	 * Kills the remote listeners list and then creates a new, fresh one.
	 */
	protected static void killRemoteListeners() {
		broadcastHandler.post(new Runnable() {
			public void run() {
				listeners.kill();
				listeners = new DynamixCallbackList<IDynamixListener>();
			}
		});
	}

	/**
	 * Utility method that notifies all applications that Dynamix is active.
	 */
	protected static void notifyAllDynamixFrameworkActive() {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values()) {
				if (session.isSessionOpen())
					sendEventCommand(session.getApp(), new DynamixFrameworkActiveCommand());
			}
		}
	}

	/**
	 * Utility method that notifies the specific listener that Dynamix is active.
	 */
	protected static void notifyDynamixFrameworkActive(DynamixApplication app, IDynamixListener listener) {
		sendEventCommand(app, listener, new DynamixFrameworkActiveCommand());
	}

	/**
	 * Utility method that notifies all applications that Dynamix is inactive.
	 */
	protected static void notifyAllDynamixFrameworkInactive() {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values()) {
				if (session.isSessionOpen())
					sendEventCommand(session.getApp(), new DynamixFrameworkInactiveCommand());
			}
		}
	}

	/**
	 * Utility method that notifies the specific listener that Dynamix is inactive.
	 */
	protected static void notifyDynamixFrameworkInactive(DynamixApplication app, IDynamixListener listener) {
		sendEventCommand(app, listener, new DynamixFrameworkInactiveCommand());
	}

	/**
	 * Notifies all applications that a new context plug-in is being installed.
	 */
	protected static void notifyAllNewContextPluginInstalling(ContextPlugin plug) {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values())
				sendEventCommand(session.getApp(), new ContextPluginInstalling(plug.getContextPluginInformation()));
		}
	}
	
	/**
	 * Notifies all applications about installing context plug-in progress.
	 * @param plug The installing plug-in.
	 * @param percentComplete The percentage complete.
	 */
	protected static void notifyContextPluginInstallProgress(ContextPlugin plug, int percentComplete){
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values())
				sendEventCommand(session.getApp(), new ContextPluginInstallProgress(plug.getContextPluginInformation(), percentComplete));
		}
	}

	/**
	 * Notifies all applications that a new context plug-in was installed.
	 */
	protected static void notifyAllNewContextPluginInstalled(ContextPlugin plug) {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values())
				sendEventCommand(session.getApp(), new ContextPluginInstalled(plug.getContextPluginInformation()));
		}
	}

	/**
	 * Notifies all applications that the context plug-in installation failed.
	 */
	protected static void notifyAllContextPluginInstallFailed(ContextPlugin plug, String message) {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values())
				sendEventCommand(session.getApp(), new ContextPluginInstallFailed(plug.getContextPluginInformation(),
						message));
		}
	}

	/**
	 * Notifies all applications that the context plug-in was uninstalled.
	 */
	protected static void notifyAllContextPluginUninstalled(ContextPlugin plug) {
		for (DynamixSession session : sessionMap.values()) {
			sendEventCommand(session.getApp(), new ContextPluginUninstalledCommand(plug.getContextPluginInformation()));
		}
	}

	/**
	 * Notifies the application that it is awaiting security authorization.
	 */
	protected static void notifyAwaitingSecurityAuthorization(DynamixApplication app) {
		sendEventCommand(app, new AwaitingSecurityAuthorization());
	}

	/**
	 * Notifies specific listener that it is awaiting security authorization.
	 */
	protected static void notifyAwaitingSecurityAuthorization(DynamixApplication app, IDynamixListener listener) {
		sendEventCommand(app, listener, new AwaitingSecurityAuthorization());
	}

	/**
	 * Notifies the application that the requested context type could not be supported.
	 * 
	 * @param app
	 *            the application to receive the event
	 * @param contextType
	 *            The context type that cannot be supported
	 */
	protected static void notifyContextTypeNotSupported(DynamixApplication app, IDynamixListener listener,
			String contextType) {
		sendEventCommand(app, listener, new ContextTypeNotSupported(contextType));
	}

	/**
	 * Utility method that notifies all applications that context plug-in discovery has started.
	 */
	protected static void notifyAllContextPluginDiscoveryStarted() {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values()) {
				if (session.isSessionOpen())
					sendEventCommand(session.getApp(), new ContextPluginDiscoveryStartedCommand());
			}
		}
	}

	/**
	 * Utility method that notifies all applications that context plug-in discovery has finished.
	 */
	protected static void notifyAllContextPluginDiscoveryFinished(List<ContextPluginInformation> discoveredPlugins) {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values()) {
				if (session.isSessionOpen())
					sendEventCommand(session.getApp(), new ContextPluginDiscoveryFinishedCommand(discoveredPlugins));
			}
		}
	}

	/**
	 * Utility method that notifies all applications that a context plug-in has encountered and error.
	 */
	protected static void notifyAllContextPluginError(ContextPlugin plug, String errorMessage) {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values()) {
				if (session.isSessionOpen())
					sendEventCommand(session.getApp(), new ContextPluginErrorCommand(
							plug.getContextPluginInformation(), errorMessage));
			}
		}
	}

	/**
	 * Sends the incoming list of ContextEvents to IDynamixListeners, as specified by the incoming eventMap. This method
	 * verifies that each listener is authorized to receive the ContextEvent before sending it. For thread safety, all
	 * interactions with the RemoteCallbackList are handled using a single Handler. <br>
	 * <br>
	 * We update each listener using the following adaptive approach: <br>
	 * 1. Try to send the ContextData to the remote client. This will succeed only if the remote client has the proper
	 * classes available in its classpath for the Parcelable contained within the ContextData's eventData. Otherwise,
	 * we'll generate an exception. <br>
	 * 2. If sending the event causes an exception, it's most probably due to the lack of proper ContextData classes on
	 * the remote side (necessary for resolving the ContextData's Percelable eventData.) In this case, null out the
	 * eventData and re-send the event, which still contains strings. The client can still use the string representation
	 * of the eventData.
	 * 
	 * @param eventMap
	 *            A Map containing DynamixSession entities and their associated SecuredEvents.
	 */
	protected static void notifyContextListeners(final Map<IDynamixListener, List<ContextEvent>> eventMap) {
		if (FrameworkConstants.DEBUG)
			Log.v(TAG, "notifyContextListeners with eventMap of size: " + eventMap.size());
		/*
		 * To ensure thread safety, we always manage our listeners using the eventHandler. Note that only one broadcast
		 * can be active at a time, so you must be sure to always call this from the same thread (usually by scheduling
		 * with Handler or do your own synchronization. You must call finishBroadcast() when done.
		 */
		broadcastHandler.post(new Runnable() {
			public void run() {
				// Begin the broadcast (N represents the size of the listener list)
				final int N = listeners.beginBroadcast();
				if (FrameworkConstants.DEBUG)
					Log.v(TAG, "notifyContextListeners for " + N + " listeners");
				// Loop through the listener list
				for (int i = 0; i < N; i++) {
					/*
					 * TODO: dispatch events using threads to prevent receiver from blocking Dynamix?
					 */
					IDynamixListener listener = listeners.getBroadcastItem(i);
					// See if we can match the listener to a listener in the eventMap
					IDynamixListener receiver = null;
					for (IDynamixListener l : eventMap.keySet()) {
						if (l.asBinder().equals(listener.asBinder())) {
							receiver = l;
							break;
						}
					}
					if (receiver != null) {
						List<ContextEvent> events = eventMap.get(receiver);
						/*
						 * We update the listener using the following adaptive approach: 1. Try to send the ContextData
						 * to the remote client. This will succeed only if the remote client has the proper classes
						 * available in its classpath for the Parcelable contained within the ContextData's eventData.
						 * Otherwise, we'll generate an exception. 2. If sending the event causes an exception, it's
						 * most probably due to the lack of proper ContextData classes at the remote side (necessary for
						 * resolving the ContextData's Percelable eventData.) In this case, remove the eventData and
						 * re-send the event, which still contains strings. The client can still use the string
						 * representation of the eventData.
						 */
						for (ContextEvent event : events) {
							if (FrameworkConstants.DEBUG)
								Log.v(TAG, "Trying to send FULL ContextEvent listener: " + receiver);
							// First, try sending event with context info attached...
							event.attachContextInfo(true);
							/*
							 * Setup embedded mode, if set. Since events contain IContextData from dynamically loaded
							 * OSGi classes, embedded hosts cannot cast event data properly, since the event
							 * IContextData objects will have different classloaders from the embedded host. To overcome
							 * this issue, we create a proxy object that bridges the OSGi classloader to the embedded
							 * host's classloader.
							 */
							if (DynamixService.isEmbedded() && event.hasIContextInfo()) {
								try {
									// Update the IContextInfo object with a deep proxy
									event.setIContextInfo((IContextInfo) createDeepProxy(event.getIContextInfo(), true));
								} catch (Exception e) {
									Log.w(TAG, "Could not create proxy for: " + event.getIContextInfo());
									break;
								}
							}
							try {
								// Try sending the event with the context info attached
								listener.onContextEvent(event);
								if (FrameworkConstants.DEBUG)
									Log.v(TAG, "Full ContextEvent successfully sent!");
							} catch (Exception e) {
								DynamixApplication app = DynamixService.getDynamixApplicationByUid(event
										.getTargetAppId());
								// Check for memory errors
								if (event.getStreamController() != null && event.getStreamController().outOfMemory()) {
									sendEventCommand(app, listener, new ContextRequestFailed(event.getResponseId(),
											"ContextEvent contained too much data. Try reducing query scope.",
											ErrorCodes.TOO_MUCH_DATA_REQUESTED));
								} else {
									/*
									 * Try sending the event without the attachment, since the error is most-likely
									 * related to the client not having proper classes on the class-path.
									 */
									event.attachContextInfo(false);
									try {
										if (FrameworkConstants.DEBUG)
											Log.d(TAG, "Trying to send string-only ContextEvent: " + event);
										listener.onContextEvent(event);
										if (FrameworkConstants.DEBUG)
											Log.v(TAG, "String-only ContextEvent successfully sent!");
									} catch (Exception e1) {
										if (FrameworkConstants.DEBUG)
											Log.e(TAG,
													"notifyContextListeners failed to update listener after removing eventData: "
															+ e1.getMessage());
										sendEventCommand(app, listener, new ContextRequestFailed(event.getResponseId(),
												"onContextEvent method raised exception: " + e1,
												ErrorCodes.APPLICATION_EXCEPTION));
									}
								}
							}
						}
					}
				}
				listeners.finishBroadcast();
			}
		});
	}

	/**
	 * Notifies the listener that context support has been added.
	 * 
	 * @param app
	 *            the application to receive the event
	 * @param plugin
	 *            The ContextPluginInformation handling the context support
	 * @param supportInfo
	 *            The ContextSupportInfo that was added.
	 */
	protected static void notifyContextSupportAdded(DynamixApplication app, IDynamixListener listener,
			ContextSupportInfo supportInfo) {
		sendEventCommand(app, listener, new ContextSupportAdded(supportInfo));
	}

	/**
	 * Notifies the listener that context support has been removed.
	 * 
	 * @param app
	 *            The application to receive the event
	 * @param listener
	 *            The listener that will receive the event.
	 * @param supportInfo
	 *            The ContextSupportInfo that was removed.
	 * @param contextType
	 *            The context type of the support removed.
	 */
	protected static void notifyContextSupportRemoved(DynamixApplication app, IDynamixListener listener,
			ContextSupportInfo supportInfo) {
		sendEventCommand(app, listener, new ContextSupportRemoved(supportInfo));
	}

	/**
	 * Notifies the listener that it was added.
	 */
	protected static void notifyDynamixListenerAdded(IDynamixListener listener, String listenerId) {
		sendEventCommand(listener, new ListenerAddedCommand(listenerId));
	}

	/**
	 * Notifies the application that a context plugin is being installed
	 * 
	 * @param app
	 *            the application to receive the event
	 * @param plugin
	 *            Information about the ContextPlugin being installed
	 */
	protected static void notifyInstallingContextPlugin(DynamixApplication app, IDynamixListener listener,
			ContextPluginInformation plugin) {
		sendEventCommand(app, listener, new ContextPluginInstalling(plugin));
	}

	/**
	 * Notifies the application that context support is being installed for a previously requested context type.
	 * 
	 * @param app
	 *            the application to receive the event
	 * @param plugin
	 *            The ContextPluginInformation of the context plugin being installed to provide the context support.
	 * @param contextDataType
	 *            The context type of the support
	 */
	protected static void notifyInstallingContextSupport(DynamixApplication app, IDynamixListener listener,
			ContextPluginInformation plugin, String contextDataType) {
		sendEventCommand(app, listener, new InstallingContextSupport(plugin, contextDataType));
	}

	/**
	 * Notifies the application that its security authorization has been granted
	 * 
	 * @param app
	 *            the application to receive the event
	 */
	protected static void notifySecurityAuthorizationGranted(DynamixApplication app) {
		sendEventCommand(app, new SecurityAuthorizationGranted());
	}

	/**
	 * Notifies the specific listener that its security authorization has been granted
	 */
	protected static void notifySecurityAuthorizationGranted(DynamixApplication app, IDynamixListener listener) {
		sendEventCommand(app, listener, new SecurityAuthorizationGranted());
	}

	/**
	 * Notifies the application that its security authorization has been revoked
	 * 
	 * @param app
	 *            the application to receive the event
	 */
	protected static void notifySecurityAuthorizationRevoked(DynamixApplication app) {
		sendEventCommand(app, new SecurityAuthorizationRevoked());
	}

	/**
	 * Notifies the application that the Dynamix Framework session is open or closed.
	 */
	protected static void notifySessionClosed(DynamixApplication app) {
		sendEventCommand(app, new SessionClosedCommand());
	}

	/**
	 * Notifies the specific listener that the Dynamix Framework session is open or closed.
	 */
	protected static void notifySessionClosed(DynamixApplication app, IDynamixListener listener) {
		sendEventCommand(app, listener, new SessionClosedCommand());
	}

	/**
	 * Notifies the application that the Dynamix Framework session is open or closed.
	 */
	protected static void notifySessionOpened(DynamixApplication app, String sessionId) {
		sendEventCommand(app, new SessionOpenedCommand(sessionId));
	}

	/**
	 * Notifies the specific listener that the Dynamix Framework session is open or closed.
	 */
	protected static void notifySessionOpened(DynamixApplication app, IDynamixListener listener, String sessionId) {
		sendEventCommand(app, listener, new SessionOpenedCommand(sessionId));
	}

	/**
	 * Opens the session for the specified app.
	 * 
	 * @param app
	 *            The Dynmaix app wishing to open a session
	 * @return A active DynamixSession
	 */
	protected static DynamixSession openSession(DynamixApplication app) {
		synchronized (sessionMap) {
			Log.d(TAG, "openSession for: " + app);
			// Check for existing session
			if (sessionMap.containsKey(app.getAppID())) {
				// Handle existing session case
				DynamixSession session = sessionMap.get(app.getAppID());
				Log.d(TAG,
						"openSession found existing session for: " + app + " with session open state: "
								+ session.isSessionOpen());
				// Open the session for the app
				if (!session.isSessionOpen())
					session.openSession(app);
				else
					Log.d(TAG, "openSession found that the session was already opened for: " + app);
				return session;
			} else {
				// Handle new session case
				DynamixSession session = new DynamixSession(app.getAppID());
				session.openSession(app);
				sessionMap.put(app.getAppID(), session);
				Log.d(TAG, "openSession created new session for: " + app);
				return session;
			}
		}
	}

	/**
	 * Closes the session for the specified app.
	 * 
	 * @param app
	 *            The Dynmaix app wishing to open a session
	 * @param notify
	 *            True to notify sessions of close; false otherwise.
	 * @return Result indicating success or fail.
	 */
	protected static Result closeSession(DynamixApplication app, boolean notify) {
		synchronized (sessionMap) {
			Log.d(TAG, "closeSession for: " + app);
			// Check for existing session
			if (app != null && sessionMap.containsKey(app.getAppID())) {
				/*
				 * We need to keep the closed session in the sessionMap in order to retain any registered Dynamix
				 * listeners. Subsequent calls to openSession will re-populate the session.
				 */
				DynamixSession session = sessionMap.get(app.getAppID());
				session.closeSession(notify);
				return new Result();
			} else {
				Log.w(TAG, "No session found for: " + app);
				return new Result("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
			}
		}
	}

	/**
	 * Closes all sessions
	 * 
	 * @param notify
	 *            True to notify sessions of close; false otherwise.
	 */
	protected static void closeAllSessions(boolean notify) {
		Collection<DynamixSession> sessions = sessionMap.values();
		for (DynamixSession session : sessions)
			closeSession(session.getApp(), notify);
	}

	/**
	 * Removes the IDynamixListener from its associated session and the RemoteCallbackList of registered listeners.
	 * Note: For thread safety, all interactions with the RemoteCallbackList are handled using a single Handler.
	 * 
	 * @param listener
	 *            The IDynamixListener to remove.
	 */
	protected static void removeDynamixListener(final IDynamixListener listener, final boolean notify) {
		Log.d(TAG, "removeDynamixListener for: " + listener);
		broadcastHandler.post(new Runnable() {
			public void run() {
				// Since we don't know the session the listener is in, try to remove it from all sessions and break on a
				// match
				for (DynamixSession session : sessionMap.values()) {
					if (session.isDynamixListenerRegistered(listener)) {
						session.removeContextSupport(listener, notify);
						session.removeDynamixListener(listener);
						// Handle notifications
						if (notify)
							removeDynamixListenerAndNotify(listener);
						else
							listeners.unregister(listener);
						// Break, since we're finished updating the session
						break;
					}
				}
			}
		});
	}

	/**
	 * Removes all context support for the specified plug-in.
	 * 
	 * @param plug
	 *            The plug-in to remove context support from.
	 * @param notify
	 *            True if we should notify the listener; false otherwise.
	 */
	protected static void removeContextSupportForPlugin(final ContextPlugin plug, final boolean notify) {
		Log.d(TAG, "removeContextSupportForPlugin for: " + plug);
		synchronized (sessionMap) {
			for (DynamixSession s : sessionMap.values())
				s.removeContextSupportFromPlugin(plug, notify);
		}
	}

	/**
	 * Removes the IDynamixListener from the specified session and the RemoteCallbackList of registered listeners. Note:
	 * For thread safety, all interactions with the RemoteCallbackList are handled using a single Handler.
	 * 
	 * @param listener
	 *            The IDynamixListener to remove.
	 */
	protected static void removeDynamixListener(final int processId, final IDynamixListener listener,
			final boolean notify) {
		Log.d(TAG, "removeDynamixListener for processId: " + processId + " and listener: " + listener);
		broadcastHandler.post(new Runnable() {
			public void run() {
				Log.d(TAG, "SessionManager is removing listener: " + listener);
				if (listener != null) {
					// Update the sessionMap
					synchronized (sessionMap) {
						// Update the sessionMap
						if (sessionMap.containsKey(processId)) {
							DynamixSession session = sessionMap.get(processId);
							session.removeContextSupport(listener, notify);
							session.removeDynamixListener(listener);
							// Handle notifications
							if (notify)
								removeDynamixListenerAndNotify(listener);
							else
								listeners.unregister(listener);
						} else {
							Log.w(TAG, "removeDynamixListener with no existing session");
						}
					}
				} else
					Log.w(TAG, "Listener was null in removeDynamixListener");
			}
		});
	}

	/**
	 * Removes the listener and notifies it of the removal.
	 */
	protected static void removeDynamixListenerAndNotify(IDynamixListener listener) {
		sendEventCommandAndRemoveReceiver(listener, new ListenerRemovedCommand());
	}

	/**
	 * Sends the IEventCommand to a single receiver.
	 */
	protected static void sendEventCommand(final DynamixApplication app, final IDynamixListener receiver,
			final IEventCommand eventCommand) {
		List<IDynamixListener> receivers = new Vector<IDynamixListener>();
		receivers.add(receiver);
		DynamixSession session = getSession(app);
		if (session != null)
			doSendEventCommand(session, receivers, eventCommand, null);
		else
			Log.w(TAG, "Could not find session for app: " + app);
	}

	/**
	 * Sends the IEventCommand to ALL listeners in the session.
	 */
	protected static void sendEventCommand(final DynamixApplication app, final IEventCommand eventCommand) {
		DynamixSession session = getSession(app);
		if (session != null)
			doSendEventCommand(session, session.getDynamixListeners(), eventCommand, null);
		else
			Log.w(TAG, "Could not find session for app: " + app);
	}

	/**
	 * Sends the IEventCommand without an associated application. This method is used for notifying listeners that don't
	 * have an established session yet (e.g., notifying that the listener was added).
	 */
	protected static void sendEventCommand(final IDynamixListener receiver, final IEventCommand eventCommand) {
		List<IDynamixListener> receivers = new Vector<IDynamixListener>();
		receivers.add(receiver);
		doSendEventCommand(null, receivers, eventCommand, null);
	}

	/**
	 * Sends the IEventCommand without an associated application (useful when the session is not yet established). This
	 * method unregisters the receivers from the remote callback list at the end of the call.
	 */
	protected static void sendEventCommandAndRemoveReceiver(final IDynamixListener receiver,
			final IEventCommand eventCommand) {
		List<IDynamixListener> receivers = new Vector<IDynamixListener>();
		receivers.add(receiver);
		doSendEventCommand(null, receivers, eventCommand, receivers);
	}

	/**
	 * Updates a DynamixApplication in this session while retaining the session's original binder and listeners.
	 */
	protected static boolean updateSessionApplication(DynamixApplication app) {
		synchronized (sessionMap) {
			for (DynamixSession session : sessionMap.values()) {
				if (session.isSessionOpen() && session.getApp().equals(app)) {
					Log.d(TAG, "updateSessionApplication replaced app: " + app);
					session.updateApp(app);
					return true;
				}
			}
		}
		Log.w(TAG, "updateSession could not find: " + app);
		return false;
	}

	/**
	 * Handles event sending logic
	 * 
	 * @param session
	 *            The session to receive the IEventCommand
	 * @param receivers
	 *            The list of receivers to receive the IEventCommand
	 * @param eventCommand
	 *            The IEventCommand to send
	 * @param receiversToRemove
	 *            A list of receivers to remove after sending the IEventCommand
	 */
	private static void doSendEventCommand(final DynamixSession session, final List<IDynamixListener> receivers,
			final IEventCommand eventCommand, final List<IDynamixListener> receiversToRemove) {
		// Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		/*
		 * Handle send delay for the incoming eventCommand
		 */
		if (eventCommand.hasSendDelay() && !eventCommand.deliveryDelayElapsed()) {
			Timer t = new Timer(true);
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					doSendEventCommand(session, receivers, eventCommand, receiversToRemove);
				}
			}, eventCommand.getDeliveryTime());
		} else {
			if (receivers != null) {
				if (!(eventCommand instanceof CheckAppLiveliness))
					if (FrameworkConstants.DEBUG)
						Log.v(TAG, "doSendEventCommand with receiver count: " + receivers.size());
				/*
				 * To ensure thread safety, we always manage the 'listeners' using the eventHandler. Note that only one
				 * broadcast can be active at a time, so you must be sure to always call this from the same thread
				 * (usually by scheduling with Handler or do your own synchronization. You must call finishBroadcast()
				 * when done.
				 */
				broadcastHandler.post(new Runnable() {
					public void run() {
						// Begin the broadcast (N represents the size of the listener list)
						final int N = listeners.beginBroadcast();
						if (!(eventCommand instanceof CheckAppLiveliness))
							if (FrameworkConstants.DEBUG)
								Log.v(TAG, "notifyApplication listener size: " + N);
						// Handle pre-processing for the incoming EventCommand
						eventCommand.preProcess();
						// Run the eventCommand on each of the listeners associated with each of the cached apps in the
						// appMap
						for (int i = 0; i < N; i++) {
							try {
								IDynamixListener listener = listeners.getBroadcastItem(i);
								for (IDynamixListener e : receivers) {
									if (listener.asBinder().equals(e.asBinder())) {
										if (!(eventCommand instanceof CheckAppLiveliness))
											if (FrameworkConstants.DEBUG)
												Log.v(TAG, "Sending " + eventCommand + " to listener: " + listener);
										if (listener.asBinder().isBinderAlive()) {
											eventCommand.processCommand(session, e);
										}
									}
								}
							} catch (Exception e) {
								// The RemoteCallbackList will take care of removing the dead object for us.
								Log.w(TAG, "RemoteException " + e);
							}
						}
						// Finish the broadcast
						listeners.finishBroadcast();
						// Handle post-processing for the incoming EventCommand
						eventCommand.postProcess();
						// Handle listener removal, if necessary
						if (receiversToRemove != null) {
							for (IDynamixListener l : receiversToRemove)
								listeners.unregister(l);
						}
					}
				});
			} else
				Log.w(TAG, "sendEventCommand received null receiver list!");
		}
	}

	/*
	 * This method is used to wrap the incoming object with OSGi-compatible proxies, which enable a non-OSGi application
	 * to utilize dynamically loaded classes (e.g., context event POJOs from ContextPlugins). Recursively introspects
	 * the incoming Object and replaces all fields of type Parcelable with OSGi-compatible proxied versions using Java
	 * reflection (including all fields on all objects in the original object's Hierarchy). Supports proxying of
	 * individual fields and Collection-based fields (not Map fields, however).
	 */
	private static Object createDeepProxy(Object object, boolean isRoot) throws IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		// Make sure Dynamix has an instance of the hosting client's class-loader
		if (DynamixService.hasEmbeddedHostClassLoader()) {
			// Grab the IContextInfo.class name for IContextInfo bridging
			String contextInfoClass = IContextInfo.class.getPackage().getName();
			for (Field f : getAllFields(object.getClass())) {
				// Remember if the field was accessible
				boolean originallyAccessable = f.isAccessible();
				// Make the field accessible, if necessary
				if (!originallyAccessable)
					f.setAccessible(true);
				// Grab the field
				Object field = f.get(object);
				// Check if the field implements Parcelable
				if (implementsInterface(field, Parcelable.class)) {
					// We also bridge the IContextInfo implementation class (if possible)
					String implimentationClass = field.getClass().getPackage().getName();
					// Create the ApiBridge
					ApiBridge apiBridge = null;
					// Handle implementations of IContextInfo
					if (implementsInterface(field, IContextInfo.class)) {
						apiBridge = ApiBridge.getApiBridge(DynamixService.getEmbeddedHostClassLoader(),
								contextInfoClass, implimentationClass);
					} else {
						apiBridge = ApiBridge.getApiBridge(DynamixService.getEmbeddedHostClassLoader(),
								implimentationClass);
					}
					// Continue introspection of the Parcelable object
					field = createDeepProxy(field, false);
					// Update the field
					f.set(object, apiBridge.bridge(field, true));
				}
				// For a collection, check if it holds Parcelables
				if (implementsInterface(field, Collection.class)) {
					Collection<?> embeddedCollection = null;
					Collection<?> coll = (Collection<?>) f.get(object);
					if (coll != null && !coll.isEmpty()) {
						// Test the first object in the collection for implementation of Parcelable
						Iterator<?> itr = coll.iterator();
						while (itr.hasNext()) {
							Object inter = itr.next();
							if (implementsInterface(inter, Parcelable.class)) {
								// We have a match, so update the embeddedCollection and break
								embeddedCollection = coll;
								break;
							}
						}
					}
					if (embeddedCollection != null) {
						// Create an instance of the original collection type for later injection
						@SuppressWarnings("rawtypes")
						Collection proxiedCollection = coll.getClass().newInstance();
						// Proxy each Parcelable in the embeddedCollection
						for (Object p : embeddedCollection) {
							// We also bridge the IContextInfo implementation class (if possible)
							String implimentationClass = p.getClass().getPackage().getName();
							// Create the ApiBridge
							ApiBridge apiBridge = null;
							// Handle implementations of IContextInfo
							if (implementsInterface(field, IContextInfo.class)) {
								apiBridge = ApiBridge.getApiBridge(DynamixService.getEmbeddedHostClassLoader(),
										contextInfoClass, implimentationClass);
							} else {
								apiBridge = ApiBridge.getApiBridge(DynamixService.getEmbeddedHostClassLoader(),
										implimentationClass);
							}
							// Add the proxied class to the proxiedCollection
							proxiedCollection.add(apiBridge.bridge(p, true));
							// Introspect the IContextInfo object for more embedded IContextInfo objects
							p = createDeepProxy(p, false);
						}
						// Update the field with the proxiedCollection
						f.set(object, proxiedCollection);
					}
					// Restore original accessible state
					f.setAccessible(originallyAccessable);
				}
			}
			// TODO: Also handle Maps with embedded Parcelables?
			if (isRoot) {
				// We also bridge the IContextInfo implementation class (if possible)
				String rootClass = object.getClass().getPackage().getName();
				// Create the ApiBridge
				ApiBridge apiBridge = null;
				// Handle implementations of IContextInfo
				if (implementsInterface(object, IContextInfo.class)) {
					apiBridge = ApiBridge.getApiBridge(DynamixService.getEmbeddedHostClassLoader(), contextInfoClass,
							rootClass);
				} else {
					apiBridge = ApiBridge.getApiBridge(DynamixService.getEmbeddedHostClassLoader(), rootClass);
				}
				// Bridge the root object
				object = apiBridge.bridge(object, true);
			}
		} else
			Log.w(TAG, "No host class-loader set, unable to proxy object!");
		return object;
	}

	/**
	 * Returns true if the Object implements the specified interface; false otherwise.
	 * 
	 * @param object
	 *            The Object to check.
	 * @param interf
	 *            The interface to check.
	 */
	private static Boolean implementsInterface(Object object, Class interf) {
		return interf.isInstance(object);
	}

	/**
	 * Returns all the fields of the specified Class, and it's super-classes.
	 * 
	 * @param type
	 *            The class to extract fields from.
	 */
	private static List<Field> getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();
		for (Field field : type.getDeclaredFields()) {
			fields.add(field);
		}
		if (type.getSuperclass() != null) {
			fields.addAll(getAllFields(type.getSuperclass()));
		}
		return fields;
	}

	/**
	 * Utility RemoteCallbackList that removes ContextListeners using the ContextManager if onCallbackDied is called.
	 * 
	 * @author Darren Carlson
	 */
	private static class DynamixCallbackList<E> extends RemoteCallbackList<IDynamixListener> {
		@Override
		public void onCallbackDied(final IDynamixListener listener) {
			super.onCallbackDied(listener);
			Log.w(TAG, "Listener died and will be removed: " + listener);
			// Remove the listener
			DynamixService.removeDynamixListener(listener);
		}
	}
}
