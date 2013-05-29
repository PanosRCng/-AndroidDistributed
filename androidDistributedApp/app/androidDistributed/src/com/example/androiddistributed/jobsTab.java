package com.example.androiddistributed;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class jobsTab extends Activity {

	private boolean tabActive = false;
	private ImageView jobImgv;
	private ListView dependenciesListView ;
	private ArrayAdapter<String> listAdapter ;
    ArrayList<String> dependencies;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobs);
        
        jobImgv = (ImageView) findViewById(R.id.imageView1);
		jobImgv.setImageResource(R.drawable.job_none);
        dependenciesListView = (ListView) findViewById( R.id.dependenciesLV );
        dependencies = new ArrayList<String>();
        
        tabActive = true;
    }
    
    public boolean isTabActive()
    {
    	return this.tabActive;
    }
    
    public void loaJobdDependencies(String dependenciesMsg)
    {
    	String[] parts = dependenciesMsg.split("!");
    	    	
    	for(String part : parts)
    	{
    		if(part.length()!=0)
    		{
    			dependencies.add(part);
    		}
    	}
        
    	refreshDependenciesListView();
    }
    
    private void refreshDependenciesListView()
    {
        listAdapter = new ArrayAdapter<String>(this, R.layout.dependency_row, dependencies);
        dependenciesListView.setAdapter( listAdapter );  
    }
    
	public void setJobState(String state)
	{
		if( (state.equals("started")) || (state.equals("pending_initialization")) )
		{
			jobImgv.setImageResource(R.drawable.job_wait);
		}
		else if(state.equals("initialized"))
		{
			jobImgv.setImageResource(R.drawable.job_initialized);
		}
		else if( state.equals("running") )
		{
			jobImgv.setImageResource(R.drawable.job_running);
		}
		else if( state.equals("stopped") )
		{
			jobImgv.setImageResource(R.drawable.job_stoped);
			
			dependencies.clear();
			refreshDependenciesListView();
		}
	}
	
    public void startJob(View view)
    {  
    	sendStartJobIntent();
    } 
    
    public void stopJob(View view)
    {
    	sendStopJobIntent();
    }
	
	// send intent to MainActivity to tell to dynamix framework to start the plugin
	private void sendStartJobIntent()
	{
	    Intent i = new Intent();
	    i.setAction("start_job");
	    sendBroadcast(i);
	}   
	
	// send intent to MainActivity to tell to dynamix framework to stop the plugin
	private void sendStopJobIntent()
	{
	    Intent i = new Intent();
	    i.setAction("stop_job");
	    sendBroadcast(i);
	}   
}
