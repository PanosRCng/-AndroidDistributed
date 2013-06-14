package com.example.androiddistributed;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class securityTab extends Activity{

	private boolean tabActive = false;
	private ImageView batteryImgv;
	private ImageView gpsImgv;
	private ImageView wifiImgv;
	private boolean batteryEnabled;
	private boolean gpsEnabled;
	private boolean wifiEnabled;
	private SharedPreferences pref;
	private Editor editor;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security);
    
        batteryImgv = (ImageView) findViewById(R.id.imageView1);
        gpsImgv = (ImageView) findViewById(R.id.imageView2);
        wifiImgv = (ImageView) findViewById(R.id.imageView3);
        
        pref = getApplicationContext().getSharedPreferences("sensors", 0); // 0 - for private mode
        editor = pref.edit();
                        
        setRules();
                
        tabActive = true;
    }
    
    private void setRules()
    {    	
    	editor.commit();
    	
        // run first time after installation - or data clean
        if( !(pref.contains("firstTime")) )
        {
        	editor.putBoolean("firstTime", false);
        	editor.putBoolean("battery", false);
        	editor.putBoolean("gps", false);
        	editor.putBoolean("wifi", false);
           	
        	editor.commit();
        }

        batteryEnabled = pref.getBoolean("battery", false);
        gpsEnabled = pref.getBoolean("gps", false);
        wifiEnabled = pref.getBoolean("wifi", false);
   	
    	if( batteryEnabled )
    	{
    		batteryImgv.setImageResource(R.drawable.battery_enabled);
    	}
    	else
    	{
            batteryImgv.setImageResource(R.drawable.battery_disabled);
    	}
   	
    	if( gpsEnabled )
    	{
    		gpsImgv.setImageResource(R.drawable.gps_enabled);
    	}
    	else
    	{
            gpsImgv.setImageResource(R.drawable.gps_disabled);
    	}
    	
    	if( wifiEnabled )
    	{
    		wifiImgv.setImageResource(R.drawable.wifi_enabled);
    	}
    	else
    	{
            wifiImgv.setImageResource(R.drawable.wifi_disabled);
    	}
    }
    
    public void batteryClick(View view)
    {
    	if(!batteryEnabled)
    	{
        	editor.putBoolean("battery", true);
    	}
    	else
    	{
        	editor.putBoolean("battery", false);   		
    	}

    	setRules();
    	sendPermissionsChangedIntent();
    }
    
    public void gpsClick(View view)
    {
    	if(!gpsEnabled)
    	{
        	editor.putBoolean("gps", true);
    	}
    	else
    	{
        	editor.putBoolean("gps", false);
    	}
    	
    	setRules();
    	sendPermissionsChangedIntent();
    }
    
    public void wifiClick(View view)
    {
    	if(!wifiEnabled)
    	{
        	editor.putBoolean("wifi", true);
    	}
    	else
    	{
        	editor.putBoolean("wifi", false);
    	}
    	
    	setRules();
    	sendPermissionsChangedIntent();
    }
    
    public boolean isTabActive()
    {
    	return this.tabActive;
    }
    
	// send intent to MainActivity to tell to scheduler to refresh sensor permissions
	private void sendPermissionsChangedIntent()
	{
	    Intent i = new Intent();
	    i.setAction("sensors_permissions_changed");
	    sendBroadcast(i);
	} 
    
}
