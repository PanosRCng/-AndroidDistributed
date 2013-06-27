package org.ambientdynamix.contextplugins.WifiPlugin;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class WifiPluginRuntime extends AutoReactiveContextPluginRuntime {
	// Static logging TAG
	private final String TAG = this.getClass().getSimpleName();
	// Our secure context
	private Context context;
	
	private String bssid = "-1";
	
	private BroadcastReceiver myWifiReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context arg0, Intent arg1)
		{
			// TODO Auto-generated method stub
			NetworkInfo networkInfo = (NetworkInfo) arg1.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
	   
			if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
			{
				DisplayWifiState();
			}
	  }};
	
	  private void DisplayWifiState()
	  {    
		  ConnectivityManager myConnManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo myNetworkInfo = myConnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		  WifiManager myWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		  WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();

		  if (myNetworkInfo.isConnected())
		  {
			  int myIp = myWifiInfo.getIpAddress();
		  
			  int intMyIp3 = myIp/0x1000000;
			  int intMyIp3mod = myIp%0x1000000;
		      
			  int intMyIp2 = intMyIp3mod/0x10000;
			  int intMyIp2mod = intMyIp3mod%0x10000;
		      
			  int intMyIp1 = intMyIp2mod/0x100;
			  int intMyIp0 = intMyIp2mod%0x100;

			  String ip = String.valueOf(intMyIp0)
					  + "." + String.valueOf(intMyIp1)
					  + "." + String.valueOf(intMyIp2)
					  + "." + String.valueOf(intMyIp3);
		  
			  Log.i("WTF", "ip:" + ip);
			  
			  String SSID = myWifiInfo.getSSID();
			  String BSSID = myWifiInfo.getBSSID();
			  String Speed = String.valueOf(myWifiInfo.getLinkSpeed()) + " " + WifiInfo.LINK_SPEED_UNITS;
			  String RSSID = String.valueOf(myWifiInfo.getRssi());

			  Log.i("WTF", "SSID: " + SSID);
			  Log.i("WTF", "BSSID: " + BSSID);
			  Log.i("WTF", "SPEED: " + Speed);
			  Log.i("WTF", "RSSID: " + RSSID);
			  
		  }
		  else
		  {
			  Log.i("WTF", "wifi not connected");
		  }
	  }

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
		bssid = "-1";
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
		context.unregisterReceiver(this.myWifiReceiver);
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
	    context.registerReceiver(this.myWifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		setState("running");
		running = true;
		handler.postDelayed(runnable, 20000);
	}
	
	private void doJob()
	{
		Log.i("bssid wifi plugin", this.bssid);
		WifiPluginInfo info = new WifiPluginInfo(this.bssid);
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
	
	private void setState(String state)
	{
		this.state = state;
		pong();
	}
	
	private void pong()
	{
		WifiPluginInfo info = new WifiPluginInfo(this.bssid);
		info.setState(state);		
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
}