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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Binder for trusted certificates.
 * 
 * @author Darren Carlson
 * 
 */
@Element(name = "trustedCert")
public class TrustedCertBinder {
	@Attribute
	String alias;
	@Attribute
	String name;
	@Attribute
	String fingerprint;
	@Element
	String url;

	/**
	 * Returns the unique alias name identifying this cert.
	 */
	public String getAlias() {
		return alias;
	}

	public String getName() {
		return name;
	}

	/**
	 * Returns the SHA-1 hash fingerprint of the cert with any ':' characters removed.
	 */
	public String getFingerprint() {
		return fingerprint.replace(":", "");
	}

	/**
	 * Returns the download url for the cert.
	 */
	public String getUrl() {
		return url;
	}
}
