package com.example.androiddistributed;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;

public class securityTab extends Activity{

	private boolean tabActive = false;
	private ImageView batteryImgv;
	private ImageView gpsImgv;
	private ImageView wifiImgv;
	
	private CheckBox chkBatteryLevel;
	private CheckBox chkBatteryTemperature;
	private CheckBox chkGpsPosition;
	private CheckBox chkWifiBSSID;
	
	private boolean batteryEnabled;
	private boolean batteryLevelEnabled;
	private boolean batteryTemperatureEnabled;
	
	private boolean gpsEnabled;
	private boolean gpsPositionEnabled;
	
	private boolean wifiEnabled;
	private boolean wifiBSSIDEnabled;
	
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
    
    	chkBatteryLevel = (CheckBox) findViewById(R.id.checkBox1);
    	chkBatteryLevel.setOnClickListener(new OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
        		if (((CheckBox) v).isChecked())
        		{
        			editor.putBoolean("batteryLevel", true);
        		}
        		else
        		{
        			editor.putBoolean("batteryLevel", false);	
        		}
        		
        		editor.commit();
        		sendPermissionsChangedIntent();
        	}
    	});
        
    	chkBatteryTemperature = (CheckBox) findViewById(R.id.checkBox2);
    	chkBatteryTemperature.setOnClickListener(new OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
        		if (((CheckBox) v).isChecked())
        		{
        			editor.putBoolean("batteryTemperature", true);
        		}
        		else
        		{
        			editor.putBoolean("batteryTemperature", false);	
        		}
        		
        		editor.commit();
        		sendPermissionsChangedIntent();
        	}
    	});
    	
    	chkGpsPosition = (CheckBox) findViewById(R.id.checkBox3);
    	chkGpsPosition.setOnClickListener(new OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
        		if (((CheckBox) v).isChecked())
        		{
        			editor.putBoolean("gpsPosition", true);
        		}
        		else
        		{
        			editor.putBoolean("gpsPosition", false);	
        		}
        		
        		editor.commit();
        		sendPermissionsChangedIntent();
        	}
    	});
    	
    	chkWifiBSSID = (CheckBox) findViewById(R.id.checkBox5);
    	chkWifiBSSID.setOnClickListener(new OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
        		if (((CheckBox) v).isChecked())
        		{
        			editor.putBoolean("wifiBSSID", true);
        		}
        		else
        		{
        			editor.putBoolean("wifiBSSID", false);	
        		}
        		
        		editor.commit();
        		sendPermissionsChangedIntent();
        	}
    	});
    	
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
        	editor.putBoolean("batteryLevel", false);
        	editor.putBoolean("battertTemperature", false);
        	
        	editor.putBoolean("gps", false);
        	editor.putBoolean("gpsPosition", false);
        	
        	editor.putBoolean("wifi", false);
           	
        	editor.putBoolean("wifiBSSID", false);
        	
        	editor.commit();
        }

        batteryEnabled = pref.getBoolean("battery", false);
        batteryLevelEnabled = pref.getBoolean("batteryLevel", false);
        batteryTemperatureEnabled = pref.getBoolean("batteryTemperature", false);
        
        gpsEnabled = pref.getBoolean("gps", false);
        gpsPositionEnabled = pref.getBoolean("gpsPosition", false);
        
        wifiEnabled = pref.getBoolean("wifi", false);
        wifiBSSIDEnabled = pref.getBoolean("wifiBSSID", false);
        
    	if( batteryEnabled )
    	{
    		batteryImgv.setImageResource(R.drawable.battery_enabled);
    		
        	if( batteryLevelEnabled )
        	{
        		chkBatteryLevel.setChecked(true);
        	}	
        	
        	if( batteryTemperatureEnabled )
        	{
        		chkBatteryTemperature.setChecked(true);
        	}
    	}
    	else
    	{
            batteryImgv.setImageResource(R.drawable.battery_disabled);
    	}
    		
    	if( gpsEnabled )
    	{
    		gpsImgv.setImageResource(R.drawable.gps_enabled);
    		
        	if( gpsPositionEnabled )
        	{
        		chkGpsPosition.setChecked(true);
        	}	
    	}
    	else
    	{
            gpsImgv.setImageResource(R.drawable.gps_disabled);
    	}
    	
    	if( wifiEnabled )
    	{
    		wifiImgv.setImageResource(R.drawable.wifi_enabled);
    		
    		if( wifiBSSIDEnabled )
    		{
    			chkWifiBSSID.setChecked(true);
    		}
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
        	
        	chkBatteryLevel.setEnabled(true);
        	chkBatteryTemperature.setEnabled(true);	
        	
        	editor.putBoolean("batteryLevel", true);
        	editor.putBoolean("batteryTemperature", true);
    	}
    	else
    	{
        	editor.putBoolean("battery", false); 
        	
        	chkBatteryLevel.setEnabled(false);
        	chkBatteryTemperature.setEnabled(false);
        	
        	editor.putBoolean("batteryLevel", false);
        	editor.putBoolean("batteryTemperature", false);
    	}

    	setRules();
    	sendPermissionsChangedIntent();
    }
    
    public void gpsClick(View view)
    {
    	if(!gpsEnabled)
    	{
        	editor.putBoolean("gps", true); 
    		
        	chkGpsPosition.setEnabled(true);
        	
        	editor.putBoolean("gpsPosition", true);	        	
    	}
    	else
    	{
        	editor.putBoolean("gps", false);
        	
        	chkGpsPosition.setEnabled(false);
        	
        	editor.putBoolean("gpsPosition", false);
    	}
    	
    	setRules();
    	sendPermissionsChangedIntent();
    }
    
    public void wifiClick(View view)
    {
    	if(!wifiEnabled)
    	{
        	editor.putBoolean("wifi", true);
        	
        	chkWifiBSSID.setEnabled(true);
        	
        	editor.putBoolean("wifiBSSID", true);
    	}
    	else
    	{
        	editor.putBoolean("wifi", false);
        	
        	chkWifiBSSID.setEnabled(false);
        	
        	editor.putBoolean("wifiBSSID", true);
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
