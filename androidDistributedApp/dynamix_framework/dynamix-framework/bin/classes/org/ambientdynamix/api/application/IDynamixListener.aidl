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

import java.util.List;
import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;

/**
 * IDynamixListener provides a set of methods that remote clients must implement in order to receive
 * context events from the Dynamix Framework. 
 *
 * @author Darren Carlson
 */
interface IDynamixListener
{	
	/**
	 * A response to the IDynamixFacade 'addDynamixListener' method indicating that the Dynamix listener has been added.
	 * @param listenerId The listener's unique listenerId.
	 */
	void onDynamixListenerAdded(in String listenerId);
	
	/**
	 * A response to the IDynamixFacade 'removeDynamixListener' method indicating that the Dynamix listener has been removed.
	 */
	void onDynamixListenerRemoved();
	
	/**
	 * Notification that the application is awaiting security authorization by the Dynamix Framework. Sent in response 
	 * to a call to the IDynamixFacade's 'openSession' method if the application has not yet been granted 
	 * security authorization. If authorization is granted by Dynamix, the 'onSecurityAuthorizationGranted' event will 
	 * be raised.
	 */
	void onAwaitingSecurityAuthorization();
	
	/**
	 * Notification that the application has been granted security authorization by the Dynamix Framework.
	 * Once security authorization has been granted, the 'onSecurityAuthorizationGranted' event will always
	 * be raised in response to calls to the IDynamixFacade's 'openSession' method; however, Applications must 
	 * wait for the 'onSessionOpened' event before they can interact with Dynamix (e.g. add context  support)
	 * or expect context events. Note that the 'onSessionOpened' implies that security authorization has
	 * been granted.
	 */
	void onSecurityAuthorizationGranted();
	
	/**
	 * Notification that the application's security authorization has been revoked by the Dynamix Framework.
	 * Once 'onSecurityAuthorizationRevoked' is received, the Dynamix Framework will immediately stop sending events to 
	 * the application and further interaction with Dynamix is not allowed until security authorization is granted again.
	 * Once security authorization is revoked, applications may only call the IDynamixFacade's 'openSession', 'addDynamixListener' and
	 * 'removeDynamixListener' methods.
	 */
	void onSecurityAuthorizationRevoked();
	
	/**
	 * Notification that the Dynamix session has been opened. Once received, applications can safely interact with Dynamix 
	 * (e.g. add context support) and expect context events. Note that 'onSessionOpened' will only be 
	 * sent to an application once security authorization has been granted by the Dynamix Framework. This event is 
	 * typically sent in response to a call to the IDynamixFacade's 'openSession' method. Note that if the
	 * Dynamix Framework is deactivated after 'onSessionOpened' has been received, the application will receive the 
	 * 'onSessionClosed' event. In this case it is NOT necessary to call the IDynamixFacade's 'openSession' 
	 * method again, since Dynamix maintains the application's session status, even while inactive. Once the 
	 * Dynamix Framework becomes active again, the 'onSessionOpened' event will be raised again. However, if the 
	 * application loses contact with Dynamix (as indicated by an 'onServiceDisconnected' provided 
	 * by the ServiceConnection object), Dynamix has been shut down by Android (or crashed). In this case, the 
	 * application will need to call the IDynamixFacade's 'openSession' method again.
	 *
	 * @param sessionId The application's sessionId.
	 */
	void onSessionOpened(in String sessionId);

	/**
	 * Notification that the Dynamix session has been closed.
	 */	
	void onSessionClosed();
		
	/**
	 * Notification of an incoming ContextEvent.
	 * @see ContextEvent
	 */
	void onContextEvent(in ContextEvent event);
	
	/**
	 * Notifies the listener that context support for the given context type has been added.
	 * Note that applications may receive more than one of this type of event for the same contextType if multiple 
	 * plugins are able to provide support for the requested type.
	 *
	 * @param supportInfo Information about the context support that was added.
	 */
	void onContextSupportAdded(in ContextSupportInfo supportInfo);
	
	/**
	 * Notifies the listener that context support for the given context type has been removed.
	 *
	 * @param supportInfo Information about the context support that was removed.
	 */
	void onContextSupportRemoved(in ContextSupportInfo supportInfo);
	
	/**
	 * Notifies the application that the requested context type is not supported, and that the requested context 
	 * support was not added. This result implies that Dynamix is not able to install support for the specified
	 * context type at the moment.
	 *
	 * @param contextType The requested context type that is not supported.
	 */
	void onContextTypeNotSupported(in String contextType);
	
	/**
	 * Notifies the application that a plugin installation has begun for the specified contextType. This 
	 * event is only raised if the application previously called 'addContextSupport' for a context 
	 * plugin that was not already installed (or was being installed) and the application has permission to install context support. 
	 * If the context support is eventually installed, the caller will receive 'onContextSupportAdded'. If the context support 
	 * was not installed, the caller will receive 'onContextTypeNotSupported'.
	 *
	 * @param ContextPluginInformation The Context Plug-in being installed.
	 * @param contextType The type of context data support being installed.
	 */	
	void onInstallingContextSupport(in ContextPluginInformation plugin, in String contextType);
		
	/**
	 * Notifies the application that a new Context Plug-in is being installed.
	 * @param plugin The Context Plug-in being installed.
	 */
	void onInstallingContextPlugin(in ContextPluginInformation plugin);
	
	/**
	 * Notifies the application that a new Context Plug-in installation has progressed.
	 * @param plugin The Context Plug-in being installed.
	 * @param plugin The Context Plug-in installation percent complete.
	 */
	void onContextPluginInstallProgress(in ContextPluginInformation plugin, in int percentComplete);
	
	/**
	 * Notifies the application that a new Context Plug-in has been installed.
	 * @param plugin The Context Plug-in that was installed. 
	 */
	void onContextPluginInstalled(in ContextPluginInformation plugin);
	
	/**
	 * Notifies the application that a previously installed Context Plug-in has been uninstalled.
	 * @param plugin The Context Plug-in that was uninstalled. 
	 */
	void onContextPluginUninstalled(in ContextPluginInformation plugin);
	
	/**
	 * Notifies the application that a Context Plug-in has failed to install. 
	 *
	 * @param plugin The Context Plug-in that failed to install.
	 * @param message The message associated with the failure.
	 */
	void onContextPluginInstallFailed(in ContextPluginInformation plug, in String message);
	
	/**
	 * Notifies the application that a context request has failed.
	 *
	 * @param requestId The id of the context request.
	 * @param message The message associated with the failure.
	 * @param errorCode The error code associated with the failure.
	 */
	void onContextRequestFailed(in String requestId, in String message, in int errorCode);
	
	/**
	 * Notification that context plug-in discovery has started.
	 */	
	void onContextPluginDiscoveryStarted();
	
	/**
	 * Notification that context plug-in discovery has finished.
	 * @param discoveredPlugins The context plug-ins discovered (may be empty)
	 */		
	void onContextPluginDiscoveryFinished(in List<ContextPluginInformation> discoveredPlugins);
	
	/**
	 * Notification that the Dynamix Framework is active.
	 */
	void onDynamixFrameworkActive();
	
	/**
	 * Notification that the Dynamix Framework is inactive.
	 */
	void onDynamixFrameworkInactive();
	
	/**
	 * Notification that a Context Plug-in has encountered an error (and mostly likely has been deactivated).
	 */
	void onContextPluginError(in ContextPluginInformation plug, in String message);
}