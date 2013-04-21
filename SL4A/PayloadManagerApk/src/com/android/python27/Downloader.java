package com.android.python27;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;

public class Downloader {

	Downloader()
	{
		//
	}
	
	public boolean downloadPayload(String DownloadUrl, String fileName, File dir)
	{
		boolean result = true;
		
		try
		{
		    if(dir.exists()==false)
		    {
		    	dir.mkdirs();
		    }

		    URL url = new URL(DownloadUrl); //you can write here any link
		    File file = new File(dir, fileName);

		    long startTime = System.currentTimeMillis();
		    Log.i("downloader","download url: " + url + "\n");

		    /* Open a connection to that URL. */
		    URLConnection ucon = url.openConnection();

		    /*
		    * Define InputStreams to read from the URLConnection.
		    */
		    InputStream is = ucon.getInputStream();
		    BufferedInputStream bis = new BufferedInputStream(is);

		    /*
		    * Read bytes to the Buffer until there is nothing more to read(-1).
		    */
		    ByteArrayBuffer baf = new ByteArrayBuffer(5000);
		    int current = 0;
		    while ((current = bis.read()) != -1)
		    {
		    	baf.append((byte) current);
		    }


		    /* Convert the Bytes read to a String. */
		    FileOutputStream fos = new FileOutputStream(file);
		    fos.write(baf.toByteArray());
		    fos.flush();
		    fos.close();
		    Log.i("downloader","downloading " + Float.toString(file.length()) + " bytes completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " sec to destination: " + dir + "\n");
		}
		catch (IOException e)
		{
			Log.i("downloader","Error downloading: " + e + "\n");
			
			result = false;
		}
		
		return result;
	}	
	
}
