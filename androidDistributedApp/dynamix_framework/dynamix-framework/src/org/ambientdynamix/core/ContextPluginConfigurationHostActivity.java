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

import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.ContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.IContextPluginConfigurationViewFactory;
import org.ambientdynamix.util.ContextPluginRuntimeWrapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * An Android Activity configured to host a dynamically injected a configuration user interface provided by an
 * authorized ContextPluginRuntime.
 * 
 * @author Darren Carlson
 */
public class ContextPluginConfigurationHostActivity extends Activity {
	private final static String TAG = ContextPluginConfigurationHostActivity.class.getSimpleName();
	private volatile static boolean viewActive = false;
	public static int TITLEBAR_HEIGHT;
	// Private data
	private ContextPluginRuntimeWrapper plugWrapper;
	private IContextPluginConfigurationViewFactory viewFactory;

	/**
	 * Returns true if the activity is active; false otherwise.
	 * 
	 * @return
	 */
	public static boolean isActive() {
		return viewActive;
	}

	@Override
	public void onResume() {
		super.onResume();
		viewActive = true;
		// Make sure Dynamix is running
		if (DynamixService.isFrameworkStarted()) {
			// Check for existing viewFactory
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
			// Grab the intent and the pluginId
			Intent i = getIntent();
			Bundle extras = i.getExtras();
			String pluginId = extras.getString("pluginId");
			boolean frameworkCall = extras.getBoolean("frameworkCall");
			// Handle non-framework calls (i.e., those from apps)
			if (!frameworkCall) {
				/*
				 * Close out the base activity so that the back button returns to the correct previous Activity and not
				 * the Dynamix GUI.
				 */
				if (DynamixService.getBaseActivity() != null) {
					DynamixService.getBaseActivity().finish();
				}
			}
			// Make sure we've got a plug-in id
			if (pluginId != null) {
				/*
				 * For the moment, make sure the Dynamix is running because This will fail if Dynamix is not running,
				 * since the runtime is not created
				 */
				if (DynamixService.isFrameworkStarted())
					plugWrapper = DynamixService.getContextPluginRuntime(pluginId);
				else {
					Log.w(TAG, "Cannot configure when Dynamix is disabled");
					// TODO: For the moment, do not process config if Dynamix is off
					if (true)
						this.finish();
					/*
					 * Managing plugin settings while Dynamix is off - ContextPlugin runtimes are not available, meaning
					 * we can't get the view factory - the DynamixFacade is not available, so when the runtime want's to
					 * save something, it can't Call in plugin: if(
					 * runtime.getAndroidFacade().storeContextPluginSettings(runtime , settings)) The point is that we
					 * probably want to be able to manage plugins whie Dynamix is off, so perhaps we can come up with a
					 * different way of managing runtime creation of settings dialogs - There will be issues with
					 * associated Bundles being started by the OsgiManager, since we need access to the underlying
					 * classloader to create the view.
					 */
				}
				if (plugWrapper != null) {
					final ContextPlugin plug = DynamixService.getInstalledContextPlugin(pluginId);
					// Check if app is allowed to launch plug-in
					final ContextPluginRuntime runtime = plugWrapper.getContextPluginRuntime();
					Log.i(TAG, "getContextPluginRuntime result was: " + runtime);
					try {
						Class<IContextPluginConfigurationViewFactory> factory = runtime.getSettingsViewFactory();
						if (factory != null) {
							viewFactory = (IContextPluginConfigurationViewFactory) factory.newInstance();
							final View v = viewFactory.initializeView(
									runtime.getPluginFacade().getSecuredContext(runtime.getSessionId()), runtime,
									TITLEBAR_HEIGHT);
							// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
							Log.i(TAG, "Got viewFactory: " + viewFactory);
							// Register our Activity so Dynamix can close us later on request from the
							// IPluginFacade
							DynamixService.registerConfigurationActivity(runtime,
									ContextPluginConfigurationHostActivity.this);
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
									setContentView(v, new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
											LayoutParams.FILL_PARENT));
									/*
									 * Note: You cannot call 'return' here because the method must complete for the
									 * activity to be shown to the user. Hence, all the 'finish' calls below.
									 */
								}
							});
						} else {
							Log.w(TAG, "IContextPluginViewFactory was NULL");
							this.finish();
						}
					} catch (Exception e) {
						{
							Log.e(TAG, e.toString());
							this.finish();
						}
					}
				} else {
					Log.e(TAG, "Could not find plugWrapper for: " + pluginId);
					this.finish();
				}
			} else {
				Log.e(TAG, "Plugin id not specified by " + pluginId);
				this.finish();
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		if (viewFactory != null)
			try {
				viewFactory.destroyView();
			} catch (Exception e) {
				Log.w(TAG, "Exception when destroying view: " + e);
			}
		viewFactory = null;
		// Make sure our activity is unregistered
		if (plugWrapper != null && plugWrapper.getContextPluginRuntime() != null)
			DynamixService.unRegisterConfigurationActivity(plugWrapper.getContextPluginRuntime());
		viewActive = false;
	}
}
