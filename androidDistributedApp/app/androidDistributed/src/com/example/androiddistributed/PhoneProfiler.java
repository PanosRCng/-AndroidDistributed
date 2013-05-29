package com.example.androiddistributed;

import java.security.MessageDigest;

import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneProfiler extends Thread implements Runnable {

	private Handler handler;
	private Context context;
	
	private final String PHONE_ID;

	// get TAG name for reporting to LogCat
	private final String TAG = this.getClass().getSimpleName();
	
	public PhoneProfiler(Handler handler, Context context)
	{
		this.handler = handler;
		this.context = context;
		
		this.PHONE_ID = createPhoneId();
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
	
	public String getPhoneId()
	{		
		return this.PHONE_ID;
	}
	
	private String createPhoneId()
	{
		String phoneId = "";
			
		String imei = getIMEI();
			
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
				
			md.update(imei.getBytes("UTF-8")); // Change this to "UTF-16" if needed
			byte[] digest = md.digest();
			phoneId = new String(digest, "UTF8");
		}
		catch(Exception e)
		{
			Log.e("TAG", e.toString());
		}
					
		return phoneId;
	}
	
	private String getIMEI()
	{
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();

		return imei;
	}
}
