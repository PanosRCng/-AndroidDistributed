package org.ambientdynamix.update.contextplugin;

public class MyPlugInfo
{
	String id;
	String runtimeFactoryClass;
	String name;
	String description;
	String installUrl;
	
	public MyPlugInfo(String id, String runtimeFactoryClass, String name, String description, String installUrl)
	{
		this.id = id;
		this.runtimeFactoryClass = runtimeFactoryClass;
		this.name = name;
		this.description = description;
		this.installUrl = installUrl;
	}
	
}
