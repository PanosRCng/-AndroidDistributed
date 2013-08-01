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

import java.io.FileDescriptor;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.contextplugin.PluginConstants;
import org.ambientdynamix.web.WebUtils;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

/**
 * Implementation of the IDynamixListener interface for web clients.
 * 
 * @author Darren Carlson
 * 
 */
public class WebListener implements IDynamixListener {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private WebConnector connector;
	private IBinder binder;
	private String webAppUrl;
	private String token;
	private boolean sessionOpen = false;
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Creates a WebListener for the specified web app.
	 * 
	 * @param webAppUrl
	 *            The web app's url.
	 * @param token
	 *            The web app's token.
	 * @param connector
	 *            The WebConnector.
	 */
	public WebListener(String webAppUrl, String token, WebConnector connector) {
		this.webAppUrl = webAppUrl;
		this.token = token;
		this.connector = connector;
		this.binder = new WebBinder(this.hashCode());
	}

	/**
	 * Returns the web app's url.
	 */
	public String getWebAppUrl() {
		return webAppUrl;
	}

	/**
	 * Returns the web app's id (hash of the web app's url).
	 */
	public int getWebAppId() {
		return webAppUrl.hashCode();
	}

	/**
	 * Returns true if the session is open; false otherwise.
	 */
	public boolean isSessionOpen() {
		return sessionOpen;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDynamixListenerAdded(String listenerId) throws RemoteException {
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onDynamixListenerAdded();}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDynamixListenerRemoved() throws RemoteException {
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onDynamixListenerRemoved();}catch(e){};");
	}

	public void onDynamixUnbind() {
		connector.sendEvent(this.token, "javascript:try{Dynamix.onDynamixUnbind();}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAwaitingSecurityAuthorization() throws RemoteException {
		connector
				.sendEvent(this.token, "javascript:try{DynamixListener.onAwaitingSecurityAuthorization();}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSecurityAuthorizationGranted() throws RemoteException {
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onSecurityAuthorizationGranted();}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSecurityAuthorizationRevoked() throws RemoteException {
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onSecurityAuthorizationRevoked();}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSessionOpened(String sessionId) throws RemoteException {
		sessionOpen = true;
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onSessionOpened();}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSessionClosed() throws RemoteException {
		sessionOpen = false;
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onSessionClosed();}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextEvent(ContextEvent event) throws RemoteException {
		//String pojoBuilder = "";
		
		try {
			// Setup a SimpleDateFormat for UTC-based ISO 8601 date/time formatting
			SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
			TimeZone tz = TimeZone.getTimeZone("UTC");
			timeFormatter.setTimeZone(tz);
			// Create a root node and basic event properties
			ObjectNode jNode = mapper.createObjectNode();
			jNode.put("sourcePluginId", event.getEventSource().getPluginId());
			jNode.put("responseId", event.getResponseId() == null ? "" : event.getResponseId());
			jNode.put("contextType", event.getContextType());
			jNode.put("implementingClassname", event.getIContextInfo().getImplementingClassname());
			jNode.put("timeStamp", timeFormatter.format(event.getTimeStamp()));
			jNode.put("expires", event.expires());
			jNode.put("expireTime", timeFormatter.format(event.getExpireTime()));
			
			/*
			 * Now add the properties from the IContextInfo. First, handle automatic web encoding, if requested. If the
			 * IContextInfo uses JavaBean standards, it can be automatically encoded into JSON. If no automatic web
			 * encoding is requested, then we use the
			 */
			JsonNode encodedNode;
			if (event.autoWebEncode()) {
				encodedNode = mapper.valueToTree(event.getIContextInfo());
				jNode.put("hasPojoData", true);
			} else {
				jNode.put("hasPojoData", false);
				if (!event.getWebEncodingFormat().equalsIgnoreCase(PluginConstants.NO_WEB_ENCODING)) {
					ObjectNode tmp = mapper.createObjectNode();
					tmp.put("encodedDataType", event.getWebEncodingFormat());
					tmp.put("encodedData", event.getStringRepresentation(event.getWebEncodingFormat()));
					encodedNode = tmp;
				} else {
					// This object cannot be sent via web serialization
					Log.w(TAG, "Event is configured with NO_WEB_ENCODING... ignoring");
					return;
				}
			}
			// Iterate through the IContextInfo fields, adding them to the event
			Iterator<String> itr = encodedNode.fieldNames();
			while (itr.hasNext()) {
				String field = itr.next();
				jNode.put(field, encodedNode.get(field));
			}
			
			String tmp = jNode.toString();//.textValue();
			// Send the event
			try {
				connector.sendEvent(this.token,
						"javascript:try{Dynamix.onContextEvent(" + jNode.toString()
								+ ");}catch(e){};");
			} catch (Exception e) {
				Log.w(TAG, "onContextEvent: " + e);
			}
			// Create the JavaScript reply
			// StringBuilder params = new StringBuilder();
			// params.append("javascript:try{Dynamix.onContextEvent('");
			// // To keep things lightweight, we are only sending plug-in id for context events
			// params.append(event.getEventSource().getPluginId());
			// params.append("','");
			// params.append(event.getContextType());
			// params.append("','");
			// params.append(event.getIContextInfo().getImplementingClassname());
			// params.append("','");
			// params.append(timeFormatter.format(event.getTimeStamp()));
			// params.append("','");
			// params.append(event.expires());
			// params.append("','");
			// params.append(timeFormatter.format(event.getExpireTime()));
			// params.append("','");
			// params.append(event.getResponseId() == null ? "" : event.getResponseId());
			// params.append("','");
			// params.append(encodedData);
			// params.append("','");
			// params.append(event.getAutoWebEncodingFormat());
			// params.append("','");
			// params.append(pojoBuilder);
			// params.append("');}catch(e){};");
			// connector.sendEvent(this.token, params.toString());
		} catch (Exception e) {
			Log.w(TAG, "onContextEvent: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextSupportAdded(ContextSupportInfo supportInfo) throws RemoteException {
		try {
			connector.sendEvent(this.token,
					"javascript:try{Dynamix.onContextSupportAdded(" + WebUtils.serializeObject(supportInfo)
							+ ");}catch(e){};");
		} catch (Exception e) {
			Log.w(TAG, "onContextSupportAdded: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextSupportRemoved(ContextSupportInfo supportInfo) throws RemoteException {
		try {
			connector.sendEvent(this.token,
					"javascript:try{Dynamix.onContextSupportRemoved(" + WebUtils.serializeObject(supportInfo)
							+ ");}catch(e){};");
		} catch (Exception e) {
			Log.w(TAG, "onContextSupportRemoved: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextTypeNotSupported(String contextType) throws RemoteException {
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onContextTypeNotSupported('" + contextType
				+ "');}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onInstallingContextSupport(ContextPluginInformation plugin, String contextType) throws RemoteException {
		try {
			connector.sendEvent(this.token,
					"javascript:try{Dynamix.onInstallingContextSupport(" + WebUtils.serializeObject(plugin) + ",'"
							+ contextType + "');}catch(e){};");
		} catch (Exception e) {
			Log.w(TAG, "onInstallingContextSupport: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onInstallingContextPlugin(ContextPluginInformation plugin) throws RemoteException {
		try {
			connector.sendEvent(this.token,
					"javascript:try{Dynamix.onInstallingContextPlugin(" + WebUtils.serializeObject(plugin)
							+ ");}catch(e){};");
		} catch (Exception e) {
			Log.w(TAG, "onInstallingContextPlugin: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextPluginInstallProgress(ContextPluginInformation plugin, int percentComplete)
			throws RemoteException {
		try {
			connector.sendEvent(this.token,
					"javascript:try{Dynamix.onContextPluginInstallProgress(" + WebUtils.serializeObject(plugin) + ",'"
							+ percentComplete + "');}catch(e){};");
		} catch (Exception e) {
			Log.w(TAG, "onContextPluginInstalled: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextPluginInstalled(ContextPluginInformation plugin) throws RemoteException {
		try {
			connector.sendEvent(this.token,
					"javascript:try{Dynamix.onContextPluginInstalled(" + WebUtils.serializeObject(plugin)
							+ ");}catch(e){};");
		} catch (Exception e) {
			Log.w(TAG, "onContextPluginInstalled: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextPluginUninstalled(ContextPluginInformation plugin) throws RemoteException {
		try {
			connector.sendEvent(this.token,
					"javascript:try{Dynamix.onContextPluginUninstalled(" + WebUtils.serializeObject(plugin)
							+ ");}catch(e){};");
		} catch (Exception e) {
			Log.w(TAG, "onContextPluginUninstalled: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextPluginInstallFailed(ContextPluginInformation plug, String message) throws RemoteException {
		try {
			connector.sendEvent(this.token,
					"javascript:try{Dynamix.onContextPluginInstallFailed(" + WebUtils.serializeObject(plug) + ",'"
							+ message + "');}catch(e){};");
		} catch (Exception e) {
			Log.w(TAG, "onContextPluginInstallFailed: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextRequestFailed(String requestId, String message, int errorCode) throws RemoteException {
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onContextRequestFailed('" + requestId + "','"
				+ message + "','" + errorCode + "');}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextPluginDiscoveryStarted() throws RemoteException {
		// Not supported for web clients yet
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextPluginDiscoveryFinished(List<ContextPluginInformation> discoveredPlugins)
			throws RemoteException {
		// Not supported for web clients yet
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDynamixFrameworkActive() throws RemoteException {
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onDynamixFrameworkActive();}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDynamixFrameworkInactive() throws RemoteException {
		connector.sendEvent(this.token, "javascript:try{DynamixListener.onDynamixFrameworkInactive();}catch(e){};");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onContextPluginError(ContextPluginInformation plug, String message) throws RemoteException {
		try {
			connector.sendEvent(this.token,
					"javascript:try{Dynamix.onContextPluginError(" + WebUtils.serializeObject(plug) + ",'" + message
							+ "');}catch(e){};");
		} catch (Exception e) {
			Log.w(TAG, "onContextPluginError: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBinder asBinder() {
		return binder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object candidate) {
		// First determine if they are the same object reference
		if (this == candidate)
			return true;
		// Make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		// Ok, they are the same class... check if their tokens are the same
		WebListener other = (WebListener) candidate;
		return this.token.equalsIgnoreCase(other.token);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + this.token.hashCode();
		return result;
	}

	/**
	 * IBinder implementation that enables the WebListener to be properly identified as a IDynamixListener by the
	 * Dynamix Framework.
	 * 
	 * @author Darren Carlson
	 * 
	 */
	private class WebBinder implements IBinder {
		// Private data
		private int id;

		public WebBinder(int id) {
			this.id = id;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object candidate) {
			// First determine if they are the same object reference
			if (this == candidate)
				return true;
			// Make sure they are the same class
			if (candidate == null || candidate.getClass() != getClass())
				return false;
			// Ok, they are the same class... check if their id's are the same
			WebBinder other = (WebBinder) candidate;
			return other.id == this.id ? true : false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + this.id;
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dump(FileDescriptor arg0, String[] arg1) throws RemoteException {
			// TODO Auto-generated method stub
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getInterfaceDescriptor() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isBinderAlive() {
			// Return true, since web app liveliness is determined by the WebConnector.
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void linkToDeath(DeathRecipient arg0, int arg1) throws RemoteException {
			// TODO Auto-generated method stub
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean pingBinder() {
			// TODO Auto-generated method stub
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IInterface queryLocalInterface(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean transact(int arg0, Parcel arg1, Parcel arg2, int arg3) throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean unlinkToDeath(DeathRecipient arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}
	}
}
