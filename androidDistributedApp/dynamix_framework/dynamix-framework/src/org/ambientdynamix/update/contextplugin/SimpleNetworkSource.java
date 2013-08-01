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

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.PluginConstants.PLATFORM;
import org.ambientdynamix.util.RepositoryInfo;

import android.util.Log;

/**
 * Handles plug-in discovery from specific repository sources. This class does not support Dynamix boot-strapping.
 * 
 * @author Darren Carlson
 */
public class SimpleNetworkSource  extends SimpleSourceBase implements IContextPluginConnector, Serializable {
	// Private data
	private static final long serialVersionUID = 876374968867546657L;
	private final String TAG = this.getClass().getSimpleName();
	private List<RepositoryInfo> repositoryServers = new Vector<RepositoryInfo>();
	private boolean cancel;

	public SimpleNetworkSource() {
	}

	/**
	 * Creates a SimpleNetworkSource using the specified list of repository servers.
	 * 
	 * @param repositoryServers
	 *            A list of repository URLs.
	 */
	public SimpleNetworkSource(List<RepositoryInfo> repositoryServers) {
		if (repositoryServers != null)
			this.repositoryServers.addAll(repositoryServers);
	}

	/**
	 * Creates a SimpleNetworkSource using a specific repository server.
	 * 
	 * @param repositoryServer
	 *            The URL of the repository server.
	 */
	public SimpleNetworkSource(RepositoryInfo repositoryServer) {
		if (repositoryServer != null)
			repositoryServers.add(repositoryServer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() {
		cancel = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		else
			return true;
	}

	/**
	 * Discovers plug-ins using the specified repository URL(s). This class automatically fails-over to the next
	 * repository server in the list if a particular server is unavailable.
	 */
	@Override
	public List<DiscoveredContextPlugin> getContextPlugins(PLATFORM platform, VersionInfo platformVersion,
			VersionInfo frameworkVersion) throws Exception {
		List<DiscoveredContextPlugin> updates = new Vector<DiscoveredContextPlugin>();
		List<ContextPlugin> plugs = new Vector<ContextPlugin>();
		cancel = false;
		InputStream stream = null;
		for (RepositoryInfo repo : repositoryServers) {
			if (cancel)
				break;
			try {
				Log.i(TAG, "Checking for context plug-ins using: " + repo.getAlias());
				Log.i(TAG, "Repository URL is: " + repo.getUrl());
				URL server = new URL(repo.getUrl());
				stream = server.openStream();
				List<DiscoveredContextPlugin> tmp = createDiscoveredPlugins(repo, stream, platform,
						platformVersion, frameworkVersion, false);
				for (DiscoveredContextPlugin update : tmp) {
					if (cancel)
						break;
					if (!plugs.contains(update.getContextPlugin())) {
						plugs.add(update.getContextPlugin());
						updates.add(update);
					}
				}
			} catch (Exception e) {
				Log.w(TAG, e);
				updates.add(new DiscoveredContextPlugin(e.toString()));
			} finally {
				if (stream != null)
					stream.close();
			}
		}
		return updates;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + this.getClass().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return TAG;
	}
}