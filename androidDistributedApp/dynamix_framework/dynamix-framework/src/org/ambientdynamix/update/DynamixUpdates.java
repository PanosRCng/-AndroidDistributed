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
package org.ambientdynamix.update;

import java.util.ArrayList;
import java.util.List;

/**
 * Updates for the Dynamix Framework.
 * @author Darren Carlson
 *
 */
public class DynamixUpdates {
	private List<TrustedCertBinder> trustedWebConnectorCerts = new ArrayList<TrustedCertBinder>();

	/**
	 * Returns the list of TrustedCertBinders for the WebConnector.
	 */
	public List<TrustedCertBinder> getTrustedWebConnectorCerts() {
		return trustedWebConnectorCerts;
	}
	/**
	 * Sets the list of TrustedCertBinders for the WebConnector.
	 */
	public void setTrustedWebConnectorCerts(List<TrustedCertBinder> trustedWebConnectorCerts) {
		this.trustedWebConnectorCerts = trustedWebConnectorCerts;
	}
	
}
