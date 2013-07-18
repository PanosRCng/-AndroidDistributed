package com.example.androiddistributed;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncCommunication extends AsyncTask<String, Void, String>
{
    @Override
    protected String doInBackground(String... params)
    {
          for(int i=0;i<5;i++) {
              try {
                  Thread.sleep(1000);
              } catch (InterruptedException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              }
          }

          return "Executed";
    }      

    @Override
    protected void onPostExecute(String result)
    {
    	Log.i("WTF", "post execute");
    }

    @Override
    protected void onPreExecute()
    {
    	Log.i("WTF", "pre execute");
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
    	Log.i("WTF", "update progress");
    }
}   