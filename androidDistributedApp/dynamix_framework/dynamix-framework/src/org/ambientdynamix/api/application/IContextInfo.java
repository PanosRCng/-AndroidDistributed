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

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import android.os.Parcelable;

/**
 * IContextInfo represents a single entity of contextual information. Concrete IContextInfo implementations provide
 * native, 'high resolution' contextual information appropriate to the given modeling domain. The IContextInfo interface
 * may also include string versions of their data type (e.g. a fully qualified class name), which can be used by capable
 * clients to cast generic IContextInfo as concrete object types. The string-based representation of the IContextInfo
 * should be encoded using a well-known string-based format, and described in the plug-in's developer documentation.
 * Clients use the getStringRepresentationFormat method to locate a suitable parser for string-based representations.
 * <p>
 * The string data is used by clients who may not have access to the given IContextInfo implementation, but are still
 * interested in working with string representations of the data. For example, a ContextEvent containing a
 * java.util.Date may provide an ISO 8601 date format string that clients can parse without needing to have
 * java.util.Date on their class-path. In this case, the string-based format type for the encoded ISO 8601 could be
 * specified using a suitable Dublin Core Date element (e.g. 'http://purl.org/dc/elements/1.1/date').
 * <p>
 * Note: This interface extends Parcelable, which forces implementers to provide the necessary methods for serializing
 * data according to the AIDL (Android Interface Definition Language). Details at
 * http://developer.android.com/guide/developing/tools/aidl.html.
 * 
 * @author Darren Carlson
 */
public interface IContextInfo extends Parcelable {
	/**
	 * Returns the type of the context information represented by the IContextInfo. This string must match one of the
	 * supported context information type strings described by the source ContextPlugin.
	 */
	public String getContextType();

	/**
	 * Returns the fully qualified class-name of the class implementing the IContextInfo interface. This allows Dynamix
	 * applications to dynamically cast IContextInfo objects to their original type using reflection. A Java
	 * "instanceof" compare can also be used for this purpose.
	 */
	public String getImplementingClassname();

	/**
	 * Returns a string-based representation of the IContextInfo for the specified format string (e.g.
	 * "application/json") or null if the requested format is not supported.
	 */
	@JsonIgnore
	public String getStringRepresentation(String format);

	/**
	 * Returns a Set of supported string-based context representation format types or null if no representation formats
	 * are supported. Examples formats could include MIME, Dublin Core, RDF, etc. See the plug-in documentation for
	 * supported representation types.
	 */
	@JsonIgnore
	public Set<String> getStringRepresentationFormats();
}