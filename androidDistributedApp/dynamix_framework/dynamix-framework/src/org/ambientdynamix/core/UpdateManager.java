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
package org.ambientdynamix.core;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.ContextPlugin;
import org.ambientdynamix.api.contextplugin.DynamixFeatureInfo;
import org.ambientdynamix.api.contextplugin.PluginConstants;
import org.ambientdynamix.data.DynamixPreferences;
import org.ambientdynamix.event.PluginDiscoveryResult;
import org.ambientdynamix.update.DynamixUpdates;
import org.ambientdynamix.update.DynamixUpdatesBinder;
import org.ambientdynamix.update.contextplugin.ContextPluginConnectorFactory;
import org.ambientdynamix.update.contextplugin.DiscoveredContextPlugin;
import org.ambientdynamix.update.contextplugin.IContextPluginConnector;
import org.ambientdynamix.update.contextplugin.NexusSource;
import org.ambientdynamix.update.contextplugin.SimpleNetworkSource;
import org.ambientdynamix.util.RepositoryInfo;
import org.ambientdynamix.util.Utils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Manages update discovery and notification for the Dynamix Framework. Currently, this class only discovers updates for
 * ContextPlugins; however, long term, all dynamically updatable parts of the framework will be managed through this
 * class.
 * <p>
 * ContextPlugin XML descriptions must adhere to the specification as described in the Dynamix developer documentation.
 * The class will automatically use a backup server (if provided) if access to the primary update server is fails
 * (automatic failover).
 * 
 * @author Darren Carlson
 */
class UpdateManager {
	// Private data
	private static final String TAG = UpdateManager.class.getSimpleName();
	private static IContextPluginConnector currentSource;
	private static volatile boolean cancelled;
	private static volatile boolean processingContextPluginUpdates;

	// Singleton constructor
	private UpdateManager() {
	}

	public static synchronized void checkForDynamixUpdates(final Context c, final String updateUrl,
			final IDynamixUpdateListener listener) {
		// Assume we're allowed to update
		boolean updateAllowed = true;
		/*
		 * Don't allow updates if the user specified WIFI only, but WIFI is not connected. Note that if Dynamix is
		 * embedded, updates are always allowed.
		 */
		if (!DynamixService.isEmbedded() && (DynamixPreferences.useWifiNetworkOnly(c) && !Utils.isWifiConnected(c))) {
			Log.w(TAG, "Update not allowed since WIFI is not connected");
			updateAllowed = false;
		}
		
		if (updateAllowed) {
			Utils.dispatch(new Runnable() {
				@Override
				public void run() {
					Log.i(TAG, "Starting Dynamix Framework Update....");
					listener.onUpdateStarted();
					try {
						URL server = new URL(updateUrl);
						InputStream input = server.openStream();
						Serializer serializer = new Persister();
						SAXReader reader = new SAXReader(); // dom4j SAXReader
						/*
						 * TODO: Using the dom4j Document here, since it can load input from a variety of sources
						 * automatically (file and network). We could explore providing our own low-overhead transport
						 * mechanisms, if the Document gets too heavy.
						 */
						Document document = reader.read(input);
						String xml = document.asXML();
						input.close();
						Reader metaReader = new StringReader(xml);
						DynamixUpdatesBinder updatesBinder = serializer.read(DynamixUpdatesBinder.class, metaReader,
								false);
						DynamixUpdates updates = new DynamixUpdates();
						updates.setTrustedWebConnectorCerts(updatesBinder.getTrustedWebConnectorCerts());
						listener.onUpdateComplete(updates);
					} catch (Exception e) {
						Log.w(TAG, "Dynamix Update Failed: " + e);
						listener.onUpdateError(e.getMessage());
					}
				}
			});
		} else {
			BaseActivity.toast("use AndroidDistributed", Toast.LENGTH_LONG);
			Log.w(TAG, "Updated cancelled");
			listener.onUpdateCancelled();
		}
	}

	/**
	 * Cancels an existing update operation initiated by 'checkForContextPluginUpdates'.
	 */
	public static void cancelContextPluginUpdate() {
		Log.d(TAG, "cancelContextPluginUpdate");
		cancelled = true;
		if (currentSource != null) {
			currentSource.cancel();
		}
		currentSource = null;
	}

	/**
	 * Asynchronously checks for context plugin updates using the incoming server(s), notifying the specified
	 * IUpdateStatusListener with results (or errors).
	 * 
	 * @param plugSources
	 *            The List of IContextPluginConnector entities to check for updates
	 * @param platform
	 *            The device platform
	 * @param platformVersion
	 *            The device platform version
	 * @param frameworkVersion
	 *            The Dynamix version
	 * @param handler
	 *            The IUpdateStatusListener to notify with results (or errors)
	 */
	public static synchronized void checkForContextPluginUpdates(final Context c,
			final List<IContextPluginConnector> plugSources, final PluginConstants.PLATFORM platform,
			final VersionInfo platformVersion, final VersionInfo frameworkVersion,
			final IContextPluginUpdateListener callback, final FeatureInfo[] availableFeatures) {
		/*
		 * Don't allow network updates if the user specified WIFI only and WIFI is not connected. Note that if Dynamix
		 * is embedded, updates are always allowed.
		 */
		boolean networkUpdateAllowed = true;
		if (!DynamixService.isEmbedded() && (DynamixPreferences.useWifiNetworkOnly(c) && !Utils.isWifiConnected(c))) {
			Log.w(TAG, "Update not allowed since WIFI is not connected");
			networkUpdateAllowed = false;
		}
		if (!processingContextPluginUpdates) {
			Log.d(TAG, "Checking for plug-in updates with source count: " + plugSources.size());
			// Handle notifications
			SessionManager.notifyAllContextPluginDiscoveryStarted();
			// Setup state
			processingContextPluginUpdates = true;
			cancelled = false;
			final boolean networkUpdateAllowedFinal = networkUpdateAllowed;
			Utils.dispatch(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "Started checking for contect plug-in updates...");
					// Stop the Dynamix update timer
					DynamixService.stopContextPluginUpdateTimer();
					// Notify the callback that we've started the update
					if (callback != null) {
						Utils.dispatch(new Runnable() {
							@Override
							public void run() {
								callback.onUpdateStarted();
							}
						});
					}
					List<DiscoveredContextPlugin> updates = new Vector<DiscoveredContextPlugin>();
					final Map<IContextPluginConnector, String> errors = new HashMap<IContextPluginConnector, String>();
					for (IContextPluginConnector source : plugSources) {
						if (cancelled) {
							Log.w(TAG, "Exiting checkForContextPluginUpdates with finished: " + cancelled);
							break;
						}
						if (!networkUpdateAllowedFinal
								&& (source instanceof SimpleNetworkSource || source instanceof NexusSource)) {
							Log.w(TAG, "Skipping update since WIFI is not enabled for: " + source);
						} else {
							currentSource = source;
							try {
								Log.d(TAG, "Updating from PluginSource: " + source);
								List<DiscoveredContextPlugin> potentialUpdates = source.getContextPlugins(platform,
										platformVersion, frameworkVersion);
								List<DiscoveredContextPlugin> remove = new ArrayList<DiscoveredContextPlugin>();
								for (DiscoveredContextPlugin update : potentialUpdates) {
									if (update.hasError()) {
										errors.put(source, update.getErrorMessage());
										remove.add(update);
									} else if (update.getContextPlugin().hasFeatureDependencies()) {
										for (DynamixFeatureInfo featureDependency : update.getContextPlugin()
												.getFeatureDependencies()) {
											if (featureDependency != null && featureDependency.isRequired()) {
												boolean featureFound = false;
												for (FeatureInfo feature : availableFeatures) {
													if (feature.name != null
															&& feature.name.equalsIgnoreCase(featureDependency
																	.getName())) {
														featureFound = true;
														break;
													}
												}
												// If we didn't find a required feature, remove the ContextPlugin from
												// the list.
												if (!featureFound) {
													Log.d(TAG,
															"Removing incompatible plug: " + update.getContextPlugin());
													remove.add(update);
												}
											}
										}
									}
								}
								// Remove incompatible updates
								potentialUpdates.removeAll(remove);
								// Finally, add all compatible updates.
								updates.addAll(potentialUpdates);
							} catch (Exception e) {
								Log.w(TAG, e);
								errors.put(source, e.toString());
							}
							currentSource = null;
						}
					}
					if (cancelled && callback != null) {
						Utils.dispatch(new Runnable() {
							@Override
							public void run() {
								callback.onUpdateCancelled();
							}
						});
					} else {
						final List<PluginDiscoveryResult> results = new Vector<PluginDiscoveryResult>();
						for (DiscoveredContextPlugin update : updates)
							results.add(new PluginDiscoveryResult(update));
						if (callback != null) {
							Utils.dispatch(new Runnable() {
								@Override
								public void run() {
									callback.onUpdateComplete(results, errors);
								}
							});
						}
					}
					Log.d(TAG, "Completed checking for context plug-in updates");
					// Restart the update timer, if necessary
					DynamixService.startContextPluginUpdateTimer();
					DynamixService.updateNotifications();
					processingContextPluginUpdates = false;
				}
			});
		} else
			Log.w(TAG, "Already discovering plug-ins!");
	}

	/**
	 * Returns the list of available IPluginSources, which are used for discovering new plug-ins and plug-in updates.
	 */
	static List<IContextPluginConnector> getContextPluginSources() {
		List<IContextPluginConnector> sources = new Vector<IContextPluginConnector>();
		// Setup primary Dynamix context plug-in repository
		if (DynamixPreferences.isDynamixRepositoryEnabled(DynamixService.getAndroidContext())) {
			RepositoryInfo repo = DynamixService.getConfig().getPrimaryContextPluginRepo();
			if (repo != null) {
				if (!DynamixService.isEmbedded())
					repo.setUrl(DynamixPreferences.getNetworkContextPluginDiscoveryPath(DynamixService
							.getAndroidContext()));
				try {
					sources.add(ContextPluginConnectorFactory.makeContextPluginConnector(repo));
				} catch (Exception e) {
					Log.e(TAG, "Could not make repository using: " + repo);
				}
			}
		}
		// Setup local Dynamix context plug-in repository (if enabled)
		if (DynamixPreferences.localContextPluginDiscoveryEnabled(DynamixService.getAndroidContext())) {
			RepositoryInfo repo = null;
			if (DynamixService.isEmbedded()) {
				repo = DynamixService.getConfig().getLocalPluginRepo();
				if (repo != null) {
					String baseUrl = repo.getUrl();
					String storageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
					if (repo.getUrl() != null) {
						// Setup storage directory, if needed
						if (!repo.getUrl().contains(storageDirectory))
							repo.setUrl(Environment.getExternalStorageDirectory() + baseUrl);
					} else
						Log.w(TAG, "URL was null for: " + repo);
				}
			} else {
				repo = new RepositoryInfo("Local Context Plug-in Repo", null, RepositoryInfo.SIMPLE_FILE_SOURCE);
				repo.setUrl(Environment.getExternalStorageDirectory()
						+ DynamixPreferences.getLocalContextPluginDiscoveryPath(DynamixService.getAndroidContext()));
			}
			if (repo != null) {
				try {
					sources.add(ContextPluginConnectorFactory.makeContextPluginConnector(repo));
				} catch (Exception e) {
					Log.e(TAG, "Could not make repository using: " + repo);
				}
			}
		}
		// Setup 3rd party Dynamix context plug-in repository (if enabled)
		if (DynamixPreferences.isExternalRepositoryEnabled(DynamixService.getAndroidContext())) {
			RepositoryInfo repo = null;
			if (DynamixService.isEmbedded()) {
				repo = DynamixService.getConfig().getExternalPluginRepo();
			} else {
				repo = new RepositoryInfo();
				repo.setAlias("3rd Party Repository");
				repo.setUrl(DynamixPreferences.getExternalDiscoveryPath(DynamixService.getAndroidContext()));
				repo.setType(RepositoryInfo.SIMPLE_NETWORK_SOURCE);
			}
			if (repo != null) {
				try {
					sources.add(ContextPluginConnectorFactory.makeContextPluginConnector(repo));
				} catch (Exception e) {
					Log.e(TAG, "Could not make repository using: " + repo);
				}
			}
		}
		// TODO: Dynamix Nexus Repo test (remove)
		// RepositoryInfo repo = new RepositoryInfo(
		// "Dynamix Core Context Plug-in Repo",
		// "http://repo1.ambientdynamix.org:8081/nexus/service/local/data_index?g=org.ambientdynamix.contextplugins&repositoryId=dynamix-core-contextplugins",
		// ContextPluginConnectorFactory.NEXUS_INDEX_SOURCE);
		// RepositoryInfo repo = new RepositoryInfo(
		// "Dynamix Core Context Plug-in Repo",
		// "http://repo1.ambientdynamix.org:8081/nexus/service/local/lucene/search?g=org.ambientdynamix.contextplugins&repositoryId=dynamix-core-contextplugins",
		// ContextPluginConnectorFactory.NEXUS_LUCENE_SOURCE);
		// try {
		// sources.add(ContextPluginConnectorFactory.makeContextPluginConnector(repo));
		// } catch (Exception e) {
		// Log.e(TAG, "Could not make repository using: " + repo);
		// }
		return sources;
	}

	/**
	 * Returns a List of ContextPlugins updates wrapped in UpdateResult objects.
	 */
	static List<PluginDiscoveryResult> getFilteredContextPluginUpdates() {
		List<PluginDiscoveryResult> results = new Vector<PluginDiscoveryResult>();
		List<PluginDiscoveryResult> updates = UpdateManager.filterDiscoveredPlugins();
		for (PluginDiscoveryResult update : updates) {
			if (update.hasUpdateTarget())
				results.add(update);
		}
		return results;
	}

	/**
	 * Returns a List of new ContextPlugins wrapped in UpdateResult objects.
	 */
	static List<PluginDiscoveryResult> getNewContextPlugins() {
		List<PluginDiscoveryResult> results = new Vector<PluginDiscoveryResult>();
		List<PluginDiscoveryResult> updates = UpdateManager.filterDiscoveredPlugins();
		for (PluginDiscoveryResult update : updates) {
			if (!update.hasUpdateTarget())
				results.add(update);
		}
		return results;
	}

	/**
	 * Returns a List of UpdateResults that are filtered to only include valid updates for ContextPlugins that are
	 * currently installed, or new ContextPlugins. Note that this method requires that 'checkForContextPluginUpdates'
	 * has already successfully stored a list of context plugin updates in the SettingsManager.
	 */
	static List<PluginDiscoveryResult> filterDiscoveredPlugins() {
		// Create a list of updates to return after filtering
		List<PluginDiscoveryResult> results = new Vector<PluginDiscoveryResult>();
		List<DiscoveredContextPlugin> discoveredPlugs = DynamixService.SettingsManager.getPendingContextPlugins();
		// Scan through each stored update from our SettingsManager
		for (DiscoveredContextPlugin update : discoveredPlugs) {
			if (update != null) {
				// Finally, see if we're updating an existing plugin
				boolean found = false;
				// Create a cloned ContextPluginUpdate (so that database state is maintained)
				DiscoveredContextPlugin updateClone = new DiscoveredContextPlugin(update.getContextPlugin().clone(),
						update.getUpdateMessage(), update.getPriority());
				ContextPlugin clonePlug = updateClone.getContextPlugin();
				// Scan through the list of existing context plugins, looking for a plugin associated with the update
				for (ContextPlugin target : DynamixService.SettingsManager.getInstalledContextPlugins()) {
					// Check for an ID match
					if (target.getId().equalsIgnoreCase(clonePlug.getId())) {
						found = true;
						// We have a matching ID. Now check if the new plugin's
						// version is greater than the existing's
						if (clonePlug.getVersionInfo().compareTo(target.getVersionInfo()) > 0) {
							// The plugin listed in the update is newer... list it as the target of the update
							results.add(new PluginDiscoveryResult(updateClone, target));
							// Log.d(TAG, clonePlug + " can be used to update " + target);
						} else
							// Log.d(TAG, "Currently installed ContextPlugin " + target + " is >= " + clonePlug);
							break;
					}
				}
				// If a target plugin was not found, add it as new
				if (!found) {
					results.add(new PluginDiscoveryResult(updateClone));
					// Log.d(TAG, clonePlug + " is new!");
				}
			} else
				Log.e(TAG, "UpdateResult was NULL in SettingsManager");
		}
		return results;
	}

	/**
	 * Base interface for update listeners
	 * 
	 * @author Darren Carlson
	 */
	interface IBaseUpdateListener {
		/**
		 * Raised when the update is started.
		 */
		void onUpdateStarted();

		/**
		 * Raised if the update is cancelled.
		 */
		void onUpdateCancelled();

		/**
		 * Raised if there was an update error.
		 */
		void onUpdateError(String message);
	}

	/**
	 * Interface for listeners interested in receiving updates about plug-in updates.
	 * 
	 * @author Darren Carlson
	 */
	interface IContextPluginUpdateListener extends IBaseUpdateListener {
		/**
		 * Raised when the update is complete. Provides a list of UpdateResults and (possibly) a Map of
		 * IContextPluginConnector error messages.
		 */
		void onUpdateComplete(List<PluginDiscoveryResult> incomingUpdates, Map<IContextPluginConnector, String> errors);
	}

	/**
	 * Interface for listeners interested in receiving updates about Dynamix updates.
	 * 
	 * @author Darren Carlson
	 */
	interface IDynamixUpdateListener extends IBaseUpdateListener {
		void onUpdateComplete(DynamixUpdates updates);
	}
}