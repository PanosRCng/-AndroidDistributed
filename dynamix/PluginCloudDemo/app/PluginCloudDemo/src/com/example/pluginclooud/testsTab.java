package com.example.pluginclooud;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class testsTab extends Activity {
	
	EditText et;
	ImageView imgv;
	String contextType = "org.ambientdynamix.contextplugins.plugin1";
	
    private Boolean testsListenerIsRegistered = false;
    private testsListener testslistener = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tests);
        
       et = (EditText) this.findViewById(R.id.editText1);
       imgv = (ImageView) this.findViewById(R.id.imageView2);
              
       Button summonBtn = (Button) this.findViewById(R.id.summonBtn);
       summonBtn.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			sendSummonPluginIntent(contextType);
		}
    	   
       });
       
       // receive intents from MainActivity avout plugin events 
       testslistener = new testsListener();
    }
    
	@Override
	public void onResume() {
	  super.onResume();
	  
	  // register intent listener
      if (!testsListenerIsRegistered) {
          registerReceiver(testslistener, new IntentFilter("event_plugin"));
          registerReceiver(testslistener, new IntentFilter("event_supported_plugin"));
          testsListenerIsRegistered = true;
      } 
	}
	
	@Override
	protected void onPause() {
	  super.onPause();
		
	  // unregister intent listener
      if (testsListenerIsRegistered) {
          unregisterReceiver(testslistener);
          testsListenerIsRegistered = false;
      }
	} 
    
	// listener to receive intents from child tabs
    protected class testsListener extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent)
        {	        	
        	if( intent.getAction().equals("event_plugin") )
            {
        		String data = intent.getStringExtra("messageData");        		
        		et.append(data + "\n");
            }
        	else if( intent.getAction().equals("event_supported_plugin") )
        	{
        		String contextType = intent.getStringExtra("contextType");
        		et.append("plugin context type supported: " + contextType + "\n");	
        		imgv.setImageResource(R.drawable.tests_selected);
        	}
        }
    }    
    
	// send intent to MainActivity to summon the plugin with context type, the contextType
	private void sendSummonPluginIntent(String contextType){
	    Intent i = new Intent();
		i.putExtra("contextType", contextType);
	    i.setAction("summon_plugin");
	    sendBroadcast(i);
	}

}  