package org.ambientdynamix.contextplugins.gpsplugin;

import java.util.UUID;

import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.contextplugin.AutoReactiveContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class GpsPluginRuntime extends AutoReactiveContextPluginRuntime {
	// Static logging TAG
	private final String TAG = this.getClass().getSimpleName();
	// Our secure context
	private Context context;
	
	private LocationManager locationManager;
	private String position = "";
	private boolean running = false;
	private Handler handler;
	private SensorManager sensorManager;
	private long time;
	private String state;
	private LocationListener locationListener;
	
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
	public void init(PowerScheme powerScheme, ContextPluginSettings settings) throws Exception
	{
		// Set the power scheme
		this.setPowerScheme(powerScheme);
		// Store our secure context

		this.context = this.getSecuredContext();
		
		state = "not_ready";
		handler = new Handler();
		running = false;
		position = "-1";	
		
		Log.i(TAG, "init OK");
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
		
		locationListener = new CurrentLocationGps();
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			
		Log.i(TAG, "start crap");
	}
	
	@Override
	public void stop()
	{			
		if(locationListener!=null)
		{
			locationManager.removeUpdates(locationListener);
		}
		
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
	
	private void startDoJob()
	{		
		if( locationListener!=null )
		{
			try
			{
				 Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				
	//			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			}
			catch(Exception e)
			{
				Log.e(TAG, e.toString());
			}
		}
		else
		{
			Log.i(TAG, "location manager is null");
		}
		
		setState("running");
		running = true;
		handler.postDelayed(runnable, 20000);
	}
	
	private void doJob()
	{
		GpsPluginInfo info = new GpsPluginInfo(this.position);
		Log.i("WTF", "posisiton is :" + this.position);
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
	
	private void setState(String state)
	{
		this.state = state;
		pong();
	}
	
	private void pong()
	{
		GpsPluginInfo info = new GpsPluginInfo(this.position);
		info.setState(state);		
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
	
	// gps location listener
	public class CurrentLocationGps implements LocationListener 
	{
	    @Override
	    public void onLocationChanged(Location loc) {
	        double lat = loc.getLatitude();
	        double lon = loc.getLongitude();
	        
	        Log.i(TAG, "location" + lat + " " + lon);
	        
	        position = lat+"@"+lon;
	    }

		@Override
		public void onProviderDisabled(String arg0)
		{
			Log.i("GPS", "disabled gps");	
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			Log.i("GPS", "enabled gps");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	}
}