package com.example.androiddistributed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class dynamixTab extends Activity {
	
	private final String TAG = this.getClass().getSimpleName();
	private boolean tabActive = false;
	
	private ImageView plugDynamixImgv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dynamix);
        
        tabActive = true;
        
        plugDynamixImgv = (ImageView) findViewById(R.id.imageView1);
		plugDynamixImgv.setImageResource(R.drawable.unplugged_dynamix);
    }

    public boolean isTabActive()
    {
    	return this.tabActive;
    }
    
    public void plugDynamix(View view)
    {  
    	sendConnectToDynamixIntent();
    } 
    
    public void unplugDynamix(View view)
    {
    	sendDisconnectFromDynamixIntent();
    }
        
	// send intent to MainActivity to connect to dynamix framework
	private void sendConnectToDynamixIntent()
	{
	    Intent i = new Intent();
	    i.setAction("connect_dynamix");
	    sendBroadcast(i);
	}    
    
	// send intent to MainActivity to disconnect from dynamix framework
	private void sendDisconnectFromDynamixIntent()
	{
	    Intent i = new Intent();
	    i.setAction("disconnect_dynamix");
	    sendBroadcast(i);
	}
    
	public void setDynamixConnected()
	{
		plugDynamixImgv.setImageResource(R.drawable.plugged_dynamix);
	}
	
	public void setDynamixDisconnected()
	{
		plugDynamixImgv.setImageResource(R.drawable.unplugged_dynamix);
	}
    
}
