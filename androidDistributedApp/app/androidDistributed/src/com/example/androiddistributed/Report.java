package com.example.androiddistributed;

public class Report{

	private String jobName;
	
	public Report(String jobName)
	{
		this.jobName = jobName;
	}
	
	String getName()
	{
		return jobName;
	}
}
