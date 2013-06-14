package com.example.androiddistributed;

import java.io.File;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	}
/*	
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
*/	
	
	public void sendThreadMessage(String message)
	{
		Message msg = new Message();
		msg.obj = message;
		handler.sendMessage(msg);
	}
}
