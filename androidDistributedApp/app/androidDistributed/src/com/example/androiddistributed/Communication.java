package com.example.androiddistributed;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.google.gson.Gson;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Communication extends Thread implements Runnable {

	final String NAMESPACE = "http://helloworld/";
	final String URL = "http://83.212.115.57:8080/ADService/services/HelloWorld?wsdl"; 
	
	private Handler handler;
	
	// get TAG name for reporting to LogCat
	private final String TAG = this.getClass().getSimpleName();
	
	public Communication(Handler handler)
	{
		this.handler = handler;
	}
	
	public void run()
	{
		Log.d(TAG, "running");
	}
	
	public boolean ping()
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
	
	public int sendPing(String jsonPing)
	{
		final String METHOD_NAME = "Ping";
		final String SOAP_ACTION = "\""+"http://helloworld/Ping"+"\"";
		
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
			
			Log.i("WTF ping", resultsRequestSOAP.toString());
		}
		catch (Exception e)
		{
			Log.i("WTF catch", e.toString());
		}
		
		// ksoap2 HttpTransportSE issue -- connection never timeout or close
		// 
		//	connection close to server - connection stays open to client	
		//  so the first call is ok, but the second call receive from the server a RST
		//
		//  this because the server receives a packet for a closed socket 
		//  and insert RST in an attempt to block traffic
		//
		// connection never timeout or close -- so force it to break
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();					
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue
		
		return pong;
	}	
	
	public void sendThreadMessage(String message)
	{
		Message msg = new Message();
		msg.obj = message;
		handler.sendMessage(msg);
	}
	
	public int registerSmartphone(int phoneId, String sensorsRules)
	{
		int serverPhoneId = 0;
		
		Smartphone smartphone = new Smartphone(phoneId);
		smartphone.setSensorsRules(sensorsRules);
		smartphone.setTimeRules("time_rules");
		
		Gson gson = new Gson();
		String jsonSmartphone = gson.toJson(smartphone);
		
		String serverPhoneId_s = sendRegisterSmartphone(jsonSmartphone);
		
		try
		{
			serverPhoneId = Integer.parseInt(serverPhoneId_s);
		}
		catch(Exception e)
		{
			serverPhoneId = -1;
		}
		
		return serverPhoneId;
	}
	
	private String sendRegisterSmartphone(String jsonSmartphone)
	{
		Log.i(TAG, "send register smartphone");

		final String METHOD_NAME = "registerSmartphone";
		final String SOAP_ACTION = "\""+"http://helloworld/registerSmartphone"+"\"";
		
		String serverPhoneId = "";
		
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
			
			serverPhoneId = resultsRequestSOAP.toString(); 
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue -- connection never timeout or close
		// 
		//	connection close to server - connection stays open to client	
		//  so the first call is ok, but the second call receive from the server a RST
		//
		//  this because the server receives a packet for a closed socket 
		//  and insert RST in an attempt to block traffic
		//
		// connection never timeout or close -- so force it to break
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();					
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue
		
		return serverPhoneId;
	}
	
	public String getExperiment(int phoneId, String sensorRules)
	{
		Smartphone smartphone = new Smartphone(phoneId);		
		smartphone.setSensorsRules(sensorRules);
		Gson gson = new Gson();
		String jsonSmartphone = gson.toJson(smartphone);
		return sendGetExperiment(jsonSmartphone);
	}
	
	private String sendGetExperiment(String jsonSmartphone)
	{
		final String METHOD_NAME = "getExperiment";
		final String SOAP_ACTION = "\""+"http://helloworld/getExperiment"+"\"";
		
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 

		PropertyInfo propInfo=new PropertyInfo();
		propInfo.name="arg0";
		propInfo.type=PropertyInfo.STRING_CLASS;
		propInfo.setValue(jsonSmartphone);
  
		request.addProperty(propInfo);  
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
		envelope.setOutputSoapObject(request);
		
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		
		String response = "0";
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			
			SoapPrimitive resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
			
			response = resultsRequestSOAP.toString(); 
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue -- connection never timeout or close
		// 
		//	connection close to server - connection stays open to client	
		//  so the first call is ok, but the second call receive from the server a RST
		//
		//  this because the server receives a packet for a closed socket 
		//  and insert RST in an attempt to block traffic
		//
		// connection never timeout or close -- so force it to break
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();					
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue
		
		return response;
	}
	
	public int sendReportResults(String jsonReport)
	{
		Log.i("WTF", "doing report call");
		
		final String METHOD_NAME = "reportResults";		
		final String SOAP_ACTION = "\""+"http://helloworld/reportResults"+"\"";
		
		int ack = 0;
		
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 

		PropertyInfo propInfo=new PropertyInfo();
		propInfo.name="arg0";
		propInfo.type=PropertyInfo.STRING_CLASS;
		propInfo.setValue(jsonReport);
  
		request.addProperty(propInfo);  

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
		envelope.setOutputSoapObject(request);
		
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
			
			ack = Integer.parseInt(resultsRequestSOAP.toString()); 
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue -- connection never timeout or close
		// 
		//	connection close to server - connection stays open to client	
		//  so the first call is ok, but the second call receive from the server a RST
		//
		//  this because the server receives a packet for a closed socket 
		//  and insert RST in an attempt to block traffic
		//
		// connection never timeout or close -- so force it to break
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();					
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue
		
		return ack;
	}
	  
}
