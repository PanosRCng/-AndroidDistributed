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
if (typeof DynamixListener === "undefined") {

	/**
	 * The DynamixListener provides callback methods from the Dynamix Framework,
	 * which web clients can customize for their own use. For details on
	 * controlling the Dynamix Framework, see the "Dynamix Data Types", "Dynamix
	 * Connection Methods" and "Dynamix REST API Methods" in the dynamix.js
	 * file.
	 */
	var DynamixListener = {};

	/**
	 * Set true to unbind Dynamix when the user leaves the page; false
	 * otherwise. By not unbinding Dynamix on page unload, subsequent pages *in
	 * the same path* will be able to reuse a previously established Dynamix
	 * session, along with any previously established context support.
	 */
	DynamixListener.unbindOnPageUnload = false;

	/**
	 * Called after the web client establishes a connection to Dynamix. Raised
	 * in response to 'Dynamix.bind()'. Note that it is NOT possible to interact
	 * with Dynamix before this event is raised.
	 */
	DynamixListener.onDynamixFrameworkBound = function() {
		console.log("onDynamixFrameworkBound");
	};

	/**
	 * Called after the web client loses connection to Dynamix. Raised in
	 * response to 'Dynamix.unbind()' or Dynamix Framework initiated unbinds.
	 * This event indicates that the web client's session has been closed along
	 * with any context support. Note that the 'onDynamixListenerRemoved',
	 * 'onSessionClosed', and 'onContextSupportRemoved' events will NOT be
	 * raised in addition to this event.
	 * 
	 */
	DynamixListener.onDynamixFrameworkUnbound = function() {
		console.log("onDynamixFrameworkUnbound");
	};

	/**
	 * Called if no connection can be established to Dynamix. Note that it is
	 * NOT possible to interact with Dynamix if this event is raised.
	 */
	DynamixListener.onDynamixFrameworkBindError = function(result) {
		console.log("onDynamixFrameworkBindError");
	};

	/**
	 * Called after the web client establishes a Dynamix listener. Raised in
	 * response to 'Dynamix.addDynamixListener'.
	 */
	DynamixListener.onDynamixListenerAdded = function() {
		console.log("onDynamixListenerAdded");
	};

	/**
	 * Called after the web client removes a Dynamix listener. Raised in
	 * response to 'Dynamix.removeDynamixListener()'. Note that removing a
	 * Dynamix listener does NOT unbind Dynamix, so 'Dynamix.addDynamixListener'
	 * may be called again, if needed.
	 */
	DynamixListener.onDynamixListenerRemoved = function() {
		console.log("onDynamixListenerRemoved");
	};

	/**
	 * Called when Dynamix is waiting for the user to approve security
	 * authorization for the web client. Note that it is impossible to interact
	 * with Dynamix until the 'onSecurityAuthorizationGranted' event is raised,
	 * which may take some time, since the user is responsible for approving web
	 * application authorization.
	 */
	DynamixListener.onAwaitingSecurityAuthorization = function() {
		console.log("onAwaitingSecurityAuthorization");
	};

	/**
	 * Called after the user grants the web client security authorization. This
	 * event is followed by 'DynamixListener.onSessionOpened'.
	 */
	DynamixListener.onSecurityAuthorizationGranted = function() {
		console.log("onSecurityAuthorizationGranted");
	};

	/**
	 * Called if the user revokes the web client's security authorization. This
	 * event is followed by 'DynamixListener.onContextSupportRemoved' for any
	 * existing context support, and then 'DynamixListener.onSessionClosed'.
	 * possibly
	 */
	DynamixListener.onSecurityAuthorizationRevoked = function() {
		console.log("onSecurityAuthorizationRevoked");
	};

	/**
	 * Called when the web client's Dynamix session has opened. A session is
	 * opened automatically for web clients, once a Dynamix listener has been
	 * added and security authorization has been approved by the user. After
	 * this event the web client may add context support, perform context
	 * requests, etc.
	 */
	DynamixListener.onSessionOpened = function() {
		console.log("onSessionOpened");
	};

	/**
	 * Called when the web client's Dynamix session has closed its Dynamix
	 * listener has been removed. After this event the web client may not
	 * interact with Dynamix aside from unbinding or adding a Dynamix listener,
	 * which will automatically open a session again if security authorization
	 * is granted by the user.
	 */
	DynamixListener.onSessionClosed = function() {
		console.log("onSessionClosed");
	};

	/**
	 * Called when the Dynamix Framework becomes active
	 */
	DynamixListener.onDynamixFrameworkActive = function() {
		console.log("onDynamixFrameworkActive");
	};

	/**
	 * Called when the Dynamix Framework becomes inactive
	 */
	DynamixListener.onDynamixFrameworkInactive = function() {
		console.log("onDynamixFrameworkInactive");
	};

	/**
	 * Called after context support has been added. Raised in response to
	 * 'Dynamix.addContextSupport' or 'Dynamix.addConfiguredContextSupport'.
	 * 
	 * @param {object}
	 *            supportInfo Information about the added context support.
	 * @param {string}
	 *            supportInfo.supportId The context support id.
	 * @param {string}
	 *            supportInfo.contextType The context type associated with the
	 *            context support.
	 * @param {object}
	 *            supportInfo.plugin The plug-in providing context support.
	 * @param {string}
	 *            plugin.version.valuepluginId The unique id of the plug-in.
	 * @param {string}
	 *            supportInfo.plugin.pluginName The name of the plug-in.
	 * @param {string}
	 *            supportInfo.plugin.pluginDescription The description of the
	 *            plug-in.
	 * @param {string}
	 *            supportInfo.plugin.version.value The version of the plug-in as
	 *            MAJOR.MINOR.MICRO (e.g., 1.2.3).
	 * @param {String[]}
	 *            supportInfo.plugin.supportedContextTypes An array of supported
	 *            context types for the plug-in.
	 * @param {boolean}
	 *            supportInfo.plugin.userControlledContextAcquisition Whether or
	 *            not this plug-in requires user controlled context acquisition
	 *            (e.g. through a GUI).
	 * @param {string}
	 *            supportInfo.plugin.installStatus Possible states are
	 *            INSTALLED, INSTALLING, NOT_INSTALLED, ERROR.
	 * @param {boolean}
	 *            supportInfo.plugin.requiresConfiguration True if the plug-in
	 *            requires configuration; false otherwise.
	 * @param {boolean}
	 *            supportInfo.plugin.configured True if the plug-in is
	 *            configured; false otherwise.
	 * @param {boolean}
	 *            supportInfo.plugin.enabled True if the plug-in is enabled;
	 *            false otherwise.
	 */
	DynamixListener.onContextSupportAdded = function(supportInfo) {
		console.log("onContextSupportAdded: " + supportInfo.contextType
				+ " using plug-in " + supportInfo.plugin.pluginId + " "
				+ supportInfo.plugin.version.value);
	};

	/**
	 * Called after context support has been removed. Raised in response to
	 * 'Dynamix.removeContextSupportForContextType',
	 * 'Dynamix.removeContextSupportForSupportId',
	 * 'Dynamix.removeAllContextSupport', or any time Dynamix removes context
	 * support (e.g., when shutting down).
	 * 
	 * @param {object}
	 *            supportInfo Information about the removed context support.
	 * @param {string}
	 *            supportInfo.supportId The context support id.
	 * @param {string}
	 *            supportInfo.contextType The context type associated with the
	 *            context support.
	 * @param {object}
	 *            supportInfo.plugin The plug-in providing context support.
	 * @param {string}
	 *            plugin.version.valuepluginId The unique id of the plug-in.
	 * @param {string}
	 *            supportInfo.plugin.pluginName The name of the plug-in.
	 * @param {string}
	 *            supportInfo.plugin.pluginDescription The description of the
	 *            plug-in.
	 * @param {string}
	 *            supportInfo.plugin.version.value The version of the plug-in as
	 *            MAJOR.MINOR.MICRO (e.g., 1.2.3).
	 * @param {String[]}
	 *            supportInfo.plugin.supportedContextTypes An array of supported
	 *            context types for the plug-in.
	 * @param {boolean}
	 *            supportInfo.plugin.userControlledContextAcquisition Whether or
	 *            not this plug-in requires user controlled context acquisition
	 *            (e.g. through a GUI).
	 * @param {string}
	 *            supportInfo.plugin.installStatus Possible states are
	 *            INSTALLED, INSTALLING, NOT_INSTALLED, ERROR.
	 * @param {boolean}
	 *            supportInfo.plugin.requiresConfiguration True if the plug-in
	 *            requires configuration; false otherwise.
	 * @param {boolean}
	 *            supportInfo.plugin.configured True if the plug-in is
	 *            configured; false otherwise.
	 * @param {boolean}
	 *            supportInfo.plugin.enabled True if the plug-in is enabled;
	 *            false otherwise.
	 */
	DynamixListener.onContextSupportRemoved = function(supportInfo) {
		console.log("onContextSupportRemoved: " + supportInfo.contextType
				+ " using plug-in " + supportInfo.plugin.pluginId + " "
				+ supportInfo.plugin.version.value);
	};

	/**
	 * Called if the requested context support cannot be installed for the
	 * specified context type. Raised after a failure of
	 * 'Dynamix.addContextSupport' or 'Dynamix.addConfiguredContextSupport'.
	 * 
	 * @param {string}
	 *            The requested context type that is not supported.
	 */
	DynamixListener.onContextTypeNotSupported = function(contextType) {
		console.log("onContextTypeNotSupported: " + contextType);
	};

	/**
	 * Called when Dynamix begins installing a plugin to handle the requested
	 * context support. Raised in response to 'Dynamix.addContextSupport' or
	 * 'Dynamix.addConfiguredContextSupport'. See 'Dynamix.ContextPluginInfo' in
	 * dynamix.js for details on the plugin object.
	 * 
	 * @param {object}
	 *            plugin The plug-in that is being installed.
	 * @param {string}
	 *            plugin.version.valuepluginId The unique id of the plug-in.
	 * @param {string}
	 *            plugin.pluginName The name of the plug-in.
	 * @param {string}
	 *            plugin.pluginDescription The description of the plug-in.
	 * @param {string}
	 *            plugin.version.value The version of the plug-in as
	 *            MAJOR.MINOR.MICRO (e.g., 1.2.3).
	 * @param {String[]}
	 *            plugin.supportedContextTypes An array of supported context
	 *            types for the plug-in.
	 * @param {boolean}
	 *            plugin.userControlledContextAcquisition Whether or not this
	 *            plug-in requires user controlled context acquisition (e.g.
	 *            through a GUI).
	 * @param {string}
	 *            plugin.installStatus Possible states are INSTALLED,
	 *            INSTALLING, NOT_INSTALLED, ERROR.
	 * @param {boolean}
	 *            plugin.requiresConfiguration True if the plug-in requires
	 *            configuration; false otherwise.
	 * @param {boolean}
	 *            plugin.configured True if the plug-in is configured; false
	 *            otherwise.
	 * @param {boolean}
	 *            plugin.enabled True if the plug-in is enabled; false
	 *            otherwise.
	 * @param {string}
	 *            contextType The context type to be supported by the plug-in.
	 */
	DynamixListener.onInstallingContextSupport = function(plugin, contextType) {
		console.log("onInstallingContextSupport for " + contextType + " using "
				+ plugin.pluginId + " " + plugin.version);
	};

	/**
	 * Called every time Dynamix begins installing a plug-in.
	 * 
	 * @param {object}
	 *            plugin The plug-in that is being installed.
	 * @param {string}
	 *            plugin.pluginId The unique id of the plug-in.
	 * @param {string}
	 *            plugin.pluginName The name of the plug-in.
	 * @param {string}
	 *            plugin.pluginDescription The description of the plug-in.
	 * @param {string}
	 *            plugin.version.value The version of the plug-in as
	 *            MAJOR.MINOR.MICRO (e.g., 1.2.3).
	 * @param {String[]}
	 *            plugin.supportedContextTypes An array of supported context
	 *            types for the plug-in.
	 * @param {boolean}
	 *            plugin.userControlledContextAcquisition Whether or not this
	 *            plug-in requires user controlled context acquisition (e.g.
	 *            through a GUI).
	 * @param {string}
	 *            plugin.installStatus Possible states are INSTALLED,
	 *            INSTALLING, NOT_INSTALLED, ERROR.
	 * @param {boolean}
	 *            plugin.requiresConfiguration True if the plug-in requires
	 *            configuration; false otherwise.
	 * @param {boolean}
	 *            plugin.configured True if the plug-in is configured; false
	 *            otherwise.
	 * @param {boolean}
	 *            plugin.enabled True if the plug-in is enabled; false
	 *            otherwise.
	 */
	DynamixListener.onInstallingContextPlugin = function(plugin) {
		console.log("onInstallingContextPlugin: " + plugin.pluginId + " "
				+ plugin.version);
	};

	/**
	 * Called during plug-in installation. See 'Dynamix.ContextPluginInfo' in
	 * dynamix.js for details on the plugin object.
	 * 
	 * @param {object}
	 *            plugin The plug-in that is being installed.
	 * @param {string}
	 *            plugin.pluginId The unique id of the plug-in.
	 * @param {string}
	 *            plugin.pluginName The name of the plug-in.
	 * @param {string}
	 *            plugin.pluginDescription The description of the plug-in.
	 * @param {string}
	 *            plugin.version.value The version of the plug-in as
	 *            MAJOR.MINOR.MICRO (e.g., 1.2.3).
	 * @param {String[]}
	 *            plugin.supportedContextTypes An array of supported context
	 *            types for the plug-in.
	 * @param {boolean}
	 *            plugin.userControlledContextAcquisition Whether or not this
	 *            plug-in requires user controlled context acquisition (e.g.
	 *            through a GUI).
	 * @param {string}
	 *            plugin.installStatus Possible states are INSTALLED,
	 *            INSTALLING, NOT_INSTALLED, ERROR.
	 * @param {boolean}
	 *            plugin.requiresConfiguration True if the plug-in requires
	 *            configuration; false otherwise.
	 * @param {boolean}
	 *            plugin.configured True if the plug-in is configured; false
	 *            otherwise.
	 * @param {boolean}
	 *            plugin.enabled True if the plug-in is enabled; false
	 *            otherwise.
	 * @param {number}
	 *            percentComplete The percent complete of the plug-in
	 *            installation process (from 0 to 100).
	 */
	DynamixListener.onContextPluginInstallProgress = function(plugin,
			percentComplete) {
		console.log("onContextPluginInstallProgress: " + plugin.pluginId + " "
				+ percentComplete);
	};

	/**
	 * Called when Dynamix successfully installs a new plug-in.
	 * 
	 * @param {object}
	 *            plugin The plug-in that was installed.
	 * @param {string}
	 *            plugin.pluginId The unique id of the plug-in.
	 * @param {string}
	 *            plugin.pluginName The name of the plug-in.
	 * @param {string}
	 *            plugin.pluginDescription The description of the plug-in.
	 * @param {string}
	 *            plugin.version.value The version of the plug-in as
	 *            MAJOR.MINOR.MICRO (e.g., 1.2.3).
	 * @param {String[]}
	 *            plugin.supportedContextTypes An array of supported context
	 *            types for the plug-in.
	 * @param {boolean}
	 *            plugin.userControlledContextAcquisition Whether or not this
	 *            plug-in requires user controlled context acquisition (e.g.
	 *            through a GUI).
	 * @param {string}
	 *            plugin.installStatus Possible states are INSTALLED,
	 *            INSTALLING, NOT_INSTALLED, ERROR.
	 * @param {boolean}
	 *            plugin.requiresConfiguration True if the plug-in requires
	 *            configuration; false otherwise.
	 * @param {boolean}
	 *            plugin.configured True if the plug-in is configured; false
	 *            otherwise.
	 * @param {boolean}
	 *            plugin.enabled True if the plug-in is enabled; false
	 *            otherwise.
	 */
	DynamixListener.onContextPluginInstalled = function(plugin) {
		console.log("onContextPluginInstalled: " + plugin.pluginId + " "
				+ plugin.version);
	};

	/**
	 * Called if a plug-in cannot be installed.
	 * 
	 * @param {object}
	 *            plugin The plug-in that is being installed.
	 * @param {string}
	 *            plugin.pluginId The unique id of the plug-in.
	 * @param {string}
	 *            plugin.pluginName The name of the plug-in.
	 * @param {string}
	 *            plugin.pluginDescription The description of the plug-in.
	 * @param {string}
	 *            plugin.version.value The version of the plug-in as
	 *            MAJOR.MINOR.MICRO (e.g., 1.2.3).
	 * @param {String[]}
	 *            plugin.supportedContextTypes An array of supported context
	 *            types for the plug-in.
	 * @param {boolean}
	 *            plugin.userControlledContextAcquisition Whether or not this
	 *            plug-in requires user controlled context acquisition (e.g.
	 *            through a GUI).
	 * @param {string}
	 *            plugin.installStatus Possible states are INSTALLED,
	 *            INSTALLING, NOT_INSTALLED, ERROR.
	 * @param {boolean}
	 *            plugin.requiresConfiguration True if the plug-in requires
	 *            configuration; false otherwise.
	 * @param {boolean}
	 *            plugin.configured True if the plug-in is configured; false
	 *            otherwise.
	 * @param {boolean}
	 *            plugin.enabled True if the plug-in is enabled; false
	 *            otherwise.
	 * @param {string}
	 *            message A description of the failure.
	 */
	DynamixListener.onContextPluginInstallFailed = function(plugin, message) {
		console.log("onContextPluginInstallFailed: " + plugin.pluginId + " "
				+ plugin.version);
	};

	/**
	 * Called after Dynamix uninstalls a plug-in.
	 * 
	 * @param {object}
	 *            plugin The plug-in that is being uninstalled.
	 * @param {string}
	 *            plugin.pluginId The unique id of the plug-in.
	 * @param {string}
	 *            plugin.pluginName The name of the plug-in.
	 * @param {string}
	 *            plugin.pluginDescription The description of the plug-in.
	 * @param {string}
	 *            plugin.version.value The version of the plug-in as
	 *            MAJOR.MINOR.MICRO (e.g., 1.2.3).
	 * @param {String[]}
	 *            plugin.supportedContextTypes An array of supported context
	 *            types for the plug-in.
	 * @param {boolean}
	 *            plugin.userControlledContextAcquisition Whether or not this
	 *            plug-in requires user controlled context acquisition (e.g.
	 *            through a GUI).
	 * @param {string}
	 *            plugin.installStatus Possible states are INSTALLED,
	 *            INSTALLING, NOT_INSTALLED, ERROR.
	 * @param {boolean}
	 *            plugin.requiresConfiguration True if the plug-in requires
	 *            configuration; false otherwise.
	 * @param {boolean}
	 *            plugin.configured True if the plug-in is configured; false
	 *            otherwise.
	 * @param {boolean}
	 *            plugin.enabled True if the plug-in is enabled; false
	 *            otherwise.
	 */
	DynamixListener.onContextPluginUninstalled = function(plugin) {
		console.log("onContextPluginUninstalled: " + plugin.pluginId + " "
				+ plugin.version);
	};

	/**
	 * Called if a plug-in encounters an error during runtime.
	 * 
	 * @param {object}
	 *            plugin The plug-in that is being installed.
	 * @param {string}
	 *            plugin.pluginId The unique id of the plug-in.
	 * @param {string}
	 *            plugin.pluginName The name of the plug-in.
	 * @param {string}
	 *            plugin.pluginDescription The description of the plug-in.
	 * @param {string}
	 *            plugin.version.value The version of the plug-in as
	 *            MAJOR.MINOR.MICRO (e.g., 1.2.3).
	 * @param {String[]}
	 *            plugin.supportedContextTypes An array of supported context
	 *            types for the plug-in.
	 * @param {boolean}
	 *            plugin.userControlledContextAcquisition Whether or not this
	 *            plug-in requires user controlled context acquisition (e.g.
	 *            through a GUI).
	 * @param {string}
	 *            plugin.installStatus Possible states are INSTALLED,
	 *            INSTALLING, NOT_INSTALLED, ERROR.
	 * @param {boolean}
	 *            plugin.requiresConfiguration True if the plug-in requires
	 *            configuration; false otherwise.
	 * @param {boolean}
	 *            plugin.configured True if the plug-in is configured; false
	 *            otherwise.
	 * @param {boolean}
	 *            plugin.enabled True if the plug-in is enabled; false
	 *            otherwise.
	 * @param {string}
	 *            message A description of the error.
	 */
	DynamixListener.onContextPluginError = function(plugin, message) {
		console.log("onContextPluginError: message = " + message + " from "
				+ plugin.pluginId + " " + plugin.version);
	};

	/**
	 * Called when a context request fails. Raised in response to a failure of
	 * 'Dynamix.contextRequest' or 'Dynamix.configuredContextRequest'.
	 * 
	 * @param {string}
	 *            requestId The original context request id.
	 * @param {string}
	 *            error_message The error message associated with the failure.
	 * @param {string}
	 *            error_code The error code associated with the failure (see
	 *            Dynamix developer documentation).
	 */
	DynamixListener.onContextRequestFailed = function(requestId, error_message,
			error_code) {
		console.log("onContextRequestFailed for " + requestId
				+ " with error code " + error_code + " and error_message "
				+ error_message);
	};

	/**
	 * Called when requested context information has been discovered for a
	 * particular context type. For auto plug-ins, this event is raised
	 * automatically when new context information is available. For reactive
	 * plug-ins, this event is raised after a successful call to
	 * 'Dynamix.contextRequest' or 'Dynamix.configuredContextRequest'.
	 * 
	 * @param {object}
	 *            event The context event object.
	 * @param {string}
	 *            event.sourcePluginId The id of the plug-in generating the
	 *            event. To obtain full plug-in details, use
	 *            Dynamix.getContextPluginInformation.
	 * @param {string}
	 *            event.responseId The response id (empty for auto-based
	 *            plug-ins).
	 * @param {string}
	 *            event.contextType The context type of the event.
	 * @param {string}
	 *            event.implementingClassname The implementation class of the
	 *            event.
	 * @param {string}
	 *            event.timeStamp The local time when the event was generated.
	 * @param {boolean}
	 *            event.expires True if this event expires; false otherwise
	 * @param {string}
	 *            event.expireTime The expiration time of this event
	 * @param {boolean}
	 *            event.hasPojoData True if this event contains pojo data; false
	 *            otherwise. If True, event.encodedDataType and
	 *            event.encodedData will be null.
	 * @param {string}
	 *            event.encodedDataType The data type of the event.encodedData
	 *            property.
	 * @param {string}
	 *            event.encodedData Web encoded context data.
	 * 
	 */
	DynamixListener.onContextEvent = function(event) {
		console.log("Listener onContextEvent from: " + event.sourcePluginId
				+ " - responseId: " + event.responseId + " - contextType: "
				+ event.contextType + " - implementingClassname: "
				+ event.implementingClassname + " - timeStamp: "
				+ event.timeStamp + " - expires: " + event.expires
				+ " - expireTime: " + event.expireTime + " - hasPojoData: "
				+ event.hasPojoData + " - encodedDataType: "
				+ event.encodedDataType + " - encodedData: "
				+ event.encodedData);
	};

}