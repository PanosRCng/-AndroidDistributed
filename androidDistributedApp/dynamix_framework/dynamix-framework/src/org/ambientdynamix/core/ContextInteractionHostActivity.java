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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.UUID;

import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.IContextPluginInteractionViewFactory;
import org.ambientdynamix.api.contextplugin.InteractiveContextPluginRuntime;
import org.ambientdynamix.core.EventCommand.ContextRequestFailed;
import org.ambientdynamix.util.ContextPluginRuntimeWrapper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

/**
 * An Android Activity configured to host a dynamically injected context acquisition user interface provided by an
 * authorized ContextPluginRuntime. Dynamix applications wishing to launch a particular ContextPlugin's acquisition
 * interface must send a specially constructed Android intent, using the following method:
 * <ol>
 * <li>Create an intent with the intent filter: 'org.ambientdynamix.contextplugin.ACQUIRE_CONTEXT'</li>
 * <li>Use intent.putExtra with the key 'pluginId' and the value of the plugin id of the requested ContextPlugin.</li>
 * <li>Use intent.putExtra with the key 'sessionId' and the value of the session id of the application.</li>
 * <li>Use intent.putExtra with the key 'listenerId' and the value of the listener id of the application.</li>
 * <li>(Optional) Use intent.putExtra with the key 'requestId' (app-defined) to receive a requestId in the response.</li>
 * </ol>
 * This class checks that the specified ContextPlugin has permission to launch its interface. Next, this class uses the
 * incoming sessionId to check that the application has permission to launch the specified ContextPlugin interface.
 * 
 * @author Darren Carlson
 */
public class ContextInteractionHostActivity extends Activity {
	// Private data
	public final static String TAG = ContextInteractionHostActivity.class.getSimpleName();
	private volatile static boolean viewActive = false;
	private ContextPluginRuntimeWrapper wrapper;
	private IContextPluginInteractionViewFactory viewFactory;

	/**
	 * Returns true if the activity is active; false otherwise.
	 * 
	 * @return
	 */
	public static boolean isActive() {
		return viewActive;
	}
	
	public int getTitleBarHeight(){
		Rect rect = new Rect();
		Window window = getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rect);
		int statusBarHeight = rect.top;
		int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		int titleBarHeight = contentViewTop - statusBarHeight;
		return Math.abs(titleBarHeight);
	}


	@Override
	public void onResume() {
		super.onResume();
		// Set viewActive to true so that NCF dispatch works, even when we're acquiring context
		viewActive = true;
		// Make sure Dynamix is running
		if (DynamixService.isFrameworkStarted()) {
			/*
			 * Close out the base activity so that the back button returns to the correct previous Activity and not the
			 * Dynamix GUI.
			 */
			if (DynamixService.getBaseActivity() != null) {
				DynamixService.getBaseActivity().finish();
			}
			// Close any existing viewFactory
			if (viewFactory != null) {
				try {
					viewFactory.destroyView();
				} catch (Exception e) {
					Log.w(TAG, "Exception when destroying view: " + e);
				}
				viewFactory = null;
				// Clear the screen
				setContentView(null);
			}
			// Grab the intent and extras
			Intent i = getIntent();
			Bundle extras = i.getExtras();
			String pluginId = extras.getString("pluginId");
			String sessionIdString = extras.getString("sessionId");
			String listenerIdString = extras.getString("listenerId");
			String appRequestIdString = extras.getString("requestId");
			
			final String contextType = extras.getString("contextType");
			// Check for sessionId and listenerId
			if (sessionIdString != null && listenerIdString != null && appRequestIdString != null) {
				Log.i(TAG, "Running ContextInteractionHostActivity 1");
				UUID sessionId = UUID.fromString(sessionIdString);
				UUID listenerId = UUID.fromString(listenerIdString);
				final UUID responseId = UUID.fromString(appRequestIdString);
				// Get the session
				final DynamixSession session = SessionManager.getSession(sessionId);
				if (session != null) {
					// Get the listener
					final IDynamixListener listener = session.getDynamixListener(listenerId);
					if (listener != null) {
						final ContextPlugin plug = DynamixService.getInstalledContextPlugin(pluginId);
						if (plug != null) {
							wrapper = DynamixService
									.getContextPluginRuntime(pluginId);
							// Make sure we got a wrapper
							if (wrapper != null) {
								Log.i(TAG, "Running ContextInteractionHostActivity 2");
								
								// Make sure the plug-in is configured
								if (!plug.isConfigured()) {
									Log.w(TAG, "Plugin Not Configured: " + pluginId);
									sendContextScanFailed(session.getApp(), listener, appRequestIdString,
											ErrorCodes.PLUG_IN_NOT_CONFIGURED, "Plugin Not Configured: " + pluginId);
								} else {
									// Make sure its an ReactiveContextPluginRuntime
									if (wrapper.getContextPluginRuntime() instanceof InteractiveContextPluginRuntime) {
										final InteractiveContextPluginRuntime runtime = (InteractiveContextPluginRuntime) wrapper
												.getContextPluginRuntime();
										Log.v(TAG, "getContextPluginRuntime result was: " + runtime);
										// TODO: Complete launch level checking
										boolean allowedToLaunch = true;
										if (allowedToLaunch) {
											/*
											 * Setup a Handler to run the View, making sure to handle uncaughtExceptions that may be
											 * thrown.
											 */
											Handler handler = new Handler();
											handler.getLooper().getThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
												@Override
												public void uncaughtException(Thread thread, Throwable ex) {
													Log.w(TAG, "Exeption in view " + ex.toString());
													DynamixService.disableContextPlugin(plug.getContextPluginInformation());
													DynamixService.destroyFramework(true, true);
													
												}
											});
											
											handler.post(new Runnable() {
												public void run() {
													Log.i(TAG, "Running ContextInteractionHostActivity 3");
													try {
														/*
														 * Register our Activity so Dynamix can close us later on
														 * request from the IPluginFacade
														 */
														DynamixService.registerContextAcquisitionActivity(runtime,
																ContextInteractionHostActivity.this);
														// Dynamically create the AcquisitionViewFactory
														Class<IContextPluginInteractionViewFactory> factory = runtime
																.getAcquisitionViewFactory();
														viewFactory = (IContextPluginInteractionViewFactory) factory
																.newInstance();
														// Create the View using the factory
														View v = viewFactory.initializeView(runtime.getPluginFacade()
																.getSecuredContext(runtime.getSessionId()), runtime,
																responseId, contextType, getTitleBarHeight());
														// Set the screen orientation
														setRequestedOrientation(runtime.getScreenOrientation());
														// Inject the plugin's View into the host Activity to show it
														setContentView(v, new ViewGroup.LayoutParams(
																LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
														
														/*
														 * Note: We cannot call 'return' here because the method must
														 * complete for the activity to be shown to the user. Hence, all
														 * the 'finish' calls below.
														 */
													} catch (Exception e) {
														Log.w(TAG, e);
														ContextInteractionHostActivity.this.finish();
													}
												}
											});
										} else {
											Log.w(TAG,
													"Plugin lacks minumum PrivacyRiskLevel to launch interface. PluginId: "
															+ pluginId);
											sendContextScanFailed(session.getApp(), listener, appRequestIdString,
													ErrorCodes.NOT_AUTHORIZED,
													"Plugin lacks minumum PrivacyRiskLevel to launch interface. PluginId: "
															+ pluginId);
											this.finish();
										}
									} else {
										Log.w(TAG, "Not a ReactiveContextPluginRuntime for pluginId: " + pluginId);
										sendContextScanFailed(session.getApp(), listener, appRequestIdString,
												ErrorCodes.PLUG_IN_TYPE_MISMATCH,
												"Not a ReactiveContextPluginRuntime for pluginId: " + pluginId);
										this.finish();
									}
								}
							} else {
								Log.w(TAG, "ContextPluginRuntimeWrapper not found for: " + pluginId);
								sendContextScanFailed(session.getApp(), listener, appRequestIdString,
										ErrorCodes.DYNAMIX_FRAMEWORK_ERROR, "Plugin Runtime Not Found: " + pluginId);
								this.finish();
							}
						} else {
							Log.w(TAG, "ContextPlugin not found for: " + pluginId);
							sendContextScanFailed(session.getApp(), listener, appRequestIdString,
									ErrorCodes.DYNAMIX_FRAMEWORK_ERROR, "ContextPlugin Not Found: " + pluginId);
							this.finish();
						}
					} else {
						Log.w(TAG, "No listener found for: " + listenerId);
						this.finish();
					}
				} else {
					Log.w(TAG, "Session not found for sessionId: " + sessionId);
					this.finish();
				}
			} else {
				Log.w(TAG, "Null sessionId or listenerId string!");
				this.finish();
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		if (viewFactory != null)
			viewFactory.destroyView();
		viewFactory = null;
		// Make sure our activity is unregistered
		if (wrapper != null && wrapper.getContextPluginRuntime() != null)
			DynamixService.unregisterContextAcquisitionActivity(wrapper.getContextPluginRuntime());
		else
			Log.w(TAG, "Context plugin runtime was null onDestroy: " + wrapper);
		viewActive = false;
	}

	/*
	 * Utility method for sending context scan failed events to Dynamix
	 */
	private void sendContextScanFailed(DynamixApplication app, IDynamixListener listener, String appRequestId,
			int errorCode, String errorMsg) {
		if (appRequestId != null) {
			// Create a ContextRequestFailed IEventCommand
			IEventCommand errorCommand = new ContextRequestFailed(appRequestId, errorMsg, errorCode);
			// Introduce a short delay so that the requesting app has time to receive its requestId from Dynamix
			errorCommand.setSendDelayMills(500);
			// Send the error using the SessionManager
			SessionManager.sendEventCommand(app, listener, errorCommand);
		} else
			Log.w(TAG, "App did not provide an appRequestId, so we can't send ContextRequestFailed event");
	}
}