package com.example.androiddistributed;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

	private final String TAG = this.getClass().getSimpleName();
	public Context context;
    private Boolean tabIntentListenerIsRegistered = false;
    private tabIntentListener tabIntentlistener = null;
    
    private Scheduler scheduler;
    private Profiler profiler;
    private SensorProfiler sensorProfiler;
    private Reporter reporter;
    private PhoneProfiler phoneProfiler;
    private Registration registration;
    
    private dynamixTab dTab;
    private profileTab pTab;
    private jobsTab jTab;
    
	// thread handler
	protected Handler handler = new Handler()
	{		
		// handle messages from threads modules
		@Override
		public void handleMessage(Message msg)
		{
			try
			{
				String message = (String) msg.obj;
		      
				if( message == "dynamix_connected" )
				{
					dTab.setDynamixConnected();
				}
				else if( message == "dynamix_disconnected" )
				{
					dTab.setDynamixDisconnected();
				}
				else if( message.contains("job_state_changed:") )
				{
					String[] parts = message.split(":");
					String state = parts[1];
					jTab.setJobState(state);
				}
				else if( message.contains("phoneId:") )
				{
					String parts[] = message.split(":");
					String phoneId = parts[1];
					pTab.setPhoneId(phoneId);
				}
				else if( message.contains("jobDependencies:") )
				{
					String[] parts = message.split(":");
					String dependencies = parts[1];
					jTab.loaJobdDependencies(dependencies);
				}
				else
				{
					Log.i(TAG, "thread message uknonwn (!)" + message);
				}
			}
			catch(Exception e)
			{
				Log.e(TAG, e.toString());
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
        // get application's context to bind dynamix service
        context = this.getApplicationContext(); 
		
		setup_tabs();
       
	    pTab = (profileTab) this.getLocalActivityManager().getActivity("profile");
	    dTab = (dynamixTab) this.getLocalActivityManager().getActivity("dynamix");
	    jTab = (jobsTab) this.getLocalActivityManager().getActivity("jobs");
	    
        // receive intents from child tabs
        tabIntentlistener = new tabIntentListener();
	    
        // create threads
        phoneProfiler = new PhoneProfiler(handler, context);
	    registration = new Registration(handler, phoneProfiler);
	    sensorProfiler = new SensorProfiler(handler, context);
		profiler = new Profiler(handler, phoneProfiler);
	    reporter = new Reporter(handler, context);
	    scheduler = new Scheduler(handler, context, sensorProfiler, reporter);
	    
	    // give some time to threads to start
	    try
	    {
	    	Thread.sleep(1000);
	    }
	    catch(Exception e)
	    {
	    	//
	    }
	    
	    // start threads
		phoneProfiler.start();
	    sensorProfiler.start();
		registration.start();
	    profiler.start();
	    reporter.start();
		scheduler.start();
				
		// connect to dynamix framework
		scheduler.connect_to_dynamix();
	
		// commit job/plugin test to dynamix framework
		scheduler.commitJob("org.ambientdynamix.contextplugins.addplugin");
		
			
	}

	@Override
	public void onResume()
	{
		super.onResume();
	  
		// register intent listener
		if (!tabIntentListenerIsRegistered)
		{
			registerReceiver(tabIntentlistener, new IntentFilter("disconnect_dynamix"));
			registerReceiver(tabIntentlistener, new IntentFilter("connect_dynamix"));
			registerReceiver(tabIntentlistener, new IntentFilter("stop_job"));
			registerReceiver(tabIntentlistener, new IntentFilter("start_job"));
			registerReceiver(tabIntentlistener, new IntentFilter("WTF"));
			tabIntentListenerIsRegistered = true;
		} 
	}	
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		// unregister intent listener
		if (tabIntentListenerIsRegistered)
		{
			unregisterReceiver(tabIntentlistener);
			tabIntentListenerIsRegistered = false;
		}
	} 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    // setup applications tabs
    private void setup_tabs()
    {
        Resources ressources = getResources(); 
        TabHost tabHost = getTabHost();
       
        // profile tab
        Intent intentProfile = new Intent().setClass(this, profileTab.class);
        TabSpec tabSpecProfile = tabHost.newTabSpec("profile")
                .setIndicator("", ressources.getDrawable(R.drawable.ic_tab_profile))
                .setContent(intentProfile);

        // dynamix tab
        Intent intentDynamix = new Intent().setClass(this, dynamixTab.class);
        TabSpec tabSpecDynamix = tabHost.newTabSpec("dynamix")
                .setIndicator("", ressources.getDrawable(R.drawable.ic_tab_dynamix))
                .setContent(intentDynamix);         
       
        //jobs tab
        Intent intentJobs = new Intent().setClass(this, jobsTab.class);
        TabSpec tabSpecJobs = tabHost.newTabSpec("jobs")
                .setIndicator("", ressources.getDrawable(R.drawable.ic_tab_jobs))
                .setContent(intentJobs);    

        // add all tabs 
        tabHost.addTab(tabSpecProfile);
        tabHost.addTab(tabSpecDynamix);
        tabHost.addTab(tabSpecJobs);

        //set Windows tab as default (zero based) -- first wake up all tab activities
        tabHost.setCurrentTab(1);
        tabHost.setCurrentTab(2);
        tabHost.setCurrentTab(0); 
    }
    
	// listener to receive intents from child tabs
    protected class tabIntentListener extends BroadcastReceiver
    {    	
        @Override
        public void onReceive(Context context, Intent intent)
        {        	
            if( intent.getAction().equals("connect_dynamix") )
            {
            	Log.i(TAG, "receiving intent to connect to dynamix");
            	scheduler.connect_to_dynamix();
            }
            else if( intent.getAction().equals("disconnect_dynamix") )
            {
            	Log.i(TAG, "receiving intent to disconnect from dynamix");
            	scheduler.disconnect_from_dynamix();
            }
            else if( intent.getAction().equals("stop_job") )
            {
            	Log.i(TAG, "receiving intent to stop job");
            	scheduler.stopCurrentPlugin();
            }
            else if( intent.getAction().equals("start_job") )
            {
            	Log.i(TAG, "receiving intent to commit job");
            	
        		scheduler.startCurrentJob();
            }
            else if( intent.getAction().equals("WTF") )
            {
            	Log.i(TAG, "get The WTF intent");
            }
        }
    }    
    
}
