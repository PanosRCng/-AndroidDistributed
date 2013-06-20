package com.example.androiddistributed;

import java.io.File;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.google.gson.Gson;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Reporter extends Thread implements Runnable {

	private final String TAG = this.getClass().getSimpleName();
	
	private Handler handler;
	private Context context;
	
	public Reporter(Handler handler, Context context)
	{
		this.handler = handler;
		this.context = context;
	}
	
	public void run()
	{
		try
		{
			Log.d(TAG, "running");
			Thread.sleep(1000); //This could be something computationally intensive.
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		File dir = context.getFilesDir();		
		File[] files = dir.listFiles();
		for(File file : files)
		{
				Log.i("WTF", file.getName());
		}
	}
	
	public void report(String jobId)
	{
		Log.i(TAG, "reporter report job with job_id: " + jobId);
		
		if( ping() )
		{
			Report report = new Report(jobId);
			report.setResultsUrl(jobId+"_report");
			
			String jobReportPath = null;
			
			File dir = context.getFilesDir();		
			File[] files = dir.listFiles();
			for(File file : files)
			{				
				if( file.getName().equals(jobId+"_report") )
				{
					jobReportPath = file.getName();
				}
			}
			
			UploadReport uploadReport = new UploadReport();
			uploadReport.upload(jobReportPath);
			
			Gson gson = new Gson();
			String jsonReport = gson.toJson(report);
						
			sendReportResults(jsonReport);
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
	
	
	private int sendReportResults(String jsonReport)
	{
		final String NAMESPACE = "http://helloworld/";
		final String URL = "http://150.140.22.232:8080/services/HelloWorld?wsdl"; 
		final String METHOD_NAME = "reportResults";
		final String SOAP_ACTION =  "";
		
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
		
		return ack;
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
	
	public void sendThreadMessage(String message)
	{
		Message msg = new Message();
		msg.obj = message;
		handler.sendMessage(msg);
	}
}
