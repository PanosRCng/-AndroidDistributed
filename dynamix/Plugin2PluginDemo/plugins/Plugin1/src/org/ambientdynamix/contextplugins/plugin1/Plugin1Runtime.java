package org.ambientdynamix.contextplugins.plugin1;

import java.util.UUID;

import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.contextplugin.AutoReactiveContextPluginRuntime;
import org.ambientdynamix.api.contextplugin.ContextPluginSettings;
import org.ambientdynamix.api.contextplugin.PowerScheme;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.content.Context;
import android.os.Bundle;
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
		
		String data = (String) config.get("data");
		
		Log.i(TAG, "ok i fucking get the fucking data: " + data);
		
	//	handleContextRequest(requestId, contextType);
	}	
	
	@Override
	public void start() {
		Log.d(TAG, "Started!");
	}

	@Override
	public void stop() {
		/*
		 * At this point, the plug-in should stop scanning for context and/or handling context requests; however, we
		 * should retain resources needed to run again.
		 */
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