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
    Map<String, Boolean> sensorsPermissions;
	private String sensorsRules;
    
	public Registration(Handler handler, PhoneProfiler phoneProfiler, SensorProfiler sensorProfiler)
	{
		this.handler = handler;
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
			register();
			
			Thread.sleep(1000); //This could be something computationally intensive.
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private void register()
	{
	//	String phoneId = phoneProfiler.getPhoneId();
		
		if( ping() )
		{
			registerSmartphone("samsungG1");
		}
	}
	
	private boolean ping()
	{
		Ping ping = new Ping();
		
		Gson gson = new Gson();
		String jsonPing = gson.toJson(ping);
		
		int pong = sendPing(jsonPing);
		
		if(pong == 1)
		{
			return true;
		}
		
		return false;
	}
	
	private void registerSmartphone(String phoneId)
	{
		Smartphone smartphone = new Smartphone(phoneId);
		smartphone.setSensorsRules(sensorsRules);
		smartphone.setTimeRules("time_rules");
		
		Gson gson = new Gson();
		String jsonSmartphone = gson.toJson(smartphone);
		
		sendRegisterSmartphone(jsonSmartphone);
	}

	private int sendPing(String jsonPing)
	{
		final String NAMESPACE = "http://helloworld/";
		final String URL = "http://150.140.22.232:8080/services/HelloWorld?wsdl"; 
		final String METHOD_NAME = "Ping";
		final String SOAP_ACTION =  "";
		
		int pong = 0;
		
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 

		PropertyInfo propInfo=new PropertyInfo();
		propInfo.name="arg0";
		propInfo.type=PropertyInfo.STRING_CLASS;
		propInfo.setValue(jsonPing);
  
		request.addProperty(propInfo);  

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
		envelope.setOutputSoapObject(request);
		
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
			
			pong = Integer.parseInt(resultsRequestSOAP.toString()); 
		}
		catch (Exception e)
		{
			//
		}
		
		return pong;
	}	
	
	private void sendRegisterSmartphone(String jsonSmartphone)
	{
		final String NAMESPACE = "http://helloworld/";
		final String URL = "http://150.140.22.232:8080/services/HelloWorld?wsdl"; 
		final String METHOD_NAME = "registerSmartphone";
		final String SOAP_ACTION =  "";
		
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 

		PropertyInfo propInfo=new PropertyInfo();
		propInfo.name="arg0";
		propInfo.type=PropertyInfo.STRING_CLASS;
		propInfo.setValue(jsonSmartphone);
  
		request.addProperty(propInfo);  
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
		envelope.setOutputSoapObject(request);
		
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
			
			Log.i("WTF", "request=" +resultsRequestSOAP.toString()); 
		}
		catch (Exception e)
		{
			//
		}
	}
}
