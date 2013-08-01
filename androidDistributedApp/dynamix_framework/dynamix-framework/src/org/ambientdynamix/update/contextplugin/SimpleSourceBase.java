/*
 * Copyright (C) The Ambient Dynamix Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambientdynamix.update.contextplugin;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.PluginConstants.PLATFORM;
import org.ambientdynamix.util.RepositoryInfo;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.google.gson.Gson;

import android.util.Log;

public class SimpleSourceBase
{
	private final String TAG = this.getClass().getSimpleName();

	protected List<DiscoveredContextPlugin> createDiscoveredPlugins(RepositoryInfo repo, InputStream input,
			PLATFORM platform, VersionInfo platformVersion, VersionInfo frameworkVersion, boolean processSingle)
			throws Exception
	{
	
		Log.i("HACK THE MOTHERFUCKER", "we are Here to hack it");
		
		String jsonPluginList="";
		
		List<DiscoveredContextPlugin> plugs = new ArrayList<DiscoveredContextPlugin>();
				
		if( ping() )
		{
			Log.i("WTF", "ping ok");
			
			jsonPluginList = getPluginList();
			
			Log.i(TAG, jsonPluginList);
			
			if(jsonPluginList.equals("0"))
			{
				Log.i(TAG, "no plugin list for us");
			}
			else
			{
				Gson gson = new Gson();
	        	PluginList pluginList = gson.fromJson(jsonPluginList, PluginList.class);
	        	
	        	Log.i(TAG, "pluginList is ok");
	        	
	        	ArrayList<MyPlugInfo> plugList = pluginList.getPluginList();
	        	
	        	if( plugList.size() > 0 )
	        	{
		        	Log.i(TAG, "plugList is full");
	        		
	        		for(MyPlugInfo plugInfo : plugList)
	        		{	
	        			ContextPluginBinder plugBinder = new ContextPluginBinder();
	        		
	        			try
	        			{
	        				DiscoveredContextPlugin plug = plugBinder.createDiscoveredPlugin(repo, plugInfo);
	        				plugs.add(plug);
	        				
	        			}catch (Exception e)
	        			{
	        				Log.w(TAG, "Exception creating plugin: " + plugBinder.id);
	        			}
	        		}
	        	}
			}
		}
		else
		{
			Log.i(TAG, "no ping for us");
		}
		
		return plugs;
	}

	public boolean ping()
	{
		Ping ping = new Ping();
		
		Gson gson = new Gson();
		String jsonPing = gson.toJson(ping);
		
		int pong = sendPing(jsonPing);
		
		if(pong == 1)
		{
			return true;
		}
		
		return false;
	}
	
	private int sendPing(String jsonPing)
	{
		final String NAMESPACE = "http://helloworld/";
		final String URL = "http://83.212.115.57:8080/ADService/services/HelloWorld?wsdl"; 
		final String METHOD_NAME = "Ping";
		final String SOAP_ACTION = "\""+"http://helloworld/Ping"+"\"";
		
		int pong = 0;
		
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 

		PropertyInfo propInfo=new PropertyInfo();
		propInfo.name="arg0";
		propInfo.type=PropertyInfo.STRING_CLASS;
		propInfo.setValue(jsonPing);
  
		request.addProperty(propInfo);  

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
		envelope.setOutputSoapObject(request);
		
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
			
			pong = Integer.parseInt(resultsRequestSOAP.toString()); 
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue -- connection never timeout or close
		// 
		//	connection close to server - connection stays open to client	
		//  so the first call is ok, but the second call receive from the server a RST
		//
		//  this because the server receives a packet for a closed socket 
		//  and insert RST in an attempt to block traffic
		//
		// connection never timeout or close -- so force it to break
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();					
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue
		
		return pong;
	}	
	
	private String getPluginList()
	{
		return sendGetPluginList();
	}
	
	private String sendGetPluginList()
	{		
		final String NAMESPACE = "http://helloworld/";
		final String URL = "http://83.212.115.57:8080/ADService/services/HelloWorld?wsdl"; 
		final String METHOD_NAME = "getPluginList";
		final String SOAP_ACTION = "\""+"http://helloworld/getPluginList"+"\"";
		
		String test="0";
		
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 
		
		PropertyInfo propInfo=new PropertyInfo();
		propInfo.name="arg0";
		propInfo.type=PropertyInfo.STRING_CLASS;
		propInfo.setValue("");
  
		request.addProperty(propInfo);  
				
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
		envelope.setOutputSoapObject(request);
		
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			
			SoapPrimitive resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
			
			test = resultsRequestSOAP.toString(); 
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue -- connection never timeout or close
		// 
		//	connection close to server - connection stays open to client	
		//  so the first call is ok, but the second call receive from the server a RST
		//
		//  this because the server receives a packet for a closed socket 
		//  and insert RST in an attempt to block traffic
		//
		// connection never timeout or close -- so force it to break
		
		try
		{			
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();					
		}
		catch (Exception e)
		{
			//
		}
		
		// ksoap2 HttpTransportSE issue
		
		return test;
	}
}
