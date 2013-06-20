package com.example.androiddistributed;

public class Smartphone
{
	String phoneId;
	String sensors_rules;
	String time_rules;
	
	public Smartphone(String phoneId)
	{
		this.phoneId = phoneId;
	}
	
	public String getPhoneId()
	{
		return this.phoneId;
	}
	
	public void setSensorsRules(String sensors_rules)
	{
		this.sensors_rules = sensors_rules;
	}
	
	public String getSensorsRules()
	{
		return this.sensors_rules;
	}
	
	public void setTimeRules(String time_rules)
	{
		this.time_rules = time_rules;
	}
	
	public String getTimeRules()
	{
		return this.time_rules;
	}
}
