package com.example.androiddistributed;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class reportArrayAdapter extends ArrayAdapter<Report> {

	private Context context;
	
	private TextView reportName;

	private List<Report> reports = new ArrayList<Report>();

	public reportArrayAdapter(Context context, int textViewResourceId, List<Report> objects)
	{
		super(context, textViewResourceId, objects);
		this.context = context;
		this.reports = objects;
	}

	public int getCount()
	{
		return this.reports.size();
	}

	public Report getItem(int index)
	{
		return this.reports.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		if (row == null) {
			// ROW INFLATION
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.report_row, parent, false);
		}

		// Get item
		Report my_report = getItem(position);
		
		reportName = (TextView) row.findViewById(R.id.textView1);
				
		reportName.setText(my_report.getName());	
		
		return row;
	}
}
