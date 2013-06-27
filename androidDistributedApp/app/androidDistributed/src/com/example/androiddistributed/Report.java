package com.example.androiddistributed;

import java.util.ArrayList;

public class Report{

	private String jobName;	
	private ArrayList<String> jobResults;
	
	public Report(String jobName)
	{
		this.jobName = jobName;
	}
	
	public void setResults(ArrayList<String> jobResults)
	{
		this.jobResults = jobResults;
	}
	
	public ArrayList<String> getResults()
	{
		return this.jobResults;
	}
	
	public String getName()
	{
		return jobName;
	}
}
