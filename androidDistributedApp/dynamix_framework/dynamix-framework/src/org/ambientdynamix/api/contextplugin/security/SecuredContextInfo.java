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
package org.ambientdynamix.api.contextplugin.security;

import org.ambientdynamix.api.application.IContextInfo;
import org.ambientdynamix.api.contextplugin.PluginConstants;

/**
 * SecuredContextInfo represents an IContextInfo that has been associated to a PrivacyRisk, providing a mechanism for
 * the Dynamix Framework to provision ContextEvents to clients based on privacy profiles. PrivacyRiskLevel information
 * is not stored in a ContextEvent so that security information (i.e. the associated PrivacyRisk) can be hidden from
 * Dynamix Framework clients (clients receive only ContextEvents without PrivacyRiskLevel information).
 * 
 * @author Darren Carlson
 */
public class SecuredContextInfo implements Comparable<SecuredContextInfo> {
	// Private data
	private PrivacyRiskLevel level;
	private IContextInfo contextInfo;
	private boolean autoWebEncode = true;
	private String webEncodingFormat = PluginConstants.JSON_WEB_ENCODING;

	/**
	 * Default constructor
	 */
	public SecuredContextInfo() {
	}

	/**
	 * Creates a SecuredContextData without Web encoding.
	 * 
	 * @param contextInfo
	 *            The IContextInfo associated with this SecuredEvent.
	 * @param level
	 *            The PrivacyRiskLevel associated with this SecuredContextInfo.
	 */
	public SecuredContextInfo(IContextInfo contextInfo, PrivacyRiskLevel level) {
		this.contextInfo = contextInfo;
		this.level = level;
	}

	/**
	 * Creates a SecuredContextInfo.
	 * 
	 * @param contextInfo
	 *            The IContextInfo associated with this SecuredEvent.
	 * @param level
	 *            The PrivacyRiskLevel associated with this SecuredContextInfo.
	 * 
	 * @param autoWebEncode
	 *            If true, Dynamix will attempt to automatically encode the IContextInfo object for Web clients. Note
	 *            that IContextInfo objects MUST adhere to JavaBean conventions for auto-web-encoding to succeed:
	 *            http://en.wikipedia.org/wiki/JavaBeans#JavaBean_conventions
	 */
	public SecuredContextInfo(IContextInfo contextInfo, PrivacyRiskLevel level, boolean autoWebEncode) {
		this.contextInfo = contextInfo;
		this.level = level;
		this.autoWebEncode = autoWebEncode;
		this.webEncodingFormat = PluginConstants.JSON_WEB_ENCODING;
	}
	
	/**
	 * Creates a SecuredContextInfo using a plug-in specified web encoding format.
	 * 
	 * @param contextInfo
	 *            The IContextInfo associated with this SecuredEvent.
	 * @param level
	 *            The PrivacyRiskLevel associated with this SecuredContextInfo.
	 * 
	 * @param webEncodingFormat
	 *            The plug-in's web encoding format. Dynamix will NOT auto-web-encode the IContextData in this case. 
	 *            Rather, the IContextInfo's string-based representation format matching the specified webEncodingFormat
	 *            will be used directly. If the IContextInfo doesn't provide the specified webEncodingFormat,
	 *            no data will be web encoded. See IContextInfo.getStringRepresentation(String format);
	 *            
	 */
	public SecuredContextInfo(IContextInfo contextInfo, PrivacyRiskLevel level, String webEncodingFormat) {
		this.contextInfo = contextInfo;
		this.level = level;
		this.autoWebEncode = false;
		this.webEncodingFormat = webEncodingFormat;
	}

	/**
	 * Supports comparisons based on PrivacyRisk
	 */
	@Override
	public int compareTo(SecuredContextInfo another) {
		return this.level.compareTo(another.getPrivacyRisk());
	}

	/**
	 * Returns the IContextInfo associated with this SecuredContextData.
	 */
	public IContextInfo getContextInfo() {
		return contextInfo;
	}

	/**
	 * Returns the PrivacyRiskLevel associated with the SecuredContextData's IContextInfo.
	 */
	public PrivacyRiskLevel getPrivacyRisk() {
		return level;
	}

	/**
	 * Returns the requested auto web encoding format.
	 */
	public String getWebEncodingFormat() {
		return this.webEncodingFormat;
	}

	/**
	 * Returns true if Dynamix should perform auto-web-encoding for the event.
	 */
	public boolean autoWebEncode() {
		return autoWebEncode;
	}
}