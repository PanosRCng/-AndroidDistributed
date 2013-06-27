package com.example.androiddistributed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
	private Communication communication;
	
	public Reporter(Handler handler, Context context, Communication communication)
	{
		this.handler = handler;
		this.context = context;
		this.communication = communication;
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
		
		if( communication.ping() )
		{		
			Log.i("WTF", "we got a ping");
			
			String jobReportPath = null;
			ArrayList<String> jobResults = new ArrayList<String>();
			
			File dir = context.getFilesDir();		
			File[] files = dir.listFiles();
			for(File file : files)
			{				
				if( file.getName().equals(jobId+"_report") )
				{
					jobReportPath = file.getName();
					
					jobResults = readResultsFromFile(jobReportPath);
					
					if(jobResults.size() > 0)
					{						
						Report report = new Report(jobId);
						report.setResults(jobResults);
						
						Gson gson = new Gson();
						String jsonReport = gson.toJson(report);
										
						int ack = communication.sendReportResults(jsonReport);
						
						Log.i(TAG, Integer.toString(ack));
						
						if(ack == 1)
						{
		                    if( file.delete() )
		                    {
		                    	Log.i(TAG, "ok we finish here");
		                    	
		                    	// refresh report tab
		                    }
						}
					}
				}
			}
		}
	}
		
	private ArrayList<String> readResultsFromFile(String jobReportPath)
	{
		ArrayList<String> jobResults = new ArrayList<String>();
		
		try
		{
			FileInputStream fis = context.openFileInput(jobReportPath);
		
			InputStreamReader in = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(in);
			
			String data = br.readLine();
			jobResults.add(data);
			
			while( data != null )
			{
				data = br.readLine();
				jobResults.add(data);
			}
		}
		catch(Exception e)
		{
			Log.e(TAG, e.toString());
		}
		
		return jobResults;
	}
	
	public void sendThreadMessage(String message)
	{
		Message msg = new Message();
		msg.obj = message;
		handler.sendMessage(msg);
	}
}
