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
package org.ambientdynamix.api.application;

import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.api.application.IdResult;
import org.ambientdynamix.api.application.ContextSupportResult; 
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextPluginInformationResult;
import org.ambientdynamix.api.application.ContextSupportInfo;
import java.util.Map;

/**
 * IDynamixFacade provides a set of methods for Dynamix applications (running in separate processes) to interact with the
 * Dynamix Framework.
 *
 * @author Darren Carlson
 */
interface IDynamixFacade
{
	/**
	 * Adds the IDynamixListener to the Dynamix Framework. This method may be called at any time, regardless of the application's
	 * security authorization.
	 * @param listener The listener to add.
	 */
	void addDynamixListener(in IDynamixListener listener);
	
	/**
	 * Removes the IDynamixListener from the Dynamix Framework. This method may be called at any time, regardless of the application's
	 * security authorization.
	 * @param listener The listener to remove.
	 */
	void removeDynamixListener(in IDynamixListener listener);
	
	/**
	 * Indicates that the calling application wishes to open a session with the Dynamix framework. After calling 'openSession', applications 
	 * must wait for the 'onSessionOpened' event before they can call additional IDynamixFacade methods (except for 
	 * 'closeSession'). Applications will only receive 'onSessionOpened' if they have been authorized by Dynamix's context firewall, which 
	 * may not be immediate, since users must create a privacy policy for the application before Dynamix interaction is 
	 * allowed. Note that once 'onSessionOpened' has been received, applications must call 'addContextSupport'
	 * for a particular listener and context type before they will be able to perform context sensing or acting. 
	 */
	void openSession();
	
	/**
	 * Immediately closes the application's Dynamix session, removing all of the application's context support.
	 * Once the application's session is closed, it will receive an 'onSessionClosed' event. To re-open a Dynamix session, 
	 * the application must call 'openSession' again (and wait for the 'onSessionOpened' event). Any registered IDynamixListeners
	 * are maintained, even when the session is closed.
	 * @return A Result indicating success or failure. 
	 */
	Result closeSession();
	
	/**
	 * Returns true if the application's session is open; false otherwise.
	 * @return True if the application's session is open; false otherwise.
	 */
	boolean isSessionOpen();
	
	/**
	 * Returns the listener's id.
	 * @return An IdResult indicating success or failure. On success, the id is provided in the IdResult.
	 */
	IdResult getListenerId(in IDynamixListener listener);
	
	/**
	 * Adds context support for the specified listener and context type.
	 * The return values for this method are provided asynchronously using events from the IContextListener interface. 
	 * If the context support was successfully added, the 'onContextSupportAdded' event is raised.
	 * If the context support was not successfully added, the 'onContextTypeNotSupported' event is raised.
	 * If the requested context support was not available, and will be installed, the
	 * 'onInstallingContextSupport' will be raised, followed by 'onInstallingContextPlugin' and events related to plugin
	 * installation.
	 *
	 * @param listener The IDynamixListener to add.
	 * @param contextType A String describing the requested context type. Note that
	 * the contextType string must reference a valid context type string (as described in the developer documentation).
	 * @return A Result indicating success or failure..
	 */	
	Result addContextSupport(in IDynamixListener listener, in String contextType);
	
	/**
	 * Adds context support for the specified listener, context type and support configuration.
	 * The return values for this method are provided asynchronously using events from the IContextListener interface. 
	 * If the context support was successfully added, the 'onContextSupportAdded' event is raised.
	 * If the context support was not successfully added, the 'onContextTypeNotSupported' event is raised.
	 * If the specified context support was not available, and will be installed, the
	 * 'onInstallingContextSupport' and the 'onInstallingContextPlugin' events will be raised.
	 *
	 * @param listener The IDynamixListener to add.
	 * @param contextSupportConfig A Bundle describing the requested context support. 
	          See the developer documentation for a description of available configuration options.
	 * @return A Result indicating success or failure.
	 */	
	Result addConfiguredContextSupport(in IDynamixListener listener, in Bundle contextSupportConfig);
	
	/**
	 * Returns the context support that has been registered by the specified IDynamixListener.
	 *
	 * @param listener The IDynamixListener.
	 * @return A ContextSupportResult.
	 */	
	ContextSupportResult getContextSupport(in IDynamixListener listener);  
	
	/**
	 * Removes previously added context support for the specified listener. 
	 *
	 * @param listener The listener owning the context support.
	 * @param info The context support to remove. 
	 * @return A Result indicating success or failure.
	 */	
	Result removeContextSupport(in IDynamixListener listener, in ContextSupportInfo info);
	
	/**
	 * Removes all context support for the specified listener and contextType.  
	 *
	 * @param listener The listener owning the context support.
	 * @param contextType The context support type to remove. 
	 * @return A Result indicating success or failure.
	 */	
	Result removeContextSupportForContextType(in IDynamixListener listener, in String contextType);
	
	/**
	 * Removes all previously added context support for the specified listener, regardless of contextType. 
	 *
	 * @param listener The listener owning the context support.
	 * @return A Result indicating success or failure.
	 */	
	Result removeAllContextSupportForListener(in IDynamixListener listener);
	
	/**
	 * Removes all previously added context support for all listeners, regardless of contextType. 
	 * @return A Result indicating success or failure.
	 */	
	Result removeAllContextSupport();	
	
	/**
	 * Returns a List of both installed and pending ContextPlugins available from Dynamix. You can check
	 * the installation status of a plug-in by calling 'getInstallStatus()' on each ContextPluginInformation entity
	 * contained in the ContextPluginInformationResult.
	 * @return A ContextPluginInformationResult indicating success or failure. On success, the ContextPluginInformationResult contains the List of context plug-ins.
	 */		
	ContextPluginInformationResult getAllContextPluginInformation();
	
	/**
	 * Returns a ContextPluginInformation object for the specified pluginId. You can check
	 * the installation status of the plug-in by calling 'getInstallStatus()' on the ContextPluginInformation entity.
	 * @return A ContextPluginInformationResult indicating success or failure. On success, the ContextPluginInformationResult 
	 * contains the requested context plug-in as the only List object.
	 */		
	ContextPluginInformationResult getContextPluginInformation(String pluginId); 
	
	/**
	 * Resends all ContextEvents that have been cached by Dynamix for the specified listener. ContextEvents are provided to 
	 * applications according to Dynamix authentication and security policies defined by Dynamix users.
	 * Note that the requesting application will only received context info they are subscribed to.
	 *
	 * @param listener The listener wishing to receive cached events.
	 * @return A Result indicating success or failure.
	 */
	Result resendAllCachedContextEvents(in IDynamixListener listener);
	
	/**
	 * Resends all ContextEvents (of the specified contextType) that have been cached by Dynamix for the specified listener. 
	 * ContextEvents are provided to applications according to Dynamix authentication and security policies defined by Dynamix users.
	 * Note that the requesting application will only received context info they are subscribed to.
	 *
	 * @param listener The listener wishing to receive cached events.
	 * @param contextType The type of ContextEvent to return.
	 * @return A Result indicating success or failure.
	 */
	Result resendAllTypedCachedContextEvents(in IDynamixListener listener, in String contextType);
	
	/**
	 * Resends the ContextEvent entities that have been cached for the listener within the specified 
	 * past number of milliseconds. If the number of milliseconds is longer than the max cache time, then all cached 
	 * events are returned. ContextEvents are provided to applications according to Dynamix authentication 
	 * and security policies. Note that the requesting application will only received context info they are subscribed to.
	 * 
	 * @param listener The listener wishing to receive the results.
	 * @param previousMills The time (in milliseconds) to retrieve past events.
	 * @return A Result indicating success or failure.
	 */
	Result resendCachedContextEvents(in IDynamixListener listener, int previousMills);
	
	/**
	 * Resends the ContextEvent entities (of the specified contextType) that have been cached for the listener within the specified 
	 * past number of milliseconds. If the number of milliseconds is longer than the max cache time, then all appropriate cached
	 * events are returned. ContextEvents are provided to applications according to Dynamix authentication 
	 * and security policies. Note that the requesting application will only received context info they are subscribed to.
	 * 
	 * @param listener The listener wishing to receive the results.
	 * @param contextType The type of ContextEvent to return.
	 * @param previousMills The time (in milliseconds) to retrieve past events.
	 * @return A Result indicating success or failure.
	 */
	Result resendTypedCachedContextEvents(in IDynamixListener listener, in String contextType, int previousMills);
	
	/**
	 * Requests that Dynamix perform a dedicated context interaction (sensing or acting) using the specified plugin and contextType.
	 * Note that this method is only available for ContextPlugins that programmatic access (i.e., reactive context plug-in types).
	 *	
	 * @param listener The listener wishing to receive the results.
	 * @param pluginId The id of the plugin that should perform the context scan.
	 * @param contextType The type of context info to scan for.
	 * @return An IdResult indicating success or failure. On success, the request id is provided in the IdResult.
	 */
	IdResult contextRequest(in IDynamixListener listener, in String pluginId, in String contextType);
	
	/**
	 * Requests that Dynamix perform a dedicated context interaction using the specified plugin, contextType and contextConfig.
	 * Context requests may be of several types and are plug-in specific (see the plug-in documentation for details). 
	 * For some plug-ins, a context request may returns specific contextual information obtained from the environment. 
	 * For other plug-ins, a context request may be a change in the underlying contextual situation (e.g., playing a media file on a nearby media renderer).
	 * See the plugin's documentation for configuration options that can be included in the interactionConfig Bundle.
	 *	
	 * @param listener The listener wishing to receive the results.
	 * @param pluginId The id of the plugin that should handle the context request.
	 * @param contextType The type of context to interact with.
	 * @param contextConfig A plug-in specific Bundle of context request configuration options.
	 * @return An IdResult indicating success or failure. On success, the request id is provided in the IdResult.
	 */
	IdResult configuredContextRequest(in IDynamixListener listener, in String pluginId, in String contextType, in Bundle contextConfig);
	
	/**
	 * Returns the session id for this application, which is used for some secure interactions with Dynamix, such as
	 * launching context acquisition interfaces for Context Plug-ins of type pull interactive.
	 * @return An IdResult indicating success or failure. On success, the session id is provided in the IdResult.
	 */
	IdResult getSessionId();
	
	/**
	 * Request that Dynamix install a specific ContextPlugin on behalf of the Application. Such a request might be made 
	 * if an application has a dependency on a specific ContextPlugin. If the installation request is accepted by Dynamix, this 
	 * method returns its results asynchronously using 'onInstallingContextPlugin' and events related to plugin installation. 
	 * 
	 * @param plugInfo The plugin to install.
	 * @return A Result indicating success or failure.
	 */
	Result requestContextPluginInstallation(in ContextPluginInformation plugInfo);
	
	/**
	 * Request that Dynamix uninstall a specific ContextPlugin on behalf of the Application.
	 * 
	 * @param plugInfo The plugin to uninstall.
	 * @return A Result indicating success or failure.
	 */
	Result requestContextPluginUninstall(in ContextPluginInformation plugInfo); 
	
	/**
	 * Returns true if Dynamix is active; false otherwise.
	 * @return True if Dynamix is active; false otherwise.
	 */
	boolean isDynamixActive();
	
	boolean isPanosMaster();
	
	void stopPlugin(String pluginId);
	
	/**
	 * Opens the specified plug-in's configuration view (if it has one).
	 * @return A Result indicating success or failure.
	 */
	Result openContextPluginConfigurationView(in IDynamixListener listener, in String pluginId);
}