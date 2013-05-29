package com.example.androiddistributed;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class profileTab extends Activity{
	
	private boolean tabActive = false;
	private TextView phoneIdTv;
	
	private ListView sensorListView ;
	private ArrayAdapter<String> listAdapter ;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        phoneIdTv = (TextView) this.findViewById(R.id.textView1);
        sensorListView = (ListView) findViewById( R.id.sensorsLV );
        
        loadAvailableSensors();
        
        tabActive = true;
    }
    
    private void loadAvailableSensors()
    {
        String[] sensors = new String[] { "TYPE_MAGNETIC_FIELD", "TYPE_ACCELEROMETER", "TYPE_MAGNETIC_FIELD", "TYPE_TEMPERATURE"};  
        ArrayList<String> sensorsList = new ArrayList<String>();
        sensorsList.addAll( Arrays.asList(sensors) );
        
        listAdapter = new ArrayAdapter<String>(this, R.layout.sensor_row, sensorsList);
        sensorListView.setAdapter( listAdapter );  
    }
    
    public boolean isTabActive()
    {
    	return this.tabActive;
    }
    
    public void setPhoneId(String phoneId)
    {
    	phoneIdTv.setText(phoneId);
    }
}
