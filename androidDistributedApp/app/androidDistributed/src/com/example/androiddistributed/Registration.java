package com.example.androiddistributed;

import android.os.Handler;
import android.util.Log;

public class Registration extends Thread implements Runnable {

	private final String TAG = this.getClass().getSimpleName();
	
	private Handler handler;
	private PhoneProfiler phoneProfiler;
	
	public Registration(Handler handler, PhoneProfiler phoneProfiler)
	{
		this.handler = handler;
		this.phoneProfiler = phoneProfiler;
	}
	
	public void run()
	{
		try
		{
			Log.d(TAG, "running");
			register();
			
			Thread.sleep(1000); //This could be something computationally intensive.
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private void register()
	{
		String phoneId = phoneProfiler.getPhoneId();
		
		Log.i(TAG, "register with PHONE_ID: " + phoneId);
	}
}
