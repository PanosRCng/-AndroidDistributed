package com.example.androiddistributed;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PhoneProfiler extends Thread implements Runnable {

	private Handler handler;
	private Context context;
	private SharedPreferences pref;
	private Editor editor;
	
	private int PHONE_ID;

	// get TAG name for reporting to LogCat
	private final String TAG = this.getClass().getSimpleName();
	
	public PhoneProfiler(Handler handler, Context context)
	{
		this.handler = handler;
		this.context = context;
		
		this.PHONE_ID = 0;
		
        pref = context.getApplicationContext().getSharedPreferences("phoneId", 0);
        editor = pref.edit();
		
        if( (pref.contains("phoneId")) )
        {
        	this.PHONE_ID = pref.getInt("phoneId", 0);
        }        
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
	
	public int getPhoneId()
	{		
		return this.PHONE_ID;
	}
	
	public void setPhoneId(int PHONE_ID)
	{
		this.PHONE_ID = PHONE_ID;
		editor.putInt("phoneId", this.PHONE_ID);
		editor.commit();
		
		sendThreadMessage("phoneId:"+PHONE_ID);
	}
	
	public void sendThreadMessage(String message)
	{
		Message msg = new Message();
		msg.obj = message;
		handler.sendMessage(msg);
	}
		
}
