package com.example.androiddistributed;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Handler;
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
	}
	
	public void report(String jobId)
	{
		Log.i(TAG, "reporter report job with job_id: " + jobId);
		
		String data = readResults(jobId+"_store");
		
		Log.i(TAG, "results from job: " + data);
	}
	
	public String readResults(String filename)
	{		
		String data="";
		
	    try
	    {
	    	FileInputStream fIn = context.openFileInput (filename) ;
	    	InputStreamReader isr = new InputStreamReader ( fIn ) ;
	        BufferedReader buffreader = new BufferedReader ( isr ) ;

	        String readString = buffreader.readLine ( ) ;
	        while ( readString != null )
	        {
	        	data = data + "\n" + readString ;
	            readString = buffreader.readLine ( ) ;
	        }

	        isr.close ( ) ;
	    } catch ( Exception e )
	    {
	    	Log.e(TAG, e.toString() );
	    }
	    
	    return data;
	}
}
