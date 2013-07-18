package org.ambientdynamix.contextplugins.WifiScanPlugin;

import java.util.ArrayList;
import java.util.List;
import android.net.wifi.ScanResult;

public class Scan
{
	List<ScanResult> wifiList;
	
	public Scan()
	{
		wifiList = new ArrayList<ScanResult>();
	}
	
	public void addWifiList(List<ScanResult> wifiList)
	{
		this.wifiList = wifiList;
	}
	
	public List<ScanResult> getWifiList()
	{
		return this.wifiList;
	}
	
	public void clearScanList()
	{
		wifiList.clear();
	}
}