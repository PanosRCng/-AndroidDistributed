package org.ambientdynamix.contextplugins.plugin1;

import java.util.UUID;

import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.contextplugin.AutoReactiveContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class Plugin1Runtime extends AutoReactiveContextPluginRuntime {
	// Static logging TAG
	private final String TAG = this.getClass().getSimpleName();
	// Our secure context
	private Context context;

	@Override
	public void init(PowerScheme powerScheme, ContextPluginSettings settings) throws Exception {
		// Set the power scheme
		this.setPowerScheme(powerScheme);
		// Store our secure context
		this.context = this.getSecuredContext();
	}

	// handle incoming context request
	@Override
	public void handleContextRequest(UUID requestId, String contextType) {
		// Check for proper context type
		if (contextType.equalsIgnoreCase(Plugin1Info.CONTEXT_TYPE)) {

				Plugin1Info info = new Plugin1Info(1.0);

				// Send the context event
				sendContextEvent(requestId, new SecuredContextInfo(info, PrivacyRiskLevel.LOW), 60000);

		} else {
			sendContextScanError(requestId, "NO_CONTEXT_SUPPORT for " + contextType, ErrorCodes.NO_CONTEXT_SUPPORT);
		}
	}

	@Override
	public void handleConfiguredContextRequest(UUID requestId, String contextType, Bundle config) {
		Log.w(TAG, "handleConfiguredContextRequest called, but we don't support configuration");
		// Drop the config and default to handleContextRequest
		handleContextRequest(requestId, contextType);
	}	
	
	// handler for received Intents for the "message_intent" event 
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    // Extract data included in the Intent
	    String data = intent.getStringExtra("message_data");
	    Log.i(TAG, "i am happy plugin - i just receive a message: " + data);
	  }
	};
	
	@Override
	public void start() {
		
		LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, new IntentFilter("message_intent"));
		Log.d(TAG, "Started!");
	}

	@Override
	public void stop() {
		/*
		 * At this point, the plug-in should stop scanning for context and/or handling context requests; however, we
		 * should retain resources needed to run again.
		 */
		
		LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
		Log.d(TAG, "Stopped!");
	}

	@Override
	public void destroy() {
		/*
		 * At this point, the plug-in should stop and release any resources. Nothing to do in this case except for stop.
		 */
		this.stop();
		Log.d(TAG, "Destroyed!");
	}

	@Override
	public void updateSettings(ContextPluginSettings settings) {
		// Not supported
	}

	@Override
	public void setPowerScheme(PowerScheme scheme) {
		// Not supported
	}

	@Override
	public void doManualContextScan() {
		// Not supported
	}
}