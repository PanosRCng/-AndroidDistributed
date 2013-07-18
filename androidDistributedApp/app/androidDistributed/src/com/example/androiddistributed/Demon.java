package com.example.androiddistributed;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
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
	
	private SharedPreferences pref;
	private Editor editor;
	String runningJob = "-1";
	String lastRunned = "-1";
	
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
		
        pref = context.getApplicationContext().getSharedPreferences("runningJob", 0); // 0 - for private mode
        editor = pref.edit();
        
        runningJob = pref.getString("runningJob", "-1");
        lastRunned = pref.getString("lastExperiment", "-1");
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
			checkFile("org.ambientdynamix.contextplugins.GpsPlugin_9.47.1.jar");
			checkFile("org.ambientdynamix.contextplugins.WifiScanPlugin_9.47.1.jar");
			
			handler.postDelayed(runnable, 10000);
											
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private Runnable runnable = new Runnable()
	{
		@Override
		public void run()
		{
//			pingExperiment();
			
	    	AsyncPingExp pingExp = new AsyncPingExp();
	    	pingExp.execute();
			
			handler.postDelayed(this, 60000);
		}
	};
	
/*	private void pingExperiment()
	{		
        runningJob = pref.getString("runningJob", "-1");
        lastRunned = pref.getString("lastExperiment", "-1");
        
		Log.i("running job", runningJob);
		
		if( runningJob.equals("-1") )
		{					
			if( communication.ping() )
			{
				String jsonExperiment = communication.getExperiment( phoneProfiler.getPhoneId(), sensorProfiler.getSensorRules() );
			
				Log.i(TAG, jsonExperiment);
			
				if(jsonExperiment.equals("0"))
				{
					Log.i(TAG, "no experiment for us");
				}
				else
				{
					Gson gson = new Gson();
					Experiment experiment = gson.fromJson(jsonExperiment, Experiment.class);				

					String[] smarDeps = sensorProfiler.getSensorRules().split("|");
					String[] expDeps = experiment.getSensorDependencies().split("|");
	       		
					Set<String> smarSet = new HashSet<String>(Arrays.asList(smarDeps));
					Set<String> expSet = new HashSet<String>(Arrays.asList(expDeps));

					if( smarSet.equals(expSet) )
					{
						String contextType = experiment.getContextType();
						String url = experiment.getUrl();

					//	if( lastRunned.equals(contextType) )
					//	{
					//		return;
					//	}
						
						Downloader downloader = new Downloader();
	       	 			downloader.DownloadFromUrl(url, contextType+"_9.47.1.jar");
	        			
	       	 			editor.putString("runningJob", contextType);
	       	 			editor.putString("runningExperimentUrl", experiment.getUrl());
	       	 			editor.commit();
	       	 			
	       	 			sendThreadMessage("job_name:"+experiment.getName());
	       	 			
	       				// tell to dynamix Framework to update its repository
	       	 			updateDynamixRepository();
	       	 			
	       	 		//	scheduler.commitJob(contextType);	
					}
					else
					{
						Log.i(TAG, "this experiment violates my sensor rules");
					}
				}
			}
			else
			{
				Log.i("WTF", "no ping for us");
			}
		}
		else
		{	
			if(scheduler.currentJob.jobState == null)
			{
				String runningExperimentUrl = pref.getString("runningExperimentUrl", "-1");
				checkExperiment(runningJob, runningExperimentUrl);
			
   	 			sendThreadMessage("job_name:"+runningJob);
				
				scheduler.commitJob(runningJob);
			}

		}
	}
*/
	
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
	
	private void checkExperiment(String contextType, String url)
	{		
		File root = android.os.Environment.getExternalStorageDirectory();               
	    File myfile = new File (root.getAbsolutePath() + "/dynamix/" + contextType + "_9.47.1.jar");

	    if(myfile.exists()==false)
	    {
			Downloader downloader = new Downloader();
			downloader.DownloadFromUrl(url, contextType+"_9.47.1.jar");
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
	
	public class AsyncPingExp extends AsyncTask<String, Void, String>
	{				
	    @Override
	    protected String doInBackground(String... params)
	    {
	        runningJob = pref.getString("runningJob", "-1");
	        lastRunned = pref.getString("lastExperiment", "-1");
	        
			Log.i("running job", runningJob);
			
			if( runningJob.equals("-1") )
			{					
				if( communication.ping() )
				{
					String jsonExperiment = communication.getExperiment( phoneProfiler.getPhoneId(), sensorProfiler.getSensorRules() );
				
					Log.i(TAG, jsonExperiment);
				
					if(jsonExperiment.equals("0"))
					{
						Log.i(TAG, "no experiment for us");
					}
					else
					{						
						Gson gson = new Gson();
						Experiment experiment = gson.fromJson(jsonExperiment, Experiment.class);				

						String[] smarDeps = sensorProfiler.getSensorRules().split("|");
						String[] expDeps = experiment.getSensorDependencies().split("|");
		       		
						Set<String> smarSet = new HashSet<String>(Arrays.asList(smarDeps));
						Set<String> expSet = new HashSet<String>(Arrays.asList(expDeps));
						
						if( smarSet.equals(expSet) )
						{							
							String contextType = experiment.getContextType();
							String url = experiment.getUrl();
							
					//		if( lastRunned.equals(contextType) )
					//		{
					//			return "";
					//		}
							
							Downloader downloader = new Downloader();
		       	 			downloader.DownloadFromUrl(url, contextType+"_9.47.1.jar");
		       	 			
		       	 			editor.putString("runningJob", contextType);
		       	 			editor.putString("runningExperimentUrl", experiment.getUrl());
		       	 			editor.commit();
		       	 			
		       	 			sendThreadMessage("job_name:"+experiment.getName());
		       	 			
		       				// tell to dynamix Framework to update its repository
		       	 			updateDynamixRepository();
		       	 			
		       	 			scheduler.commitJob(contextType);	
						}
						else
						{
							Log.i(TAG, "this experiment violates my sensor rules");
						}
					}
				}
				else
				{
					Log.i("WTF", "no ping for us");
				}
			}
			else
			{	
				if(scheduler.currentJob.jobState == null)
				{
					String runningExperimentUrl = pref.getString("runningExperimentUrl", "-1");
					checkExperiment(runningJob, runningExperimentUrl);
				
	   	 			sendThreadMessage("job_name:"+runningJob);
					
					scheduler.commitJob(runningJob);
				}

			}

	    	return "Executed";
	    }      

	    @Override
	    protected void onPostExecute(String result)
	    {
	    	Log.i("WTF", "post execute");
	    }

	    @Override
	    protected void onPreExecute()
	    {
	    	Log.i("WTF", "pre execute");
	    }

	    @Override
	    protected void onProgressUpdate(Void... values)
	    {
	    	Log.i("WTF", "update progress");
	    }
	}  
	
}
