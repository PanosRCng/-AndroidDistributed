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
package org.ambientdynamix.data;

import java.io.FileInputStream;
import java.util.Properties;

import org.ambientdynamix.core.FrameworkConstants;
import org.ambientdynamix.util.RepositoryInfo;

import android.util.Log;

/**
 * Represents general configuration settings for the Dynamix Framework.
 * 
 * @author Darren Carlson
 */
public class FrameworkConfiguration {
	public final static String TAG = FrameworkConfiguration.class.getSimpleName();
	// Configuration keys
	public static final String DATABASE_PATH = "database.path";
	public static final String ALLOW_FRAMEWORK_UPDATES = "allow.framework.updates";
	public static final String ALLOW_CONTEXT_PLUGIN_UPDATES = "allow.context.plugin.updates";
	public static final String CONTEXT_CACHE_MAX_EVENTS = "context.cache.max.events";
	public static final String CONTEXT_CACHE_MAX_DURATION_MILLS = "context.cache.max.duration.mills";
	public static final String CONTEXT_CACHE_CULL_INTERVAL_MILLS = "context.cache.cull.interval.mills";
	public static final String ALLOW_REMOTE_CONFIGURATION = "allow.remote.configuration";
	public static final String ALLOW_USER_SERVER_MANAGEMENT = "allow.user.server.management";
	public static final String APP_LIVELINESS_CHECK_MILLS = "app.liveliness.check.mills";
	public static final String APP_INACTIVITY_TIMEOUT_MILLS = "app.inactivity.timeout.mills";
	public static final String PRIMARY_DYNAMIX_SERVER_ALIAS = "primary.dynamix.server.alias";
	public static final String PRIMARY_DYNAMIX_SERVER_URL = "primary.dynamix.server.url";
	public static final String BACKUP_DYNAMIX_SERVER_ALIAS = "backup.dynamix.server.alias";
	public static final String BACKUP_DYNAMIX_SERVER_URL = "backup.dynamix.server.url";
	public static final String PRIMARY_REMOTE_CONFIG_SERVER_ALIAS = "primary.remote.config.server.alias";
	public static final String PRIMARY_REMOTE_CONFIG_SERVER_URL = "primary.remote.config.server.url";
	public static final String BACKUP_REMOTE_CONFIG_SERVER_ALIAS = "backup.remote.config.server.alias";
	public static final String BACKUP_REMOTE_CONFIG_SERVER_URL = "backup.remote.config.server.url";
	public static final String INSTALLER_WORKER_COUNT = "installer.workers.count";
	public static final String DEFAULT_BUFFER_SIZE = "default.buffer.size";
	public static final String HEAP_MEMORY_PROTECTION_THRESHOLD = "heap.memory.protection.threshold";
	public static final String PRIMARY_CONTEXT_PLUGIN_REPO_ALIAS = "primary.context.plugin.repo.alias";
	public static final String PRIMARY_CONTEXT_PLUGIN_REPO_URL = "primary.context.plugin.repo.url";
	public static final String BACKUP_CONTEXT_PLUGIN_REPO_ALIAS = "backup.context.plugin.repo.alias";
	public static final String BACKUP_CONTEXT_PLUGIN_REPO_URL = "backup.context.plugin.repo.url";
	public static final String ALLOW_PRIMARY_REPO_DEACTIVATE = "allow.primary.context.plugin.repo.deactivate";
	public static final String ALLOW_ADDITIONAL_CONTEXT_PLUGIN_REPOS = "allow.additional.context.plugin.repos";
	public static final String ALLOW_SELF_SIGNED_CERTS_DEFAULT = "allow.self.signed.certs.default";
	public static final String PRIMARY_CONTEXT_PLUGIN_REPO_TYPE = "primary.context.plugin.repo.type";
	public static final String BACKUP_CONTEXT_PLUGIN_REPO_TYPE = "backup.context.plugin.repo.type";
	public static final String PRIMARY_DYNAMIX_SERVER_TYPE = "primary.dynamix.server.type";
	public static final String BACKUP_DYNAMIX_SERVER_TYPE = "backup.dynamix.server.type";
	public static final String PRIMARY_REMOTE_CONFIG_SERVER_TYPE = "primary.remote.config.server.type";
	public static final String BACKUP_REMOTE_CONFIG_SERVER_TYPE = "backup.remote.config.server.type";
	public static final String DEFAULT_LOCAL_PLUGIN_REPO_ALIAS = "default.local.plugin.repo.alias";
	public static final String DEFAULT_LOCAL_PLUGIN_REPO_URL = "default.local.plugin.repo.url";
	public static final String DEFAULT_LOCAL_PLUGIN_REPO_TYPE = "default.local.plugin.repo.type";
	public static final String PLUGIN_PERMISSION_CHECKING_ENABLED = "plugin.permission.checking.enabled";
	public static final String WEB_CONNECTOR_ENABLED = "web.connector.enabled";
	public static final String WEB_CONNECTOR_PORTS = "web.connector.ports";
	public static final String WEB_CONNECTOR_CLIENT_TIMEOUT = "web.connector.client.timeout";
	public static final String WEB_CONNECTOR_CLIENT_TIMEOUT_CHECK_INTERVAL = "web.connector.client.timeout.check.interval";
	// Private data
	private String databaseFilePath = "data/database.dat";
	private boolean frameworkUpdatesAllowed = true;
	private boolean contextPluginUpdatesAllowed = true;
	private boolean remoteConfigAllowed = false;
	private boolean userServerManagementAllowed = true;
	private int appLivelinessCheckIntervalMills = 5000;
	private int contextCacheMaxEvents = 250;
	private int contextCacheMaxDurationMills = 3600000;
	private int contextCacheCullIntervalMills = 2500;
	private int appInactivityTimeoutMills = 15000;
	private int installerWorkersCount = 2;
	private int defaultBufferSize = 8192;
	private int heapMemoryProtectionThreshold = 90;
	private boolean allowPrimaryContextPluginRepoDeactivate = true;
	private boolean allowAdditionalContextPluginRepos = true;
	private boolean allowSelfSignedCertsDefault = false;
	private boolean allowAutoContextPluginInstall = true;
	private boolean localContextPluginDiscoveryEnabled = true;
	private RepositoryInfo localPluginRepo;
	private RepositoryInfo externalPluginRepo;
	private RepositoryInfo primaryDynamixServer;
	private RepositoryInfo backupDynamixServer;
	private RepositoryInfo primaryRemoteConfigServer;
	private RepositoryInfo backupRemoteConfigServer;
	private RepositoryInfo primaryContextPluginRepo;
	private RepositoryInfo backupContextPluginRepo;
	private boolean autoContextPluginUpdateCheckEnabled;
	private int contextPluginUpdateCheckInterval;
	private boolean pluginPermissionCheckingEnabled = true;
	private boolean isWebConnectorEnabled = false;
	private Integer[] webConnectorPorts = new Integer[] { 18087, 5633, 5634, 5635, 5636, 5637, 6130, 6131, 6132, 6133,
			6134, 8223, 8224, 8225, 8226, 8227, 10026, 10027, 10028, 10029, 10030, 12224, 12225, 12226, 12227, 12228,
			16001, 16002, 16003, 16004, 16005, 19316, 19317, 19318, 19319 };
	private int webConnectorTimeoutCheckMills = 5000;
	private int webConnectorClientTimeoutMills = 120000;

	public FrameworkConfiguration() {
	}

	/**
	 * Factory method that creates a FrameworkConfiguration using the specified properties file path. This method
	 * intelligently parses the specified properties file and throws detailed exceptions if errors are found.
	 * 
	 * @param propsFilePath
	 *            The path to the properties file Returns a configured FrameworkConfiguration based on the specified
	 *            properties file.
	 * @throws Exception
	 *             If the properties file cannot be parsed (includes a detailed error message).
	 */
	public static FrameworkConfiguration createFromPropsFile(String propsFilePath) throws Exception {
		Log.i(TAG, "Creating FrameworkConfiguration using path: " + propsFilePath);
		// Create a new FrameworkConfiguration
		FrameworkConfiguration config = new FrameworkConfiguration();
		// Create a Properties entity
		Properties props = new Properties();
		// Load the config settings using the propsFilePath
		props.load(new FileInputStream(propsFilePath));
		// Set basic configuration options
		config.setDatabaseFilePath(validate(props, DATABASE_PATH));
		config.setFrameworkUpdatesAllowed(Boolean.parseBoolean(validate(props, ALLOW_FRAMEWORK_UPDATES)));
		config.setContextPluginUpdatesAllowed(Boolean.parseBoolean(validate(props, ALLOW_CONTEXT_PLUGIN_UPDATES)));
		config.setContextCacheMaxEvents(Integer.parseInt(validate(props, CONTEXT_CACHE_MAX_EVENTS)));
		config.setContextCacheMaxDurationMills(Integer.parseInt(validate(props, CONTEXT_CACHE_MAX_DURATION_MILLS)));
		config.setContextCacheCullIntervalMills(Integer.parseInt(validate(props, CONTEXT_CACHE_CULL_INTERVAL_MILLS)));
		config.setRemoteConfigAllowed(Boolean.parseBoolean(validate(props, ALLOW_REMOTE_CONFIGURATION)));
		config.setUserServerManagementAllowed(Boolean.parseBoolean(validate(props, ALLOW_USER_SERVER_MANAGEMENT)));
		config.setAppLivelinessCheckIntervalMills(Integer.parseInt(validate(props, APP_LIVELINESS_CHECK_MILLS)));
		config.setAppInactivityTimeoutMills(Integer.parseInt(validate(props, APP_INACTIVITY_TIMEOUT_MILLS)));
		config.setInstallerWorkersCount(Integer.parseInt(validate(props, INSTALLER_WORKER_COUNT)));
		config.setDefaultBufferSize(Integer.parseInt(validate(props, DEFAULT_BUFFER_SIZE)));
		config.setHeapMemoryProtectionThreshold(Integer.parseInt(validate(props, HEAP_MEMORY_PROTECTION_THRESHOLD)));
		config.setAllowPrimaryContextPluginRepoDeactivate(Boolean.parseBoolean(validate(props,
				ALLOW_PRIMARY_REPO_DEACTIVATE)));
		config.setAllowAdditionalContextPluginRepos(Boolean.parseBoolean(validate(props,
				ALLOW_ADDITIONAL_CONTEXT_PLUGIN_REPOS)));
		config.setAllowSelfSignedCertsDefault(Boolean.parseBoolean(validate(props, ALLOW_SELF_SIGNED_CERTS_DEFAULT)));
		config.setPluginPermissionCheckingEnabled(Boolean.parseBoolean(validate(props,
				PLUGIN_PERMISSION_CHECKING_ENABLED)));
		// if Dynamix updates are allowed, setup the update servers
		if (config.isFrameworkUpdatable()) {
			config.setPrimaryDynamixServer(makeServer("primaryDynamixServer", true,
					props.getProperty(PRIMARY_DYNAMIX_SERVER_ALIAS), props.getProperty(PRIMARY_DYNAMIX_SERVER_URL),
					props.getProperty(PRIMARY_DYNAMIX_SERVER_TYPE)));
			config.setBackupDynamixServer(makeServer("backupDynamixServer", false,
					props.getProperty(BACKUP_DYNAMIX_SERVER_ALIAS), props.getProperty(BACKUP_DYNAMIX_SERVER_URL),
					props.getProperty(BACKUP_DYNAMIX_SERVER_TYPE)));
		}
		// if context plug-in updates are allowed, setup the context plug-in repos
		if (config.areContextPluginUpdatesAllowed()) {
			config.setPrimaryContextPluginRepo(makeServer("PrimaryContextPluginRepo", true,
					props.getProperty(PRIMARY_CONTEXT_PLUGIN_REPO_ALIAS),
					props.getProperty(PRIMARY_CONTEXT_PLUGIN_REPO_URL),
					props.getProperty(PRIMARY_CONTEXT_PLUGIN_REPO_TYPE)));
			config.setBackupContextPluginRepo(makeServer("BackupContextPluginRepo", false,
					props.getProperty(BACKUP_CONTEXT_PLUGIN_REPO_ALIAS),
					props.getProperty(BACKUP_CONTEXT_PLUGIN_REPO_URL),
					props.getProperty(BACKUP_CONTEXT_PLUGIN_REPO_TYPE)));
		}
		// If remote configuration is allowed, setup the remote config servers
		if (config.isRemoteConfigAllowed()) {
			config.setPrimaryRemoteConfigServer(makeServer("primaryRemoteConfigServer", true,
					props.getProperty(PRIMARY_REMOTE_CONFIG_SERVER_ALIAS),
					props.getProperty(PRIMARY_REMOTE_CONFIG_SERVER_URL),
					props.getProperty(PRIMARY_REMOTE_CONFIG_SERVER_TYPE)));
			config.setBackupRemoteConfigServer(makeServer("backupRemoteConfigServer", false,
					props.getProperty(BACKUP_REMOTE_CONFIG_SERVER_ALIAS),
					props.getProperty(BACKUP_REMOTE_CONFIG_SERVER_URL),
					props.getProperty(BACKUP_REMOTE_CONFIG_SERVER_TYPE)));
		}
		// config.setDefaultLocalPluginPath(props.getProperty(DEFAULT_LOCAL_PLUGIN_REPO_URL));
		// Setup the optional local repo
		config.setLocalPluginRepo(makeServer("defaultLocalPluginRepo", false,
				props.getProperty(DEFAULT_LOCAL_PLUGIN_REPO_ALIAS), props.getProperty(DEFAULT_LOCAL_PLUGIN_REPO_URL),
				props.getProperty(DEFAULT_LOCAL_PLUGIN_REPO_TYPE)));
		config.setWebConnectorEnabled(Boolean.parseBoolean(props.getProperty(WEB_CONNECTOR_ENABLED, "false")));
		String ports = props.getProperty(WEB_CONNECTOR_PORTS, "");
		if (ports.length() > 0) {
			// Remove whitespace
			ports = ports.replaceAll("\\s", "");
			// Split the ports into an array
			String[] portArray = ports.split(",");
			// Setup the web connector port array
			config.webConnectorPorts = new Integer[portArray.length];
			// Create the ports using the strings
			for (int i = 0; i < portArray.length; i++) {
				config.webConnectorPorts[i] = Integer.parseInt(portArray[i]);
			}
		} else
			config.webConnectorPorts = new Integer[0];
		config.setWebConnectorClientTimeoutMills(Integer.parseInt(props.getProperty(WEB_CONNECTOR_CLIENT_TIMEOUT,
				"120000")));
		config.setWebConnectorTimeoutCheckMills(Integer.parseInt(props.getProperty(
				WEB_CONNECTOR_CLIENT_TIMEOUT_CHECK_INTERVAL, "5000")));
		return config;
	}

	/**
	 * Returns true if the web connector is enabled; false otherwise. Default is false.
	 */
	public boolean isWebConnectorEnabled() {
		return isWebConnectorEnabled;
	}

	/**
	 * Set true if the web connector is enabled; false otherwise.
	 */
	public void setWebConnectorEnabled(boolean isWebConnectorEnabled) {
		this.isWebConnectorEnabled = isWebConnectorEnabled;
	}

	/**
	 * Returns the list of allowed ports for the web connector. Default ports are: [18087, 5633, 5634, 5635, 5636, 5637,
	 * 6130, 6131, 6132, 6133, 6134, 8223, 8224, 8225, 8226, 8227, 10026, 10027, 10028, 10029, 10030, 12224, 12225,
	 * 12226, 12227, 12228, 16001, 16002, 16003, 16004, 16005, 19316, 19317, 19318, 19319]
	 */
	public Integer[] getWebConnectorPorts() {
		return webConnectorPorts;
	}

	/**
	 * Returns the web connector timeout check interval (in milliseconds). Default is 5000.
	 */
	public int getWebConnectorTimeoutCheckMills() {
		return webConnectorTimeoutCheckMills;
	}

	/**
	 * Sets the web connector timeout check interval (in milliseconds).
	 */
	public void setWebConnectorTimeoutCheckMills(int webConnectorTimeoutCheckMills) {
		this.webConnectorTimeoutCheckMills = webConnectorTimeoutCheckMills;
	}

	/**
	 * Returns the web connector client timeout period (in milliseconds). Default is 120000.
	 */
	public int getWebConnectorClientTimeoutMills() {
		return webConnectorClientTimeoutMills;
	}

	/**
	 * Sets the web connector client timeout period (in milliseconds).
	 */
	public void setWebConnectorClientTimeoutMills(int webConnectorClientTimeoutMills) {
		this.webConnectorClientTimeoutMills = webConnectorClientTimeoutMills;
	}

	/**
	 * Sets the list of allowed ports for the web connector.
	 */
	public void setWebConnectorPorts(Integer[] webConnectorPorts) {
		this.webConnectorPorts = webConnectorPorts;
	}

	/**
	 * Specifies if Dynamix is allowed to dynamically discover and dynamically install context plugins at runtime.
	 * Returns true if context plugin updates are allowed; false otherwise
	 */
	public boolean areContextPluginUpdatesAllowed() {
		return contextPluginUpdatesAllowed;
	}

	/**
	 * Returns the time period of inactivity necessary for a bound application to listed as inactive (in milliseconds)
	 */
	public int getAppInactivityTimeoutMills() {
		return appInactivityTimeoutMills;
	}

	/**
	 * Returns how often Dynamix should check if registered applications are alive (in milliseconds)
	 */
	public int getAppLivelinessCheckIntervalMills() {
		return appLivelinessCheckIntervalMills;
	}

	/**
	 * Returns the backup Dynamix server.
	 */
	public RepositoryInfo getBackupDynamixServer() {
		return backupDynamixServer;
	}

	/**
	 * Returns the backup server for Dynamix Framework remote configuration
	 */
	public RepositoryInfo getBackupRemoteConfigServer() {
		return backupRemoteConfigServer;
	}

	/**
	 * Returns How often the context event cache should scan for and remove expired events (in milliseconds)
	 */
	public int getContextCacheCullIntervalMills() {
		return contextCacheCullIntervalMills;
	}

	/**
	 * Returns the max duration that a context event may be cached (in milliseconds)
	 */
	public int getContextCacheMaxDurationMills() {
		return contextCacheMaxDurationMills;
	}

	/**
	 * Returns the max number of context events that may be cached (Note that 0 implies disabled)
	 */
	public int getContextCacheMaxEvents() {
		return contextCacheMaxEvents;
	}

	/**
	 * Returns the path of the database from the root of the Dynamix installation directory, including the database's
	 * filename and extension.
	 */
	public String getDatabaseFilePath() {
		return databaseFilePath;
	}

	/**
	 * Returns the primary Dynamix server
	 */
	public RepositoryInfo getPrimaryDynamixServer() {
		return primaryDynamixServer;
	}

	/**
	 * Returns the primary server for Dynamix Framework remote configuration
	 */
	public RepositoryInfo getPrimaryRemoteConfigServer() {
		return primaryRemoteConfigServer;
	}

	/**
	 * Specifies if Dynamix is allowed to discover and dynamically install Dynamix Framework updates at runtime. Returns
	 * true if Dynamix updates are allowed; false otherwise
	 */
	public boolean isFrameworkUpdatable() {
		return frameworkUpdatesAllowed;
	}

	/**
	 * Specifies if Dynamix can be remotely configured using the remote configuration server settings Returns true if
	 * Dynamix can be remotely configured; false otherwise
	 */
	public boolean isRemoteConfigAllowed() {
		return remoteConfigAllowed;
	}

	/**
	 * Specifies whether or not users are allowed to change and manage 's server settings Note: if false, servers are
	 * configured exclusively from the settings below, or through remote config (if allowed) Returns true if users are
	 * allowed to change and manage 's server settings; false otherwise
	 */
	public boolean isUserServerManagementAllowed() {
		return userServerManagementAllowed;
	}

	/**
	 * Sets the time period of inactivity necessary for a bound application to listed as inactive (in milliseconds)
	 */
	public void setAppInactivityTimeoutMills(int appInactivityTimeoutMills) {
		this.appInactivityTimeoutMills = appInactivityTimeoutMills;
	}

	/**
	 * Sets how often Dynamix should check if bound applications are alive (in milliseconds)
	 */
	public void setAppLivelinessCheckIntervalMills(int appLivelinessCheckIntervalMills) {
		this.appLivelinessCheckIntervalMills = appLivelinessCheckIntervalMills;
	}

	/**
	 * Sets the backup Dynamix server
	 */
	public void setBackupDynamixServer(RepositoryInfo backupDynamixServer) {
		this.backupDynamixServer = backupDynamixServer;
	}

	/**
	 * Sets the backup server for Dynamix Framework remote configuration
	 */
	public void setBackupRemoteConfigServer(RepositoryInfo backupRemoteConfigServer) {
		this.backupRemoteConfigServer = backupRemoteConfigServer;
	}

	/**
	 * Sets how often the context event cache should scan for and remove expired events (in milliseconds)
	 */
	public void setContextCacheCullIntervalMills(int contextCacheCullIntervalMills) {
		this.contextCacheCullIntervalMills = contextCacheCullIntervalMills;
	}

	/**
	 * Sets the max duration that a context event may be cached (in milliseconds)
	 */
	public void setContextCacheMaxDurationMills(int contextCacheMaxDurationMills) {
		this.contextCacheMaxDurationMills = contextCacheMaxDurationMills;
	}

	/**
	 * Sets the max number of context events that may be cached (Note that 0 implies disabled)
	 */
	public void setContextCacheMaxEvents(int contextCacheMaxEvents) {
		this.contextCacheMaxEvents = contextCacheMaxEvents;
	}

	/**
	 * Sets if Dynamix is allowed to dynamically discover and dynamically install context plugins at runtime.
	 * 
	 * @param contextPluginUpdatesAllowed
	 *            true if context plugin updates are allowed; false otherwise
	 */
	public void setContextPluginUpdatesAllowed(boolean contextPluginUpdatesAllowed) {
		this.contextPluginUpdatesAllowed = contextPluginUpdatesAllowed;
	}

	/**
	 * Sets path of the database from the root of the Dynamix installation directory, including the database's filename
	 * and extension.
	 */
	public void setDatabaseFilePath(String databaseFilePath) {
		this.databaseFilePath = databaseFilePath;
	}

	/**
	 * Sets if Dynamix is allowed to discover and dynamically install Dynamix Framework updates at runtime.
	 * 
	 * @param frameworkUpdatesAllowed
	 *            true if Dynamix updates are allowed; false otherwise
	 */
	public void setFrameworkUpdatesAllowed(boolean frameworkUpdatesAllowed) {
		this.frameworkUpdatesAllowed = frameworkUpdatesAllowed;
	}

	/**
	 * Sets the primary Dynamix server.
	 */
	public void setPrimaryDynamixServer(RepositoryInfo primaryDynamixServer) {
		this.primaryDynamixServer = primaryDynamixServer;
	}

	/**
	 * Sets the primary server for Dynamix Framework remote configuration
	 */
	public void setPrimaryRemoteConfigServer(RepositoryInfo primaryRemoteConfigServer) {
		this.primaryRemoteConfigServer = primaryRemoteConfigServer;
	}

	/**
	 * Sets if Dynamix can be remotely configured using the remote configuration server settings
	 * 
	 * @param remoteConfigAllowed
	 *            true if Dynamix can be remotely configured; false otherwise
	 */
	public void setRemoteConfigAllowed(boolean remoteConfigAllowed) {
		this.remoteConfigAllowed = remoteConfigAllowed;
	}

	/**
	 * Sets whether or not users are allowed to change and manage 's server settings
	 * 
	 * @param userServerManagementAllowed
	 *            true if users are allowed to change and manage 's server settings; false otherwise
	 */
	public void setUserServerManagementAllowed(boolean userServerManagementAllowed) {
		this.userServerManagementAllowed = userServerManagementAllowed;
	}

	/**
	 * Returns the total number of installer workers allowed.
	 */
	public int getInstallerWorkersCount() {
		return installerWorkersCount;
	}

	/**
	 * Sets the total number of installer workers allowed.
	 */
	public void setInstallerWorkersCount(int installerWorkersCount) {
		this.installerWorkersCount = installerWorkersCount;
	}

	/**
	 * Returns the default buffer size.
	 */
	public int getDefaultBufferSize() {
		return defaultBufferSize;
	}

	/**
	 * Sets the default buffer size.
	 */
	public void setDefaultBufferSize(int defaultBufferSize) {
		this.defaultBufferSize = defaultBufferSize;
	}

	/**
	 * Returns the percentage of process heap memory (as an int - e.g., 90 = 90%) that may be consumed before Dynamix
	 * implements memory protection (e.g., by dropping events).
	 */
	public int getHeapMemoryProtectionThreshold() {
		return heapMemoryProtectionThreshold;
	}

	/**
	 * Sets the percentage of process heap memory (as an int - e.g., 90 = 90%) that may be consumed before Dynamix
	 * implements memory protection (e.g., by dropping events).
	 */
	public void setHeapMemoryProtectionThreshold(int heapMemoryProtectionThreshold) {
		this.heapMemoryProtectionThreshold = heapMemoryProtectionThreshold;
	}

	/**
	 * Returns the default local file-system plug-in repository.
	 */
	public RepositoryInfo getLocalPluginRepo() {
		return localPluginRepo;
	}

	/**
	 * Sets the default local file-system plug-in repository.
	 */
	public void setLocalPluginRepo(RepositoryInfo localPluginRepo) {
		this.localPluginRepo = localPluginRepo;
	}

	/**
	 * Returns true if Dynamix is allowed to discover and dynamically install framework updates at runtime; false
	 * otherwise.
	 */
	public boolean areFrameworkUpdatesAllowed() {
		return frameworkUpdatesAllowed;
	}

	/**
	 * Returns the primary context plug-in repo.
	 */
	public RepositoryInfo getPrimaryContextPluginRepo() {
		return primaryContextPluginRepo;
	}

	/**
	 * Sets the external context plug-in repo.
	 */
	public void setExternalPluginRepo(RepositoryInfo externalPluginRepo) {
		this.externalPluginRepo = externalPluginRepo;
	}

	/**
	 * Returns the primary backup context plug-in repo.
	 */
	public RepositoryInfo getBackupContextPluginRepo() {
		return backupContextPluginRepo;
	}

	/**
	 * Sets the primary backup context plug-in repo.
	 */
	public void setBackupContextPluginRepo(RepositoryInfo backupContextPluginRepo) {
		this.backupContextPluginRepo = backupContextPluginRepo;
	}

	/**
	 * Sets the primary context plug-in repo.
	 */
	public void setPrimaryContextPluginRepo(RepositoryInfo primaryContextPluginRepo) {
		this.primaryContextPluginRepo = primaryContextPluginRepo;
	}

	/**
	 * Returns the external context plug-in repo.
	 */
	public RepositoryInfo getExternalPluginRepo() {
		return externalPluginRepo;
	}

	/**
	 * Returns true if the primary context plug-in repo can be deactivated by the user; false otherwise.
	 */
	public boolean allowPrimaryContextPluginRepoDeactivate() {
		if (FrameworkConstants.ADMIN_RELEASE)
			return true;
		else
			return allowPrimaryContextPluginRepoDeactivate;
	}

	/**
	 * Set true if the primary context plug-in repo can be deactivated by the user; false otherwise.
	 */
	public void setAllowPrimaryContextPluginRepoDeactivate(boolean allowPrimaryContextPluginRepoDeactivate) {
		this.allowPrimaryContextPluginRepoDeactivate = allowPrimaryContextPluginRepoDeactivate;
	}

	/**
	 * Returns true if additional context plugin repos can be added in addition to the primary repo; false otherwise.
	 */
	public boolean allowAdditionalContextPluginRepos() {
		return allowAdditionalContextPluginRepos;
	}

	/**
	 * Set true if additional context plugin repos can be added in addition to the primary repo; false otherwise.
	 */
	public void setAllowAdditionalContextPluginRepos(boolean allowAdditionalContextPluginRepos) {
		this.allowAdditionalContextPluginRepos = allowAdditionalContextPluginRepos;
	}

	/**
	 * Returns true if the default for allowing self signed certs is true; false otherwise.
	 */
	public boolean allowSelfSignedCertsDefault() {
		return allowSelfSignedCertsDefault;
	}

	/**
	 * Set true if the default for allowing self signed certs is true; false otherwise.
	 */
	public void setAllowSelfSignedCertsDefault(boolean allowSelfSignedCerts) {
		this.allowSelfSignedCertsDefault = allowSelfSignedCerts;
	}

	/**
	 * Returns true if the auto context plug-in installations are allowed; false otherwise.
	 */
	public boolean allowAutoContextPluginInstall() {
		return allowAutoContextPluginInstall;
	}

	/**
	 * Set true if the auto context plug-in installations are allowed; false otherwise.
	 */
	public void setAllowAutoContextPluginInstall(boolean allowAutoContextPluginInstall) {
		this.allowAutoContextPluginInstall = allowAutoContextPluginInstall;
	}

	/**
	 * Returns true if local context plug-in discovery is enabled; false otherwise.
	 */
	public boolean isLocalContextPluginDiscoveryEnabled() {
		return localContextPluginDiscoveryEnabled;
	}

	/**
	 * Set true if local context plug-in discovery is enabled; false otherwise.
	 */
	public void setLocalContextPluginDiscoveryEnabled(boolean enableLocalRepo) {
		this.localContextPluginDiscoveryEnabled = enableLocalRepo;
	}

	/**
	 * Returns true if automatic context plug-in update checking is enabled; false otherwise.
	 */
	public boolean isAutoContextPluginUpdateCheckEnabled() {
		return autoContextPluginUpdateCheckEnabled;
	}

	/**
	 * Set true if automatic context plug-in update checking is enabled; false otherwise.
	 */
	public void setAutoContextPluginUpdateCheckEnabled(boolean autoContextPluginUpdateCheckEnabled) {
		this.autoContextPluginUpdateCheckEnabled = autoContextPluginUpdateCheckEnabled;
	}

	/**
	 * Returns the context plug-in update interval in milliseconds.
	 */
	public int getContextPluginUpdateCheckInterval() {
		return contextPluginUpdateCheckInterval;
	}

	/**
	 * Sets the context plug-in update interval in milliseconds.
	 */
	public void setContextPluginUpdateCheckInterval(int contextPluginUpdateCheckInterval) {
		this.contextPluginUpdateCheckInterval = contextPluginUpdateCheckInterval;
	}

	/**
	 * Returns true if plugin permission checking is enabled; false otherwise.
	 */
	public boolean isPluginPermissionCheckingEnabled() {
		return pluginPermissionCheckingEnabled;
	}

	/**
	 * Set true if plugin permission checking is enabled; false otherwise.
	 */
	public void setPluginPermissionCheckingEnabled(boolean pluginPermissionCheckingEnabled) {
		this.pluginPermissionCheckingEnabled = pluginPermissionCheckingEnabled;
	}

	/**
	 * Utility method that creates a RepositoryInfo from the incoming data. This method automatically validates the
	 * created RepositoryInfo and throws a detailed exception (if requested) using the incoming description string as a
	 * descriptor for the exception.
	 */
	private static RepositoryInfo makeServer(String description, boolean throwException, String alias, String url,
			String type) throws Exception {
		RepositoryInfo info = new RepositoryInfo(alias, url, type);
		if (!info.validateServerInfo()) {
			if (throwException)
				throw new Exception("Could not validate: " + description);
			else
				return null;
		}
		return info;
	}

	/**
	 * Utility method that extracts the requested propString from the incoming Properties, throwing a detailed exception
	 * if the string cannot be found.
	 * 
	 * @param props
	 *            The Properties file.
	 * @param propString
	 *            The Property string to extract Returns the extracted property string.
	 * @throws Exception
	 *             If the property string cannot be found.
	 */
	private static String validate(Properties props, String propString) throws Exception {
		String s = props.getProperty(propString);
		if (s == null)
			throw new Exception("Could not find: " + propString);
		return s;
	}
}
