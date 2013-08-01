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
package org.ambientdynamix.update.contextplugin;

import org.simpleframework.xml.Element;
/**
 * Simple Framework binder class for XML parsing.
 * @author Darren Carlson
 *
 */
@Element(name = "search-results")
class NexusArtifactBinder {
	@Element(required = false)
	String resourceURI;
	@Element
	String groupId;
	@Element
	String artifactId;
	@Element
	String version;
	@Element(required = false)
	String packaging;
	@Element(required = false)
	String classifier;
	@Element(required = false) 
	String extension;
	@Element
	String repoId;
	@Element(name = "contextId")
	String repoName;
	@Element(required = false)
	String pomLink;
	@Element(required = false)
	String artifactLink;

	public String getId() {
		return groupId + "." + artifactId;
	}

	@Override
	public boolean equals(Object candidate) {
		// First determine if they are the same object reference
		if (this == candidate)
			return true;
		// Make sure they are the same class
		if (candidate != null || candidate.getClass() == getClass()) {
			NexusArtifactBinder other = (NexusArtifactBinder) candidate;
			// Make sure they have the same classifier
			if (classifier.equalsIgnoreCase(other.classifier)) {
				// Now check if their id's are the same
				String otherId = other.groupId + "." + other.artifactId;
				if (getId().equalsIgnoreCase(otherId)) {
					// Finally, check the versions
					if (version.equalsIgnoreCase(other.version))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + classifier.hashCode() + getId().hashCode() + version.hashCode();
		return result;
	}
}