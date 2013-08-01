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

import java.util.List;
import java.util.Vector;

import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextPluginInformationResult;
import org.ambientdynamix.api.application.ContextSupportConfig;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.ContextSupportResult;
import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.application.IDynamixFacade;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.IdResult;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.core.DynamixService.IDynamixFrameworkListener;
import org.ambientdynamix.update.contextplugin.DiscoveredContextPlugin;
import org.ambientdynamix.util.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

/**
 * The AppFacadeBinder provides an implementation of the IDynamixFacade API, as defined through AIDL. This class is used
 * in combination with the DynamixService to handle API calls from Dynamix applications.
 * 
 * @see IDynamixFacade
 * @author Darren Carlson
 */
class AppFacadeBinder extends IDynamixFacade.Stub implements IDynamixFrameworkListener {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private Context context;
	private boolean embeddedMode;
	protected ContextManager conMgr;
	protected static List<Integer> cachedUserIds = new Vector<Integer>();

	/**
	 * Creates a AppFacadeBinder
	 */
	protected AppFacadeBinder(Context context, ContextManager conMgr, boolean embeddedMode) {
		this.context = context;
		this.conMgr = conMgr;
		this.embeddedMode = embeddedMode;
		DynamixService.addDynamixFrameworkListener(this);
	}

	/**
	 * Adds the user id to the list of cached ids, whose sessions are opened once Dynamix is initialized.
	 * 
	 * @param id
	 *            The id to cache.
	 */
	protected void addCachedUserId(int id) {
		synchronized (cachedUserIds) {
			if (!cachedUserIds.contains(id))
				cachedUserIds.add(id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDynamixActive() throws RemoteException {
		return DynamixService.isFrameworkStarted();
	}
	
	@Override
	public boolean isPanosMaster() throws RemoteException{
		return DynamixService.isPanosMaster();
	}

	@Override
	public void stopPlugin(String pluginId) throws RemoteException{
				
		ContextPlugin conplug = conMgr.getContextPlugin(pluginId);
		DynamixService.stopPlugin(conplug);
		
//		conMgr.getContextPlugin(pluginId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result addContextSupport(final IDynamixListener listener, final String contextType) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null && contextType != null) {
			// We only allow adding context support when Dynamix is started
			if (DynamixService.isFrameworkStarted()) {
				// Access the application securely... returns null if the app is not authorized
				final DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
				// Continue if the app is authorized
				if (app != null) {
					// Try to grab the cached app from the DynamixService
					DynamixSession session = SessionManager.getSession(app);
					if (session != null && session.isSessionOpen()) {
						// This method is asynchronous and runs in a dedicated thread
						doAddContextSupport(app, listener, contextType, null);
						return new Result();
					} else
						Log.w(TAG, "addContextSupport could not find open session for: " + app);
					return new Result("Session Not Found", ErrorCodes.SESSION_NOT_FOUND);
				} else {
					Log.w(TAG, app + " is not authorized!");
					return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
				}
			} else {
				Log.w(TAG, "Dynamix not started!");
				return new Result("Dynamix not started!", ErrorCodes.NOT_READY);
			}
		} else
			return new Result("All parameters required", ErrorCodes.MISSING_PARAMETERS);
	}

	@Override
	public Result addConfiguredContextSupport(IDynamixListener listener, Bundle contextConfiguration)
			throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null && contextConfiguration != null) {
			if (DynamixService.isFrameworkInitialized()) {
				// Access the application securely... returns null if the app is not authorized
				DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
				// Continue if the app is authorized
				if (app != null) {
					// App is authorized, check for session and context support
					DynamixSession session = SessionManager.getSession(app);
					if (session != null && session.isSessionOpen()) {
						String contextType = contextConfiguration.getString(ContextSupportConfig.CONTEXT_TYPE);
						String pluginId = contextConfiguration.getString(ContextSupportConfig.REQUESTED_PLUGIN);
						// TODO: Handle versions - currently we use the latest version available
						String pluginVersion = contextConfiguration
								.getParcelable(ContextSupportConfig.REQUESTED_PLUGIN_VERSION);
						doAddContextSupport(app, listener, contextType, pluginId);
						return new Result();
					} else {
						Log.w(TAG, "could not find open session for: " + app);
						return new Result("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
					}
				} else {
					Log.w(TAG, app + " is not authorized!");
					return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
				}
			} else {
				Log.w(TAG, "Dynamix not started!");
				return new Result("Dynamix not initialized!", ErrorCodes.NOT_READY);
			}
		} else {
			return new Result("All Parameters Required", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDynamixListener(IDynamixListener listener) throws RemoteException {
		if (listener != null) {
			Log.d(TAG, "addDynamixListener for: " + listener);
			// Make sure Looper.prepare has been called for the incoming Thread
			setupThreadLooper();
			SessionManager.addDynamixListener(getCallerId(listener), listener);
		} else
			Log.w(TAG, "Listener was null in addDynamixListener");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeDynamixListener(IDynamixListener listener) throws RemoteException {
		Log.d(TAG, "removeDynamixListener for: " + listener);
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		// Access the application securely... returns null if the app is not authorized
		DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
		// Remove support, if the app is authorized
		if (app != null) {
			conMgr.removeAllContextSupport(app, listener);
		}
		// Remove the listener from the SessionManager
		SessionManager.removeDynamixListener(getCallerId(listener), listener, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContextPluginInformationResult getAllContextPluginInformation() throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		// Access the application securely... returns null if the app
		DynamixApplication app = getAuthorizedApplication(getCallerId(null));
		// Continue if the app is authorized
		if (app != null)
			return new ContextPluginInformationResult(DynamixService.getAllContextPluginInfo());
		else {
			Log.w(TAG, app + " is not authorized!");
			return new ContextPluginInformationResult("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContextPluginInformationResult getContextPluginInformation(String pluginId) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		// Access the application securely... returns null if the app
		DynamixApplication app = getAuthorizedApplication(getCallerId(null));
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContextSupportResult getContextSupport(IDynamixListener listener) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			// Try to grab the cached app from the DynamixService
			DynamixSession session = SessionManager.getSession(getCallerId(listener));
			if (session != null) {
				return conMgr.getContextSupport(session.getApp(), listener);
			} else {
				Log.w(TAG, "could not find open session for: " + listener);
				return new ContextSupportResult("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
			}
		} else {
			Log.w(TAG, "Listener was null in getContextSupport");
			return new ContextSupportResult("Listener was null in getContextSupport", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IdResult getListenerId(IDynamixListener listener) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			DynamixSession session = SessionManager.getSession(getCallerId(listener));
			if (session != null) {
				return new IdResult(session.getDynamixListenerId(listener));
			} else {
				Log.w(TAG, "could not find open session for: " + listener);
				return new IdResult("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
			}
		} else {
			Log.w(TAG, "Listener was null in getListenerId");
			return new IdResult("Listener was null in getListenerId", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IdResult getSessionId() throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		// Access the application securely... returns null if the app
		DynamixApplication app = getAuthorizedApplication(getCallerId(null));
		// Continue if the app is authorized
		if (app != null) {
			// App is authorized
			DynamixSession session = SessionManager.getSession(app);
			if (session != null && session.isSessionOpen()) {
				return new IdResult(session.getSessionId().toString());
			} else {
				Log.w(TAG, "could not find open session for: " + app);
				return new IdResult("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
			}
		} else {
			Log.w(TAG, app + " is not authorized!");
			return new IdResult("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSessionOpen() throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		return SessionManager.isSessionOpen(getCallerId(null));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void openSession() {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		int userId = getCallerId(null);
		Log.d(TAG, "openSession for process: " + userId);
		if (DynamixService.isFrameworkInitialized()) {
			doOpenSession(userId);
		} else {
			Log.w(TAG, "DynamixService not initialized during openSession... caching request for: " + userId);
			addCachedUserId(userId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result closeSession() {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		// Access the application securely... returns null if the app
		DynamixApplication app = getAuthorizedApplication(getCallerId(null));
		// Continue if the app is authorized
		if (app != null) {
			conMgr.removeAllContextSupport(app);
			return SessionManager.closeSession(app, true);
		} else {
			Log.w(TAG, app + " is not authorized!");
			return new ContextPluginInformationResult("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result removeAllContextSupport() throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		// Access the application securely... returns null if the app is not authorized
		DynamixApplication app = getAuthorizedApplication(getCallerId(null));
		// Continue if the app is authorized
		if (app != null) {
			// App is authorized
			return conMgr.removeAllContextSupport(app);
		} else {
			Log.w(TAG, app + " is not authorized!");
			return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result removeContextSupport(final IDynamixListener listener, ContextSupportInfo supportInfo)
			throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null && supportInfo != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
			// Continue if the app is authorized
			if (app != null) {
				// App is authorized
				return conMgr.removeContextSupport(app, listener, supportInfo);
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Missing parameters in removeContextSupport");
			return new Result("Missing parameters in removeContextSupport", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result removeContextSupportForContextType(final IDynamixListener listener, String contextType)
			throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null && contextType != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
			// Continue if the app is authorized
			if (app != null) {
				// App is authorized
				return conMgr.removeContextSupportForContextType(app, listener, contextType);
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Missing parameters in removeContextSupportForContextType");
			return new Result("Missing parameters in removeContextSupportForContextType", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result removeAllContextSupportForListener(final IDynamixListener listener) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
			// Continue if the app is authorized
			if (app != null) {
				// App is authorized
				return conMgr.removeAllContextSupport(app, listener);
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Null listener in removeAllContextSupportForListener");
			return new Result("Null listener in removeAllContextSupportForListener", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IdResult configuredContextRequest(IDynamixListener listener, String pluginId, String contextType,
			Bundle scanConfig) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		// Make sure Dynamix is active
		if (isDynamixActive()) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
			// Continue if the app is authorized
			if (app != null) {
				// App is authorized, check for session and context support
				DynamixSession session = SessionManager.getSession(app);
				if (session != null && session.isSessionOpen()) {
					// Request the context scan and return the result
					IdResult result = DynamixService.handleContextRequest(app, session, listener, pluginId,
							contextType, scanConfig);
					if (!result.wasSuccessful())
						Log.w(TAG, "Request Context Scan Failed: " + result.getMessage());
					return result;
				} else {
					Log.w(TAG, "could not find open session for: " + app);
					return new IdResult("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
				}
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new IdResult("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else
			return new IdResult("Dynamix not active! ", ErrorCodes.NOT_READY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result requestContextPluginInstallation(ContextPluginInformation plugInfo) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (plugInfo != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(null));
			// Continue if the app is authorized
			if (app != null && app.isAdmin()) {
				List<DiscoveredContextPlugin> pendingPlugs = DynamixService.getPendingContextPlugins();
				for (DiscoveredContextPlugin plug : pendingPlugs) {
					if (plug.getContextPlugin().getContextPluginInformation().equals(plugInfo)) {
						DynamixService.installPlugin(plug.getContextPlugin(), null);
						return new Result();
					}
				}
				return new Result("Plug-in not found", ErrorCodes.PLUG_IN_NOT_FOUND);
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Null plugInfo in requestContextPluginInstallation");
			return new Result("Null plugInfo in requestContextPluginInstallation", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result requestContextPluginUninstall(ContextPluginInformation plugInfo) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (plugInfo != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(null));
			// Continue if the app is authorized
			if (app != null && app.isAdmin()) {
				List<ContextPlugin> installed = DynamixService.getInstalledContextPlugins();
				for (ContextPlugin plug : installed) {
					if (plug.getContextPluginInformation().equals(plugInfo)) {
						DynamixService.uninstallPlugin(plug, true);
						return new Result();
					}
				}
				return new Result("Plug-in not found", ErrorCodes.PLUG_IN_NOT_FOUND);
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Null plugInfo in requestContextPluginUninstall");
			return new Result("Null plugInfo in requestContextPluginUninstall", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IdResult contextRequest(IDynamixListener listener, String pluginId, String contextType)
			throws RemoteException {
		return configuredContextRequest(listener, pluginId, contextType, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result resendAllCachedContextEvents(IDynamixListener listener) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
			// Continue if the app is authorized
			if (app != null) {
				// App is authorized
				DynamixSession session = SessionManager.getSession(app);
				if (session != null && session.isSessionOpen()) {
					conMgr.resendCachedEvents(app, listener);
					return new Result();
				} else {
					Log.w(TAG, "could not find open session for: " + app);
					return new Result("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
				}
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Null listener in resendAllCachedContextEvents");
			return new Result("Null listener in resendAllCachedContextEvents", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result resendAllTypedCachedContextEvents(IDynamixListener listener, String contextType)
			throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null && contextType != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
			// Continue if the app is authorized
			if (app != null) {
				// App is authorized
				DynamixSession session = SessionManager.getSession(app);
				if (session != null && session.isSessionOpen()) {
					conMgr.resendCachedEvents(app, listener, contextType);
					return new Result();
				} else {
					Log.w(TAG, "could not find open session for: " + app);
					return new Result("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
				}
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Missing parameters in resendAllCachedContextEvents");
			return new Result("Missing parameters in resendAllCachedContextEvents", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result resendCachedContextEvents(IDynamixListener listener, int pastMills) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
			// Continue if the app is authorized
			if (app != null) {
				// App is authorized
				DynamixSession session = SessionManager.getSession(app);
				if (session != null && session.isSessionOpen()) {
					conMgr.resendCachedEvents(app, listener, pastMills);
					return new Result();
				} else {
					Log.w(TAG, "could not find open session for: " + app);
					return new Result("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
				}
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Missing parameters in resendCachedContextEvents");
			return new Result("Missing parameters in resendCachedContextEvents", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result resendTypedCachedContextEvents(IDynamixListener listener, String contextType, int pastMills)
			throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null && contextType != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
			// Continue if the app is authorized
			if (app != null) {
				// App is authorized
				DynamixSession session = SessionManager.getSession(app);
				if (session != null && session.isSessionOpen()) {
					conMgr.resendCachedEvents(app, listener, contextType, pastMills);
					return new Result();
				} else {
					Log.w(TAG, "could not find open session for: " + app);
					return new Result("Session Not found", ErrorCodes.SESSION_NOT_FOUND);
				}
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Missing parameters in resendTypedCachedContextEvents");
			return new Result("Missing parameters in resendTypedCachedContextEvents", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result openContextPluginConfigurationView(IDynamixListener listener, String pluginId) throws RemoteException {
		// Make sure Looper.prepare has been called for the incoming Thread
		setupThreadLooper();
		if (listener != null && pluginId != null) {
			// Access the application securely... returns null if the app is not authorized
			DynamixApplication app = getAuthorizedApplication(getCallerId(listener));
			// Continue if the app is authorized
			if (app != null) {
				return DynamixService.openContextPluginConfigurationForApp(app, pluginId);
			} else {
				Log.w(TAG, app + " is not authorized!");
				return new Result("Not Authorized", ErrorCodes.NOT_AUTHORIZED);
			}
		} else {
			Log.w(TAG, "Missing parameters in openContextPluginConfigurationView");
			return new Result("Missing parameters in openContextPluginConfigurationView", ErrorCodes.MISSING_PARAMETERS);
		}
	}

	/**
	 * Sets the ContextManager
	 */
	protected ContextManager getConMgr() {
		return conMgr;
	}

	/**
	 * Calls doOpenSession for any apps that called openSession when Dynamix was not yet booted
	 */
	protected void processCachedUserIds() {
		synchronized (cachedUserIds) {
			for (Integer userId : cachedUserIds) {
				Log.d(TAG, "Processing openSession for cached ID: " + userId);
				doOpenSession(userId);
			}
			cachedUserIds.clear();
		}
	}

	/**
	 * Creates a new application using the caller's unique UID from Android.
	 */
	private DynamixApplication createNewApplicationFromCaller(int id, boolean admin) {
		// Construct a new application for the caller
		ApplicationInfo info = null;
		PackageManager pm = context.getPackageManager();
		String[] packages = pm.getPackagesForUid(id);
		PackageInfo pkgInfo = null;
		try {
			info = pm.getApplicationInfo(packages[0], PackageManager.GET_UNINSTALLED_PACKAGES);
			// We need to include the GET_PERMISSIONS flag to introspect app permissions
			pkgInfo = pm.getPackageInfo(packages[0], PackageManager.GET_PERMISSIONS);
			DynamixApplication app = new DynamixApplication(pm, pkgInfo, info);
			app.setAdmin(admin);
			return app;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Count not get information for calling UID: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Utility method that attempts to open a Dynamix session for the incoming userId.
	 * 
	 * @param userId
	 *            The user id of the process wishing to open a session.
	 */
	protected synchronized void doOpenSession(int userId) {
		// Access the application securely... returns null if the app is not authorized
		DynamixApplication app = null;
		if (userId == -1) {
			Log.w(TAG, "Invalid user id: " + userId);
			return;
		}
		app = getAuthorizedApplication(userId);
		// If the app is not null, it's authorized
		if (app != null) {
			// Open the session for the app
			DynamixSession session = SessionManager.openSession(app);
			// Ping the app and notify it that Dynamix is active
			app.pingConnected();
			// Send notifications
			SessionManager.notifySecurityAuthorizationGranted(app);
			SessionManager.notifySessionOpened(app, session.getSessionId().toString());
			// Notify Dynamix state
			if (DynamixService.isFrameworkStarted())
				SessionManager.notifyAllDynamixFrameworkActive();
			else
				SessionManager.notifyAllDynamixFrameworkInactive();
		} else
		// The App was not authorized, so check if it's new (i.e. not pending)
		if (DynamixService.SettingsManager.checkApplicationPending(userId)) {
			// Access the pending app
			DynamixApplication pendingApp = getPendingApplication(userId);
			if (pendingApp != null) {
				// Open the session for the app
				SessionManager.openSession(pendingApp);
				// Ping the app and notify it that Dynamix is active
				pendingApp.pingConnected();
				// Update notifications
				DynamixService.updateNotifications();
				// The application is awaiting security authorization notification, so simply send event
				SessionManager.notifyAwaitingSecurityAuthorization(pendingApp);
			}
		} else {
			// The application is new... so set it up as pending
			if (FrameworkConstants.DEBUG)
				Log.d(TAG, "Application ID " + userId + " is new!");
			// Construct a new application for the caller
			DynamixApplication newApp = createNewApplicationFromCaller(userId, false);
			if (newApp != null) {
				// Add a new pendingApp to the SettingsManager
				if (DynamixService.SettingsManager.addPendingApplication(newApp)) {
					// Open the session for the app
					SessionManager.openSession(newApp);
					// Ping the app and update notifications
					newApp.pingConnected();
					// Update notifications
					DynamixService.updateNotifications();
					// Send awaiting security authorization notification
					SessionManager.notifyAwaitingSecurityAuthorization(newApp);
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
			Log.v(TAG, "Checking authorization for app id: " + id + " myUid is " + android.os.Process.myUid()
					+ " getCallingUid is " + Binder.getCallingUid());
		// Handle embedded mode, if necessary
		if (embeddedMode) {
			Log.w(TAG, "Setting up Admin app for " + id);
			if (!DynamixService.SettingsManager.checkApplicationAuthorized(getCallerId(null))) {
				DynamixApplication app = createNewApplicationFromCaller(id, true);
				DynamixService.SettingsManager.addPendingApplication(app);
				DynamixService.authorizeApplication(app);
			}
		}
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

	/**
	 * Utility method used to securely identify a remote caller
	 * 
	 * @return The caller's unique id
	 */
	private int getCallerId(IDynamixListener listener) {
		if (listener instanceof WebListener) {
			return ((WebListener) listener).getWebAppId();
		} else {
			if (embeddedMode)
				return android.os.Process.myUid();
			else {
				if (Binder.getCallingUid() == android.os.Process.myUid()) {
					Log.w(TAG, "Caller was Dynamix when not running in embedded mode... invalid");
					return -1;
				} else
					return Binder.getCallingUid();
			}
		}
	}

	/**
	 * Returns the pending application matching the caller (if authorized), or null if the caller is unauthorized.
	 */
	protected DynamixApplication getPendingApplication(int id) {
		// Check if the application has been authorized to receive events
		if (DynamixService.SettingsManager.checkApplicationPending(id)) {
			for (DynamixApplication a : DynamixService.SettingsManager.getPendingApplications()) {
				if (a.getAppID() == id) {
					return a;
				}
			}
		}
		Log.d(TAG, "App is not pending");
		return null;
	}

	/**
	 * Calls looper prepare on the calling thread, if the thread has not had looper prepare called yet.
	 */
	protected synchronized void setupThreadLooper() {
		if (Looper.myLooper() == null)
			Looper.prepare();
	}

	protected void doAddContextSupport(final DynamixApplication app, final IDynamixListener listener,
			final String contextType, final String pluginId) {
		Utils.dispatch(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "doAddContextSupport for " + contextType);
				List<ContextSupport> supporting = conMgr.addContextSupport(app, listener, contextType, pluginId);
				// The return list contains (potentially several) elements if the contextDataType is supported
				if (supporting != null && supporting.size() > 0) {
					// Verify the state of each returned ContextSupportInfo, sending events as needed
					for (ContextSupport supportInfo : supporting) {
						switch (supportInfo.getContextPlugin().getInstallStatus()) {
						case INSTALLED:
							if (!supportInfo.getContextPlugin().isEnabled()) {
								Log.w(TAG, "Support added for " + supportInfo + " but plugin is disabled!");
							}
							// Context support is available, so simply notify.
							SessionManager.notifyContextSupportAdded(app, listener, supportInfo.getContextSupportInfo());
							break;
						case PENDING_INSTALL:
							// Notify app that context support should be available soon
							SessionManager.notifyInstallingContextSupport(app, listener, supportInfo.getContextPlugin()
									.getContextPluginInformation(), contextType);
							break;
						case INSTALLING:
							// Notify app that context support should be available soon
							SessionManager.notifyInstallingContextSupport(app, listener, supportInfo.getContextPlugin()
									.getContextPluginInformation(), contextType);
							break;
						case NOT_INSTALLED:
							// Unable to find support, so notify the app that we can't support the requested context
							// type
							SessionManager.notifyContextTypeNotSupported(app, listener, contextType);
							break;
						case ERROR:
							SessionManager.notifyContextTypeNotSupported(app, listener, contextType);
							break;
						}
					}
				} else {
					// We found no supporting ContextSupportInfo, so notify the app about the bad news...
					SessionManager.notifyContextTypeNotSupported(app, listener, contextType);
				}
			}
		});
	}

	@Override
	public void onDynamixInitializing() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDynamixInitializingError(String message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDynamixInitialized(DynamixService dynamix) {
		processCachedUserIds();
	}

	@Override
	public void onDynamixStarting() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDynamixStarted() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDynamixStopping() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDynamixStopped() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDynamixError(String message) {
		// TODO Auto-generated method stub
	}
}