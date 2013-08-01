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
if (typeof Dynamix === "undefined") {

	/**
	 * The Dynamix object allows web applications to control a local Dynamix
	 * Framework instance that is running on the device. The data types and
	 * methods available to web applications are contained in the"Dynamix Data
	 * Types", "Dynamix Connection Methods" and "Dynamix REST API Methods"
	 * sections below. Many Dynamix methods synchronously return a Result object
	 * indicating if a requests was accepted; however, results from most methods
	 * are returned asynchronously using DynamixListener events, which are
	 * contained in the 'dynamix_listener.js' file.
	 */
	// ===============================================================
	// Supported Browsers
	// ===============================================================
	// - Standard Android Browser
	// - Chrome for Android
	// - Firefox for Android
	// - Dolphin Browser HD for Android
	// - Dolphin Browser Mini for Android
	// - Boat Browser
	// - Boat Browser Mini
	// - Maxthon Android Web Browser
	// - SkyFire Browser
	// ===============================================================
	var Dynamix = {};

	// ===============================================================
	// Dynamix Configuration Data (!!USED INTERNALLY - DO NOT MODIFY!!)
	// ===============================================================
	/*
	 * Base URL for the Dynamix Web Connector. Note that we need to use
	 * '127.0.0.1' and not 'localhost', since on some devices, 'localhost' is
	 * problematic.
	 */
	Dynamix.base_url = "http://127.0.0.1";
	// List of possible Dynamix ports
	Dynamix.port_list = [ 18087, 5633, 5634, 5635, 5636, 5637, 6130, 6131,
			6132, 6133, 6134, 8223, 8224, 8225, 8226, 8227, 10026, 10027,
			10028, 10029, 10030, 12224, 12225, 12226, 12227, 12228, 16001,
			16002, 16003, 16004, 16005, 19316, 19317, 19318, 19319 ];

	// ===============================================================
	// Dynamix Private Data (!!USED INTERNALLY - DO NOT MODIFY!!)
	// ===============================================================

	Dynamix.port = 0;
	Dynamix.binding = false;
	Dynamix.bound = false;
	Dynamix.token = null;
	Dynamix.token_cookie = "DynamixTokenCookie";
	Dynamix.port_cookie = "DynamixPortCookie";
	Dynamix.call_timeout = 1000;
	Dynamix.bind_call_timeout = 100;
	Dynamix.SUCCESS = 0;
	Dynamix.JAVASCRIPT_ERROR = 100;
	Dynamix.BIND_ERROR = 101;
	Dynamix.HTTP_ERROR = 102;
	Dynamix.JSON_ERROR = 103;
	// For Dynamix-specific error codes, see the Dynamix documentation.

	// ===============================================================
	// Dynamix Data Types (used internally and by web listeners)
	// ===============================================================

	/**
	 * Indicates if a Dynamix request call was accepted for processing. Note
	 * that results are sent via DynamixListener events.
	 */
	Dynamix.Result = function(success, resultCode, resultMessage) {
		// True if successful; false otherwise.
		this.success = success;
		// The result code
		this.resultCode = resultCode;
		// The result message
		this.resultMessage = decodeURIComponent(resultMessage);
		// We return 'this' so that eval-based object creation works
		return this;
	};

	/**
	 * Indicates if a Dynamix request was accepted for processing (includes a
	 * requestId). Note that results are sent via DynamixListener events.
	 * Resulting events will include a responseId that matches the requestId
	 * provided by this object. For example, a 'Dynamix.contextRequest' request
	 * will return a 'Dynamix.IdResult', which will be later included in the a
	 * context event with an associated responseId.
	 */
	Dynamix.IdResult = function(success, resultCode, resultMessage, requestId) {
		// True if successful; false otherwise.
		this.success = success;
		// The result code
		this.resultCode = resultCode;
		// The result message
		this.resultMessage = decodeURIComponent(resultMessage);
		// The request id
		this.requestId = requestId;
		// We return 'this' so that eval-based object creation works
		return this;
	};

	/**
	 * The response object for calls to 'Dynamix.getContextSupport'.
	 */
	Dynamix.ContextSupportInfoResult = function(success, resultCode,
			resultMessage, contextSupportArray) {
		// True if successful; false otherwise.
		this.success = success;
		// The result code
		this.resultCode = resultCode;
		// The result message
		this.resultMessage = decodeURIComponent(resultMessage);
		// An array of Dynamix.ContextSupportInfo
		this.contextSupportArray = contextSupportArray;
		// We return 'this' so that eval-based object creation works
		return this;
	};

	/**
	 * The response object for calls to 'Dynamix.getContextPluginInformation' of
	 * 'Dynamix.getAllContextPluginInformation')
	 */
	Dynamix.ContextPluginInfoResult = function(success, resultCode,
			resultMessage, pluginArray) {
		// True if successful; false otherwise.
		this.success = success;
		// The result code
		this.resultCode = resultCode;
		// The result message
		this.resultMessage = decodeURIComponent(resultMessage);
		// An array of Dynamix.ContextPluginInfo
		this.pluginArray = pluginArray;
		// We return 'this' so that eval-based object creation works
		return this;
	};

	// ===============================================================
	// Dynamix Connection Methods (used internally and by web listeners)
	// ===============================================================

	/**
	 * Used to test browser security by setting illegal request header values.
	 */
	Dynamix.testSecurity = function() {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		try {
			xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
					+ "/isDynamixActive?token=" + Dynamix.token, false);
			xmlhttp.setRequestHeader('Referer', 'http://www.fakereferer.com/');
			xmlhttp.setRequestHeader('Origin', 'http://www.fakeorigin.com/');
			xmlhttp.send();
			return Dynamix.getBooleanFromResponse(xmlhttp);
		} catch (e) {
			console.log("Error connecting to Dynamix: " + e);
			return false;
		}
	};

	/**
	 * Binds to the Dynamix Framework. On success,
	 * 'DynamixListener.onDynamixFrameworkBound' is raised. On failure
	 * 'DynamixListener.onDynamixFrameworkBindError' is raised.
	 */
	Dynamix.bind = function() {
		console.log("Dynamix.bind called");
		/*
		 * Check if we're already binding.
		 */
		if (!Dynamix.binding) {
			// Set binding
			Dynamix.binding = true;

			/*
			 * Check if we're already bound.
			 */
			if (!Dynamix.bound) {
				// Check for cookie values
				var cookieToken = Dynamix.getCookie(Dynamix.token_cookie);
				var cookiePort = Dynamix.getCookie(Dynamix.port_cookie);
				if (cookieToken != null && cookieToken != ""
						&& cookiePort != null && cookiePort != "") {

					console.log("Found Dynamix cookie token: " + cookieToken);
					console.log("Found Dynamix cookie port: " + cookiePort);
					// Use the cookie to set the port value
					Dynamix.port = cookiePort;
					// We have a cookie token, check if it's valid
					if (Dynamix.isTokenValid(cookieToken)) {
						// The cookie token is still valid, so use it
						Dynamix.token = cookieToken;
						// Set bound
						Dynamix.bound = true;
						Dynamix.binding = false;
						// Start the event loop
						setTimeout(Dynamix.eventLoop, 10);
						console.log("Dynamix cookie token was valid");

						// Notify bound
						DynamixListener.onDynamixFrameworkBound();

						/*
						 * Notify Dynamix listener, since this page has not
						 * received state events yet because it didn't setup the
						 * session.
						 */
						// Notify session state
						if (Dynamix.isSessionOpen())
							DynamixListener.onSessionOpened();
						else
							DynamixListener.onSessionClosed();
						// Notify active state
						if (Dynamix.isDynamixActive())
							DynamixListener.onDynamixFrameworkActive();
						else
							DynamixListener.onDynamixFrameworkInactive();

						return;

					} else {
						console.log("Dynamix cookie has expired");
					}
				} else {
					console.log("No Dynamix cookie found");
				}
				/*
				 * No valid cookie, so try connecting on each specified Dynamix
				 * port
				 */
				Dynamix.bindHelper(0);

			} else {
				console.log("Dynamix Already Bound!");
				// Notify
				DynamixListener.onDynamixFrameworkBound();
			}
		} else {
			console.log("Dynamix Already Binding!");
		}
	};

	/**
	 * Helper method that is used by Dynamix.bind to attempt to bind on a
	 * specific port. This is recursively called with an index into the
	 * Dynamix.port_list.
	 */
	Dynamix.bindHelper = function(index) {
		// Make sure we're binding
		if (Dynamix.binding) {
			console.log("Trying to bind to Dynamix on port: "
					+ Dynamix.port_list[index]);
			Dynamix.port = Dynamix.port_list[index];
			var xmlhttp = Dynamix.getXmlHttpRequest();
			// Set a short timeout
			xmlhttp.timeout = Dynamix.bind_call_timeout;
			console.log("Making bind request to: " + Dynamix.base_url + ":"
					+ Dynamix.port + "/dynamixBind");
			xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
					+ "/dynamixBind", true);

			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4) {
					if (xmlhttp.status === 200) {
						// Store the token
						Dynamix.token = xmlhttp.responseText;
						// Set bound
						Dynamix.bound = true;
						// Set not binding
						Dynamix.binding = false;
						// Set cookies
						Dynamix.setCookie(Dynamix.token_cookie, Dynamix.token,
								1);
						Dynamix.setCookie(Dynamix.port_cookie, Dynamix.port, 1);
						// Start the event loop
						setTimeout(Dynamix.eventLoop, 10);
						console.log("Dynamix newly bound on port: "
								+ Dynamix.port_list[index]);
						// Notify
						DynamixListener.onDynamixFrameworkBound();
						return;
					} else if (xmlhttp.status === 400) {
						// We found Dynamix, but there was an error with the
						// request
						var r = Dynamix.parameterizeResult(xmlhttp);
						console.log("Dynamix error during bind on port: "
								+ Dynamix.port_list[index] + " "
								+ r.resultMessage);
						DynamixListener.onDynamixFrameworkBindError(result);

						return;
					} else if (xmlhttp.status === 403) {
						// We found Dynamix, but we are not authorized
						var r = Dynamix.parameterizeResult(xmlhttp);
						console.log("Authorization error during bind on port: "
								+ Dynamix.port_list[index] + " "
								+ r.resultMessage);
						DynamixListener.onDynamixFrameworkBindError(result);
						return;
					} else {
						// Failed to bind on port
						console.log("Failed to bind to Dynamix on port: "
								+ Dynamix.port_list[index]);
						console.log("Total ports are "
								+ Dynamix.port_list.length);
						if (index++ < Dynamix.port_list.length - 1) {
							Dynamix.bindHelper(index);
						} else {
							// Stop binding
							Dynamix.binding = false;
							// Notify that we failed to bind to Dynamix
							console
									.log("Failed to bind Dynamix on all specified ports");
							DynamixListener
									.onDynamixFrameworkBindError(new Dynamix.Result(
											false, Dynamix.BIND_ERROR,
											"Could not bind to Dynamix"));
						}
					}
				}
			};

			xmlhttp.send();
		} else {
			console.log("Can only be called when binding!");
		}

	};

	/**
	 * Unbinds from the Dynamix Framework. Once unbound,
	 * 'DynamixListener.onDynamixFrameworkUnbound' is raised.
	 */
	Dynamix.unbind = function() {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		try {
			xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
					+ "/dynamixUnbind?token=" + Dynamix.token, false);
			xmlhttp.send();
			if (xmlhttp.status !== 200) {
				// Notify locally
				console.log("Unbind call failed in Dynamix");
				Dynamix.onDynamixUnbind();
			} else {
				console.log("Unbind call succeeded in Dynamix");
				// NOTE: Dynamix will raise Dynamix.onDynamixUnbind()
			}
		} catch (e) {
			console.log("Error unbinding Dynamix: " + e);
			// Notify locally
			Dynamix.onDynamixUnbind();
		}
	};

	// ===============================================================
	// Dynamix REST API Methods (used internally and by web listeners)
	// ===============================================================

	/**
	 * Returns true if Dynamix is active; false otherwise.
	 */
	Dynamix.isDynamixActive = function() {

		var xmlhttp = Dynamix.getXmlHttpRequest();
		try {
			xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
					+ "/isDynamixActive?token=" + Dynamix.token, false);
			xmlhttp.send();
			return Dynamix.getBooleanFromResponse(xmlhttp);
		} catch (e) {
			console.log("Error connecting to Dynamix: " + e);
			return false;
		}
	};

	/**
	 * Returns true if the specified token is valid (i.e. registered by
	 * Dynamix); false otherwise.
	 */
	Dynamix.isTokenValid = function(token) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		try {
			console.log("Making bind request to: " + Dynamix.base_url + ":"
					+ Dynamix.port + "/isDynamixTokenValid");
			xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
					+ "/isDynamixTokenValid?token=" + token, false);
			xmlhttp.send();
			return Dynamix.getBooleanFromResponse(xmlhttp);
		} catch (e) {
			console.log("Error connecting to Dynamix: " + e);
			return false;
		}
	};

	/**
	 * Returns true if the web client's session is open; false otherwise.
	 */
	Dynamix.isSessionOpen = function() {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		try {
			xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
					+ "/isDynamixSessionOpen?token=" + Dynamix.token, false);
			xmlhttp.send();
			return Dynamix.getBooleanFromResponse(xmlhttp);
		} catch (e) {
			console.log("Error connecting to Dynamix: " + e);
			return false;
		}
	};

	/**
	 * Registers the web application as a Dynamix listener. After registration,
	 * 'DynamixListener.onDynamixListenerAdded' is raised.
	 */
	Dynamix.addDynamixListener = function() {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.timeout = Dynamix.call_timeout;
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/addDynamixListener?token=" + Dynamix.token, true);
		xmlhttp.send();
	};

	/**
	 * Removes the web application as a Dynamix listener. After removal,
	 * 'DynamixListener.onDynamixListenerRemoved' is raised.
	 */
	Dynamix.removeDynamixListener = function() {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.timeout = Dynamix.call_timeout;
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/removeDynamixListener?token=" + Dynamix.token, true);
		xmlhttp.send();
	};

	/**
	 * Adds Dynamix context support for the specified contextType. This method
	 * may raise 'DynamixListener.onContextSupportAdded' or
	 * 'DynamixListener.onContextTypeNotSupported' (see dynamix_listener.js for
	 * details).
	 */
	Dynamix.addContextSupport = function(contextType) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/addContextSupport?token=" + Dynamix.token + "&contextType="
				+ contextType, false);

		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Adds Dynamix context support for the specified config. The config is a
	 * string of key/value pairs separated by an '&', which include the
	 * 'pluginId', the 'contextType' and any plug-in specific configuration
	 * arguments (see plug-in documentation for details). This method may raise
	 * 'DynamixListener.addConfiguredContextSupport' or
	 * 'DynamixListener.onContextTypeNotSupported' (see dynamix_listener.js for
	 * details).
	 */
	Dynamix.addConfiguredContextSupport = function(config) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/addConfiguredContextSupport?token=" + Dynamix.token
				+ "&config=" + encodeURIComponent(config), false);
		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Removes all context support for the specified contextType string. This
	 * method will raise 'DynamixListener.onContextSupportRemoved'.
	 */
	Dynamix.removeContextSupportForContextType = function(contextType) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/removeContextSupportForContextType?token=" + Dynamix.token
				+ "&contextType=" + contextType, false);
		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Removes all context support for the specified supportId string. This
	 * method will raise 'DynamixListener.onContextSupportRemoved'.
	 */
	Dynamix.removeContextSupportForSupportId = function(supportId) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/removeContextSupportForSupportId?token=" + Dynamix.token
				+ "&supportId=" + supportId, false);

		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Removes all context support. This method will raise
	 * 'DynamixListener.onContextSupportRemoved' for all context support types
	 * removed.
	 */
	Dynamix.removeAllContextSupport = function() {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/removeAllContextSupport?token=" + Dynamix.token, false);
		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Requests a context scan for pull-based plug-ins using the pluginId string
	 * and contextType string. When context information becomes available for
	 * this request, 'DynamixListener.onContextEvent' is raised.
	 */
	Dynamix.contextRequest = function(pluginId, contextType) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/contextRequest?token=" + Dynamix.token + "&pluginId="
				+ pluginId + "&contextType=" + contextType, false);
		xmlhttp.send();
		return Dynamix.parameterizeIdResult(xmlhttp);
	};

	/**
	 * Requests a context interaction for pull-based plug-ins using the pluginId
	 * string, contextType string, and a plug-in specific scanConfig. The
	 * scanConfig is a string of key/value pairs separated by an '&', which
	 * refer to specific context scan arguments (see plug-in documentation for
	 * details). When context information becomes available for this request,
	 * 'DynamixListener.onContextEvent' is raised.
	 */
	Dynamix.configuredContextRequest = function(pluginId, contextType,
			interactionConfig) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/configuredContextRequest?token=" + Dynamix.token
				+ "&pluginId=" + pluginId + "&contextType=" + contextType
				+ "&config=" + encodeURIComponent(interactionConfig), false);
		xmlhttp.send();
		return Dynamix.parameterizeIdResult(xmlhttp);
	};

	/**
	 * Resends all context events cached by Dynamix.
	 */
	Dynamix.resendAllCachedContextEvents = function() {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/resendCachedContextEvents?token=" + Dynamix.token, false);
		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Resents all context events cached by Dynamix for the previous number of
	 * milliseconds specified by pastMills. Past context events are resent using
	 * 'DynamixListener.onContextEvent'.
	 */
	Dynamix.resendAllCachedContextEvents = function(pastMills) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/resendCachedContextEvents?token=" + Dynamix.token
				+ "&pastMills=" + pastMills, false);
		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Resends all context events cached by Dynamix for the specified
	 * contextType. Past context events are resent using
	 * 'DynamixListener.onContextEvent'.
	 */
	Dynamix.resendCachedContextEventsOfType = function(contextType) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/resendCachedContextEvents?token=" + Dynamix.token
				+ "&contextType=" + contextType, false);
		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Resends all context events cached by Dynamix for the specified
	 * contextType and previous number of milliseconds specified by pastMills.
	 * Past context events are resent using 'DynamixListener.onContextEvent'.
	 */
	Dynamix.resendCachedContextEventsOfType = function(contextType, pastMills) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/resendCachedContextEvents?token=" + Dynamix.token
				+ "&contextType=" + contextType + "&pastMills=" + pastMills,
				false);
		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Requests the Dynamix open the configuration view of the plug-in specified
	 * by pluginId. Note that the plug-in must support configuration for this to
	 * work.
	 */
	Dynamix.openContextPluginConfigurationView = function(pluginId) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/openContextPluginConfigurationView?token=" + Dynamix.token
				+ "&pluginId=" + pluginId, false);
		xmlhttp.send();
		return Dynamix.parameterizeResult(xmlhttp);
	};

	/**
	 * Returns all context support currently installed for the web client. See
	 * the 'Dynamix.ContextSupportInfoResult' object for details.
	 */
	Dynamix.getContextSupport = function() {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/getContextSupport?token=" + Dynamix.token, false);
		xmlhttp.send();
		return Dynamix.parameterizeContextSupportInfoResult(xmlhttp);
	};

	/**
	 * Returns all plug-ins currently installed by Dynamix.
	 */
	Dynamix.getAllContextPluginInformation = function() {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/getAllContextPluginInformation?token=" + Dynamix.token,
				false);
		xmlhttp.send();
		return Dynamix.parameterizeContextPluginInfoResult(xmlhttp);
	};

	/**
	 * Returns plug-in details for the specified pluginId.
	 */
	Dynamix.getContextPluginInformation = function(pluginId) {
		var xmlhttp = Dynamix.getXmlHttpRequest();
		xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
				+ "/getContextPluginInformation?token=" + Dynamix.token
				+ "&pluginId=" + pluginId, false);
		xmlhttp.send();
		return Dynamix.parameterizeContextPluginInfoResult(xmlhttp);
	};

	// ===============================================================
	// Dynamix Event Handlers (used internally only)
	// ===============================================================

	// onDynamixUnbind
	Dynamix.onDynamixUnbind = function() {
		// Set not bound
		Dynamix.bound = false;
		Dynamix.binding = false;
		Dynamix.bind_index = 0;
		// Remove our token
		Dynamix.token = null;
		// Notify listener
		DynamixListener.onDynamixFrameworkUnbound();
	};

	// onContextEvent
	Dynamix.onContextEvent = function(json) {
		try {
			/* Dispatch to the DynamixListener */
			DynamixListener.onContextEvent(Dynamix.convertToJsonObject(json));
		} catch (e) {
			console.log(e);
		}

	};

	// onContextSupportAdded
	Dynamix.onContextSupportAdded = function(json) {
		/* Dispatch to the DynamixListener */
		DynamixListener
				.onContextSupportAdded(Dynamix.convertToJsonObject(json));
	};

	// onContextSupportRemoved
	Dynamix.onContextSupportRemoved = function(json) {
		/* Dispatch to the DynamixListener */
		DynamixListener.onContextSupportRemoved(Dynamix
				.convertToJsonObject(json));
	};

	// onInstallingContextSupport
	Dynamix.onInstallingContextSupport = function(json, contextType) {
		/* Dispatch to the DynamixListener */
		DynamixListener.onInstallingContextSupport(Dynamix
				.convertToJsonObject(json), contextType);
	};

	// onInstallingContextPlugin
	Dynamix.onInstallingContextPlugin = function(json) {
		/* Dispatch to the DynamixListener */
		DynamixListener.onInstallingContextPlugin(Dynamix
				.convertToJsonObject(json));
	};

	// onContextPluginInstallProgress
	Dynamix.onContextPluginInstallProgress = function(json, percentComplete) {
		/* Dispatch to the DynamixListener */
		DynamixListener.onContextPluginInstallProgress(Dynamix
				.convertToJsonObject(json), parseInt(percentComplete));
	};

	// onContextPluginInstalled
	Dynamix.onContextPluginInstalled = function(json) {
		/* Dispatch to the DynamixListener */
		DynamixListener.onContextPluginInstalled(Dynamix
				.convertToJsonObject(json));
	};

	// onContextPluginUninstalled
	Dynamix.onContextPluginUninstalled = function(json) {
		/* Dispatch to the DynamixListener */
		DynamixListener.onContextPluginUninstalled(Dynamix
				.convertToJsonObject(json));

	};

	// onContextPluginInstallFailed
	Dynamix.onContextPluginInstallFailed = function(json, message) {
		/* Dispatch to the DynamixListener */
		DynamixListener.onContextPluginInstallFailed(Dynamix
				.convertToJsonObject(json), message);
	};

	// onContextPluginError
	Dynamix.onContextPluginError = function(json, message) {
		/* Dispatch to the DynamixListener */
		DynamixListener.onContextPluginError(Dynamix.convertToJsonObject(json),
				message);
	};

	// ===============================================================
	// Dynamix Utility Methods (used internally only)
	// ===============================================================

	// getXmlHttpRequest
	Dynamix.getXmlHttpRequest = function() {
		/*
		 * Cross platform link:
		 * http://stackoverflow.com/questions/1203074/firefox-extension-multiple-xmlhttprequest-calls-per-page
		 */
		var xmlhttp = false;
		if (window.XMLHttpRequest) { // Mozilla, Safari,...

			xmlhttp = new XMLHttpRequest();
			if (xmlhttp.overrideMimeType) {
				/*
				 * Override Mime type to prevent some browsers from trying to
				 * parse responses as XML (e.g., Firefox).
				 */
				xmlhttp.overrideMimeType('text/plain');
				/* Can't use timeouts in some browsers for sync calls */
				// xmlhttp.timeout = Dynamix.call_timeout;
			}
		} else if (window.ActiveXObject) { // IE
			try {
				xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");

				/*
				 * Override Mime type to prevent some browsers from trying to
				 * parse responses as XML (e.g., Firefox).
				 */
				xmlhttp.overrideMimeType('text/plain');
				/* Can't use timeouts in some browsers for sync calls */
				// xmlhttp.timeout = Dynamix.call_timeout;
			} catch (e) {
				try {
					xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
					/*
					 * Override Mime type to prevent some browsers from trying
					 * to parse responses as XML (e.g., Firefox).
					 */
					xmlhttp.overrideMimeType('text/plain');
					/* Can't use timeouts in some browsers for sync calls */
					// xmlhttp.timeout = Dynamix.call_timeout;
				} catch (e) {
				}
			}
		}

		return xmlhttp;
	};

	// eventLoop
	Dynamix.eventLoop = function() {

		if (Dynamix.bound) {
			// Create the xmlHttp request
			var xmlhttp = Dynamix.getXmlHttpRequest();
			// Set 12 second timeout
			xmlhttp.timeout = 12000;
			// Handle state changes
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState === 4) {
					if (!Dynamix.bound) {
						console
								.log("Dynamix.eventLoop: Dynamix Not Bound.... exiting event loop");
						return;
					}

					// If eventLoop has JavaScript statement to execute
					if (xmlhttp.status === 200) {

						// URI decode the response
						var msg = decodeURIComponent(xmlhttp.responseText);
						/*
						 * Make sure we're only executing Dynamix method calls,
						 * which always start with 'javascript:try{Dynamix.' or
						 * 'javascript:try{DynamixListener.'
						 */
						if ((msg.indexOf("javascript:try{Dynamix.") != -1)
								|| (msg
										.indexOf("javascript:try{DynamixListener.") != -1)) {
							setTimeout(
									function() {
										try {
											// Eval the message JavaScript
											// command
											var t = eval(msg);
										} catch (e) {
											console
													.log("Dynamix.eventLoop: Error handling command: "
															+ msg
															+ " | Exception was: "
															+ e);
										}
									}, 1);
							setTimeout(Dynamix.eventLoop, 1);
						} else {
							console
									.log("Dynamix.eventLoop Security Error: Detected non-Dynamix JavaScript call.  Stopping eventLoops.");
							Dynamix.unbind();
							return;
						}
					}

					/*
					 * If there are no events to send, Dynamix will send us HTTP
					 * 404 (to prevent XHR from timing out).
					 */
					else if (xmlhttp.status === 404) {
						setTimeout(Dynamix.eventLoop, 10);
					}

					// Handle security error
					else if (xmlhttp.status === 403) {
						console.log("Dynamix.eventLoop Error: Invalid token. ");
						Dynamix.unbind();
						return;
					}

					// Handle server is stopping
					else if (xmlhttp.status === 503) {
						console
								.log("Dynamix.eventLoop Error: Service unavailable.");
						Dynamix.unbind();
						return;
					}

					// Handle bad request
					else if (xmlhttp.status === 400) {
						console.log("Dynamix.eventLoop Error: Bad request..");
						Dynamix.unbind();
						return;
					}

					// Finally, handle error
					else {
						console.log("Dynamix.eventLoop Error: Request failed.");
						/*
						 * Don't unbind here, since we may be unloading the
						 * page, and we want to keep our token valid for
						 * subsequent Dynamix-enabled pages.
						 */
						// Dynamix.unbind();
					}
				}
			};

			// Connect to the Dynamix event callback
			xmlhttp.open("GET", Dynamix.base_url + ":" + Dynamix.port
					+ "/eventcallback?token=" + Dynamix.token, true);
			xmlhttp.send();
		} else {
			console
					.log("Dynamix.eventLoop: Dynamix Not Bound.... exiting event loop");
		}

	};

	// setCookie
	Dynamix.setCookie = function(c_name, value, exdays) {
		var exdate = new Date();
		exdate.setDate(exdate.getDate() + exdays);
		var c_value = encodeURI(value)
				+ ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
		document.cookie = c_name + "=" + c_value;
	};

	// getCookie
	Dynamix.getCookie = function(c_name) {
		var i, x, y, ARRcookies = document.cookie.split(";");
		for (i = 0; i < ARRcookies.length; i++) {
			x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
			y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
			x = x.replace(/^\s+|\s+$/g, "");
			if (x == c_name) {
				return decodeURI(y);
			}
		}
	};

	// getBooleanFromResponse
	Dynamix.getBooleanFromResponse = function(xmlhttp) {
		if (xmlhttp.status === 200) {
			var string = xmlhttp.responseText;
			// Convert 'true' or 'false' strings from the response text
			switch (string.toLowerCase()) {
			case "true":
				return true;
			case "yes":
				return true;
			case "1":
				return true;
			case "false":
				return false;
			case "no":
				return false;
			case "0":
				return false;
			case null:
				return false;
			default:
				return Boolean(string);
			}
		} else
			return false;
	};

	// parameterizeResult
	Dynamix.parameterizeResult = function(xmlhttp) {
		var nvPairs = decodeURIComponent(xmlhttp.responseText).split(",");
		if (xmlhttp.status === 200) {
			if (nvPairs[0] == Dynamix.SUCCESS) {
				return new Dynamix.Result(true, nvPairs[0], nvPairs[1]);
			} else {
				return new Dynamix.Result(false, nvPairs[0], nvPairs[1]);
			}
		} else {
			console.log("HTTP Error: " + xmlhttp.status);
			return new Dynamix.Result(false, Dynamix.HTTP_ERROR, "HTTP Error: "
					+ xmlhttp.status);
		}

	};

	// parameterizeIdResult
	Dynamix.parameterizeIdResult = function(xmlhttp) {
		var nvPairs = decodeURIComponent(xmlhttp.responseText).split(",");
		if (xmlhttp.status === 200) {
			if (nvPairs[0] == Dynamix.SUCCESS) {
				return new Dynamix.IdResult(true, nvPairs[0], nvPairs[1],
						xmlhttp.responseText);
			} else {
				return new Dynamix.IdResult(false, nvPairs[0], nvPairs[1],
						"DYNAMIX_ERROR_NO_ID_RECEIVED");
			}

		} else {
			console.log("HTTP Error: " + xmlhttp.status);
			return new Dynamix.IdResult(false, Dynamix.HTTP_ERROR,
					"HTTP Error: " + xmlhttp.status,
					"HTTP_ERROR_NO_ID_RECEIVED");
		}

	};

	// parameterizeContextSupportInfoResult
	Dynamix.parameterizeContextSupportInfoResult = function(xmlhttp) {
		/*
		 * Note: don't use 'decodeURIComponent' yet, since the JSON may include
		 * commas.
		 */
		var nvPairs = xmlhttp.responseText.split(",");
		if (xmlhttp.status === 200) {
			if (nvPairs[0] == Dynamix.SUCCESS) {

				try {
					return new Dynamix.ContextSupportInfoResult(
							true,
							nvPairs[0],
							"SUCCESS",
							Dynamix
									.convertToJsonObject(decodeURIComponent(nvPairs[1])));
				} catch (e) {
					console.log("Could not parse: " + nvPairs[1]
							+ " | Exception was: " + e);
					return new Dynamix.ContextSupportInfoResult(false,
							Dynamix.JSON_ERROR, e);
				}

			} else {
				return new Dynamix.ContextSupportInfoResult(false, nvPairs[0],
						nvPairs[1]);
			}

		} else {
			console.log("HTTP Error: " + xmlhttp.status);
			return new Dynamix.ContextSupportInfoResult(false,
					Dynamix.HTTP_ERROR, "HTTP Error: " + xmlhttp.status);
		}

	};

	// parameterizeContextPluginInfoResult
	Dynamix.parameterizeContextPluginInfoResult = function(xmlhttp) {
		/*
		 * Note: don't use 'decodeURIComponent' yet, since the JSON may include
		 * commas.
		 */
		console.log("parameterizeContextPluginInfoResult:"
				+ xmlhttp.responseText);
		var nvPairs = xmlhttp.responseText.split(",");
		if (xmlhttp.status === 200) {
			if (nvPairs[0] == Dynamix.SUCCESS) {
				try {
					return new Dynamix.ContextPluginInfoResult(
							true,
							nvPairs[0],
							"SUCCESS",
							Dynamix
									.convertToJsonObject(decodeURIComponent(nvPairs[1])));
				} catch (e) {
					console.log("Could not parse JSON, Exception was: " + e);
					return new Dynamix.ContextPluginInfoResult(false,
							Dynamix.JSON_ERROR, e);
				}

			} else {
				return new Dynamix.ContextPluginInfoResult(false, nvPairs[0],
						nvPairs[1]);
			}

		} else {
			console.log("HTTP Error: " + xmlhttp.status);
			return new Dynamix.ContextPluginInfoResult(false,
					Dynamix.HTTP_ERROR, "HTTP Error: " + xmlhttp.status);
		}
	};

	// convertToJsonObject
	Dynamix.convertToJsonObject = function(json) {
		// Perform type checking
		if (typeof (json) == "string") {
			/*
			 * The incoming json is a string, so simply try to parse it.
			 */
			return JSON.parse(json);
		} else {
			/*
			 * The incoming json is not a string, so try to stringify it before
			 * parsing.
			 */
			return JSON.parse(JSON.stringify(json));
		}
	};
}

/**
 * Setup cross-platform unload handling that removes the Dynamix listener
 * automatically when the user navigates away from the page. See:
 * http://stackoverflow.com/questions/8508987/webkit-chrome-or-safary-way-doing-ajax-safely-on-onunload-onbeforeunload
 */
// Browser detection
var Browser = {
	IE : !!(window.attachEvent && !window.opera),
	Opera : !!window.opera,
	WebKit : navigator.userAgent.indexOf('AppleWebKit/') > -1,
	Gecko : navigator.userAgent.indexOf('Gecko') > -1
			&& navigator.userAgent.indexOf('KHTML') == -1,
	MobileSafari : !!navigator.userAgent.match(/Apple.*Mobile.*Safari/)
};

/**
 * Ensures the Ajax Get is performed... Asynchronously if possible or
 * Synchronously in WebKit Browsers (otherwise it'll most probably fail)
 */
function ensureAJAXGet(url, args) {
	var async = !Browser.WebKit;
	var finalUrl = url;
	var sep = "";
	for ( var key in args) {
		sep = (sep == "?") ? "&" : "?";
		finalUrl = finalUrl + sep + encodeURIComponent(key) + "="
				+ encodeURIComponent(args[key]);
	}
	var req = new XMLHttpRequest();
	req.open("GET", finalUrl, async);
	req.send();
	return req;
}

/**
 * Sets up an unload function for all browsers to work (onunload or
 * onbeforeunload)
 */
function onUnload(func) {
	if (Browser.WebKit) {
		window.onbeforeunload = func;
	} else {
		window.onunload = func;
	}
}

/**
 * Handle unload.
 */
function unload() {
	if (DynamixListener.unbindOnPageUnload) {
		console.log("Page Unload... Dynamix unbind requested");
		Dynamix.unbind();
	}
}

/**
 * Handle unload.
 */
onUnload(function() {
	unload();
});

/*
 * json2.js 2011-10-19
 * 
 * Public Domain.
 * 
 * NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.
 * 
 * See http://www.JSON.org/js.html
 * 
 * 
 * This code should be minified before deployment. See
 * http://javascript.crockford.com/jsmin.html
 * 
 * USE YOUR OWN COPY. IT IS EXTREMELY UNWISE TO LOAD CODE FROM SERVERS YOU DO
 * NOT CONTROL.
 * 
 * 
 * This file creates a global JSON object containing two methods: stringify and
 * parse.
 * 
 * JSON.stringify(value, replacer, space) value any JavaScript value, usually an
 * object or array.
 * 
 * replacer an optional parameter that determines how object values are
 * stringified for objects. It can be a function or an array of strings.
 * 
 * space an optional parameter that specifies the indentation of nested
 * structures. If it is omitted, the text will be packed without extra
 * whitespace. If it is a number, it will specify the number of spaces to indent
 * at each level. If it is a string (such as '\t' or '&nbsp;'), it contains the
 * characters used to indent at each level.
 * 
 * This method produces a JSON text from a JavaScript value.
 * 
 * When an object value is found, if the object contains a toJSON method, its
 * toJSON method will be called and the result will be stringified. A toJSON
 * method does not serialize: it returns the value represented by the name/value
 * pair that should be serialized, or undefined if nothing should be serialized.
 * The toJSON method will be passed the key associated with the value, and this
 * will be bound to the value
 * 
 * For example, this would serialize Dates as ISO strings.
 * 
 * Date.prototype.toJSON = function (key) { function f(n) { // Format integers
 * to have at least two digits. return n < 10 ? '0' + n : n; }
 * 
 * return this.getUTCFullYear() + '-' + f(this.getUTCMonth() + 1) + '-' +
 * f(this.getUTCDate()) + 'T' + f(this.getUTCHours()) + ':' +
 * f(this.getUTCMinutes()) + ':' + f(this.getUTCSeconds()) + 'Z'; };
 * 
 * You can provide an optional replacer method. It will be passed the key and
 * value of each member, with this bound to the containing object. The value
 * that is returned from your method will be serialized. If your method returns
 * undefined, then the member will be excluded from the serialization.
 * 
 * If the replacer parameter is an array of strings, then it will be used to
 * select the members to be serialized. It filters the results such that only
 * members with keys listed in the replacer array are stringified.
 * 
 * Values that do not have JSON representations, such as undefined or functions,
 * will not be serialized. Such values in objects will be dropped; in arrays
 * they will be replaced with null. You can use a replacer function to replace
 * those with JSON values. JSON.stringify(undefined) returns undefined.
 * 
 * The optional space parameter produces a stringification of the value that is
 * filled with line breaks and indentation to make it easier to read.
 * 
 * If the space parameter is a non-empty string, then that string will be used
 * for indentation. If the space parameter is a number, then the indentation
 * will be that many spaces.
 * 
 * Example:
 * 
 * text = JSON.stringify(['e', {pluribus: 'unum'}]); // text is
 * '["e",{"pluribus":"unum"}]'
 * 
 * 
 * text = JSON.stringify(['e', {pluribus: 'unum'}], null, '\t'); // text is
 * '[\n\t"e",\n\t{\n\t\t"pluribus": "unum"\n\t}\n]'
 * 
 * text = JSON.stringify([new Date()], function (key, value) { return this[key]
 * instanceof Date ? 'Date(' + this[key] + ')' : value; }); // text is
 * '["Date(---current time---)"]'
 * 
 * 
 * JSON.parse(text, reviver) This method parses a JSON text to produce an object
 * or array. It can throw a SyntaxError exception.
 * 
 * The optional reviver parameter is a function that can filter and transform
 * the results. It receives each of the keys and values, and its return value is
 * used instead of the original value. If it returns what it received, then the
 * structure is not modified. If it returns undefined then the member is
 * deleted.
 * 
 * Example: // Parse the text. Values that look like ISO date strings will // be
 * converted to Date objects.
 * 
 * myData = JSON.parse(text, function (key, value) { var a; if (typeof value ===
 * 'string') { a =
 * /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*)?)Z$/.exec(value);
 * if (a) { return new Date(Date.UTC(+a[1], +a[2] - 1, +a[3], +a[4], +a[5],
 * +a[6])); } } return value; });
 * 
 * myData = JSON.parse('["Date(09/09/2001)"]', function (key, value) { var d; if
 * (typeof value === 'string' && value.slice(0, 5) === 'Date(' &&
 * value.slice(-1) === ')') { d = new Date(value.slice(5, -1)); if (d) { return
 * d; } } return value; });
 * 
 * 
 * This is a reference implementation. You are free to copy, modify, or
 * redistribute.
 */

/* jslint evil: true, regexp: true */

/*
 * members "", "\b", "\t", "\n", "\f", "\r", "\"", JSON, "\\", apply, call,
 * charCodeAt, getUTCDate, getUTCFullYear, getUTCHours, getUTCMinutes,
 * getUTCMonth, getUTCSeconds, hasOwnProperty, join, lastIndex, length, parse,
 * prototype, push, replace, slice, stringify, test, toJSON, toString, valueOf
 */

// Create a JSON object only if one does not already exist. We create the
// methods in a closure to avoid creating global variables.
var JSON;
if (!JSON) {
	JSON = {};
}

(function() {
	'use strict';

	function f(n) {
		// Format integers to have at least two digits.
		return n < 10 ? '0' + n : n;
	}

	if (typeof Date.prototype.toJSON !== 'function') {

		Date.prototype.toJSON = function(key) {

			return isFinite(this.valueOf()) ? this.getUTCFullYear() + '-'
					+ f(this.getUTCMonth() + 1) + '-' + f(this.getUTCDate())
					+ 'T' + f(this.getUTCHours()) + ':'
					+ f(this.getUTCMinutes()) + ':' + f(this.getUTCSeconds())
					+ 'Z' : null;
		};

		String.prototype.toJSON = Number.prototype.toJSON = Boolean.prototype.toJSON = function(
				key) {
			return this.valueOf();
		};
	}

	var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, gap, indent, meta = { // table
		// of
		// character
		// substitutions
		'\b' : '\\b',
		'\t' : '\\t',
		'\n' : '\\n',
		'\f' : '\\f',
		'\r' : '\\r',
		'"' : '\\"',
		'\\' : '\\\\'
	}, rep;

	function quote(string) {

		// If the string contains no control characters, no quote characters,
		// and no
		// backslash characters, then we can safely slap some quotes around it.
		// Otherwise we must also replace the offending characters with safe
		// escape
		// sequences.

		escapable.lastIndex = 0;
		return escapable.test(string) ? '"'
				+ string.replace(escapable,
						function(a) {
							var c = meta[a];
							return typeof c === 'string' ? c : '\\u'
									+ ('0000' + a.charCodeAt(0).toString(16))
											.slice(-4);
						}) + '"' : '"' + string + '"';
	}

	function str(key, holder) {

		// Produce a string from holder[key].

		var i, // The loop counter.
		k, // The member key.
		v, // The member value.
		length, mind = gap, partial, value = holder[key];

		// If the value has a toJSON method, call it to obtain a replacement
		// value.

		if (value && typeof value === 'object'
				&& typeof value.toJSON === 'function') {
			value = value.toJSON(key);
		}

		// If we were called with a replacer function, then call the replacer to
		// obtain a replacement value.

		if (typeof rep === 'function') {
			value = rep.call(holder, key, value);
		}

		// What happens next depends on the value's type.

		switch (typeof value) {
		case 'string':
			return quote(value);

		case 'number':

			// JSON numbers must be finite. Encode non-finite numbers as null.

			return isFinite(value) ? String(value) : 'null';

		case 'boolean':
		case 'null':

			// If the value is a boolean or null, convert it to a string. Note:
			// typeof null does not produce 'null'. The case is included here in
			// the remote chance that this gets fixed someday.

			return String(value);

			// If the type is 'object', we might be dealing with an object or an
			// array or
			// null.

		case 'object':

			// Due to a specification blunder in ECMAScript, typeof null is
			// 'object',
			// so watch out for that case.

			if (!value) {
				return 'null';
			}

			// Make an array to hold the partial results of stringifying this
			// object value.

			gap += indent;
			partial = [];

			// Is the value an array?

			if (Object.prototype.toString.apply(value) === '[object Array]') {

				// The value is an array. Stringify every element. Use null as a
				// placeholder
				// for non-JSON values.

				length = value.length;
				for (i = 0; i < length; i += 1) {
					partial[i] = str(i, value) || 'null';
				}

				// Join all of the elements together, separated with commas, and
				// wrap them in
				// brackets.

				v = partial.length === 0 ? '[]' : gap ? '[\n' + gap
						+ partial.join(',\n' + gap) + '\n' + mind + ']' : '['
						+ partial.join(',') + ']';
				gap = mind;
				return v;
			}

			// If the replacer is an array, use it to select the members to be
			// stringified.

			if (rep && typeof rep === 'object') {
				length = rep.length;
				for (i = 0; i < length; i += 1) {
					if (typeof rep[i] === 'string') {
						k = rep[i];
						v = str(k, value);
						if (v) {
							partial.push(quote(k) + (gap ? ': ' : ':') + v);
						}
					}
				}
			} else {

				// Otherwise, iterate through all of the keys in the object.

				for (k in value) {
					if (Object.prototype.hasOwnProperty.call(value, k)) {
						v = str(k, value);
						if (v) {
							partial.push(quote(k) + (gap ? ': ' : ':') + v);
						}
					}
				}
			}

			// Join all of the member texts together, separated with commas,
			// and wrap them in braces.

			v = partial.length === 0 ? '{}' : gap ? '{\n' + gap
					+ partial.join(',\n' + gap) + '\n' + mind + '}' : '{'
					+ partial.join(',') + '}';
			gap = mind;
			return v;
		}
	}

	// If the JSON object does not yet have a stringify method, give it one.

	if (typeof JSON.stringify !== 'function') {
		JSON.stringify = function(value, replacer, space) {

			// The stringify method takes a value and an optional replacer, and
			// an optional
			// space parameter, and returns a JSON text. The replacer can be a
			// function
			// that can replace values, or an array of strings that will select
			// the keys.
			// A default replacer method can be provided. Use of the space
			// parameter can
			// produce text that is more easily readable.

			var i;
			gap = '';
			indent = '';

			// If the space parameter is a number, make an indent string
			// containing that
			// many spaces.

			if (typeof space === 'number') {
				for (i = 0; i < space; i += 1) {
					indent += ' ';
				}

				// If the space parameter is a string, it will be used as the
				// indent string.

			} else if (typeof space === 'string') {
				indent = space;
			}

			// If there is a replacer, it must be a function or an array.
			// Otherwise, throw an error.

			rep = replacer;
			if (replacer
					&& typeof replacer !== 'function'
					&& (typeof replacer !== 'object' || typeof replacer.length !== 'number')) {
				throw new Error('JSON.stringify');
			}

			// Make a fake root object containing our value under the key of ''.
			// Return the result of stringifying the value.

			return str('', {
				'' : value
			});
		};
	}

	// If the JSON object does not yet have a parse method, give it one.

	if (typeof JSON.parse !== 'function') {
		JSON.parse = function(text, reviver) {

			// The parse method takes a text and an optional reviver function,
			// and returns
			// a JavaScript value if the text is a valid JSON text.

			var j;

			function walk(holder, key) {

				// The walk method is used to recursively walk the resulting
				// structure so
				// that modifications can be made.

				var k, v, value = holder[key];
				if (value && typeof value === 'object') {
					for (k in value) {
						if (Object.prototype.hasOwnProperty.call(value, k)) {
							v = walk(value, k);
							if (v !== undefined) {
								value[k] = v;
							} else {
								delete value[k];
							}
						}
					}
				}
				return reviver.call(holder, key, value);
			}

			// Parsing happens in four stages. In the first stage, we replace
			// certain
			// Unicode characters with escape sequences. JavaScript handles many
			// characters
			// incorrectly, either silently deleting them, or treating them as
			// line endings.

			text = String(text);
			cx.lastIndex = 0;
			if (cx.test(text)) {
				text = text.replace(cx,
						function(a) {
							return '\\u'
									+ ('0000' + a.charCodeAt(0).toString(16))
											.slice(-4);
						});
			}

			// In the second stage, we run the text against regular expressions
			// that look
			// for non-JSON patterns. We are especially concerned with '()' and
			// 'new'
			// because they can cause invocation, and '=' because it can cause
			// mutation.
			// But just to be safe, we want to reject all unexpected forms.

			// We split the second stage into 4 regexp operations in order to
			// work around
			// crippling inefficiencies in IE's and Safari's regexp engines.
			// First we
			// replace the JSON backslash pairs with '@' (a non-JSON character).
			// Second, we
			// replace all simple value tokens with ']' characters. Third, we
			// delete all
			// open brackets that follow a colon or comma or that begin the
			// text. Finally,
			// we look to see that the remaining characters are only whitespace
			// or ']' or
			// ',' or ':' or '{' or '}'. If that is so, then the text is safe
			// for eval.

			if (/^[\],:{}\s]*$/
					.test(text
							.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@')
							.replace(
									/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,
									']').replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

				// In the third stage we use the eval function to compile the
				// text into a
				// JavaScript structure. The '{' operator is subject to a
				// syntactic ambiguity
				// in JavaScript: it can begin a block or an object literal. We
				// wrap the text
				// in parens to eliminate the ambiguity.

				j = eval('(' + text + ')');

				// In the optional fourth stage, we recursively walk the new
				// structure, passing
				// each name/value pair to a reviver function for possible
				// transformation.

				return typeof reviver === 'function' ? walk({
					'' : j
				}, '') : j;
			}

			// If the text is not JSON parseable, then a SyntaxError is thrown.

			throw new SyntaxError('JSON.parse');
		};
	}
}());