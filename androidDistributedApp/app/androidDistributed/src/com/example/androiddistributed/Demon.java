package com.example.androiddistributed;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Demon extends Thread implements Runnable {

	private Handler handler;
	private Scheduler scheduler;
	private PhoneProfiler phoneProfiler;
	private Context context;
	private Communication communication;
	private SensorProfiler sensorProfiler;
	
	// get TAG name for reporting to LogCat
	private final String TAG = this.getClass().getSimpleName();
	
	public Demon(Handler handler, Context context, Communication communication, Scheduler scheduler, PhoneProfiler phoneProfiler, SensorProfiler sensorProfiler)
	{
		this.context = context;
		this.handler = handler;
		this.scheduler = scheduler;
		this.phoneProfiler = phoneProfiler;
		this.communication = communication;
		this.sensorProfiler = sensorProfiler;
	}
	
	public void run()
	{	
		try
		{
			Log.d(TAG, "running");
			Thread.sleep(1000); //This could be something computationally intensive.

			// create folder /sdcard/dynamix/
			File root = android.os.Environment.getExternalStorageDirectory();              
		    File dir = new File (root.getAbsolutePath() + "/dynamix");
		    if(dir.exists()==false)
		    {
		    	dir.mkdirs();
		    }
			
		    // check if all dependency plugins exist and repo, if not download them to /sdcard/dynamix/
			checkFile("plugs.xml");
		    checkFile("org.ambientdynamix.contextplugins.batteryLevelPlugin_9.47.1.jar");
			checkFile("org.ambientdynamix.contextplugins.batteryTemperaturePlugin_9.47.1.jar");
			checkFile("org.ambientdynamix.contextplugins.gpsplugin_9.47.1.jar");
			checkFile("org.ambientdynamix.contextplugins.WifiPlugin_9.47.1.jar");
			
			// tell to dynamix Framework to update its repository
			updateDynamixRepository();
				
			if( communication.ping() )
			{
				String jsonExperiment = communication.getExperiment( phoneProfiler.getPhoneId() );
					
				Log.i(TAG, jsonExperiment);
					
				if(jsonExperiment.equals("0"))
				{
					Log.i(TAG, "no experiment for us");
				}
				else
				{
					Gson gson = new Gson();
			       	Experiment experiment = gson.fromJson(jsonExperiment, Experiment.class);				

			       	if( experiment.getContextType().equals( scheduler.currentJob.getContextType() ) )
			       	{
			       		Log.i(TAG, "i already have this experiment, do not downloading it");
			       	}
			       	else 
			       	{
			            String[] smarDeps = sensorProfiler.getSensorRules().split("|");
			            String[] expDeps = experiment.getSensorDependencies().split("|");
			       		
			            Set<String> smarSet = new HashSet<String>(Arrays.asList(smarDeps));
			            Set<String> expSet = new HashSet<String>(Arrays.asList(expDeps));

	                    if( smarSet.equals(expSet) )
	                    {
			       			String contextType = experiment.getContextType();
			       			String url = experiment.getUrl();

			     	 		Downloader downloader = new Downloader();
			       	 		downloader.DownloadFromUrl(url, contextType+"_9.47.1.jar");
			        	
			       	 		scheduler.commitJob(contextType);
			       	 		
			        	}
			        	else
			        	{
			        		Log.i(TAG, "this experiment violates my sensor rules");
			        	}
			        }
				}
			}
				
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private void checkFile(String myFile)
	{	
		File root = android.os.Environment.getExternalStorageDirectory();               
	    File myfile = new File (root.getAbsolutePath() + "/dynamix/" + myFile);

	    if(myfile.exists()==false)
	    {	
	    	Downloader downloader = new Downloader();
	    	downloader.DownloadFromUrl("http://83.212.115.57/androidDistributed/dynamixRepository/"+myFile, myFile);
	    }
	}
	
	private void updateDynamixRepository()
	{
        Log.i(TAG, "send update dynamix repository intent");
        
        Intent i = new Intent();
        i.setAction("org.ambiendynamix.core.DynamixService");        
        context.sendBroadcast(i);
	}
	
	public void sendThreadMessage(String message)
	{
		Message msg = new Message();
		msg.obj = message;
		handler.sendMessage(msg);
	}
}
