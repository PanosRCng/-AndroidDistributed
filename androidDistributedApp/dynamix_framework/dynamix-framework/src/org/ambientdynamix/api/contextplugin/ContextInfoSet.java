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
package org.ambientdynamix.api.contextplugin;

import java.util.List;
import java.util.UUID;

import org.ambientdynamix.api.application.Expirable;
import org.ambientdynamix.api.contextplugin.PluginConstants.EventType;
import org.ambientdynamix.api.contextplugin.PluginConstants.LogPriority;
import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.util.Log;

/**
 * As ContextPluginRuntimes may support various PrivacyRisks, a ContextInfoSet provides a mechanism for the results of a
 * specific context modeling scan to be sent together to the Dynamix Framework. Each SecuredEvent within a
 * ContextInfoSet will typically be of a different PrivacyRisk. Dynamix routes the SecuredEvents within the
 * ContextInfoSet to registered applications based on their security profile and context support.
 * 
 * @author Darren Carlson
 */
public class ContextInfoSet extends Expirable {
	private static final long serialVersionUID = -6267867748921431783L;
	private static final String TAG = ContextInfoSet.class.getSimpleName();
	// Private data
	private List<SecuredContextInfo> securedContextInfo;
	private EventType eventType;
	private String implementingClassname;
	private UUID responseId;
	private String contextType;
	private String logMessage;
	private LogPriority priority;

	/**
	 * Creates a BROADCAST ContextInfoSet.
	 * 
	 * @param info
	 *            The group of associated SecuredContextInfo(typically, each has a different PrivacyRisk)
	 * @param responseId
	 *            The responseId of the specific request, which must match the associated requestId from the
	 *            'handleContextRequest' of the IReactiveContextPluginRuntime interface.
	 * @see IAutoContextPluginRuntime
	 */
	private ContextInfoSet(List<SecuredContextInfo> info, int expireMills) {
		super(expireMills);
		this.securedContextInfo = info;
		this.implementingClassname = info.get(0).getContextInfo().getImplementingClassname();
		this.contextType = info.get(0).getContextInfo().getContextType();
		eventType = EventType.BROADCAST;
	}

	/**
	 * Creates a UNICAST ContextInfoSet.
	 * 
	 * @param info
	 *            The group of associated SecuredContextInfo(typically, each has a different PrivacyRisk)
	 * @param responseId
	 *            The responseId of the specific request, which must match the associated requestId from the
	 *            'handleContextRequest' of the IReactiveContextPluginRuntime interface.
	 * @see IReactiveContextPluginRuntime
	 */
	private ContextInfoSet(List<SecuredContextInfo> info, UUID responseId, int expireMills) {
		super(expireMills);
		this.securedContextInfo = info;
		this.responseId = responseId;
		/*
		 * Note: Since this object validates the incoming SecuredContextInfo list before creation, the first element is
		 * guaranteed to be non-null and all elements will be of the same context type (but at different
		 * PrivacyRiskLevels). Just use the first element for reference.
		 */
		this.implementingClassname = info.get(0).getContextInfo().getImplementingClassname();
		this.contextType = info.get(0).getContextInfo().getContextType();
		eventType = EventType.UNICAST;
	}

	private ContextInfoSet(LogPriority priority, String logMessage) {
		super(-1);
		eventType = EventType.LOGGING;
		this.logMessage = logMessage;
	}

	/**
	 * Factory method that creates a BROADCAST ContextInfoSet, which is normally used by push-based
	 * ContextPluginRuntimes.
	 * 
	 * @param info
	 *            The group of associated SecuredContextInfo(typically, each has a different PrivacyRisk)
	 * @see IAutoContextPluginRuntime
	 */
	public static ContextInfoSet createBroadcastContextInfoSet(List<SecuredContextInfo> info) {
		if (verifyContextInfo(info))
			return new ContextInfoSet(info, -1);
		return null;
	}

	/**
	 * Factory method that creates a BROADCAST ContextInfoSet, which is normally used by push-based
	 * ContextPluginRuntimes.
	 * 
	 * @param info
	 *            The group of associated SecuredContextInfo(typically, each has a different PrivacyRisk)
	 * @param expireMills
	 *            How long until the ContextInfoSet expires
	 * @see IAutoContextPluginRuntime
	 */
	public static ContextInfoSet createBroadcastContextInfoSet(List<SecuredContextInfo> info, int expireMills) {
		if (verifyContextInfo(info))
			return new ContextInfoSet(info, expireMills);
		return null;
	}

	/**
	 * Factory method that creates a BROADCAST ContextInfoSet, which is normally used by push-based
	 * ContextPluginRuntimes.
	 * 
	 * @param info
	 *            The group of associated SecuredContextInfo(typically, each has a different PrivacyRisk)
	 * @see IAutoContextPluginRuntime
	 */
	public static ContextInfoSet createLoggingContextInfoSet(LogPriority priority, String logMessage) {
		return new ContextInfoSet(priority, logMessage);
	}

	/**
	 * Factory method that creates a non-expiring UNICAST ContextInfoSet, which is normally used by reactive
	 * ContextPluginRuntimes.
	 * 
	 * @param info
	 *            The group of associated SecuredContextInfo(typically, each has a different PrivacyRisk)
	 * @param responseId
	 *            The responseId of the specific request, which must match the associated requestId from the
	 *            'handleContextRequest' of the IReactiveContextPluginRuntime interface.
	 * @param expireMills
	 *            How long until the ContextInfoSet expires
	 * @see IReactiveContextPluginRuntime
	 */
	public static ContextInfoSet createUnicastContextInfoSet(List<SecuredContextInfo> info, UUID responseId) {
		if (verifyContextInfo(info))
			return new ContextInfoSet(info, responseId, -1);
		return null;
	}

	/**
	 * Factory method that creates an expiring UNICAST ContextInfoSet, which is normally used by reactive
	 * ContextPluginRuntimes.
	 * 
	 * @param info
	 *            The group of associated SecuredContextInfo(typically, each has a different PrivacyRisk)
	 * @param responseId
	 *            The responseId of the specific request, which must match the associated requestId from the
	 *            'handleContextRequest' of the IReactiveContextPluginRuntime interface.
	 * @param expireMills
	 *            How long until the ContextInfoSet expires
	 * @see IReactiveContextPluginRuntime
	 */
	public static ContextInfoSet createUnicastContextInfoSet(List<SecuredContextInfo> info, UUID responseId,
			int expireMills) {
		if (verifyContextInfo(info))
			return new ContextInfoSet(info, responseId, expireMills);
		return null;
	}

	/**
	 * Returns true if all SecuredContextInfo have the same context type; false otherwise.
	 */
	private static boolean verifyContextInfo(List<SecuredContextInfo> data) {
		if (data == null || data.size() == 0) {
			Log.w(TAG, "Null or zero ContextInfoSet in verifyContextInfo: " + data);
			return false;
		}
		String type = null;
		for (SecuredContextInfo sd : data) {
			// First time through, check and cache the context type
			if (type == null) {
				if (sd.getContextInfo() != null) {
					type = sd.getContextInfo().getContextType();
				} else {
					Log.w(TAG, "SecuredContextInfo contained a null IContextInfo... rejecting.");
					return false;
				}
			} else {
				if (sd.getContextInfo() != null) {
					if (!type.equalsIgnoreCase(sd.getContextInfo().getContextType())) {
						Log.w(TAG, "All SecuredContextInfo must have the same context type in SecuredContextInfo!");
						return false;
					}
				} else {
					Log.w(TAG, "SecuredContextInfo contained a null IContextInfo... rejecting.");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns the context type of the underlying IContextInfo entities (each will be of the same type).
	 */
	public String getContextType() {
		return contextType;
	}

	/**
	 * Returns the EventType of this ContextInfoSet (e.g. BROADCAST or UNICAST).
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * Returns the implementing classname of the embedded ContextInfoSet.
	 */
	public String getImplementingClassname() {
		return implementingClassname;
	}

	/**
	 * Returns the debug message (if the event is of type LOG).
	 */
	public String getLogMessage() {
		return logMessage;
	}

	/**
	 * Returns the log priority.
	 */
	public LogPriority getLogPriority() {
		return priority;
	}

	/**
	 * Returns the responseId if the event is of type UNICAST, or null if the event is of type BROADCAST.
	 */
	public UUID getResponseId() {
		return responseId;
	}

	/**
	 * Returns the List of SecuredContextInfo associated with this ContextInfoSet.
	 */
	public List<SecuredContextInfo> getSecuredContextInfo() {
		return securedContextInfo;
	}
}