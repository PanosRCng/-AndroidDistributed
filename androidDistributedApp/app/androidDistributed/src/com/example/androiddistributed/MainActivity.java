package com.example.androiddistributed;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

	private final String TAG = this.getClass().getSimpleName();
	public Context context;
    private Boolean tabIntentListenerIsRegistered = false;
    private tabIntentListener tabIntentlistener = null;
    
    private Boolean serviceIntentListenerIsRegistered = false;
    private ServiceIntentListener serviceIntentlistener = null;
    
    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;
        
    private dynamixTab dTab;
    private profileTab pTab;
    private jobsTab jTab;
    private reportTab rTab;
    private securityTab sTab;
    
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
            
            Log.i(TAG, "main service connected ok");
        }

        public void onServiceDisconnected(ComponentName className)
        {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };
    
    // send message to main service
    public void sendMessage(String message)
    {
        if (!mBound) return;
        
        Message msg = null;
        
        // Create and send a message to the service, using a supported 'what' value
        if( message.equals("connect_to_dynamix") )
        {
        	 msg = Message.obtain(null, MainService.MSG_CONNECT_TO_DYNAMIX, 0, 0);
        }
        else if( message.equals("disconnect_dynamix") )
        {
        	msg = Message.obtain(null, MainService.MSG_DISCONNECT_DYNAMIX, 0, 0);
        }
        else if( message.equals("stop_job") )
        {
        	msg = Message.obtain(null, MainService.MSG_STOP_JOB, 0, 0);
        }
        else if( message.equals("start_job") )
        {
        	msg = Message.obtain(null, MainService.MSG_START_JOB, 0, 0);
        }
        else if( message.equals("sensorsPermissionsChanged") )
        {
        	msg = Message.obtain(null, MainService.MSG_SENSORS_PERMISSIONS_CHANGED, 0, 0);
        }
      
        try
        {
            mService.send(msg);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
        // get application's context
        context = this.getApplicationContext(); 
		
		setup_tabs();
       
	    pTab = (profileTab) this.getLocalActivityManager().getActivity("profile");
	    dTab = (dynamixTab) this.getLocalActivityManager().getActivity("dynamix");
	    jTab = (jobsTab) this.getLocalActivityManager().getActivity("jobs");
	    rTab = (reportTab) this.getLocalActivityManager().getActivity("reports");
	    sTab = (securityTab) this.getLocalActivityManager().getActivity("security");
	    
        // receive intents from child tabs
        tabIntentlistener = new tabIntentListener();
	    
        // receive intents from service
        serviceIntentlistener = new ServiceIntentListener();	
	}

    @Override
    protected void onStart()
    {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this.context, MainService.class), mConnection, Context.BIND_AUTO_CREATE);
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
	  
		// register intent listener for tabs
		if (!tabIntentListenerIsRegistered)
		{
			registerReceiver(tabIntentlistener, new IntentFilter("disconnect_dynamix"));
			registerReceiver(tabIntentlistener, new IntentFilter("connect_dynamix"));
			registerReceiver(tabIntentlistener, new IntentFilter("stop_job"));
			registerReceiver(tabIntentlistener, new IntentFilter("start_job"));
			registerReceiver(tabIntentlistener, new IntentFilter("WTF"));
			registerReceiver(tabIntentlistener, new IntentFilter("sensors_permissions_changed"));
			
			tabIntentListenerIsRegistered = true;
		} 
		
		// register intent listener for MainService
		if (!serviceIntentListenerIsRegistered)
		{
			registerReceiver(serviceIntentlistener, new IntentFilter("hi"));
			registerReceiver(serviceIntentlistener, new IntentFilter("dynamix_state"));
			registerReceiver(serviceIntentlistener, new IntentFilter("job_state"));
			registerReceiver(serviceIntentlistener, new IntentFilter("phone_id"));
			registerReceiver(serviceIntentlistener, new IntentFilter("jobDependencies"));
			registerReceiver(serviceIntentlistener, new IntentFilter("internet_status"));
			registerReceiver(serviceIntentlistener, new IntentFilter("job_report"));
			registerReceiver(serviceIntentlistener, new IntentFilter("job_name"));			
			
			serviceIntentListenerIsRegistered = true;
		} 
	}	
	
    @Override
    protected void onStop()
    {
        super.onStop();
        // Unbind from the service
        if (mBound)
        {
            unbindService(mConnection);
            mBound = false;
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
		
		// unregister intent listener
		if (serviceIntentListenerIsRegistered)
		{
			unregisterReceiver(serviceIntentlistener);
			serviceIntentListenerIsRegistered = false;
		}
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
        
        // security tab
        Intent intentSecurity = new Intent().setClass(this, securityTab.class);
        TabSpec tabSpecSecurity = tabHost.newTabSpec("security")
                .setIndicator("", ressources.getDrawable(R.drawable.ic_tab_security))
                .setContent(intentSecurity);

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
        
        //report tab
        Intent intentReports = new Intent().setClass(this, reportTab.class);
        TabSpec tabSpecReports = tabHost.newTabSpec("reports")
                .setIndicator("", ressources.getDrawable(R.drawable.ic_tab_reports))
                .setContent(intentReports);    

        // add all tabs 
        tabHost.addTab(tabSpecProfile);
        tabHost.addTab(tabSpecSecurity);
        tabHost.addTab(tabSpecDynamix);
        tabHost.addTab(tabSpecJobs);
        tabHost.addTab(tabSpecReports);

        //set Windows tab as default (zero based) -- first wake up all tab activities
        tabHost.setCurrentTab(1);
        tabHost.setCurrentTab(2);
        tabHost.setCurrentTab(3);
        tabHost.setCurrentTab(4);
        tabHost.setCurrentTab(0); 
    }
    
	// listener to receive intents from service
    protected class ServiceIntentListener extends BroadcastReceiver
    {    	
        @Override
        public void onReceive(Context context, Intent intent)
        {            	
            if( intent.getAction().equals("dynamix_state") )
            {     
            	String state = intent.getExtras().getString("value");
            	
            	if(state.equals("connected"))
            	{
            		dTab.setDynamixConnected();
            	}
            	else if( state.equals("disconnected") )
            	{
    				dTab.setDynamixDisconnected();
            	}
            }
            else if( intent.getAction().equals("job_state") )
            {
            	String state = intent.getExtras().getString("value");
				jTab.setJobState(state);	
            }
            else if( intent.getAction().equals("phone_id") )
            {
            	String phoneId = intent.getExtras().getString("value");
            	pTab.setPhoneId(phoneId);	
            }
            else if( intent.getAction().equals("jobDependencies") )
            {
            	String dependencies = intent.getExtras().getString("value");
    			jTab.loaJobdDependencies(dependencies);	
            }
            else if( intent.getAction().equals("internet_status") )
            {
            	String internet_status = intent.getExtras().getString("value");
    			rTab.setInternetStatus(internet_status);	
    			pTab.setInternetStatus(internet_status); 	
            }
            else if( intent.getAction().equals("job_report") )
            {
            	String jobName = intent.getExtras().getString("value");
    			rTab.jobToReport(jobName);
            }
            else if( intent.getAction().equals("job_name") )
            {
            	String jobName = intent.getExtras().getString("value");
    			jTab.commitJob(jobName);
            }  
        }
    }  
    
	// listener to receive intents from child tabs
    protected class tabIntentListener extends BroadcastReceiver
    {    	
        @Override
        public void onReceive(Context context, Intent intent)
        {        	
            if( intent.getAction().equals("connect_dynamix") )
            {
            	sendMessage("connect_to_dynamix");
            }
            else if( intent.getAction().equals("disconnect_dynamix") )
            {
            	sendMessage("disconnect_dynamix");
            }
            else if( intent.getAction().equals("stop_job") )
            {
            	sendMessage("stop_job");
            }
            else if( intent.getAction().equals("start_job") )
            {
            	sendMessage("start_job");
            }
            else if( intent.getAction().equals("sensors_permissions_changed") )
            {
            	sendMessage("sensorsPermissionsChanged");
            }
        }
    }    
    
}
