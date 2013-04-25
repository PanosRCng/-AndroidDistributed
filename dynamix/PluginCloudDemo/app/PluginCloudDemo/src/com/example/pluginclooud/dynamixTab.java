package com.example.pluginclooud;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.app.Activity;
import android.content.Intent;

public class dynamixTab extends Activity {
	
	ImageView imgv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.dynamix);

	    imgv = (ImageView) this.findViewById(R.id.imageView1);
	    
		// the connect to dynamix button
	    Button connectBtn = (Button) this.findViewById(R.id.connectBtn);
	    connectBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				sendConnectIntent();
				
			    imgv.setImageResource(R.drawable.dynamix_selected);
			}
	    });	    
	    
		// the disconnect from dynamix button
		Button disconnectBtn = (Button) findViewById(R.id.disconnectBtn);
		disconnectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {			
				sendDisconnectIntent();

			    imgv.setImageResource(R.drawable.dynamix_unselected);
			}
		});	

	}
    
	@Override
	public void onResume() {
	  super.onResume();	
	}
	
	@Override
	protected void onPause() {
		super.onPause();		
	}
	
	// send intent to MainActivity to disconnect from dynamix framework
	private void sendDisconnectIntent(){
	    Intent i = new Intent();
	    i.setAction("disconnect_dynamix");
	    sendBroadcast(i);
	}

	// send intent to MainActivity to connect to dynamix framework
	private void sendConnectIntent(){
	    Intent i = new Intent();
	    i.setAction("connect_dynamix");
	    sendBroadcast(i);
	}	
}  