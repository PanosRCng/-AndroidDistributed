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
package org.ambientdynamix.security;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.ambientdynamix.util.Utils;

/**
 * Information about a trusted certificate.
 * 
 * @author Darren Carlson
 * 
 */
public class TrustedCert {
	private String alias;
	private String fingerprint; // SHA-1 hash of the cert
	private X509Certificate cert;

	public TrustedCert(String alias, X509Certificate cert) throws CertificateEncodingException,
			NoSuchAlgorithmException {
		this.alias = alias;
		this.cert = cert;
		fingerprint = Utils.getFingerprint(cert);
	}

	/**
	 * Returns the unique alias name identifying this cert.
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Returns the SHA-1 hash fingerprint of the cert.
	 */
	public String getFingerprint() {
		return fingerprint;
	}

	/**
	 * Returns the X509Certificate.
	 */
	public X509Certificate getCert() {
		return cert;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + this.cert.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		else {
			TrustedCert other = (TrustedCert) candidate;
			return this.cert.equals(other.getCert());
		}
	}
}
