package com.example.androiddistributed;

import java.util.Map;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.google.gson.Gson;

import android.os.Handler;
import android.util.Log;

public class Registration extends Thread implements Runnable {

	private final String TAG = this.getClass().getSimpleName();
	
	private Handler handler;
	private PhoneProfiler phoneProfiler;
	private SensorProfiler sensorProfiler;
	private Communication communication;
    Map<String, Boolean> sensorsPermissions;
	private String sensorsRules;
    
	public Registration(Handler handler, Communication communication, PhoneProfiler phoneProfiler, SensorProfiler sensorProfiler)
	{
		this.handler = handler;
		this.communication = communication;
		this.phoneProfiler = phoneProfiler;
		this.sensorProfiler = sensorProfiler;
		
		sensorsRules = sensorProfiler.getSensorRules();
		Log.i("sensor rules", sensorsRules);
	}
	
	public void run()
	{
		try
		{
			Log.d(TAG, "running");
			
			Thread.sleep(1000);
			
			register();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private void register()
	{
		int phoneId = phoneProfiler.getPhoneId();
		
		if( communication.ping() )
		{						
			if( communication.ping() )
			{				
				int serverPhoneId = communication.registerSmartphone(phoneId, sensorsRules);	
				phoneProfiler.setPhoneId(serverPhoneId);
			}
		}

	}
	
}
