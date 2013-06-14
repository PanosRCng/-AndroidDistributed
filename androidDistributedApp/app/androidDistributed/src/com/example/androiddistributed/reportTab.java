package com.example.androiddistributed;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class reportTab extends Activity
{
	private boolean tabActive = false;
	private TextView reportsTv;
	private ImageView internetStatusImgv;
	private ImageView reportImgv;
	private TextView reportNameTv;
	
	private ArrayList<Report> reportsList;
	private ListView reportsListView ;
	private ArrayAdapter<Report> listAdapter ;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reporter);
        
        reportsTv = (TextView) this.findViewById(R.id.textView1);
        reportNameTv = (TextView) this.findViewById(R.id.TextView05);
        reportsListView = (ListView) findViewById( R.id.reportsLV );
        internetStatusImgv = (ImageView) findViewById(R.id.imageView2);
        reportImgv = (ImageView) findViewById(R.id.imageView1);
        
        loadReports();
        
        tabActive = true;
    }
        
    public boolean isTabActive()
    {
    	return this.tabActive;
    }
	
    private void loadReports()
    {   
        reportsList = new ArrayList<Report>();
    	
        // get reports filenames from internal space
		File dir = this.getFilesDir();		
		File[] files = dir.listFiles();
		for(File file : files)
		{
		    	Report my_report = new Report(file.getName());
		    	reportsList.add( my_report );
		}
    	
		// Create a customized ArrayAdapter
        listAdapter = new reportArrayAdapter(getApplicationContext(), R.layout.report_row, reportsList);		
		reportsListView.setAdapter(listAdapter);
		
		
		reportsListView.setOnItemClickListener(new OnItemClickListener()
		{	  
			@Override
			public void onItemClick(AdapterView<?> arg0, android.view.View arg1, int arg2, long arg3)
			{
				Report my_report = reportsList.get(arg2);	
				selectReport(my_report.getName());
			}
		});
		
		if(reportsList.size() == 0)
		{
			reportsTv.setText("no reported jobs");
	    	reportImgv.setImageResource(R.drawable.no_job_report);		
		}
    }
    
    private void selectReport(String reportName)
    {
    	reportImgv.setImageResource(R.drawable.job_report);
    	reportNameTv.setText(reportName);
    }
    
    public void jobToReport(String jobName)
    {
    	loadReports();
    }
    
    public void setInternetStatus(String internet_status)
    {    	
    	if( internet_status.equals("internet_ok") )
    	{
    		internetStatusImgv.setImageResource(R.drawable.database_connected);
    	}
    	else if( internet_status.equals("no_internet") )
    	{
    		internetStatusImgv.setImageResource(R.drawable.database_disconnected);
    	}
    }
}
