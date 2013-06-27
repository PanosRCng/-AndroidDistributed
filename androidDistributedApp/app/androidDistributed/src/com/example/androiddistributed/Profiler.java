package com.example.androiddistributed;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Profiler extends Thread implements Runnable {

	private Handler handler;
	private PhoneProfiler phoneProfiler;
	
	private int PHONE_ID=0;
	
	// get TAG name for reporting to LogCat
	private final String TAG = this.getClass().getSimpleName();
	
	public Profiler(Handler handler, PhoneProfiler phoneProfiler)
	{
		this.handler = handler;
		this.phoneProfiler = phoneProfiler;
	}
	
	public void run()
	{	
		try
		{
			Log.d(TAG, "running");
			Thread.sleep(1000); //This could be something computationally intensive.
			
			this.PHONE_ID = phoneProfiler.getPhoneId();
			sendThreadMessage("phoneId:"+PHONE_ID);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendThreadMessage(String message)
	{
		Message msg = new Message();
		msg.obj = message;
		handler.sendMessage(msg);
	}
}
