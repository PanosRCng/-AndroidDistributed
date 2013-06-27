package helloworld;

/**
 * Created with IntelliJ IDEA.
 * User: panos
 * Date: 6/17/13
 * Time: 6:09 AM
 * To change this template use File | Settings | File Templates.
 */

import java.util.ArrayList;

public class Report
{
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
