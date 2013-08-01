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

import org.ambientdynamix.util.RepositoryInfo;

/**
 * Creates IContextPluginConnectors using the incoming RepositoryInfo.
 * @author Darren Carlson
 *
 */
public class ContextPluginConnectorFactory {
	// Singleton constructor
	private ContextPluginConnectorFactory() {
	}



	public static IContextPluginConnector makeContextPluginConnector(RepositoryInfo repo) throws Exception {
		if (repo == null)
			throw new Exception("RepositoryInfo cannot be null");
		String type = repo.getType();
		if (type == null || type.length() == 0)
			throw new Exception("Repository type must be provided");
		type = type.trim();
		if (repo.getType().equalsIgnoreCase(RepositoryInfo.SIMPLE_FILE_SOURCE))
			return new SimpleFileSource(repo);
		if (repo.getType().equalsIgnoreCase(RepositoryInfo.SIMPLE_NETWORK_SOURCE))
			return new SimpleNetworkSource(repo);
		if (repo.getType().equalsIgnoreCase(RepositoryInfo.NEXUS_LUCENE_SOURCE))
			return new NexusSource(repo);
		if (repo.getType().equalsIgnoreCase(RepositoryInfo.NEXUS_INDEX_SOURCE))
			return new NexusSource(repo);
		throw new Exception("Repository type not known");
	}
}
