package com.example.androiddistributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientdynamix.api.application.IContextInfo;
import org.ambientdynamix.contextplugins.GpsPlugin.IGpsPluginInfo;
import org.ambientdynamix.contextplugins.WifiPlugin.IWifiPluginInfo;
import org.ambientdynamix.contextplugins.WifiScanPlugin.IWifiScanPluginInfo;
import org.ambientdynamix.contextplugins.batteryLevelPlugin.IBatteryLevelPluginInfo;
import org.ambientdynamix.contextplugins.batteryTemperaturePlugin.IBatteryTemperaturePluginInfo;
import org.ambientdynamix.contextplugins.myExperimentPlugin.IExperimentPluginInfo;
import org.ambientdynamix.contextplugins.oneplugin.IOnePluginInfo;

import android.os.Bundle;
import android.util.Log;

public class Job {
	
	private String contextType;
	private Scheduler scheduler;
	public String jobState;
	
	private List<String> dependencies;
	private Map<String, Boolean> allowedDependencies;
	private Map<String, Boolean> wakedDependencies;
	
	// null constructor
	public Job()
	{
		this.jobState = null;
	}
	
	// constructor
	public Job(String contextType, Scheduler scheduler)
	{
		this.contextType = contextType;
		this.scheduler = scheduler;
		setState("not_ready");				
		dependencies = new ArrayList<String>();
		allowedDependencies = new HashMap<String, Boolean>();
		wakedDependencies = new HashMap<String, Boolean>();
	}
	
	public void setState(String state)
	{
		this.jobState = state;
		scheduler.sendThreadMessage("job_state_changed:"+state);
	}
	

	public void getMsg(IContextInfo nativeInfo)
	{					
		if(nativeInfo instanceof IExperimentPluginInfo)
		{			
			IExperimentPluginInfo info = (IExperimentPluginInfo) nativeInfo;
			String pluginState = info.getState();
			
			if( jobState.equals("not_ready") )
			{	
				if(pluginState.equals("ready"))
				{
					// get job dependencies
					if( setDependencies(info.getDependencies()) )
					{
						for(String dependency : dependencies)
						{	
							if( scheduler.sensorsPermissions.get(dependency) )
							{										
								setAllowedDependency(dependency, true);
							}
							else
							{								
								setAllowedDependency(dependency, false);
							}
						}
					}
					
					if( isDependenciesAllowed() )
					{
						// call dependencies plugins
						for( String dependency : dependencies )
						{						
							scheduler.commitDependency(dependency);	
						}

						setState("pending_initialization");
					}
					else
					{
						scheduler.calcelCurrentJob();
					}
					
				}
				else if( pluginState.equals("stopped") )
				{	
					scheduler.startPlugin(info.getContextType());
				}
			}
			else if( jobState.equals("running") )
			{	
				if( pluginState.equals("finished") )
				{
					setState("finished");

					Bundle results = info.getData();	
					scheduler.storeJobResults(results);					
					scheduler.reportJob(this.getContextType());
				}
			}
			else if( jobState.equals("stopped") )
			{
				if( pluginState.equals("stopped") )
				{
					Bundle results = info.getData();	
					scheduler.storeJobResults(results);
				}
			}
		}
		else if( (nativeInfo instanceof IOnePluginInfo) )
		{	
			IOnePluginInfo info = (IOnePluginInfo) nativeInfo;

			if( jobState.equals("pending_initialization") )
			{	
				setWakedDependency(nativeInfo.getContextType(), true);

				if( isDependenciesWaked() )
				{
					setState("initialized");

					scheduler.doJobPlugin(this.getContextType());
					for(String dependency : dependencies)
					{
						scheduler.doJobPlugin(dependency);
					}

					setState("running");
				}
			}
			else if( jobState.equals("running") )
			{
				double batteryLevel = info.getBatteryLevel();	
				Bundle data = new Bundle();
				data.putString("command", info.getContextType());
				data.putDouble("data", batteryLevel);
				scheduler.sendData(this.getContextType(), data);
			}
		}
		else if(nativeInfo instanceof IBatteryLevelPluginInfo)
		{			
			IBatteryLevelPluginInfo info = (IBatteryLevelPluginInfo) nativeInfo;

			if( jobState.equals("pending_initialization") )
			{	
				setWakedDependency(nativeInfo.getContextType(), true);

				if( isDependenciesWaked() )
				{
					setState("initialized");

					scheduler.doJobPlugin(this.getContextType());
					for(String dependency : dependencies)
					{
						scheduler.doJobPlugin(dependency);
					}

					setState("running");
				}
			}
			else if( jobState.equals("running") )
			{				
				int batteryLevel = info.getBatteryLevel();	
				
				Bundle data = new Bundle();
				data.putString("command", info.getContextType());
				data.putString("data", Integer.toString(batteryLevel));
				
				scheduler.sendData(this.getContextType(), data);
			}	
		}
		else if(nativeInfo instanceof IBatteryTemperaturePluginInfo)
		{
			IBatteryTemperaturePluginInfo info = (IBatteryTemperaturePluginInfo) nativeInfo;

			if( jobState.equals("pending_initialization") )
			{	
				setWakedDependency(nativeInfo.getContextType(), true);

				if( isDependenciesWaked() )
				{
					setState("initialized");

					scheduler.doJobPlugin(this.getContextType());
					for(String dependency : dependencies)
					{
						scheduler.doJobPlugin(dependency);
					}

					setState("running");
				}
			}
			else if( jobState.equals("running") )
			{				
				int batteryTemperature = info.getTemperature();	
				
				Bundle data = new Bundle();
				data.putString("command", info.getContextType());
				data.putString("data", Integer.toString(batteryTemperature));
				
				scheduler.sendData(this.getContextType(), data);
			}	
		}
		else if(nativeInfo instanceof IGpsPluginInfo)
		{
			IGpsPluginInfo info = (IGpsPluginInfo) nativeInfo;

			if( jobState.equals("pending_initialization") )
			{	
				setWakedDependency(nativeInfo.getContextType(), true);

				if( isDependenciesWaked() )
				{
					setState("initialized");

					scheduler.doJobPlugin(this.getContextType());
					for(String dependency : dependencies)
					{
						scheduler.doJobPlugin(dependency);
					}

					setState("running");
				}
			}
			else if( jobState.equals("running") )
			{				
				String position = info.getPosition();	
				
				Bundle data = new Bundle();
				data.putString("command", info.getContextType());
				data.putString("data", position);
				
				scheduler.sendData(this.getContextType(), data);
			}	
		}
		else if(nativeInfo instanceof IWifiPluginInfo)
		{			
			IWifiPluginInfo info = (IWifiPluginInfo) nativeInfo;
			
			if( jobState.equals("pending_initialization") )
			{	
				setWakedDependency(nativeInfo.getContextType(), true);
				
				if( isDependenciesWaked() )
				{					
					setState("initialized");

					scheduler.doJobPlugin(this.getContextType());
					for(String dependency : dependencies)
					{
						scheduler.doJobPlugin(dependency);
					}

					setState("running");
				}
			}
			else if( jobState.equals("running") )
			{				
				String bssid = info.getBssid();	
				
				Bundle data = new Bundle();
				data.putString("command", info.getContextType());
				data.putString("data", bssid);
				
				scheduler.sendData(this.getContextType(), data);
			}	
		}
		else if(nativeInfo instanceof IWifiScanPluginInfo)
		{			
			IWifiScanPluginInfo info = (IWifiScanPluginInfo) nativeInfo;
			
			if( jobState.equals("pending_initialization") )
			{	
				setWakedDependency(nativeInfo.getContextType(), true);
				
				if( isDependenciesWaked() )
				{					
					setState("initialized");

					scheduler.doJobPlugin(this.getContextType());
					for(String dependency : dependencies)
					{
						scheduler.doJobPlugin(dependency);
					}

					setState("running");
				}
			}
			else if( jobState.equals("running") )
			{				
				String scanJson = info.getScan();
				
				Bundle data = new Bundle();
				data.putString("command", info.getContextType());
				data.putString("data", scanJson);
				
				scheduler.sendData(this.getContextType(), data);
			}	
		}
	}

	
	public String getState()
	{
		return this.jobState;
	}
		
	public String getContextType()
	{
		return contextType;
	}
		
	private boolean setDependencies(List<String> contextTypes)
	{		
		if(dependencies.size() == 0)
		{
			String message = "!";
		
			for(String dependency : contextTypes)
			{				
				dependencies.add(dependency);
				message = message + dependency + "!";
			}
		
			scheduler.sendThreadMessage("jobDependencies:"+message);
		}
		
		return true;
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
		boolean allowed = true;
		
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
	
	public void stopJob()
	{
		try
		{
			for(String dependency : this.dependencies)
			{
				scheduler.stopPlugin(dependency);
			}
			
			scheduler.stopPlugin(this.getContextType());
						
			setState("stopped");
		}
		catch(Exception e)
		{
			Log.e("WTF", e.toString());
		}
	}
	
	public void startJob()
	{
		for(String dependency : dependencies)
		{
			scheduler.pingPlugin(dependency);
		}
	}
	
}
