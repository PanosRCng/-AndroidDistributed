package com.example.androiddistributed;

import java.util.List;

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.IDynamixFacade;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.Result;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class Scheduler {
	
	// get TAG name for reporting to LogCat
	private final String TAG = this.getClass().getSimpleName();
	
	IDynamixFacade dynamix;
	
	// scheduler constructor
	public Scheduler()
	{
		//
	}
	
	// connect to dynamix framework - needs the application's context to bind the dynamix service
    public void connect_to_dynamix(Context context)
    {
		if (dynamix == null)
		{
			context.bindService(new Intent(IDynamixFacade.class.getName()), sConnection, Context.BIND_AUTO_CREATE);
			Log.i(TAG, "Connecting to Dynamix...\n");
		} else {
			try {
				if (!dynamix.isSessionOpen()) {
					Log.i(TAG, "Dynamix connected... trying to open session\n");
					dynamix.openSession();
				} else
					Log.i(TAG, "Session is already open\n");
			} catch (RemoteException e) {
				Log.e(TAG, e.toString());
			}
		}
    }
    
    // disconnect from dynamix framework
    public void disconnect_from_dynamix()
    {
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
					Log.i(TAG, "Disconnecting from Dynamix\n");
				}
			} catch (RemoteException e) {
				Log.e(TAG, e.toString());
			}
		}
    }
    
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

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "A1 - Dynamix is disconnected!");
			dynamix = null;
		}
	};
	
	private IDynamixListener dynamixCallback = new IDynamixListener.Stub() {
		@Override
		public void onDynamixListenerAdded(String listenerId) throws RemoteException {
			Log.i(TAG, "A1 - onDynamixListenerAdded for listenerId: " + listenerId);
			// Open a Dynamix Session if it's not already opened
			if (dynamix != null) {
				if (!dynamix.isSessionOpen())
					dynamix.openSession();
			} else
				Log.i(TAG, "dynamix already connected");
		}
		
		// catch events from our plugin

		@Override
		public void onContextEvent(ContextEvent event) throws RemoteException {
		      			
		//	  String representation = event.getStringRepresentation("text/plain");
			  
		}
		
		
		// events about the interaction with the dynamix framework
		
		@Override
		public void onDynamixListenerRemoved() throws RemoteException {
			Log.i(TAG, "A1 - onDynamixListenerRemoved");
		}

		@Override
		public void onSessionOpened(String sessionId) throws RemoteException {
			Log.i(TAG, "A1 - onSessionOpened");
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

	//		String contextType = supportInfo.getContextType(); 
						
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
	
}
