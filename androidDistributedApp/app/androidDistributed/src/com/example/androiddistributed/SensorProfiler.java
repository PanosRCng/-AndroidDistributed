package com.example.androiddistributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SensorProfiler extends Thread implements Runnable {

	private Handler handler;
	
	// get TAG name for reporting to LogCat
	private final String TAG = this.getClass().getSimpleName();

	private List<String> sensors;
	private List<String> permissions;
	private Map<String,Boolean> sensorsPermissions=new HashMap<String, Boolean>();
	
	Context context;
	
	public SensorProfiler(Handler handler, Context context)
	{		
		this.handler = handler;
		this.context = context;
		
		sensors= new ArrayList<String>();
		permissions = new ArrayList<String>();
	}
	
	public void run()
	{			
		try
		{
			Log.d(TAG, "running");
			Thread.sleep(1000); //This could be something computationally intensive.

			// get available sensors on this android device
		    getAvailableSensors(context);
		    
		     // get user permisions about sensors
			getPermissions();
			     
			     // set sensor permitions
			setPermissions(); 		
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	

	// make the sensors permissions available to other modules
	public Map<String, Boolean> getSensorsPermissions()
	{
		return sensorsPermissions;
	}
	
	// set the user permissions about the sensors
	private void setPermissions()
	{		
		for(String sensor : sensors)
		{
			if(permissions.contains(sensor))
			{
				sensorsPermissions.put(sensor, true);
			}
			else
			{
				sensorsPermissions.put(sensor, false);
			}
		}
	}
	
	// get user permissions about the sensors
	private void getPermissions()
	{		
		permissions.add("accelerometer");
		permissions.add("orientation");
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
	    		case Sensor.TYPE_TEMPERATURE : { type_s = "temperature"; break; }
	    		case Sensor.TYPE_MAGNETIC_FIELD : { type_s = "magnetic field"; break; }
	    		case Sensor.TYPE_ORIENTATION : { type_s = "orientation"; break;  }
	    		default : { type_s = "uknown" ; break; }
	    	}

	    	// add it to sensor list
	    	listSensorType.add(type_s);
	    }
	       
	    return listSensorType;
	}
	
	private void sensorsPresmissionsChanged()
	{
        Message message = handler.obtainMessage();
        message.obj = Double.toString(Math.random());
        handler.sendMessage(message);
	}
}
