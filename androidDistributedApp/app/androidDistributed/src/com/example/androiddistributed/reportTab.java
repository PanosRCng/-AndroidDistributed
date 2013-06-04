package com.example.androiddistributed;

import android.app.Activity;
import android.os.Bundle;


public class reportTab extends Activity
{
	private boolean tabActive = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        tabActive = true;
    }
        
    public boolean isTabActive()
    {
    	return this.tabActive;
    }
	
}
