package org.ambientdynamix.update.contextplugin;

import java.util.ArrayList;

public class PluginList
{
	private ArrayList<MyPlugInfo> plugList; 
	
	public PluginList()
	{
		plugList = new ArrayList<MyPlugInfo>();
	}
	
	public void setPluginList(ArrayList<MyPlugInfo> plugList)
	{
		this.plugList = plugList;
	}
	
	public ArrayList<MyPlugInfo> getPluginList()
	{
		return this.plugList;
	}
}
