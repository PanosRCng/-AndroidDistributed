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

import java.net.MalformedURLException;

import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextPluginInformationResult;
import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.application.IDynamixFacade;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.IdResult;
import org.ambientdynamix.api.application.Result;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

/**
 * The WebFacadeBinder provides an implementation of the IDynamixFacade API for web clients. This class is used in
 * combination with the DynamixService to handle API calls from Dynamix web applications. Note that not all
 * IDynamixFacade methods are supported for web applications.
 * 
 * @see IDynamixFacade
 * @author Darren Carlson
 */
public class WebFacadeBinder extends AppFacadeBinder {
	// Private data
	private final String TAG = this.getClass().getSimpleName();

	protected WebFacadeBinder(Context context, ContextManager conMgr, boolean embeddedMode) {
		super(context, conMgr, embeddedMode);
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public IdResult getListenerId(IDynamixListener listener) {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public void openSession() {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public Result closeSession() {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public IdResult getSessionId() throws RemoteException {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public boolean isSessionOpen() throws RemoteException {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public Result removeAllContextSupport() throws RemoteException {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public Result requestContextPluginInstallation(ContextPluginInformation plugInfo) throws RemoteException {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public Result requestContextPluginUninstall(ContextPluginInformation plugInfo) throws RemoteException {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public ContextPluginInformationResult getAllContextPluginInformation() throws RemoteException {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported for web clients (throws UnsupportedOperationException).
	 */
	@Override
	public ContextPluginInformationResult getContextPluginInformation(String pluginId) throws RemoteException {
		// Throw Unsupported Exception
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDynamixListener(IDynamixListener listener) throws RemoteException {
		Log.d(TAG, "addDynamixListener for (web client): " + listener);
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			WebListener wl = (WebListener) listener;
			SessionManager.addDynamixListener(wl.getWebAppId(), listener);
			// We open a session automatically for web clients
			openSession(wl);
		} else
			Log.w(TAG, "Null listener in addDynamixListener");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeDynamixListener(IDynamixListener listener) throws RemoteException {
		Log.d(TAG, "removeDynamixListener for (web client): " + listener);
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			WebListener wl = (WebListener) listener;
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(wl.getWebAppId());
			boolean notifiedSessionClosed = false;
			// Remove context support, if the app is authorized
			if (app != null) {
				conMgr.removeAllContextSupport(app, listener);
				DynamixSession session = SessionManager.getSession(app);
				/*
				 * Send session closed event here to web clients, even though the app's session may still be open. We do
				 * this because each web client listener automatically has its session opened and closed when it calls
				 * 'addDynamixListener' and 'removeDynamixListener' respectively.
				 */
				if (session != null) {
					SessionManager.notifySessionClosed(app, listener);
					notifiedSessionClosed = true;
				}
			}
			// Remove the listener from the SessionManager
			SessionManager.removeDynamixListener(wl.getWebAppId(), listener, true);
			// We automatically close session for web clients if there are no more listeners
			DynamixSession session = SessionManager.getSession(wl.getWebAppId());
			if (session != null && session.isSessionOpen()) {
				if (session.getDynamixListeners().isEmpty())
					// Close the session and notify listener (if they have not been notified yet)
					if (notifiedSessionClosed)
						closeSession(wl, false);
					else
						closeSession(wl, true);
			}
		} else
			Log.w(TAG, "Null listener in removeDynamixListener");
	}

	/**
	 * Returns a List of both installed and pending ContextPlugins available from Dynamix. You can check the
	 * installation status of a plug-in by calling 'getInstallStatus()' on each ContextPluginInformation entity
	 * contained in the ContextPluginInformationResult.
	 * 
	 * @return A ContextPluginInformationResult indicating success or failure. On success, the
	 *         ContextPluginInformationResult contains the List of context plug-ins.
	 */
	public ContextPluginInformationResult getAllContextPluginInformation(WebListener listener) {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(listener.getWebAppId());
			// Continue if the app is authorized
			if (app != null) {
				return new ContextPluginInformationResult(DynamixService.getAllContextPluginInfo());
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new ContextPluginInformationResult("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Null listener in getContextPluginInformation");
			return new ContextPluginInformationResult("Listener was null in getContextPluginInformation",
					ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * Returns a ContextPluginInformation object for the specified pluginId. You can check the installation status of
	 * the plug-in by calling 'getInstallStatus()' on the ContextPluginInformation entity.
	 * 
	 * @return A ContextPluginInformationResult indicating success or failure. On success, the
	 *         ContextPluginInformationResult contains the requested context plug-in as the only List object.
	 */
	public ContextPluginInformationResult getContextPluginInformation(WebListener listener, String pluginId)
			throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(listener.getWebAppId());
			// Continue if the app is authorized
			if (app != null) {
				for (ContextPluginInformation info : DynamixService.getAllContextPluginInfo()) {
					if (info.getPluginId().equalsIgnoreCase(pluginId))
						return new ContextPluginInformationResult(info);
				}
				return new ContextPluginInformationResult("Plug-in Not Found", ErrorCodes.PLUG_IN_NOT_FOUND);
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new ContextPluginInformationResult("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Null listener in getContextPluginInformation");
			return new ContextPluginInformationResult("Listener was null in getContextPluginInformation",
					ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * Opens a session for the specified WebListener.
	 */
	protected void openSession(WebListener listener) {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			Log.d(TAG, "openSession for web app: " + listener.getWebAppUrl());
			if (DynamixService.isFrameworkInitialized()) {
				doOpenSession(listener);
			} else {
				Log.w(TAG, "DynamixService not initialized during openSession for web client... caching request for "
						+ listener.getWebAppId());
				addCachedUserId(listener.getWebAppId());
			}
		} else
			Log.w(TAG, "Null listener in openSession");
	}

	/**
	 * Closes a session for the specified WebListener.
	 */
	protected Result closeSession(WebListener listener, boolean notify) {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(listener.getWebAppId());
			// Continue if the app is authorized
			if (app != null) {
				conMgr.removeAllContextSupport(app);
				return SessionManager.closeSession(app, notify);
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Null listener in closeSession");
			return new Result("Null listener in closeSession", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * Utility method that attempts to open a Dynamix session for the incoming WebListener.
	 * 
	 * @param listener
	 *            The WebListener wishing to open a session.
	 */
	protected synchronized void doOpenSession(WebListener listener) {
		// Access the application securely... returns null if the app is not authorized
		DynamixApplication app = null;
		app = getAuthorizedApplication(listener.getWebAppId());
		// If the app is not null, it's authorized
		if (app != null) {
			// Open the session for the app
			DynamixSession session = SessionManager.openSession(app);
			// Ping the app and notify it that Dynamix is active
			app.pingConnected();
			// Send notifications
			SessionManager.notifySecurityAuthorizationGranted(app, listener);
			SessionManager.notifySessionOpened(app, listener, session.getSessionId().toString());
			if (DynamixService.isFrameworkStarted())
				SessionManager.notifyDynamixFrameworkActive(app, listener);
			else
				SessionManager.notifyDynamixFrameworkInactive(app, listener);
		} else
		// The App was not authorized, so check if it's new (i.e. not pending)
		if (DynamixService.SettingsManager.checkApplicationPending(listener.getWebAppId())) {
			// Access the pending app
			DynamixApplication pendingApp = getPendingApplication(listener.getWebAppId());
			if (pendingApp != null) {
				// Open the session for the app
				SessionManager.openSession(pendingApp);
				// Ping the app and notify it that Dynamix is active
				pendingApp.pingConnected();
				// Update notifications
				DynamixService.updateNotifications();
				// Send notification
				SessionManager.notifyAwaitingSecurityAuthorization(pendingApp, listener);
			}
		} else {
			// The application is new... so set it up as pending
			if (FrameworkConstants.DEBUG)
				Log.d(TAG, "Web application ID " + listener.getWebAppUrl() + " is new!");
			// Construct a new application for the caller
			DynamixApplication newApp = null;
			try {
				newApp = new DynamixApplication(listener.getWebAppId(), listener.getWebAppUrl());
			} catch (MalformedURLException e) {
				Log.w(TAG, "Error opening session for webapp " + listener.getWebAppUrl() + " | " + e.toString());
			}
			if (newApp != null) {
				// Add a new pendingApp to the SettingsManager
				if (DynamixService.SettingsManager.addPendingApplication(newApp)) {
					// Open the session for the app
					SessionManager.openSession(newApp);
					// Ping the app and update notifications
					newApp.pingConnected();
					// Update notifications
					DynamixService.updateNotifications();
					// Send notifications
					SessionManager.notifyAwaitingSecurityAuthorization(newApp, listener);
				}
			} else
				Log.e(TAG, "App was null after doOpenSession");
		}
	}

	/**
	 * Returns the DynamixApplication associated with the incoming id, or null if the id is not authorized.
	 * 
	 * @param id
	 *            The id of the application.
	 * @return The DynamixApplication associated with the incoming id, or null if the id is not authorized.
	 */
	protected DynamixApplication getAuthorizedApplication(int id) {
		if (FrameworkConstants.DEBUG)
			Log.v(TAG, "Checking authorization for app id: " + id);
		// Check if the application has been authorized to receive events
		if (DynamixService.SettingsManager.checkApplicationAuthorized(id)) {
			// Grab the application from the SettingsMaanger using the calling UID as the key
			DynamixApplication app = DynamixService.SettingsManager.getAuthorizedApplication(id);
			if (app == null)
				Log.e(TAG, "App not found in getAuthorizedApplication. Should not happen");
			else if (FrameworkConstants.DEBUG)
				Log.v(TAG, "Application " + id + " is authorized!");
			return app;
		}
		if (FrameworkConstants.DEBUG)
			Log.d(TAG, "App is not authorized");
		return null;
	}
}
