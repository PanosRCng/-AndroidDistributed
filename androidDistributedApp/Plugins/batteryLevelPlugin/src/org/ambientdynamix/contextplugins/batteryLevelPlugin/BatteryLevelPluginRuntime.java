package org.ambientdynamix.contextplugins.batteryLevelPlugin;

import java.util.UUID;

import org.ambientdynamix.api.contextplugin.AutoReactiveContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class BatteryLevelPluginRuntime extends AutoReactiveContextPluginRuntime {
	// Static logging TAG
	private final String TAG = this.getClass().getSimpleName();
	// Our secure context
	private Context context;
	
	private int batteryLevel = -1;
	
	private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			batteryLevel = intent.getIntExtra("level", -1);
			
			Log.i("battery status chaned: ", Integer.toString(batteryLevel));
		}
	};

	private boolean running = false;
	private Handler handler;
	private String state;
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
		
		state = "not_ready";
		handler = new Handler();
		running = false;
		batteryLevel = -1;
	}

	// handle incoming context request
	@Override
	public void handleContextRequest(UUID requestId, String contextType)
	{
		//
	}

	@Override
	public void handleConfiguredContextRequest(UUID requestId, String contextType, Bundle config)
	{
		// get command
		String command = (String) config.get("command");
		// get data
		String data = (String) config.get("data");
		
		if( command.equals("ping") )
		{
			pong();
		}
		else if( command.equals("do") )
		{
			startDoJob();
		}
		else if( command.equals("stop") )
		{
			stop();
		}
		else
		{
			Log.i(TAG, "command not supported");
		}
	}	
	
	@Override
	public void start()
	{		
		Log.d(TAG, "ready!");	
		setState("ready");
		doJob();
	}
	
	@Override
	public void stop()
	{
		/*
		 * At this point, the plug-in should stop scanning for context and/or handling context requests; however, we
		 * should retain resources needed to run again.
		 */

		setState("stopped");
		context.unregisterReceiver(batteryLevelReceiver);
		running = false;
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
	
	
	private void startDoJob()
	{
		context.registerReceiver(batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		setState("running");
		running = true;
		handler.postDelayed(runnable, 20000);
	}
	
	private void doJob()
	{
		Log.i("battery level plugin", Integer.toString(this.batteryLevel));
		BatteryLevelPluginInfo info = new BatteryLevelPluginInfo(this.batteryLevel);
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
	
	private void setState(String state)
	{
		this.state = state;
		pong();
	}
	
	private void pong()
	{
		BatteryLevelPluginInfo info = new BatteryLevelPluginInfo(this.batteryLevel);
		info.setState(state);		
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
}