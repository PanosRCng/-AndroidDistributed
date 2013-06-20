package com.example.androiddistributed;

public class Report{

	private String jobName;
	private String resultsUrl;
	
	public Report(String jobName)
	{
		this.jobName = jobName;
	}
	
	public void setResultsUrl(String resultsUrl)
	{
		this.resultsUrl = resultsUrl;
	}
	
	public String getResultsUrl()
	{
		return this.resultsUrl;
	}
	
	public String getName()
	{
		return jobName;
	}
}
