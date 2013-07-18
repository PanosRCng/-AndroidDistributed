package com.example.androiddistributed;

import java.io.File;
import java.io.FileOutputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.IContextInfo;

import org.ambientdynamix.api.application.IDynamixFacade;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.contextplugins.WifiPlugin.IWifiPluginInfo;
import org.ambientdynamix.contextplugins.myExperimentPlugin.IExperimentPluginInfo;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.google.gson.Gson;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

public class Scheduler extends Thread implements Runnable {
		
	// get TAG name for reporting to LogCat
	private final String TAG = this.getClass().getSimpleName();
	
	private Handler handler;
	private Context context;
	
	IDynamixFacade dynamix;
	private SensorProfiler sensorProfiler;
	private Reporter reporter;
	private PhoneProfiler phoneProfiler;
	public Job currentJob;
	private int resultCounter;
	
	private SharedPreferences pref;
	private Editor editor;
	
	Stack jobs;
	Stack msgs;
    Map<String, Boolean> sensorsPermissions;
	
    private boolean free_to_commit = true;
    
    // scheduler constructor
	public Scheduler(Handler handler, Context context, SensorProfiler sensorProfiler, Reporter reporter, PhoneProfiler phoneProfiler)
	{
		this.handler = handler;
		this.context = context;
		this.sensorProfiler = sensorProfiler;
		this.reporter = reporter;
		this.phoneProfiler = phoneProfiler;
		
		currentJob = new Job();
		jobs = new Stack();
		msgs = new Stack();
		
        pref = context.getApplicationContext().getSharedPreferences("runningJob", 0); // 0 - for private mode
        editor = pref.edit();
		
		// get list of permissions about the available sensors
		sensorsPermissions = sensorProfiler.getSensorsPermissions();
	}
	
	public void run()
	{	
		try
		{
			Log.d(TAG, "running");
			Thread.sleep(1000); //This could be something computationally intensive.
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	// connect to dynamix framework - needs the application's context to bind the dynamix service
    public void connect_to_dynamix()
    {
		if (dynamix == null)
		{
			context.bindService(new Intent(IDynamixFacade.class.getName()), sConnection, Context.BIND_AUTO_CREATE);
			Log.i(TAG, "Connecting to Dynamix...\n");
		}
		else
		{
			try
			{
				if (!dynamix.isSessionOpen())
				{
					Log.i(TAG, "Dynamix connected... trying to open session\n");
					dynamix.openSession();					
				}
				else
				{
					Log.i(TAG, "Session is already open\n");
					sendThreadMessage("dynamix_connected");
				}
			}
			catch (RemoteException e)
			{
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
						
			// Open a Dynamix Session if it's not already opened, otherwise start commited jobs
			if (dynamix != null)
			{
				if (!dynamix.isSessionOpen())
				{
					dynamix.openSession();
				}
				else
				{
					sendThreadMessage("dynamix_connected");
					startJob();
				}
			}
			else
			{				
				Log.i(TAG, "dynamix already connected");
			}
		}
		
		// catch events from our plugin

		@Override
		public void onContextEvent(ContextEvent event) throws RemoteException
		{			
			if (event.hasIContextInfo())
			{	
				IContextInfo nativeInfo = event.getIContextInfo();
				currentJob.getMsg(nativeInfo);
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
			
			sendThreadMessage("dynamix_connected");
			
			// start commited jobs
			startJob(); 
		}

		@Override
		public void onSessionClosed() throws RemoteException {
			Log.i(TAG, "A1 - onSessionClosed");
			
			sendThreadMessage("dynamix_disconnected");
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
						
			pingPlugin( supportInfo.getContextType() );

			free_to_commit = true;
			startJob();	
		}

		@Override
		public void onContextSupportRemoved(ContextSupportInfo supportInfo) throws RemoteException {
			Log.i(TAG, "A1 - onContextSupportRemoved for " + supportInfo.getSupportId());
		}

		@Override
		public void onContextTypeNotSupported(String contextType) throws RemoteException {
			Log.i(TAG, "A1 - onContextTypeNotSupported for " + contextType);

			free_to_commit = true;
			startJob();
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
			
			commitJob("org.ambientdynamix.contextplugins.myExperimentPlugin");
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

		@Override
		public void onContextPluginInstallProgress(
				ContextPluginInformation arg0, int arg1) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	
	
	//	@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@	//
	
	
	// call to synamix commit a job plugin to dynamix framework
	public void commitJob(String contextType)
	{		
		currentJob = new Job(contextType, this);
		
		resultCounter=0;
		
		jobs.push(contextType);

		try
		{
			// Open a Dynamix Session if it's not already opened, otherwise start commited jobs
			if (dynamix != null)
			{
				if (!dynamix.isSessionOpen())
				{
					dynamix.openSession();	// open session -- onSessionOpened we call and there startJobs()
				}
				else
				{				
					startJob();
				}
			}

		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
		
	}
	
	// call to synamix commit a dependency plugin to dynamix framework
	public void commitDependency(String contextType)
	{
		jobs.push(contextType);
			
		try
		{
			// Open a Dynamix Session if it's not already opened, otherwise start commited jobs
			if (dynamix != null)
			{
				if (!dynamix.isSessionOpen())
				{
					dynamix.openSession();	// open session -- onSessionOpened we call and there startJobs()
				}
				else
				{				
					startJob();
				}
			}

		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
		
	}
	
	// start commited jobs
	private void startJob()
	{				
		if( !(jobs.isEmpty()) && (free_to_commit) )
		{
			String jobContextType = (String) jobs.pop();
			
			try
			{				
				free_to_commit = false;
				summonPlugin(jobContextType);
			}
			catch (Exception e)
			{
				Log.w(TAG, e.toString());
			}
		}
		
	}
	
	
	// add context support using plugin context type 
	private void summonPlugin(String contextType) throws RemoteException
	{				
		Result result1 = dynamix.addContextSupport(dynamixCallback, contextType);
		if (!result1.wasSuccessful())
			Log.w(TAG,
					"Call was unsuccessful! Message: " + result1.getMessage() + " | Error code: "
							+ result1.getErrorCode());	
	}
	
	public void deletePlugin(String contextType) throws RemoteException
	{				
	//	dynamix.requestContextPluginUninstall(GpsPLuginInfo);
		
		Result result1 = dynamix.removeContextSupportForContextType(dynamixCallback, contextType);
		if (!result1.wasSuccessful())
			Log.w(TAG,
					"Call was unsuccessful! Message: " + result1.getMessage() + " | Error code: "
							+ result1.getErrorCode());	
	}
		
	// send context request to plugin with context type contextType
	private void sendRequest(String contextType) throws RemoteException, InterruptedException
	{
		String requestId = contextType;			
		dynamix.contextRequest(dynamixCallback, requestId, contextType);
	}
	
	// send configured request to plugin with context type contextType and a bundle
	private void sendConfiguredRequest(String contextType, Bundle config) throws RemoteException, InterruptedException
	{
		String requestId = contextType;		
		dynamix.configuredContextRequest(dynamixCallback, requestId, contextType, config);
	}
	
	// pass data to experiment plugin from a dependency plugin
	public void sendData(String dstPluginId, Bundle data)
	{				
		try
		{
			sendConfiguredRequest(dstPluginId, data);
		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
	}
	
	// stop plugin with ContextType contextType
	public void stopPlugin(String pluginId)
	{
		// set stop Bundle
		Bundle stop = new Bundle();
		stop.putString("command", "stop");
		
		try
		{
			sendConfiguredRequest(pluginId, stop);
		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
	}
	
	// start plugin with ContextType contextType
	public void startPlugin(String contextType)
	{		
		// set start Bundle
		Bundle start = new Bundle();
		start.putString("command", "start");
		
		try
		{
			sendConfiguredRequest(contextType, start);
		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
	}
		
	// start plugin with ContextType contextType
	public void pingPlugin(String contextType)
	{		
		// set start Bundle
		Bundle ping = new Bundle();
		ping.putString("command", "ping");
		
		try
		{
			sendConfiguredRequest(contextType, ping);
		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
	}
	
	// destroy plugin with ContextType contextType
	private void destroyPlugin(String contextType)
	{
		// set destroy Bundle
		Bundle destroy = new Bundle();
		destroy.putString("command", "destroy");
		
		try
		{
			
		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
	}
	
	private void pausePlugin(String contextType)
	{
		//set pause bundle
		Bundle pause = new Bundle();
		pause.putString("command", "pause");
		
		try
		{

		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
	}
	
	public void doJobPlugin(String contextType)
	{				
		//set do bundle
		Bundle dojob = new Bundle();
		dojob.putString("command", "do");
		
		try
		{
			sendConfiguredRequest(contextType, dojob);	
		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
	}
	
	public void storeJobResults(Bundle data)
	{	
		String FILENAME = currentJob.getContextType()+"_report";	
					
		try
		{
			FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_APPEND | Context.MODE_PRIVATE);
		
			Set<String> keys = data.keySet();
			for(String key : keys)
			{
				String value = data.getString(key);
				
				Log.i("key", key);
				Log.i("value", value);
				
				String line = key + "\t" + value + System.getProperty("line.separator");
				fos.write(line.getBytes());
			}
			
			fos.close();
		}
		catch(Exception e)
		{
			Log.e(TAG, e.toString());
		}
		
		// finished or stopped
		sendThreadMessage("report_job:" + currentJob.getContextType());
	}
	
	public void reportJob(String jobId)
	{
		reporter.report(jobId);
		
		stopCurrentPlugin();
	//	currentJob = new Job();
		
		calcelCurrentJob();
	}
	
	public void sendThreadMessage(String message)
	{
		Message msg = new Message();
		msg.obj = message;
		handler.sendMessage(msg);
	}
	
	public void stopCurrentPlugin()
	{			
		if(currentJob.jobState!=null)
		{
			currentJob.stopJob();
		}
	}
	
	public void calcelCurrentJob()
	{
		currentJob.setState("finished");
		currentJob = new Job();
	
		editor.putString("runningJob", "-1");
		editor.putString("runningExperimentUrl", "-1");
		editor.commit();
		
		sendThreadMessage("job_name:" + "no job running");
	}
	
	public void startCurrentJob()
	{
		if(currentJob.jobState != null)
		{
			currentJob.setState("not_ready");	
			try
			{
				summonPlugin(currentJob.getContextType());
			}
			catch(Exception e)
			{
				Log.e(TAG, e.toString());
			}
		}
	}
	
	public void sensorsPermissionsChanged()
	{
		sensorsPermissions = sensorProfiler.getSensorsPermissions();
		Log.i(TAG, "sensor permissions changed");
		// stop the job if violates the new sensors permissions
	}

	
}
