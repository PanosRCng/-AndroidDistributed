package org.ambientdynamix.contextplugins.batteryTemperaturePlugin;

import java.util.UUID;

import org.ambientdynamix.api.contextplugin.AutoReactiveContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


@SuppressLint("NewApi")
public class BatteryTemperaturePluginRuntime extends AutoReactiveContextPluginRuntime {
	// Static logging TAG
	private final String TAG = this.getClass().getSimpleName();
	// Our secure context
	private Context context;
	
	private SensorManager sensorManager;
	private int temperature;
	private boolean running = false;
	private Handler handler;
	private String state;
	
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver()
	{
	    @Override
	    public void onReceive(Context arg0, Intent intent)
	    {
	    	temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
	      
	    	Log.i("battery temperature", "temp is: " + Integer.toString(temperature));
	    }
	  };
	
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
		temperature = -1;
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

		context.unregisterReceiver(mBatInfoReceiver);
		
		setState("stopped");
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
	
	
	@SuppressLint("NewApi")
	private void startDoJob()
	{
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		setState("running");
		running = true;

		context.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				
		handler.postDelayed(runnable, 20000);
	}
	
	private void doJob()
	{
		BatteryTemperaturePluginInfo info = new BatteryTemperaturePluginInfo(this.temperature);
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
	
	private void setState(String state)
	{
		this.state = state;
		pong();
	}
	
	private void pong()
	{
		BatteryTemperaturePluginInfo info = new BatteryTemperaturePluginInfo(this.temperature);
		info.setState(state);		
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
}