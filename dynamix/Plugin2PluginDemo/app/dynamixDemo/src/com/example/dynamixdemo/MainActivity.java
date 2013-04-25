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

package com.example.dynamixdemo;

import java.util.List;

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.IDynamixFacade;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.Result;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends Activity {

	// Classname for logging purposes
	private final String TAG = this.getClass().getSimpleName();
	
	private IDynamixFacade dynamix;
	EditText et;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "ON CREATE for: Dynamix Simple Logger (A1)");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);	
		et = (EditText) this.findViewById(R.id.editText1);
		
		Button connectBtn = (Button) findViewById(R.id.connectBtn);
		connectBtn.setOnClickListener(new OnClickListener() {
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
					et.append("Connecting to Dynamix...\n");
				} else {
					try {
						if (!dynamix.isSessionOpen()) {
							et.append("Dynamix connected... trying to open session\n");
							dynamix.openSession();
						} else
							et.append("Session is already open\n");
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
		});
		// Setup the disconnect button
		Button disconnectBtn = (Button) findViewById(R.id.disconnectBtn);
		disconnectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						/*
						 * In this example, this Activity controls the session, so we call closeSession here. This will
						 * close the session for ALL of the application's IDynamixListeners.
						 */
						Result result = dynamix.closeSession();
						
						if (!result.wasSuccessful())
						{
							Log.w(TAG, "Call was unsuccessful! Message: " + result.getMessage() + " | Error code: "
									+ result.getErrorCode());
						}
						else
						{
							et.append("Disconnecting from Dynamix\n");
						}
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
			
		});	
		
		// button to call plugin 1
		Button summonPlugin1Btn = (Button) this.findViewById(R.id.callPlugin1Btn);
		summonPlugin1Btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
				String contextType = "org.ambientdynamix.contextplugins.plugin1";
				
				try
				{
					summonPlugin(contextType);
				}
				catch (Exception e)
				{
					Log.e(TAG, "can not summon the plugin 1");
				}
			}
		});
	}
		
	@Override
	protected void onDestroy() {
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
	
	@Override
	public void onResume() {
	  super.onResume();

	  // Register mMessageReceiver to receive messages from Dynamix events.
	  LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("message_intent"));
	}
	
	@Override
	protected void onPause() {
	  // Unregister receiving messages from Dynamix events, since the activity is not visible
	  LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	  super.onPause();
	} 
	
	// handler for received Intents for the "message_intent" event 
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    // Extract data included in the Intent
	    String data = intent.getStringExtra("message_data");
	    
	    et.append("message_data: " + data + "\n");
	  }
	};
		
	// Send an Intent to MainActivity with message_data -- used to update UI from Dynamix events   
	private void sendMessage(String data) {
	  Intent intent = new Intent("message_intent");
	  // Add data
	  intent.putExtra("message_data", data);
	  LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
				// Create a Dynamix Facade using the incoming IBinder
				dynamix = IDynamixFacade.Stub.asInterface(service);
				// Create a Dynamix listener using the callback
				dynamix.addDynamixListener(dynamixCallback);
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
			Log.i(TAG, "A1 - Dynamix is disconnected!");
			dynamix = null;
		}
	};
	/*
	 * Implementation of the IDynamixListener interface. For details on the IDynamixListener interface, see the Dynamix
	 * developer website.
	 */
	private IDynamixListener dynamixCallback = new IDynamixListener.Stub() {
		@Override
		public void onDynamixListenerAdded(String listenerId) throws RemoteException {
			Log.i(TAG, "A1 - onDynamixListenerAdded for listenerId: " + listenerId);
			// Open a Dynamix Session if it's not already opened
			if (dynamix != null) {
				if (!dynamix.isSessionOpen())
					dynamix.openSession();
//				else
//					registerForContextTypes();
			} else
				Log.i(TAG, "dynamix already connected");
		}
		
		
		// catch events from our plugin

		@Override
		public void onContextEvent(ContextEvent event) throws RemoteException {
		      			
			  String representation = event.getStringRepresentation("text/plain");
			  
			  // handle context event from plugin1
			  if( representation.contains("number1=") )
			  {
				  String[] splits = representation.split("=");	  
				  String number1 = splits[1];
				  				  
				  sendMessage(number1);
				  
				  summonPlugin("org.ambientdynamix.contextplugins.plugin2");
			  }
			  
			  if( representation.contains("number2=") )
			  {
				  String[] splits = representation.split("=");	  
				  String number1 = splits[1];
				  
				  sendMessage(number1);
			  }
		}
		
		
		// events about the interaction with the dynamix framework
		
		@Override
		public void onDynamixListenerRemoved() throws RemoteException {
			Log.i(TAG, "A1 - onDynamixListenerRemoved");
		}

		@Override
		public void onSessionOpened(String sessionId) throws RemoteException {
			Log.i(TAG, "A1 - onSessionOpened");
//			registerForContextTypes();
		}

		@Override
		public void onSessionClosed() throws RemoteException {
			Log.i(TAG, "A1 - onSessionClosed");
		}

		@Override
		public void onAwaitingSecurityAuthorization() throws RemoteException {
			Log.i(TAG, "A1 - onAwaitingSecurityAuthorization");
		}

		@Override
		public void onSecurityAuthorizationGranted() throws RemoteException {
			Log.i(TAG, "A1 - onSecurityAuthorizationGranted");
		}

		@Override
		public void onSecurityAuthorizationRevoked() throws RemoteException {
			Log.w(TAG, "A1 - onSecurityAuthorizationRevoked");
		}

		@Override
		public void onContextSupportAdded(ContextSupportInfo supportInfo) throws RemoteException {
			Log.i(TAG,
					"A1 - onContextSupportAdded for " + supportInfo.getContextType() + " using plugin "
							+ supportInfo.getPlugin() + " | id was: " + supportInfo.getSupportId());

			String contextType = supportInfo.getContextType(); 
						
			// send context request to plugin1 
			try{
				sendRequest(contextType);
			}catch (Exception e) 
			{
				//
			}
		}

		@Override
		public void onContextSupportRemoved(ContextSupportInfo supportInfo) throws RemoteException {
			Log.i(TAG, "A1 - onContextSupportRemoved for " + supportInfo.getSupportId());
		}

		@Override
		public void onContextTypeNotSupported(String contextType) throws RemoteException {
			Log.i(TAG, "A1 - onContextTypeNotSupported for " + contextType);
		}

		@Override
		public void onInstallingContextSupport(ContextPluginInformation plug, String contextType)
				throws RemoteException {
			Log.i(TAG, "A1 - onInstallingContextSupport: plugin = " + plug + " | Context Type = " + contextType);
		}

		@Override
		public void onInstallingContextPlugin(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A1 - onInstallingContextPlugin: plugin = " + plug);
		}

		@Override
		public void onContextPluginInstalled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginInstalled for " + plug);
		}

		@Override
		public void onContextPluginUninstalled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginUninstalled for " + plug);
		}

		@Override
		public void onContextPluginInstallFailed(ContextPluginInformation plug, String message) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginInstallFailed for " + plug + " with message: " + message);
		}

		@Override
		public void onContextRequestFailed(String requestId, String errorMessage, int errorCode) throws RemoteException {
			Log.w(TAG, "A1 - onContextRequestFailed for requestId " + requestId + " with error message: " + errorMessage);
		}

		@Override
		public void onContextPluginDiscoveryStarted() throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginDiscoveryStarted");
		}

		@Override
		public void onContextPluginDiscoveryFinished(List<ContextPluginInformation> discoveredPlugins)
				throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginDiscoveryFinished");
		}

		@Override
		public void onDynamixFrameworkActive() throws RemoteException {
			Log.i(TAG, "A1 - onDynamixFrameworkActive");
		}

		@Override
		public void onDynamixFrameworkInactive() throws RemoteException {
			Log.i(TAG, "A1 - onDynamixFrameworkInactive");
		}

		@Override
		public void onContextPluginError(ContextPluginInformation plug, String message) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginError for " + plug + " with message " + message);
		}
		
	};

	// send context request to plugin1
	private void sendRequest(String contextType) throws RemoteException, InterruptedException
	{
			String requestId = contextType;		
			dynamix.contextRequest(dynamixCallback, requestId, contextType);
	}

	// add context support using plugin context type 
	private void summonPlugin(String contextType) throws RemoteException {
	
		Result result1 = dynamix.addContextSupport(dynamixCallback, contextType);
		if (!result1.wasSuccessful())
			Log.w(TAG,
					"Call was unsuccessful! Message: " + result1.getMessage() + " | Error code: "
							+ result1.getErrorCode());	
	}	

}
