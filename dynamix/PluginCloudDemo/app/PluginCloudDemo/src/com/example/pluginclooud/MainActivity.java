package com.example.pluginclooud;

import java.util.List;

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.IDynamixFacade;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.Result;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

	private final String TAG = this.getClass().getSimpleName();
	Context context;
	IDynamixFacade dynamix;
    private Boolean MyListenerIsRegistered = false;
    private MyListener listener = null;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // use this to call bind service -- TabActivity issue
        context = this.getApplicationContext(); 
        
        setup_tabs();
        
        connect_to_dynamix();
        
        // receive intents from child tabs
        listener = new MyListener();
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
	  
	  // register intent listener
      if (!MyListenerIsRegistered) {
          registerReceiver(listener, new IntentFilter("disconnect_dynamix"));
          registerReceiver(listener, new IntentFilter("connect_dynamix"));
          registerReceiver(listener, new IntentFilter("summon_plugin"));
          MyListenerIsRegistered = true;
      } 
	}
	
	@Override
	protected void onPause() {
	  super.onPause();
		
	  // unregister intent listener
      if (MyListenerIsRegistered) {
          unregisterReceiver(listener);
          MyListenerIsRegistered = false;
      }
	} 
	
	// listener to receive intents from child tabs
    protected class MyListener extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent)
        {	
            if( intent.getAction().equals("connect_dynamix") )
            {
                connect_to_dynamix();
            }
            else if( intent.getAction().equals("disconnect_dynamix") )
            {
            	disconnect_from_dynamix();
            }
            else if( intent.getAction().equals("summon_plugin") )
            {
            	String contextType = intent.getStringExtra("contextType");
            	
            	try
            	{
            		summonPlugin(contextType);
            	}
            	catch (Exception e)
            	{
            		Log.e(TAG, e.toString());
            	}
            }
        }
    }	
	
	// connect to dynamix framework
    private void connect_to_dynamix()
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
    private void disconnect_from_dynamix()
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
        
    // setup applications tabs
    private void setup_tabs()
    {
        Resources ressources = getResources(); 
        TabHost tabHost = getTabHost();
    
              
        // profile tab
        Intent intentProfile = new Intent().setClass(this, profileTab.class);
        TabSpec tabSpecProfile = tabHost.newTabSpec("profile")
                .setIndicator("", ressources.getDrawable(R.drawable.ic_tab_profile))
                .setContent(intentProfile);

        // dynamix tab
        Intent intentDynamix = new Intent().setClass(this, dynamixTab.class);
        TabSpec tabSpecDynamix = tabHost.newTabSpec("dynamix")
                .setIndicator("", ressources.getDrawable(R.drawable.ic_tab_dynamix))
                .setContent(intentDynamix);         
        
        //tests tab
        Intent intentTests = new Intent().setClass(this, testsTab.class);
        TabSpec tabSpecTests = tabHost.newTabSpec("tests")
                .setIndicator("", ressources.getDrawable(R.drawable.ic_tab_tests))
                .setContent(intentTests);    

        // add all tabs 
        tabHost.addTab(tabSpecProfile);
        tabHost.addTab(tabSpecDynamix);
        tabHost.addTab(tabSpecTests);

        //set Windows tab as default (zero based)
        tabHost.setCurrentTab(0);      
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
		      			
			  String representation = event.getStringRepresentation("text/plain");
			  
			  // handle context event from plugin1
			  if( representation.contains("number1=") )
			  {
				  String[] splits = representation.split("=");	  
				  String number1 = splits[1];

				  sendEventPluginIntent(number1);
				  
				  Log.i(TAG, "get event from plugin1");
			  }
			  else if( representation.contains("dependency") )
			  {
				  String[] splits = representation.split("=");	  
				  String dependency_contextType = splits[1];

				  summonPlugin(dependency_contextType);
				  				  
				  Log.i(TAG, "summon plugin as a dependency");
			  }
			  
			  if( representation.contains("number2=") )
			  {
				  String[] splits = representation.split("=");	  
				  String number2 = splits[1];

				  sendEventPluginIntent(number2);
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
			
			sendEventPluginSupportedIntent(contextType);
			
			// send context request to plugin with contextType which just get support
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

	// send intent to testTabActivity about plugin data event
	private void sendEventPluginIntent(String data){
	    Intent i = new Intent();
		i.putExtra("messageData", data);
	    i.setAction("event_plugin");
	    sendBroadcast(i);
	}	
	
	// send intent to testTabActivity about plugin supported event
	private void sendEventPluginSupportedIntent(String contextType){
	    Intent i = new Intent();
		i.putExtra("contextType", contextType);
	    i.setAction("event_supported_plugin");
	    sendBroadcast(i);
	}
	
	// send context request to plugin with context type contextType
	private void sendRequest(String contextType) throws RemoteException, InterruptedException
	{
			String requestId = contextType;		
			dynamix.contextRequest(dynamixCallback, requestId, contextType);
	}

	// add context support using plugin context type 
	private void summonPlugin(String contextType) throws RemoteException {
	
		Log.i(TAG, "must summon plugin: " + contextType);
		
		Result result1 = dynamix.addContextSupport(dynamixCallback, contextType);
		if (!result1.wasSuccessful())
			Log.w(TAG,
					"Call was unsuccessful! Message: " + result1.getMessage() + " | Error code: "
							+ result1.getErrorCode());	
	}
    
}
