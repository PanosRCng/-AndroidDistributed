package com.example.androiddistributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Message;
import android.util.Log;

public class Job {
	
	private String contextType;
	private Scheduler scheduler;
	private String state;
	
	private List<String> dependencies;
	private Map<String, Boolean> allowedDependencies;
	private Map<String, Boolean> wakedDependencies;
	
	// null constructor
	public Job()
	{
		//
	}
	
	// constructor
	public Job(String contextType, Scheduler scheduler)
	{
		this.contextType = contextType;
		this.scheduler = scheduler;
		setState("none");		
		dependencies = new ArrayList<String>();
		allowedDependencies = new HashMap<String, Boolean>();
		wakedDependencies = new HashMap<String, Boolean>();
	}
	
	public void setState(String state)
	{
		this.state = state;
		scheduler.sendThreadMessage("job_state_changed:"+state);
		
		if(state.equals("pending_stopping"))
		{
			for(String dependency : dependencies )
			{
				try
				{
					scheduler.deletePlugin(dependency);
				}
				catch(Exception e)
				{
					Log.e("WTF", e.toString());
				}
			}
		}
	}
	
	public void getMsg(String msg)
	{
		if(state == "none")
		{
			scheduler.initPlugin(contextType);
			setState("started");
		}
		else if( state == "started" )
		{	
			String[] parts = msg.split("=");

			if(parts[1].contains("started"))
			{
				return;
			}
			
			// get dependencies plugins
			List<String> dependencies = new ArrayList<String>();
			String[] depend = parts[1].split("@");
			for(String dependency : depend)
			{
				if( dependency.length() > 0 )
				{
					dependencies.add(dependency);
				}
			}
			
			setDependencies(dependencies);
			
			// TODO - check if job's dependencies is Ok with sensor permissions 
			for(String dependency : dependencies)
			{
				setAllowedDependency(dependency, true);
			}
							
			// call dependencies plugins
			for( String dependency : dependencies )
			{
				Log.i("WTF", "commiting: " + dependency);
				scheduler.commitDependency(dependency);	
			}

			setState("pending_initialization");
		}
		else if( state == "running" )
		{
			String[] splits = msg.split("=");	  
			String number = splits[1];	
			Log.i("WTF", "result: " + number);
			
			scheduler.storeResult(number);
		}

	}
	
	public void getMsg(String srcPluginId, String msg)
	{
		if( state == "pending_initialization" )
		{
			setWakedDependency(srcPluginId, true);
				
			if( isDependenciesWaked() )
			{
				setState("initialized");
					
				scheduler.doJobPlugin(this.getContextType());
			
				setState("running");
			}
		}
		else if( state == "running" )
		{
			if( msg.contains("counter=") )
			{
					String[] splits = msg.split("=");	  
					String number = splits[1];			

					Log.i("WTF", "Sending" + number);
					
					scheduler.sendData(srcPluginId, this.getContextType(), number);
			}
		}
	}
	
	public String getState()
	{
		return this.state;
	}
		
	public String getContextType()
	{
		return contextType;
	}
		
	private void setDependencies(List<String> contextTypes)
	{
		String message = "!";
		
		for(String dependency : contextTypes)
		{
			dependencies.add(dependency);
			message = message + dependency + "!";
		}
		
		scheduler.sendThreadMessage("jobDependencies:"+message);
	}
	
	public List<String> getDependencies()
	{
		return this.dependencies;
	}
	
	public void setWakedDependency(String contextType, boolean waked)
	{
		wakedDependencies.put(contextType, waked);
	}
	
	private void setAllowedDependency(String contextType, boolean allowed)
	{
		allowedDependencies.put(contextType, allowed);
	}
	
	private boolean isDependenciesAllowed()
	{
		boolean allowed = false;
		
		for(String dependency : dependencies)
		{
			if( !allowedDependencies.get(dependency) )
			{
				allowed = false;
			}
		}
		
		return allowed;
	}
	
	public boolean isDependenciesWaked()
	{
		boolean waked = true;
		
		for(String dependency : dependencies)
		{
			if( !wakedDependencies.get(dependency) )
			{
				waked = false;
			}
		}
		
		return waked;
	}
	
}
