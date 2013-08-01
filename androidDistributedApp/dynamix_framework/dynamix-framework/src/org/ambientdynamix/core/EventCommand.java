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
package org.ambientdynamix.core;

import java.util.List;

import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.IDynamixListener;

import android.util.Log;

/**
 * Event sending encapsulation based on the command pattern. By abstracting event sending in this way, we are able to
 * simplify the process of sending events to registered Dynamix applications using the ContextManager. IEventCommands
 * can be sent to a filterable list of Dynamix applications through a single ContextManager method named
 * 'sendEventCommand'. The IEventCommand provides pre-processing, event sending and post processing logic.
 * <p>
 * Note that the abstract version of this class (EventCommand) does nothing for each IEventCommand, allowing specific
 * extensions of EventCommand to override only the methods needed.
 * 
 * @author Darren Carlson
 * @see ContextManager
 */
abstract class EventCommand extends IEventCommand {
	public final static String TAG = EventCommand.class.getSimpleName();

	/**
	 * Returns true if the listener's IDynamixListener's Binder is alive; false otherwise.
	 */
	private static boolean isBinderAlive(IDynamixListener listener) {
		if (listener != null)
			if (listener.asBinder() != null)
				return listener.asBinder().isBinderAlive();
		Log.w(TAG, "Binder is dead for: " + listener);
		return false;
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void preProcess() {
	}

	@Override
	public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
	}

	/**
	 * EventCommand class used to process 'onAwaitingSecurityAuthorization' events.
	 */
	public static class AwaitingSecurityAuthorization extends EventCommand {
		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onAwaitingSecurityAuthorization();
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to check for application liveliness. Command checks if the listener's IBinder is alive,
	 * and pings the app if it is.
	 */
	public static class CheckAppLiveliness extends EventCommand {
		@Override
		public void processCommand(DynamixSession session, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				if (session.isSessionOpen())
					session.getApp().pingConnected();
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onContextSupportAdded' events.
	 */
	public static class ContextSupportAdded extends EventCommand {
		protected ContextSupportInfo supportInfo;

		public ContextSupportAdded(ContextSupportInfo supportInfo) {
			this.supportInfo = supportInfo;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onContextSupportAdded(supportInfo);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onContextSupportRemoved' events.
	 */
	public static class ContextSupportRemoved extends EventCommand {
		protected ContextSupportInfo supportInfo;

		public ContextSupportRemoved(ContextSupportInfo supportInfo) {
			this.supportInfo = supportInfo;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onContextSupportRemoved(supportInfo);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onContextInfoTypeNotSupported' events.
	 */
	public static class ContextTypeNotSupported extends EventCommand {
		protected String contextInfoType;

		public ContextTypeNotSupported(String contextInfoType) {
			this.contextInfoType = contextInfoType;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onContextTypeNotSupported(contextInfoType);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onContextPluginInstalled' events.
	 */
	public static class ContextPluginInstalled extends EventCommand {
		private ContextPluginInformation plug;

		public ContextPluginInstalled(ContextPluginInformation plugin) {
			this.plug = plugin;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onContextPluginInstalled(plug);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}
	
	/**
	 * EventCommand class used to process 'onContextPluginInstalled' events.
	 */
	public static class ContextPluginInstallFailed extends EventCommand {
		private ContextPluginInformation plug;
		private String message;

		public ContextPluginInstallFailed(ContextPluginInformation plugin, String message) {
			this.plug = plugin;
			this.message = message;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onContextPluginInstallFailed(plug, message);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onContextPluginUninstalled' events.
	 */
	public static class ContextPluginUninstalledCommand extends EventCommand {
		private ContextPluginInformation plug;

		public ContextPluginUninstalledCommand(ContextPluginInformation plugin) {
			this.plug = plugin;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onContextPluginUninstalled(plug);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onContextRequestFailed' events.
	 */
	public static class ContextRequestFailed extends EventCommand {
		private String requestId;
		private String message;
		private int errorCode;

		public ContextRequestFailed(String requestId, String message, int errorCode) {
			this.requestId = requestId;
			this.message = message;
			this.errorCode = errorCode;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onContextRequestFailed(requestId, message, errorCode);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onInstallingContextPlugin' events.
	 */
	public static class ContextPluginInstalling extends EventCommand {
		private ContextPluginInformation plug;

		public ContextPluginInstalling(ContextPluginInformation plugin) {
			this.plug = plugin;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onInstallingContextPlugin(plug);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}
	
	/**
	 * EventCommand class used to process 'onContextPluginInstallProgress' events.
	 */
	public static class ContextPluginInstallProgress extends EventCommand {
		private ContextPluginInformation plug;
		private int percentComplete;

		public ContextPluginInstallProgress(ContextPluginInformation plugin, int percentComplete) {
			this.plug = plugin;
			this.percentComplete = percentComplete;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onContextPluginInstallProgress(plug, percentComplete);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}	

	/**
	 * EventCommand class used to process 'onInstallingContextSupport' events.
	 */
	public static class InstallingContextSupport extends EventCommand {
		protected ContextPluginInformation plugin;
		protected String contextInfoType;

		public InstallingContextSupport(ContextPluginInformation plugin, String contextInfoType) {
			this.plugin = plugin;
			this.contextInfoType = contextInfoType;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onInstallingContextSupport(plugin, contextInfoType);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onListenerAdded' events.
	 */
	public static class ListenerAddedCommand extends EventCommand {
		private String listenerId;

		public ListenerAddedCommand(String listenerId) {
			this.listenerId = listenerId;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onDynamixListenerAdded(listenerId);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onListenerRemoved' events.
	 */
	public static class ListenerRemovedCommand extends EventCommand {
		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onDynamixListenerRemoved();
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onSecurityAuthorizationGranted' events.
	 */
	public static class SecurityAuthorizationGranted extends EventCommand {
		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onSecurityAuthorizationGranted();
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onSecurityAuthorizationRevoked' events.
	 */
	public static class SecurityAuthorizationRevoked extends EventCommand {
		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onSecurityAuthorizationRevoked();
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onSessionClosed' events.
	 */
	public static class SessionClosedCommand extends EventCommand {
		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			try {
				if (isBinderAlive(listener))
					listener.onSessionClosed();
			} catch (Exception e) {
				Log.w(TAG, "SessionClosedCommand Exception: " + e.toString());
			}
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	/**
	 * EventCommand class used to process 'onSessionOpened' events
	 */
	public static class SessionOpenedCommand extends EventCommand {
		private String sessionId;

		public SessionOpenedCommand(String sessionId) {
			this.sessionId = sessionId;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onSessionOpened(sessionId);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}
	
	/**
	 * EventCommand class used to process 'onContextPluginDiscoveryStarted' events.
	 */
	public static class ContextPluginDiscoveryStartedCommand extends EventCommand {
		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			try {
				if (isBinderAlive(listener))
					listener.onContextPluginDiscoveryStarted();
			} catch (Exception e) {
				Log.w(TAG, "ContextPluginDiscoveryStartedCommand Exception: " + e.toString());
			}
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}
	
	/**
	 * EventCommand class used to process 'onContextPluginDiscoveryFinished' events.
	 */
	public static class ContextPluginDiscoveryFinishedCommand extends EventCommand {
		List<ContextPluginInformation> discoveredPlugins;
		
		public ContextPluginDiscoveryFinishedCommand(List<ContextPluginInformation> discoveredPlugins){
			this.discoveredPlugins = discoveredPlugins;
		}
		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			try {
				if (isBinderAlive(listener))
					listener.onContextPluginDiscoveryFinished(discoveredPlugins);
			} catch (Exception e) {
				Log.w(TAG, "ContextPluginDiscoveryFinishedCommand Exception: " + e.toString());
			}
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}
	
	/**
	 * EventCommand class used to process 'onDynamixFrameworkActive' events.
	 */
	public static class DynamixFrameworkActiveCommand extends EventCommand {
		List<ContextPluginInformation> discoveredPlugins;
		
		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			try {
				if (isBinderAlive(listener))
					listener.onDynamixFrameworkActive();
			} catch (Exception e) {
				Log.w(TAG, "onDynamixFrameworkActive Exception: " + e.toString());
			}
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}
	
	/**
	 * EventCommand class used to process 'onDynamixFrameworkInactive' events.
	 */
	public static class DynamixFrameworkInactiveCommand extends EventCommand {
		List<ContextPluginInformation> discoveredPlugins;
		
		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			try {
				if (isBinderAlive(listener))
					listener.onDynamixFrameworkInactive();
			} catch (Exception e) {
				Log.w(TAG, "onDynamixFrameworkInactive Exception: " + e.toString());
			}
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}
	
	/**
	 * EventCommand class used to process 'onContextPluginError' events.
	 */
	public static class ContextPluginErrorCommand extends EventCommand {
		protected ContextPluginInformation plugin;
		protected String errorMessage;

		public ContextPluginErrorCommand(ContextPluginInformation plugin, String errorMessage) {
			this.plugin = plugin;
			this.errorMessage = errorMessage;
		}

		@Override
		public void processCommand(DynamixSession app, IDynamixListener listener) throws Exception {
			if (isBinderAlive(listener))
				listener.onContextPluginError(plugin, errorMessage);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}
}