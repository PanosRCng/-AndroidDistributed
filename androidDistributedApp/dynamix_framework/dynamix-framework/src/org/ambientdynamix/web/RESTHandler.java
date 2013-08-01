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
package org.ambientdynamix.web;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;

import org.ambientdynamix.api.application.ContextPluginInformationResult;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.ContextSupportResult;
import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.api.application.IdResult;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.core.WebFacadeBinder;
import org.ambientdynamix.web.NanoHTTPD.Response;

import android.os.Bundle;
import android.util.Log;

/**
 * Provides a REST interface for web clients connecting to Dynamix.
 * 
 * @author Darren Carlson
 * 
 */
public class RESTHandler {
	// Rest endpoints
	public static final String DYNAMIX_BIND = "/dynamixBind";
	public static final String DYNAMIX_UNBIND = "/dynamixUnbind";
	public static final String EVENT_CALLBACK = "/eventcallback";
	public static final String ADD_DYNAMIX_LISTENER = "/addDynamixListener";
	public static final String REMOVE_DYNAMIX_LISTENER = "/removeDynamixListener";
	public static final String OPEN_SESSION = "/openSession";
	public static final String ADD_CONTEXT_SUPPORT = "/addContextSupport";
	public static final String ADD_CONFIGURED_CONTEXT_SUPPORT = "/addConfiguredContextSupport";
	public static final String REMOVE_CONTEXT_SUPPORT_FOR_TYPE = "/removeContextSupportForContextType";
	public static final String REMOVE_CONTEXT_SUPPORT_FOR_ID = "/removeContextSupportForSupportId";
	public static final String REMOVE_ALL_CONTEXT_SUPPORT = "/removeAllContextSupport";
	public static final String CONTEXT_REQUEST = "/contextRequest";
	public static final String CONFIGURED_CONTEXT_REQUEST = "/configuredContextRequest";
	public static final String CHECK_DYNAMIX_ACTIVE = "/isDynamixActive";
	public static final String IS_DYNAMIX_TOKEN_VALID = "/isDynamixTokenValid";
	public static final String IS_DYNAMIX_SESSION_OPEN = "/isDynamixSessionOpen";
	public static final String GET_CONTEXT_SUPPORT = "/getContextSupport";
	public static final String GET_CONTEXT_PLUG_INS = "/getAllContextPluginInformation";
	public static final String GET_CONTEXT_PLUG_IN = "/getContextPluginInformation";
	public static final String RESEND_CACHED_EVENTS = "/resendCachedContextEvents";
	public static final String OPEN_CONTEXT_PLUGIN_CONFIGURATION_VIEW = "/openContextPluginConfigurationView";
	// Private data
	private WebFacadeBinder facade;
	private final String TAG = this.getClass().getSimpleName();

	/**
	 * Creates a RESTHandler.
	 * 
	 * @param facade
	 *            The WebFacadeBinder for this handler to use.
	 */
	public RESTHandler(WebFacadeBinder facade) {
		this.facade = facade;
	}

	/**
	 * Process a web client request using the Dynamix REST interface.
	 */
	public Response processRequest(Response r, WebListenerManager<String> wlMgr, String uri, String method,
			Properties header, Properties parms) throws Exception {
		// Start by setting the response to HTTP_OK with a MIME_HTML type and a '0' for the content (success)
		r.status = NanoHTTPD.HTTP_OK;
		r.mimeType = NanoHTTPD.MIME_HTML;
		r.setText("0");
		// Handle GET-only requests
		if (method.equalsIgnoreCase("GET")) {
			// Handle EVENT_CALLBACK
			if (uri.equalsIgnoreCase(EVENT_CALLBACK)) {
				// Check if we need to wait for an event to send
				if (wlMgr.isEmpty()) {
					/*
					 * Wait for events to arrive; however, after 10 seconds we send HTTP_NOTFOUND so that the XHR
					 * request doesn't timeout. Javascript will then call us back to wait for events.
					 */
					wlMgr.waitForEvent(10000);
				}
				// Check for an event to send
				if (wlMgr.isEmpty()) {
					// No event, so send 404
					r.status = NanoHTTPD.HTTP_NOTFOUND;
				} else {
					// Access and send the event's command
					String command = wlMgr.poll();
					r.setText(command);
				}
			}
			// Handle CHECK_DYNAMIX_ACTIVE
			else if (uri.equalsIgnoreCase(CHECK_DYNAMIX_ACTIVE)) {
				r.setText(Boolean.toString(facade.isDynamixActive()));
			}
			// Handle ADD_DYNAMIX_LISTENER
			else if (uri.equalsIgnoreCase(ADD_DYNAMIX_LISTENER)) {
				facade.addDynamixListener(wlMgr.getListener());
			}
			// Handle REMOVE_DYNAMIX_LISTENER
			else if (uri.equalsIgnoreCase(REMOVE_DYNAMIX_LISTENER)) {
				facade.removeDynamixListener(wlMgr.getListener());
			}
			// Handle RESEND_CACHED_EVENTS
			else if (uri.equalsIgnoreCase(RESEND_CACHED_EVENTS)) {
				String contextType = parms.getProperty("contextType");
				String pastMills = parms.getProperty("pastMills");
				Integer pastMillsInt = null;
				// If we have pastMills, try to convert the string to an Integer
				if (pastMills != null) {
					try {
						pastMillsInt = Integer.parseInt(pastMills);
					} catch (Exception e) {
						Log.w(TAG, "Could not convert pastMills to integer: " + pastMills);
					}
				}
				if (contextType == null && pastMills == null) {
					parameterizeResponse(r, facade.resendAllCachedContextEvents((wlMgr.getListener())));
				} else if (contextType == null && pastMillsInt != null) {
					parameterizeResponse(r, facade.resendCachedContextEvents(wlMgr.getListener(), pastMillsInt));
				} else if (contextType != null && pastMills == null) {
					parameterizeResponse(r, facade.resendAllTypedCachedContextEvents(wlMgr.getListener(), contextType));
				} else if (contextType != null && pastMillsInt != null) {
					parameterizeResponse(r,
							facade.resendTypedCachedContextEvents(wlMgr.getListener(), contextType, pastMillsInt));
				} else {
					failOnMissingParam(r, "contextType");
				}
			}
			// Handle OPEN_CONTEXT_PLUGIN_CONFIGURATION_VIEW
			else if (uri.equalsIgnoreCase(OPEN_CONTEXT_PLUGIN_CONFIGURATION_VIEW)) {
				String pluginId = parms.getProperty("pluginId");
				if (pluginId != null)
					parameterizeResponse(r, facade.openContextPluginConfigurationView(wlMgr.getListener(), pluginId));
				else
					failOnMissingParam(r, "pluginId");
			}
			// Handle GET_CONTEXT_SUPPORT
			else if (uri.equalsIgnoreCase(GET_CONTEXT_SUPPORT)) {
				parameterizeContextSupportResultResponse(r, facade.getContextSupport(wlMgr.getListener()));
			}
			// Handle GET_CONTEXT_PLUG_INS
			else if (uri.equalsIgnoreCase(GET_CONTEXT_PLUG_INS)) {
				parameterizeContextPluginInformationResultResponse(r,
						facade.getAllContextPluginInformation(wlMgr.getListener()));
			}
			// Handle GET_CONTEXT_PLUG_IN
			else if (uri.equalsIgnoreCase(GET_CONTEXT_PLUG_IN)) {
				String pluginId = parms.getProperty("pluginId");
				if (pluginId != null) {
					parameterizeContextPluginInformationResultResponse(r,
							facade.getContextPluginInformation(wlMgr.getListener(), pluginId));
				} else {
					failOnMissingParam(r, "pluginId");
				}
			}
			// Handle ADD_CONTEXT_SUPPORT
			else if (uri.equalsIgnoreCase(ADD_CONTEXT_SUPPORT)) {
				String contextType = parms.getProperty("contextType");
				if (contextType != null) {
					parameterizeResponse(r, facade.addContextSupport(wlMgr.getListener(), contextType));
				} else {
					failOnMissingParam(r, "contextType");
				}
			}
			// Handle ADD_CONFIGURED_CONTEXT_SUPPORT
			else if (uri.equalsIgnoreCase(ADD_CONFIGURED_CONTEXT_SUPPORT)) {
				String config = parms.getProperty("config");
				if (config != null) {
					parameterizeResponse(r,
							facade.addConfiguredContextSupport(wlMgr.getListener(), createConfigBundle(config)));
				} else {
					failOnMissingParam(r, "config");
				}
			}
			// Handle REMOVE_CONTEXT_SUPPORT_FOR_TYPE
			else if (uri.equalsIgnoreCase(REMOVE_CONTEXT_SUPPORT_FOR_TYPE)) {
				String contextType = parms.getProperty("contextType");
				if (contextType != null) {
					parameterizeResponse(r, facade.removeContextSupportForContextType(wlMgr.getListener(), contextType));
				} else {
					failOnMissingParam(r, "contextType");
				}
			}
			// Handle REMOVE_CONTEXT_SUPPORT_FOR_ID
			else if (uri.equalsIgnoreCase(REMOVE_CONTEXT_SUPPORT_FOR_ID)) {
				String supportId = parms.getProperty("supportId");
				/*
				 * Create a ContextSupportInfo based on the supportId. Note that we can use nulls for the plug-in info
				 * and context type, since 'removeContextSupport' searches using the supportId only.
				 */
				ContextSupportInfo supportInfo = new ContextSupportInfo(supportId, null, null);
				if (supportId != null) {
					parameterizeResponse(r, facade.removeContextSupport(wlMgr.getListener(), supportInfo));
				} else {
					failOnMissingParam(r, "supportId");
				}
			}
			// Handle REMOVE_ALL_CONTEXT_SUPPORT
			else if (uri.equalsIgnoreCase(REMOVE_ALL_CONTEXT_SUPPORT)) {
				parameterizeResponse(r, facade.removeAllContextSupportForListener(wlMgr.getListener()));
			}
			// Handle CONTEXT_REQUEST
			else if (uri.equalsIgnoreCase(CONTEXT_REQUEST)) {
				String pluginId = parms.getProperty("pluginId");
				String contextType = parms.getProperty("contextType");
				if (pluginId != null && contextType != null)
					parameterizeResponse(r, facade.contextRequest(wlMgr.getListener(), pluginId, contextType));
				else {
					failOnMissingParam(r, "pluginId", "contextType");
				}
			}
			// Handle CONFIGURED_CONTEXT_REQUEST
			else if (uri.equalsIgnoreCase(CONFIGURED_CONTEXT_REQUEST)) {
				String pluginId = parms.getProperty("pluginId");
				String contextType = parms.getProperty("contextType");
				String config = parms.getProperty("config");
				if (pluginId != null && contextType != null && config != null) {
					parameterizeResponse(r, facade.configuredContextRequest(wlMgr.getListener(), pluginId, contextType,
							createConfigBundle(config)));
				} else {
					failOnMissingParam(r, "pluginId", "contextType", "config");
				}
			}
			// Error: No valid REST endpoint found
			else {
				Log.w(TAG, "REST endpoint invalid: " + uri);
				r.status = NanoHTTPD.HTTP_BADREQUEST;
				r.setText("REST endpoint invalid: " + uri);
			}
		}
		// Error: Request was not GET
		else {
			Log.w(TAG, "Request not GET: " + uri);
			r.status = NanoHTTPD.HTTP_BADREQUEST;
			r.setText("Request not GET: " + uri);
		}
		return r;
	}

	/*
	 * Helper method that creates a Bundle using the name/value pairs within the configString. This config a URL encoded
	 * set of string/value pairs, each delineated by an '&' character
	 * e.g.,"config_value1%3D9%26config_value2%3Dtest%26config_value3%3Dsomething%20else" becomes
	 * "config_value1=9&config_value2=test&config_value3=something else"
	 */
	private Bundle createConfigBundle(String config) throws Exception {
		String decoded = URLDecoder.decode(config);
		// Extract the key/value pairs
		String[] nvPairs = decoded.split("&");
		// Create a Bundle to hold the config
		Bundle scanConfig = new Bundle();
		// Make sure we got some data
		if (nvPairs.length > 0) {
			// Parameterize the Bundle using the key/value pairs
			for (String pair : nvPairs) {
				String[] nvPair = pair.split("=");
				if (nvPair.length == 2) {
					scanConfig.putString(nvPair[0], nvPair[1]);
				} else {
					throw new Exception("Invalid Config String: " + config);
				}
			}
		} else {
			throw new Exception("Invalid Config String: " + config);
		}
		return scanConfig;
	}

	/*
	 * Helper method for parameterizing a Response with a Result.
	 */
	private void parameterizeResponse(Response response, Result result) {
		if (result.wasSuccessful()) {
			response.setText(ErrorCodes.NO_ERROR + ",SUCCESS");
		} else {
			response.setText(result.getErrorCode() + "," + result.getMessage());
		}
	}

	/*
	 * Helper method for parameterizing a Response with an IdResult.
	 */
	private void parameterizeResponse(Response response, IdResult result) {
		if (result.wasSuccessful()) {
			response.setText(ErrorCodes.NO_ERROR + "," + result.getId());
		} else {
			response.setText(result.getErrorCode() + "," + result.getMessage());
		}
	}

	/*
	 * Helper method for parameterizing a Response with a ContextSupportResult.
	 */
	private void parameterizeContextSupportResultResponse(Response response, ContextSupportResult result) {
		if (result.wasSuccessful()) {
			response.mimeType = NanoHTTPD.MIME_HTML;
			try {
				String json = WebUtils.serializeObject(result.getContextSupportInfo());
				response.setText(ErrorCodes.NO_ERROR + "," + URLEncoder.encode(json));
			} catch (Exception e) {
				Log.w(TAG, "Could not encode plug-ins: " + e);
				response.setText(ErrorCodes.DYNAMIX_FRAMEWORK_ERROR + "," + e.toString());
			}
		} else {
			response.setText(result.getErrorCode() + "," + result.getMessage());
		}
	}

	/*
	 * Helper method for parameterizing a Response with a ContextPluginInformationResult.
	 */
	private void parameterizeContextPluginInformationResultResponse(Response response,
			ContextPluginInformationResult result) {
		if (result.wasSuccessful()) {
			response.mimeType = NanoHTTPD.MIME_HTML;
			try {
				String json = WebUtils.serializeObject(result.getContextPluginInformation());
				response.setText(ErrorCodes.NO_ERROR + "," + URLEncoder.encode(json));
			} catch (Exception e) {
				Log.w(TAG, "Could not encode plug-ins: " + e);
				response.setText(ErrorCodes.DYNAMIX_FRAMEWORK_ERROR + "," + e.toString());
			}
		} else {
			response.setText(result.getErrorCode() + "," + result.getMessage());
		}
	}

	/*
	 * Utility that configures a failed response for requests that are missing parameters.
	 */
	private void failOnMissingParam(Response r, String... missingParams) {
		StringBuilder params = new StringBuilder();
		for (String param : missingParams) {
			params.append(param + " ");
		}
		Log.w(TAG, "Request missing parameters. Required Parameters: " + params.toString());
		r.setText(ErrorCodes.MISSING_PARAMETERS + ",Required Parameters: " + params.toString());
	}
}
