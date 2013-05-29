package org.ambientdynamix.contextplugins.twoplugin;

import java.util.UUID;

import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.contextplugin.AutoReactiveContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class TwoPluginRuntime extends AutoReactiveContextPluginRuntime {
	// Static logging TAG
	private final String TAG = this.getClass().getSimpleName();
	// Our secure context
	private Context context;
	
	private boolean running = false;
	private Handler handler;
	private Runnable runnable = new Runnable()
	{
		@Override
		public void run()
		{
			if(running)
			{	
				doJob();
				handler.postDelayed(this, 20000);
			}
		}
	};
	
	@Override
	public void init(PowerScheme powerScheme, ContextPluginSettings settings) throws Exception {
		// Set the power scheme
		this.setPowerScheme(powerScheme);
		// Store our secure context
		this.context = this.getSecuredContext();
	}

	// handle incoming context request
	@Override
	public void handleContextRequest(UUID requestId, String contextType)
	{
		//
	}

	@Override
	public void handleConfiguredContextRequest(UUID requestId, String contextType, Bundle config) {
		
		// get command
		String command = (String) config.get("command");
		// get data
		String data = (String) config.get("data");
		
		if( command.equals("start") )
		{
			start();
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
		
	//	handleContextRequest(requestId, contextType);
	}	
	
	@Override
	public void start() {
		Log.d(TAG, "Started!");
		
		running = true;
		handler = new Handler();
		handler.postDelayed(runnable, 20000);
	}
	
	@Override
	public void stop()
	{
		/*
		 * At this point, the plug-in should stop scanning for context and/or handling context requests; however, we
		 * should retain resources needed to run again.
		 */
		Log.d(TAG, "Stopped!");
		
		running = false;
		handler = null;
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
	
	private void doJob()
	{
		TwoPluginInfo info = new TwoPluginInfo(1.0);
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
}