package com.example.androiddistributed;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class profileTab extends Activity{
	
	private boolean tabActive = false;
	private TextView phoneIdTv;
	private ImageView internetStatusImgv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        phoneIdTv = (TextView) this.findViewById(R.id.textView1);
        internetStatusImgv = (ImageView) findViewById(R.id.imageView2);
        
        tabActive = true;
    }
    
    public boolean isTabActive()
    {
    	return this.tabActive;
    }
    
    public void setPhoneId(String phoneId)
    {
    	phoneIdTv.setText(phoneId);
    }
    
    public void setInternetStatus(String internet_status)
    {    	
    	if( internet_status.equals("internet_ok") )
    	{
    		internetStatusImgv.setImageResource(R.drawable.database_connected);
    	}
    	else if( internet_status.equals("no_internet") )
    	{
    		internetStatusImgv.setImageResource(R.drawable.database_disconnected);
    	}
    }
}
