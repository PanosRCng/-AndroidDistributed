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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.PluginConstants.PLATFORM;
import org.ambientdynamix.util.RepositoryInfo;
import org.ambientdynamix.util.Utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

/**
 * IContextPluginConnector for accessing Context Plug-ins using Sonatype Nexus. Supports index_data and Lucene search
 * modes.
 * 
 * @author Darren Carlson
 * 
 */
public class NexusSource implements IContextPluginConnector {
	// Private data
	private String TAG = getClass().getSimpleName();
	private DefaultHttpClient client = new DefaultHttpClient();
	private RepositoryInfo[] nexusRepos;
	private HttpGet getRequest;
	private boolean cancel;

	public NexusSource(RepositoryInfo nexusServer) {
		this.nexusRepos = new RepositoryInfo[] { nexusServer };
	}

	public NexusSource(RepositoryInfo[] nexusServers) {
		this.nexusRepos = nexusServers;
	}

	@Override
	public void cancel() {
		cancel = true;
		try {
			if (getRequest != null)
				getRequest.abort();
		} catch (Exception e) {
			Log.w(TAG, "Exception while aborting getRequest: " + e.toString());
		}
	}

	@Override
	public synchronized List<DiscoveredContextPlugin> getContextPlugins(PLATFORM platform, VersionInfo platformVersion,
			VersionInfo frameworkVersion) throws Exception {
		cancel = false;
		List<DiscoveredContextPlugin> plugs = new ArrayList<DiscoveredContextPlugin>();
		for (RepositoryInfo repo : nexusRepos) {
			if (cancel)
				break;
			Log.i(TAG, "Checking for context plug-ins using: " + repo.getAlias());
			Log.i(TAG, "Repository URL is: " + repo.getUrl());
			Log.i(TAG, "Repository type is: " + repo.getType());
			if (repo.getType().equalsIgnoreCase(RepositoryInfo.NEXUS_INDEX_SOURCE))
				plugs.addAll(getPlugsFromIndexSearch(repo, platform, platformVersion, frameworkVersion));
			else if (repo.getType().equalsIgnoreCase(RepositoryInfo.NEXUS_LUCENE_SOURCE))
				plugs.addAll(getPlugsFromLuceneSearch(repo, platform, platformVersion, frameworkVersion));
			else
				Log.w(TAG, "Repo type not known: " + repo.getType());
		}
		return plugs;
	}

	/**
	 * Extracts plug-ins from a Nexus repo using the Lucene search method.
	 * 
	 * @See https://repository.sonatype.org/nexus-indexer-lucene-plugin/default/docs/rest.lucene.search.html
	 * @param repo
	 *            RepositoryInfo configured with a NEXUS_LUCENE_SOURCE type and associated url.
	 * @return A List of DiscoveredContextPlugin(s)
	 * @throws Exception
	 */
	private List<DiscoveredContextPlugin> getPlugsFromLuceneSearch(RepositoryInfo repo, PLATFORM platform,
			VersionInfo platformVersion, VersionInfo frameworkVersion) throws Exception {
		Serializer serializer = new Persister();
		String xmlData = retrieve(repo.getUrl());
		Reader reader = new StringReader(xmlData);
		List<DiscoveredContextPlugin> plugs = new ArrayList<DiscoveredContextPlugin>();
		NexusMavenLuceneResultsBinder searchResults = serializer.read(NexusMavenLuceneResultsBinder.class, reader,
				false);
		NexusNGRepositoryDetail repoDetails = searchResults.repoDetails.repoDetail;
		if (searchResults != null && searchResults.totalCount > 0) {
			// Grab Nexus' base url (may not be at {url}/nexus/)
			String nexusBaseUrl = repoDetails.repositoryURL.substring(0,
					repoDetails.repositoryURL.indexOf("/service/local/"));
			Log.d(TAG, "Nexus Repo Base URL: " + nexusBaseUrl);
			for (NexusNGArtifactBinder art : searchResults.data) {
				// Each art should be a plug-in, if it contains a plugin and metadata classifier
				for (NexusNgArtifactHitBinder hit : art.artifactHits) {
					DiscoveredContextPlugin plug = null;
					// Search for the metadata
					for (NexusArtifactLinkBinder link : hit.artifactLinks) {
						if (link.isMetadata()) {
							try {
								// Grab the metadata
								String metaXml = retrieve(makeRetrievalUrl(nexusBaseUrl, art, hit, link));
								Reader metaReader = new StringReader(metaXml);
								ContextPluginBinder metaResult = serializer.read(ContextPluginBinder.class, metaReader,
										false);
								metaReader.close();
								DiscoveredContextPlugin tmp = metaResult.createDiscoveredPlugin(repo);
								if (UpdateUtils.checkCompatibility(tmp.getContextPlugin(), platform, platformVersion,
										frameworkVersion)) {
									plug = tmp;
									Log.d(TAG, "Found Plugin Metadata for " + plug.getContextPlugin().getId()
											+ "... checking for JAR");
									break;
								}
							} catch (Exception e) {
								Log.w(TAG, "Problem creating plugin for " + art + ", exception was: " + e.toString());
							}
						}
					}
					// If we created a plugin, create the download link to the JAR
					if (plug != null) {
						boolean found = false;
						for (NexusArtifactLinkBinder link : hit.artifactLinks) {
							if (link.isPlugin()) {
								plug.getContextPlugin().setInstallUrl(makeRetrievalUrl(nexusBaseUrl, art, hit, link));
								if (!plugs.contains(plug)) {
									plugs.add(plug);
									Log.d(TAG, "Found Plugin JAR for " + plug.getContextPlugin().getId()
											+ "... adding");
									break;
								} else
									Log.w(TAG, "Plugin was already added... skipping");
							}
						}
						if (!found)
							Log.w(TAG, "Could not find JAR for " + art);
					}
				}
			}
		}
		return plugs;
	}

	/**
	 * Extracts plug-ins from a Nexus repo using the (deprecated) date_index search method.
	 * 
	 * @See https://repository.sonatype.org/nexus-indexer-lucene-plugin/default/docs/rest.data_index.html
	 * @param repo
	 *            RepositoryInfo configured with a NEXUS_INDEX_SOURCE type and associated url.
	 * @return A List of DiscoveredContextPlugin(s)
	 * @throws Exception
	 */
	private List<DiscoveredContextPlugin> getPlugsFromIndexSearch(RepositoryInfo repo, PLATFORM platform,
			VersionInfo platformVersion, VersionInfo frameworkVersion) throws Exception {
		Serializer serializer = new Persister();
		String repoId = Utils.getArgumentValueFromUrl(repo.getUrl(), "repositoryId");
		String xmlData = retrieve(repo.getUrl());
		Reader reader = new StringReader(xmlData);
		List<DiscoveredContextPlugin> plugs = new ArrayList<DiscoveredContextPlugin>();
		NexusSearchResultsBinder searchResults = serializer.read(NexusSearchResultsBinder.class, reader, false);
		if (searchResults != null && searchResults.totalCount > 0) {
			// Gather the metadata artifacts from the search results
			List<NexusArtifactBinder> metadata = new ArrayList<NexusArtifactBinder>();
			for (NexusArtifactBinder art : searchResults.data) {
				if (repoId != null && art.repoId.equalsIgnoreCase(repoId)) {
					if (art.classifier != null && art.classifier.equalsIgnoreCase("metadata"))
						if (!metadata.contains(art))
							metadata.add(art);
						else
							Log.w(TAG, "Already stored metadata for ID: " + art.getId());
				}
			}
			// Next gather the plugin artifacts for each associated metadata artifact
			Map<NexusArtifactBinder, NexusArtifactBinder> metaMap = new HashMap<NexusArtifactBinder, NexusArtifactBinder>();
			for (NexusArtifactBinder meta : metadata) {
				for (NexusArtifactBinder art : searchResults.data) {
					if (repoId != null && art.repoId.equalsIgnoreCase(repoId)) {
						if (art.classifier != null && art.classifier.equalsIgnoreCase("plugin")
								&& art.getId().equalsIgnoreCase(meta.getId())
								&& art.version.equalsIgnoreCase(meta.version)) {
							if (!metaMap.keySet().contains(meta))
								metaMap.put(meta, art);
							else
								Log.w(TAG, "Already stored plugin for ID: " + meta.getId());
							break;
						}
					}
				}
			}
			/*
			 * Now create the plug-ins using the metadata and the download URIs from the artifacts
			 */
			for (NexusArtifactBinder metaBinder : metaMap.keySet()) {
				/*
				 * Steps: 1. Download the metadata file 2. De-serialize it to create a template 3. Use the values from
				 * the template to construct a ContextPlugin 4. Use the associated pluging uri to update the
				 * ContextPlugin download link
				 */
				NexusArtifactBinder plugBinder = metaMap.get(metaBinder);
				if (plugBinder != null) {
					try {
						String metaXml = retrieve(metaBinder.resourceURI);
						Reader metaReader = new StringReader(metaXml);
						ContextPluginBinder metaResult = serializer.read(ContextPluginBinder.class, metaReader, false);
						metaReader.close();
						if (metaResult.repoType.equals("nexus-rest")) {
							metaResult.installUrl = plugBinder.resourceURI;
							metaResult.updateUrl = plugBinder.artifactLink;
						}
						DiscoveredContextPlugin plug = metaResult.createDiscoveredPlugin(repo);
						if (UpdateUtils.checkCompatibility(plug.getContextPlugin(), platform, platformVersion,
								frameworkVersion)) {
							Log.d(TAG, "Found Plugin: " + plug.getContextPlugin().getId() + " with URL: "
									+ plug.getContextPlugin().getInstallUrl());
							if (!plugs.contains(plug))
								plugs.add(plug);
							else
								Log.w(TAG, "Plugin was already added... skipping");
						}
					} catch (Exception e) {
						Log.w(TAG, "Problem parsing plugin metadata for: " + metaBinder.getId() + " | " + e.toString());
					}
				} else
					Log.w(TAG, "Could not find plugBinder for: " + metaBinder);
			}
		}
		return plugs;
	}

	/**
	 * Utility for downloading XML-based resources using HttpGet.
	 * 
	 * @param url
	 *            The URL of the XML resource to download.
	 * @return The XML resource as a string, or null if the resource could not be downloaded.
	 */
	private String retrieve(String url) {
		if (!cancel) {
			getRequest = new HttpGet(url);
			try {
				HttpResponse getResponse = client.execute(getRequest);
				final int statusCode = getResponse.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					Log.w(getClass().getSimpleName(), "HTTP fail with status code: " + statusCode);
					return null;
				}
				HttpEntity getResponseEntity = getResponse.getEntity();
				if (getResponseEntity != null) {
					return EntityUtils.toString(getResponseEntity);
				}
			} catch (IOException e) {
				getRequest.abort();
				Log.w(TAG, "Error for URL " + url, e);
			}
		}
		return null;
	}

	/**
	 * Utility for creating a Nexus retrieval url
	 * 
	 * @see https://repository.sonatype.org/nexus-core-documentation-plugin/core/docs/rest.artifact.maven.content.html
	 * @param nexusBaseUrl
	 *            The base url of the Nexus instance
	 * @param art
	 *            The NexusNGArtifactBinder
	 * @param hit
	 *            The NexusNgArtifactHitBinder
	 * @param link
	 *            The NexusArtifactLinkBinder
	 * @return A retrieval url for the artifact
	 */
	private String makeRetrievalUrl(String nexusBaseUrl, NexusNGArtifactBinder art, NexusNgArtifactHitBinder hit,
			NexusArtifactLinkBinder link) {
		return nexusBaseUrl + "/service/local/artifact/maven/content?r=" + hit.repositoryId + "&g=" + art.groupId
				+ "&a=" + art.artifactId + "&v=" + art.version + "&c=" + link.classifier + "&e=" + link.extension;
	}
}
