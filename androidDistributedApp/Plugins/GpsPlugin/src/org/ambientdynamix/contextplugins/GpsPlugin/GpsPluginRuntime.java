package org.ambientdynamix.contextplugins.GpsPlugin;

import java.util.UUID;

import org.ambientdynamix.api.contextplugin.AutoReactiveContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class GpsPluginRuntime extends AutoReactiveContextPluginRuntime {
	// Static logging TAG
	private final String TAG = this.getClass().getSimpleName();
	// Our secure context
	private Context context;
	
	private String location = "-1";
    private LocationManager locationManager;
    private LocationListener locationListenerGps;
	
	public class CurrentLocationGps implements LocationListener
	{
	    @Override
	    public void onLocationChanged(Location loc) 
	    {
	        double lat = loc.getLatitude();
	        double lon = loc.getLongitude();
	        
	        Log.i("GPS", "location changed" + lat + " " + loc);
	        
	        location = lat + " " + lon;
	    }

		@Override
		public void onProviderDisabled(String arg0)
		{
			// TODO Auto-generated method stub	
			location = "-2";
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			// TODO Auto-generated method stub			
			location = "-1";
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			// TODO Auto-generated method stub
			location = "-3";
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
		location = "-1";
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
		
		locationManager.removeUpdates(locationListenerGps);
		
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
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        locationListenerGps = new CurrentLocationGps();

        try
        {
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1.0f, locationListenerGps);
        }
        catch (Exception e)
        {
        	Log.i("WTF", e.toString());
        }
        
		setState("running");
		running = true;
		handler.postDelayed(runnable, 20000);
	}
	
	private void doJob()
	{
        try
        {
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1.0f, locationListenerGps);
        }
        catch (Exception e)
        {
        	Log.i("WTF", e.toString());
        }
        	
		Log.i("gps plugin", this.location);
		GpsPluginInfo info = new GpsPluginInfo(this.location);
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
	
	private void setState(String state)
	{
		this.state = state;
		pong();
	}
	
	private void pong()
	{
		GpsPluginInfo info = new GpsPluginInfo(this.location);
		info.setState(state);		
		this.sendBroadcastContextEvent(new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);
	}
}