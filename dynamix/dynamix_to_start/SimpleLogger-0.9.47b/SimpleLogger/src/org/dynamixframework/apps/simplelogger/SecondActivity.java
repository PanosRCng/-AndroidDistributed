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
package org.dynamixframework.apps.simplelogger;

import java.util.List;
import java.util.UUID;

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportConfig;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.IContextInfo;
import org.ambientdynamix.api.application.IDynamixFacade;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.IdResult;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.contextplugins.ambientsound.IAmbientSoundContextInfo;
import org.ambientdynamix.contextplugins.barcode.IBarcodeContextInfo;
import org.ambientdynamix.contextplugins.nfc.INfcTag;
import org.ambientdynamix.contextplugins.pedometer.IPedometerStepInfo;
import org.ambientdynamix.contextplugins.sampleplugin.ISamplePluginContextInfo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * This Activity demonstrates how to create a second IDynamixListener using the Dynamix Framework. This Activity is
 * opened by FirstActivity.java, which controls the Dynamix session.
 * 
 * @author Darren Carlson
 */
public class SecondActivity extends Activity {
	// Classname for logging purposes
	private final String TAG = this.getClass().getSimpleName();
	// Local variable for our Dynamix Facade
	private IDynamixFacade dynamix;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "ON CREATE for: Dynamix Simple Logger (A2)");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView activityLabel = (TextView) findViewById(R.id.activity_label);
		activityLabel.setText("Dynamix Simple Logger (A2)");
		// Setup the connect button
		Button btnConnect = (Button) findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix == null) {
					/*
					 * Bind to the Dynamix service using the Activity's 'bindService' method, which completes
					 * asynchronously. As such, you must wait until the 'onServiceConnected' method of the
					 * ServiceConnection 'sConnection' implementation is called (see below) before calling Dynamix
					 * methods.
					 */
					bindService(new Intent(IDynamixFacade.class.getName()), sConnection, Context.BIND_AUTO_CREATE);
					Log.i(TAG, "A2 - Connecting to Dynamix...");
				} else {
					try {
						if (!dynamix.isSessionOpen()) {
							Log.i(TAG, "Dynamix connected... trying to open session");
							dynamix.openSession();
						} else {
							Log.i(TAG, "Session is already open");
							dynamix.addDynamixListener(dynamixCallback);
						}
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
		});
		// Setup the disconnect button
		Button btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		btnDisconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						/*
						 * In this example, this Activity does not control the session, so simply remove our listener.
						 * Use FirstActivity to close the session.
						 */
						dynamix.removeDynamixListener(dynamixCallback);
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
		});
		// Setup the send cached context button
		Button btnGetCachedContext = (Button) findViewById(R.id.btnGetCachedContext);
		btnGetCachedContext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						Result result = dynamix.resendAllCachedContextEvents(dynamixCallback);
						if (!result.wasSuccessful())
							Log.w(TAG, "Call was unsuccessful! Message: " + result.getMessage() + " | Error code: "
									+ result.getErrorCode());
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		Button btnProgrammaticConfiguration = (Button) findViewById(R.id.btnProgrammaticConfiguration);
		btnProgrammaticConfiguration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "A2 - Requesting Programmatic Configuration");
				if (dynamix != null) {
					try {
						Result result = dynamix.openContextPluginConfigurationView(dynamixCallback,
								"org.ambientdynamix.contextplugins.sampleplugin");
						if (!result.wasSuccessful())
							Log.w(TAG, "Call was unsuccessful! Message: " + result.getMessage() + " | Error code: "
									+ result.getErrorCode());
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		/*
		 * Setup the interactive context acquisition button. Note that this method only works if the
		 * 'org.ambientdynamix.contextplugins.barcode' plug-in is installed.
		 */
		Button btnInteractiveAcquisition = (Button) findViewById(R.id.btnInteractiveAcquisition);
		btnInteractiveAcquisition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						Log.i(TAG, "A2 - Requesting Interactive Context Acquisition");
						/*
						 * Launch interactive context acquisition. Note that you no longer need to use Intents; simply
						 * use the normal 'contextRequest' method.
						 */
						IdResult result1 = dynamix.contextRequest(dynamixCallback,
								"org.ambientdynamix.contextplugins.barcode",
								"org.ambientdynamix.contextplugins.barcode");
						if (result1.wasSuccessful())
							Log.i(TAG, "A2 - Request id was: " + result1.getId()
									+ " for org.ambientdynamix.contextplugins.barcode");
						else
							Log.w(TAG, "Call was unsuccessful! Message: " + result1.getMessage() + " | Error code: "
									+ result1.getErrorCode());
					} catch (Exception e) {
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		/*
		 * Setup the interactive context acquisition button. Note that this method only works if the
		 * 'org.ambientdynamix.contextplugins.sampleplugin' plug-in is installed.
		 */
		Button btnProgrammaticAcquisition = (Button) findViewById(R.id.btnProgrammaticAcquisition);
		btnProgrammaticAcquisition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						Log.i(TAG, "A2 - Requesting Programmatic Context Acquisitions");
						// First, request the sample data using the org.ambientdynamix.contextplugins.sampleplugin
						IdResult result1 = dynamix.contextRequest(dynamixCallback,
								"org.ambientdynamix.contextplugins.sampleplugin",
								"org.ambientdynamix.contextplugins.sample_context_info");
						if (result1.wasSuccessful())
							Log.i(TAG, "A2 - Request id was: " + result1.getId()
									+ " for org.ambientdynamix.contextplugins.sampleplugin");
						else
							Log.w(TAG, "Call was unsuccessful! Message: " + result1.getMessage() + " | Error code: "
									+ result1.getErrorCode());
						// Next, request the user's sound level using org.ambientdynamix.contextplugins.ambientsound
						IdResult result2 = dynamix.contextRequest(dynamixCallback,
								"org.ambientdynamix.contextplugins.ambientsound",
								"org.ambientdynamix.contextplugins.ambientsound");
						if (result2.wasSuccessful())
							Log.i(TAG, "A2 - Request id was: " + result2.getId()
									+ " for org.ambientdynamix.contextplugins.ambientsound");
						else
							Log.w(TAG, "Call was unsuccessful! Message: " + result2.getMessage() + " | Error code: "
									+ result2.getErrorCode());
					} catch (Exception e) {
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		Button btnToggleActivities = (Button) findViewById(R.id.btn_toggle_activities);
		btnToggleActivities.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SecondActivity.this, FirstActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "ON DESTROY for: Dynamix Simple Logger (A2)");
		/*
		 * Always remove our listener and unbind so we don't leak our service connection
		 */
		if (dynamix != null) {
			try {
				dynamix.removeDynamixListener(dynamixCallback);
				unbindService(sConnection);
			} catch (RemoteException e) {
			}
		}
		super.onDestroy();
	}

	/*
	 * The ServiceConnection is used to receive callbacks from Android telling our application that it's been connected
	 * to Dynamix, or that it's been disconnected from Dynamix. These events come from Android, not Dynamix. Dynamix
	 * events are always sent to our IDynamixListener object (defined farther below), which is registered (in this case)
	 * in during the 'addDynamixListener' call in the 'onServiceConnected' method of the ServiceConnection.
	 */
	private ServiceConnection sConnection = new ServiceConnection() {
		/*
		 * Indicates that we've successfully connected to Dynamix. During this call, we transform the incoming IBinder
		 * into an instance of the IDynamixFacade, which is used to call Dynamix methods.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// Add ourselves as a Dynamix listener
			try {
				// Create a connection to Dynamix using the IBinder
				dynamix = IDynamixFacade.Stub.asInterface(service);
				// Add the dynamixCallback as a dynamix listener
				dynamix.addDynamixListener(dynamixCallback);
				Log.i(TAG, "A2 - Dynamix is connected!");
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}

		/*
		 * Indicates that a previously connected IDynamixFacade has been disconnected from Dynamix. This typically means
		 * that Dynamix has crashed or been shut down by Android to conserve resources. In this case,
		 * 'onServiceConnected' will be called again automatically once Dynamix boots again.
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			dynamix = null;
			Log.i(TAG, "A2 - Dynamix is disconnected!");
		}
	};
	/*
	 * Implementation of the IDynamixListener interface. For details on the IDynamixListener interface, see the Dynamix
	 * developer website.
	 */
	private IDynamixListener dynamixCallback = new IDynamixListener.Stub() {
		@Override
		public void onDynamixListenerAdded(String listenerId) throws RemoteException {
			Log.i(TAG, "A2 - onDynamixListenerAdded for listenerId: " + listenerId);
			// Open a Dynamix Session if it's not already opened
			if (dynamix != null) {
				if (!dynamix.isSessionOpen())
					dynamix.openSession();
				else
					registerForContextTypes();
			} else
				Log.i(TAG, "dynamix already connected");
		}

		@Override
		public void onDynamixListenerRemoved() throws RemoteException {
			Log.i(TAG, "A2 - onDynamixListenerRemoved");
		}

		@Override
		public void onSessionOpened(String sessionId) throws RemoteException {
			Log.i(TAG, "A2 - onSessionOpened");
			registerForContextTypes();
		}

		@Override
		public void onSessionClosed() throws RemoteException {
			Log.i(TAG, "A2 - onSessionClosed");
		}

		@Override
		public void onAwaitingSecurityAuthorization() throws RemoteException {
			Log.i(TAG, "A2 - onAwaitingSecurityAuthorization");
		}

		@Override
		public void onSecurityAuthorizationGranted() throws RemoteException {
			Log.i(TAG, "A2 - onSecurityAuthorizationGranted");
		}

		@Override
		public void onSecurityAuthorizationRevoked() throws RemoteException {
			Log.w(TAG, "A2 - onSecurityAuthorizationRevoked");
		}

		@Override
		public void onContextSupportAdded(ContextSupportInfo supportInfo) throws RemoteException {
			Log.i(TAG,
					"A2 - onContextSupportAdded for " + supportInfo.getContextType() + " using plugin "
							+ supportInfo.getPlugin() + " | id was: " + supportInfo.getSupportId());
		}

		@Override
		public void onContextSupportRemoved(ContextSupportInfo supportInfo) throws RemoteException {
			Log.i(TAG, "A2 - onContextSupportRemoved for " + supportInfo.getSupportId());
		}

		@Override
		public void onContextTypeNotSupported(String contextType) throws RemoteException {
			Log.i(TAG, "A2 - onContextTypeNotSupported for " + contextType);
		}

		@Override
		public void onInstallingContextSupport(ContextPluginInformation plug, String contextType)
				throws RemoteException {
			Log.i(TAG, "A2 - onInstallingContextSupport: plugin = " + plug + " | Context Type = " + contextType);
		}

		@Override
		public void onInstallingContextPlugin(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A2 - onInstallingContextPlugin: plugin = " + plug);
		}

		@Override
		public void onContextPluginInstalled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A2 - onContextPluginInstalled for " + plug);
			/*
			 * Automatically create context support for each supported context type
			 */
			for (String type : plug.getSupportedContextTypes()) {
				dynamix.addContextSupport(dynamixCallback, type);
			}
		}

		@Override
		public void onContextPluginUninstalled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A2 - onContextPluginUninstalled for " + plug);
		}

		@Override
		public void onContextPluginInstallFailed(ContextPluginInformation plug, String message) throws RemoteException {
			Log.i(TAG, "A2 - onContextPluginInstallFailed for " + plug + " with message: " + message);
		}

		@Override
		public void onContextEvent(ContextEvent event) throws RemoteException {
			/*
			 * Log some information about the incoming event
			 */
			Log.i(TAG, "A2 - onContextEvent received from plugin: " + event.getEventSource());
			Log.i(TAG, "A2 - -------------------");
			Log.i(TAG, "A2 - Event context type: " + event.getContextType());
			Log.i(TAG, "A2 - Event timestamp " + event.getTimeStamp().toLocaleString());
			if (event.expires())
				Log.i(TAG, "A2 - Event expires at " + event.getExpireTime().toLocaleString());
			else
				Log.i(TAG, "A2 - Event does not expire");
			/*
			 * To illustrate how string-based context representations are accessed, we log each contained in the event.
			 */
			for (String format : event.getStringRepresentationFormats()) {
				Log.i(TAG,
						"Event string-based format: " + format + " contained data: "
								+ event.getStringRepresentation(format));
			}
			// Check for native IContextInfo
			if (event.hasIContextInfo()) {
				Log.i(TAG, "A2 - Event contains native IContextInfo: " + event.getIContextInfo());
				IContextInfo nativeInfo = event.getIContextInfo();
				/*
				 * Note: At this point you can cast the IContextInfo into it's native type and then call its methods. In
				 * order for this to work, you'll need to have the proper Java classes for the IContextInfo data types
				 * on your app's classpath. If you don't, event.hasIContextInfo() will return false and
				 * event.getIContextInfo() would return null, meaning that you'll need to rely on the string
				 * representation of the context info. To use native context data-types, simply download the data-types
				 * JAR for the Context Plug-in you're interested in, include the JAR(s) on your build path, and you'll
				 * have access to native context type objects instead of strings.
				 */
				Log.i(TAG, "A2 - IContextInfo implimentation class: " + nativeInfo.getImplementingClassname());
				// Example of using IPedometerStepInfo
				if (nativeInfo instanceof IPedometerStepInfo) {
					IPedometerStepInfo stepInfo = (IPedometerStepInfo) nativeInfo;
					Log.i(TAG, "A2 - Received IPedometerStepInfo with RmsStepForce: " + stepInfo.getRmsStepForce());
				}
				// Example of using ISamplePluginContextInfo
				if (nativeInfo instanceof ISamplePluginContextInfo) {
					ISamplePluginContextInfo info = (ISamplePluginContextInfo) nativeInfo;
					Log.i(TAG, "A2 - Received ISamplePluginContextInfo with sample data: " + info.getSampleData());
				}
				// Example of using IAmbientSoundContextInfo
				if (nativeInfo instanceof IAmbientSoundContextInfo) {
					IAmbientSoundContextInfo info = (IAmbientSoundContextInfo) nativeInfo;
					Log.i(TAG, "A2 - Received IAmbientSoundContextInfo with dB value: " + info.getDbValue());
				}
				// Example of using INfcTag
				if (nativeInfo instanceof INfcTag) {
					INfcTag tagInfo = (INfcTag) nativeInfo;
					Log.i(TAG, "A2 - Received INfcTag with tag value: " + tagInfo.getTagIdAsString());
				}
				// Example of using IBarcodeContextInfo
				if (nativeInfo instanceof IBarcodeContextInfo) {
					IBarcodeContextInfo info = (IBarcodeContextInfo) nativeInfo;
					Log.i(TAG, "A2 - Received IBarcodeContextInfo with format  " + info.getBarcodeFormat()
							+ " and value " + info.getBarcodeValue());
				}
				// Check for other interesting types, if needed...
			} else
				Log.i(TAG,
						"Event does NOT contain native IContextInfo... we need to rely on the string representation!");
		}

		@Override
		public void onContextRequestFailed(String requestId, String errorMessage, int errorCode) throws RemoteException {
			Log.w(TAG, "A2 - onContextRequestFailed for requestId " + requestId + " with error message: " + errorMessage);
		}

		@Override
		public void onContextPluginDiscoveryStarted() throws RemoteException {
			Log.i(TAG, "A2 - onContextPluginDiscoveryStarted");
		}

		@Override
		public void onContextPluginDiscoveryFinished(List<ContextPluginInformation> discoveredPlugins)
				throws RemoteException {
			Log.i(TAG, "A2 - onContextPluginDiscoveryFinished");
		}

		@Override
		public void onDynamixFrameworkActive() throws RemoteException {
			Log.i(TAG, "A2 - onDynamixFrameworkActive");
		}

		@Override
		public void onDynamixFrameworkInactive() throws RemoteException {
			Log.i(TAG, "A2 - onDynamixFrameworkInactive");
		}

		@Override
		public void onContextPluginError(ContextPluginInformation plug, String message) throws RemoteException {
			Log.i(TAG, "A2 - onContextPluginError for " + plug + " with message " + message);
		}
	};

	/*
	 * Utility method that registers for the context types needed by this class.
	 */
	private void registerForContextTypes() throws RemoteException {
		/*
		 * Below are several examples of adding context support using context types. In this case, the plug-in (or
		 * plug-ins) assigned to handle context type will be automatically selected by Dynamix.
		 */
		Result result1 = dynamix.addContextSupport(dynamixCallback,
				"org.ambientdynamix.contextplugins.ambientsound");
		if (!result1.wasSuccessful())
			Log.w(TAG,
					"Call was unsuccessful! Message: " + result1.getMessage() + " | Error code: "
							+ result1.getErrorCode());
		Result result2 = dynamix.addContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.barcode");
		if (!result2.wasSuccessful())
			Log.w(TAG,
					"Call was unsuccessful! Message: " + result2.getMessage() + " | Error code: "
							+ result2.getErrorCode());
		Result result3 = dynamix.addContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.nfc.tag");
		if (!result3.wasSuccessful())
			Log.w(TAG,
					"Call was unsuccessful! Message: " + result3.getMessage() + " | Error code: "
							+ result3.getErrorCode());
		Result result4 = dynamix.addContextSupport(dynamixCallback,
				"org.ambientdynamix.contextplugins.sample_context_info");
		if (!result4.wasSuccessful())
			Log.w(TAG,
					"Call was unsuccessful! Message: " + result4.getMessage() + " | Error code: "
							+ result4.getErrorCode());
		/*
		 * Here is an example of how to add a context support using a specific context type and plug-in. In this
		 * case, Dynamix will only assign the specified plug-in to handle the context support.
		 */
		Bundle config = new Bundle();
		config.putString(ContextSupportConfig.REQUESTED_PLUGIN, "org.ambientdynamix.contextplugins.pedometer");
		config.putString(ContextSupportConfig.CONTEXT_TYPE, "org.ambientdynamix.contextplugins.pedometer");
		Result result5 = dynamix.addConfiguredContextSupport(dynamixCallback, config);
		if (!result5.wasSuccessful())
			Log.w(TAG,
					"Call was unsuccessful! Message: " + result5.getMessage() + " | Error code: "
							+ result5.getErrorCode());
	}
}