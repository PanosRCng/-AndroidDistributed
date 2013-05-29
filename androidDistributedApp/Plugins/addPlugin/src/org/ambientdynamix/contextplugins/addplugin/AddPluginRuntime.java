package org.ambientdynamix.contextplugins.addplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.contextplugin.AutoReactiveContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;


public class AddPluginRuntime extends AutoReactiveContextPluginRuntime {
	// Static logging TAG
	private final String TAG = this.getClass().getSimpleName();
	// Our secure context
	private Context context;
	
	private List<String> dependencies;	
	UUID requestId;
	
	private String dependency1 = "org.ambientdynamix.contextplugins.oneplugin";
	private String dependency2 = "org.ambientdynamix.contextplugins.twoplugin";
	
	private double number1 = 0;
	private double number2 = 0;
	private double result = 0;
	
	private boolean running;
	
	@Override
	public void init(PowerScheme powerScheme, ContextPluginSettings settings) throws Exception {
		// Set the power scheme
		this.setPowerScheme(powerScheme);
		// Store our secure context
		this.context = this.getSecuredContext();
		
		// set experiment dependencies as a list of ContextTypes of standard plugins
		dependencies = new ArrayList<String>();
		dependencies.add(dependency1);
		dependencies.add(dependency2);
		running = false;
	}

	// handle incoming context request
	@Override
	public void handleContextRequest(UUID requestId, String contextType)
	{		
		// Check for proper context type
		if (contextType.equalsIgnoreCase(AddPluginInfo.CONTEXT_TYPE))
		{

		} else {
			sendContextScanError(requestId, "NO_CONTEXT_SUPPORT for " + contextType, ErrorCodes.NO_CONTEXT_SUPPORT);
		}
	}

	@Override
	public void handleConfiguredContextRequest(UUID requestId, String contextType, Bundle config) {
		
		// store caller application UUID - interact only with that, until stop 
		this.requestId = requestId;
		
		// get command
		String command = (String) config.get("command");
		// get data
		String data = (String) config.get("data");
		
		if( command.equals("do") )
		{
			running = true;
		}
		else if( command.equals(dependency1) )
		{
			number1 = Double.parseDouble(data);
			doJob();
		}
		else if( command.equals(dependency2) )
		{
			number2 = Double.parseDouble(data);
			doJob();
		}
		else if( command.equals("start") )
		{
			start();
		}
		else if( command.equals("init") )
		{
			callForDependencies();
		}
		else if( command.equals("stop") )
		{
			stop();
		}
		else if( command.equals("destroy") )
		{
			destroy();
		}
		else if( command.equals("pause") )
		{
			 // pause();
		}
		else
		{
			Log.i(TAG, "command not supported");
		}
		
		if( command.equals(dependency1) )
		{
			number1 = Double.parseDouble(data);
		}
		else if( command.equals(dependency2) )
		{
			number2 = Double.parseDouble(data);
		}
		
	//	handleContextRequest(requestId, contextType);
	}	
	
	@Override
	public void start() {
		Log.d(TAG, "Started!");
		
		sendStatusMsg("started");
	}
	
	@Override
	public void stop() {
		
		/*
		 * At this point, the plug-in should stop scanning for context and/or handling context requests; however, we
		 * should retain resources needed to run again.
		 */
		Log.d(TAG, "Stopped!");
	}

	@Override
	public void destroy() {
		/*
		 * At this point, the plug-in should stop and release any resources. Nothing to do in this case except for stop.
		 */
		Log.d(TAG, "Destroyed!");
	}

	@Override
	public void updateSettings(ContextPluginSettings settings) {
		// Not supported
	}

	@Override
	public void setPowerScheme(PowerScheme scheme) {
		// Not supported
	}

	@Override
	public void doManualContextScan() {
		// Not supported
	}
		
	private void callForDependencies()
	{
		String message = "";
		for( String dependency : dependencies )
		{
			message = message + "@" + dependency;
		}
		
		AddPluginInfo info = new AddPluginInfo(message);
				
		sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
	
	private void doJob()
	{
		result = number1 + number2;
		Log.i(TAG, "result= " + result);
		
		String message = Double.toString(result);
		AddPluginInfo info = new AddPluginInfo(message);
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
	
	private void sendStatusMsg(String msg)
	{
		String message = msg;
		AddPluginInfo info = new AddPluginInfo(message);
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
}