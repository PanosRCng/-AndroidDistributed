package com.example.androiddistributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SensorProfiler extends Thread implements Runnable {

	private Handler handler;
	private Communication communication;
	private PhoneProfiler phoneProfiler;
	
	// get TAG name for reporting to LogCat
	private final String TAG = this.getClass().getSimpleName();

	private boolean batteryEnabled;
	private boolean batteryLevelEnabled;
	private boolean batteryTemperatureEnabled;
	private boolean gpsEnabled;
	private boolean gpsPositionEnabled;
	private boolean wifiEnabled;
	private boolean wifiBSSIDEnabled;
	private SharedPreferences pref;
	private Editor editor;
	
	private List<String> sensors;
	private List<String> permissions;
	private Map<String,Boolean> sensorsPermissions=new HashMap<String, Boolean>();
	private Map<String,String> sensorsContextTypes=new HashMap<String, String>();
	private String sensorRules="|";
	
	private  NetworkStateReceiver mReceiver;
	
	Context context;
	
	public SensorProfiler(Handler handler, Context context, Communication communication, PhoneProfiler phoneProfiler)
	{		
		this.handler = handler;
		this.context = context;
		this.communication = communication;
		this.phoneProfiler = phoneProfiler;
				
        pref = context.getApplicationContext().getSharedPreferences("sensors", 0); // 0 - for private mode
        editor = pref.edit();
		
        mReceiver = new NetworkStateReceiver();
        
		sensors= new ArrayList<String>();
		sensors = getAvailableSensors(context);
		
		// map sensors to contextTypes
		sensorsContextTypes.put("batteryLevel", "org.ambientdynamix.contextplugins.batteryLevelPlugin");
		sensorsContextTypes.put("batteryTemperature", "org.ambientdynamix.contextplugins.batteryTemperaturePlugin");		
		sensorsContextTypes.put("gpsPosition", "org.ambientdynamix.contextplugins.GpsPlugin");		
		sensorsContextTypes.put("wifiBSSID", "org.ambientdynamix.contextplugins.WifiScanPlugin");
		
		// get sensor permissions
		permissions = new ArrayList<String>();
		getPermissions();
		setPermissions();
	}
	
	public void run()
	{			
		try
		{
			Log.d(TAG, "running");
			Thread.sleep(1000); //This could be something computationally intensive.
			
	        // register network status receiver
	        IntentFilter filter = new IntentFilter();
	        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
	        context.registerReceiver(mReceiver, filter);
			
			if( isNetworkAvailable() )
			{
				sendThreadMessage("internet_status:internet_ok");
			}
			else
			{
				sendThreadMessage("internet_status:no_internet");
			}
					
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	// network changed receiver
	public class NetworkStateReceiver extends BroadcastReceiver
	{
	    public void onReceive(Context context, Intent intent)
	    {
	    	networkStatusChanged();
	    }
	}
	    
	// make the sensors permissions available to other modules
	public Map<String, Boolean> getSensorsPermissions()
	{		
		return sensorsPermissions;
	}
	
	public String getSensorRules()
	{
		return sensorRules;
	}
	
	// set the user permissions about the sensors
	private void setPermissions()
	{	
		sensorRules = "|";
		sensorsPermissions.clear();
		
		for(String sensor : sensors)
		{
			if(permissions.contains(sensor))
			{
				sensorsPermissions.put( sensorsContextTypes.get(sensor) , true);
				sensorRules = sensorRules + sensor + "|";
			}
			else
			{
				sensorsPermissions.put( sensorsContextTypes.get(sensor) , false);
			}
		}
	}
	
	// get user permissions about the sensors
	private void getPermissions()
	{		
		permissions.clear();
		
		editor.commit();
    	
	    // run first time after installation - or data clean
	    if( !(pref.contains("firstTime")) )
	    {
        	editor.putBoolean("firstTime", false);
        	
        	editor.putBoolean("battery", false);
        	editor.putBoolean("batteryLevel", false);
        	editor.putBoolean("batteryTemperature", false);
        	
        	editor.putBoolean("gps", false);
        	editor.putBoolean("gpsPosition", false);
        	
        	editor.putBoolean("wifi", false);
        	editor.putBoolean("wifiBSSID", false);
           	
        	editor.commit();
	    }


        batteryEnabled = pref.getBoolean("battery", false);
        batteryLevelEnabled = pref.getBoolean("batteryLevel", false);
        batteryTemperatureEnabled = pref.getBoolean("batteryTemperature", false);
	    gpsEnabled = pref.getBoolean("gps", false);
	    gpsPositionEnabled = pref.getBoolean("gpsPosition", false);
	    wifiEnabled = pref.getBoolean("wifi", false);
	   	wifiBSSIDEnabled = pref.getBoolean("wifiBSSID", false);
	    
	    if( batteryEnabled )
	    {
	    	if( batteryLevelEnabled )
	    	{
	    		permissions.add("batteryLevel");
	    	}
	    	
	    	if( batteryTemperatureEnabled )
	    	{
	    		permissions.add("batteryTemperature");
	    	}
	    }
	   	
	    if( gpsEnabled )
	    {
	    	if( gpsPositionEnabled )
	    	{
	    		permissions.add("gpsPosition");
	    	}
	    }
	    	
	    if( wifiEnabled )
	    {
	    	if( wifiBSSIDEnabled )
	    	{
	    		permissions.add("wifiBSSID");
	    	}
	    }				
	}
	
	// get list of the available sensor types
	private List<String> getAvailableSensors(Context context)
	{
		List<String> listSensorType = new ArrayList<String>();

		SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> listSensor = sensorManager.getSensorList(Sensor.TYPE_ALL);
	       
	    for(int i=0; i<listSensor.size(); i++)
	    {
	    	// find sensor type
	    	int type = listSensor.get(i).getType();
	    	String type_s="";
	    		    	
	    	switch(type)
	    	{
	    		case Sensor.TYPE_ACCELEROMETER : { type_s = "accelerometer"; break; }
	 //   		case Sensor.TYPE_TEMPERATURE : { type_s = "batteryTemperature"; break; }
	    		case Sensor.TYPE_MAGNETIC_FIELD : { type_s = "magnetic field"; break; }
	    		case Sensor.TYPE_ORIENTATION : { type_s = "orientation"; break;  }
	    		default : { type_s = "uknown" ; break; }
	    	}

	    	// add it to sensor list
	    	listSensorType.add(type_s);
	    }
	      
	    // always available
	    listSensorType.add("batteryLevel");
	    listSensorType.add("batteryTemperature");
	    listSensorType.add("gpsPosition");
	    listSensorType.add("wifiBSSID");
	    
	    return listSensorType;
	}
	
	// checks if there is a network interface - call and a service to make sure it goes to the internet 
	private boolean isNetworkAvailable()
	{
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private void networkStatusChanged()
	{
		if( isNetworkAvailable() )
		{
			sendThreadMessage("internet_status:internet_ok");
		}
		else
		{
			sendThreadMessage("internet_status:no_internet");
		}
	}
	
	public void sensorsPermissionsChanged()
	{
		getPermissions();
		setPermissions();
	//	communication.registerSmartphone(phoneProfiler.getPhoneId(), this.sensorRules);
	}
	
	public void sendThreadMessage(String message)
	{
		Message msg = new Message();
		msg.obj = message;
		handler.sendMessage(msg);
	}
}
