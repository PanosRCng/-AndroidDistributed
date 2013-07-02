package com.example.androiddistributed;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class MainService extends Service
{
	private final String TAG = this.getClass().getSimpleName();
		
	private Context context;
	
//	private DynamixReceiver dynamixReceiver;
	
	private Demon demon;
    private Scheduler scheduler;
    private Profiler profiler;
    private SensorProfiler sensorProfiler;
    private Reporter reporter;
    private PhoneProfiler phoneProfiler;
    private Registration registration;
    private Communication communication;
	
    static final int MSG_CONNECT_TO_DYNAMIX = 1;
    static final int MSG_DISCONNECT_DYNAMIX = 2;
    static final int MSG_STOP_JOB = 3;
    static final int MSG_START_JOB = 4;
    static final int MSG_SENSORS_PERMISSIONS_CHANGED = 5;

    // Handler of incoming messages from MainActivity
    class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_CONNECT_TO_DYNAMIX:
                    scheduler.connect_to_dynamix();
                    break;
                case MSG_DISCONNECT_DYNAMIX:
                	scheduler.disconnect_from_dynamix();
                	break;
                case MSG_STOP_JOB:
                    scheduler.stopCurrentPlugin();
                	break;
                case MSG_START_JOB:
                	scheduler.startCurrentJob();
                	break;
                case MSG_SENSORS_PERMISSIONS_CHANGED:
                	sensorProfiler.sensorsPermissionsChanged();
                	scheduler.sensorsPermissionsChanged();
                default:
                    super.handleMessage(msg);
            }
        }
    }

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
					sendMessageIntent("dynamix_state", "connected");
				}
				else if( message == "dynamix_disconnected" )
				{
					sendMessageIntent("dynamix_state", "disconnected");
				}
				else if( message.contains("job_state_changed:") )
				{
					String[] parts = message.split(":");
					String state = parts[1];					
					sendMessageIntent("job_state", state);
				}
				else if( message.contains("phoneId:") )
				{
					String parts[] = message.split(":");
					String phoneId = parts[1];
					
					sendMessageIntent("phone_id", phoneId);
				}
				else if( message.contains("jobDependencies:") )
				{
					String[] parts = message.split(":");
					String dependencies = parts[1];
					
					sendMessageIntent("jobDependencies", dependencies);
				}
				else if( message.contains("internet_status:") )
				{
					String[] parts = message.split(":");
					String internet_status = parts[1];
					
					sendMessageIntent("internet_status", internet_status);
				}
				else if( message.contains("report_job:") )
				{
					String[] parts = message.split(":");
					String jobName = parts[1];
					
					sendMessageIntent("job_report", jobName);
				}
				else if( message.contains("job_name:") )
				{
					String[] parts = message.split(":");
					String jobName = parts[1];

					sendMessageIntent("job_name", jobName);
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
    
    // Target we publish for clients to send messages to IncomingHandler
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent)
    {
    	// get application's context and work with it
        context = this.getApplicationContext();
        
        // create threads
        communication = new Communication(handler);
        phoneProfiler = new PhoneProfiler(handler, context);
	    sensorProfiler = new SensorProfiler(handler, context, communication, phoneProfiler);
	    registration = new Registration(handler, communication, phoneProfiler, sensorProfiler);
		profiler = new Profiler(handler, phoneProfiler);
	    reporter = new Reporter(handler, context, communication);
	    scheduler = new Scheduler(handler, context, sensorProfiler, reporter, phoneProfiler);
        demon = new Demon(handler, context, communication, scheduler, phoneProfiler, sensorProfiler);
	    
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
	    communication.start();
		phoneProfiler.start();
	    sensorProfiler.start();
		registration.start();
	    profiler.start();
	    reporter.start();
		scheduler.start();
		demon.start();
	
//		dynamixReceiver = new DynamixReceiver();
		
	    IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.androiddistributed.MainService");
		
//		registerReceiver(dynamixReceiver, filter);
		
		// connect to dynamix framework
		scheduler.connect_to_dynamix();
		
        return mMessenger.getBinder();
    }
       
    public class DynamixReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {      
            Log.i("WTF", " ok dynamix, you start, i fucking get it");
        }
        
    }
    
	// send intent to MainActivity
	private void sendMessageIntent(String message, String value)
	{		
	    Intent i = new Intent();
	    i.setAction(message);
	    i.putExtra("value", value);
	    sendBroadcast(i);
	}  
}