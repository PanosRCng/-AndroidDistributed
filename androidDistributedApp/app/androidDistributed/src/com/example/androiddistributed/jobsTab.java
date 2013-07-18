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
import android.widget.TextView;

public class jobsTab extends Activity {

	private boolean tabActive = false;
	private ImageView jobImgv;
	private ListView dependenciesListView ;
	private ArrayAdapter<String> listAdapter ;
    ArrayList<String> dependencies;
    private String jobState;
    private TextView jobNameTxt;
    private TextView statusTxt;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobs);
        
        jobImgv = (ImageView) findViewById(R.id.imageView1);
		jobImgv.setImageResource(R.drawable.jobs_unselected);
		jobNameTxt = (TextView) findViewById(R.id.textView2);
		statusTxt = (TextView) findViewById(R.id.textView3);
        dependenciesListView = (ListView) findViewById( R.id.dependenciesLV );
        dependencies = new ArrayList<String>();
        
        jobState = "none";
        
        tabActive = true;
    }
    
    public boolean isTabActive()
    {
    	return this.tabActive;
    }
    
    public void loaJobdDependencies(String dependenciesMsg)
    {
    	dependencies.clear();
    	
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
		if( state.equals("ready"))
		{
			jobState = "ready";
			jobImgv.setImageResource(R.drawable.job_ready);
			statusTxt.setText("ready");
		}
		else if(state.equals("pending_initialization"))
		{
			jobState = "pending_initialization";
			jobImgv.setImageResource(R.drawable.job_wait);
			statusTxt.setText("initializing...");
		}
		else if(state.equals("initialized"))
		{
			jobState = "initialized";
			jobImgv.setImageResource(R.drawable.job_initialized);
			statusTxt.setText("initialized");
		}
		else if( state.equals("running") )
		{
			jobState = "running";
			jobImgv.setImageResource(R.drawable.job_running);
			statusTxt.setText("running");
		}
		else if( state.equals("stopped") )
		{
			jobState = "stopped";
			jobImgv.setImageResource(R.drawable.job_stoped);
			statusTxt.setText("stopped");
			dependencies.clear();
			refreshDependenciesListView();
		}
		else if( state.equals("finished") )
		{
			jobState = "none";
			statusTxt.setText("");
			jobImgv.setImageResource(R.drawable.jobs_unselected);
			dependencies.clear();
			refreshDependenciesListView();
		}
	}
	
	public void commitJob(String name)
	{
		jobNameTxt.setText(name);
	}
	
    public void startJob(View view)
    {  
    	if(jobState.equals("none"))
    	{
    		return;
    	}
    	sendStartJobIntent();
    } 
    
    public void stopJob(View view)
    {
    	if(jobState.equals("none"))
    	{
    		return;
    	}
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
