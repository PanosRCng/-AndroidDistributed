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
package org.ambientdynamix.api.contextplugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.ambientdynamix.api.application.AppConstants.ContextPluginType;
import org.ambientdynamix.api.application.AppConstants.PluginInstallStatus;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.security.Permission;
import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;
import org.ambientdynamix.util.RepositoryInfo;

/**
 * ContextPlugin represents meta-data describing an installable plugin capable of performing context modeling within the
 * Dynamix Framework. ContextPlugins operate in conjunction with an associated ContextPluginRuntime, which does the
 * actual context acquisition and modeling work; generating ContextEvents as new (or updated) IContextInfo are
 * discovered. The ContextPlugin class provides meta-data describing the plugin's name, description, version, supported
 * fidelity levels, etc. This class also holds information regarding the install status and id of the ContextPlugin's
 * underlying ContextPluginRuntime (as an OSGi bundle). ContextPlugin's are capable of being serialized and stored in an
 * object-based database (if necessary); however, ContextPluginRuntimes are typically not serializable due to their
 * often complex object graph structures (hence their separation from the ContextPlugin).
 * 
 * @author Darren Carlson
 * @see ContextPluginRuntime
 * @see org.ambientdynamix.IContextInfo.IContextInfo
 * @see org.ambientdynamix.applicaiton.ContextEvent
 */
public class ContextPlugin implements Serializable {
	// Private data
	private final String TAG = this.getClass().getSimpleName();
	private static final long serialVersionUID = 7084158670931809780L;
	private String id;
	private String name;
	private String provider;
	private String description;
	private String installUrl;
	private String updateUrl;
	private VersionInfo versionInfo;
	private PluginInstallStatus installStat;
	private boolean enabled = true;
	private List<String> supportedContextTypes;
	private Map<PrivacyRiskLevel, String> supportedPrivacyRisks;
	private String runtimeFactoryClass;
	private long bundleId;
	private VersionInfo minPlatformApiLevel;
	private PluginConstants.PLATFORM targetPlatform;
	private VersionInfo minFrameworkVersion;
	private ContextPluginSettings settings;
	private boolean requiresConfiguration;
	private boolean hasConfigurationView;
	private boolean configured;
	private Set<Permission> permissions;
	private ContextPluginType plugType;
	private Set<DynamixFeatureInfo> featureDependencies;
	private VersionInfo maxPlatformApiLevel;
	private VersionInfo maxFrameworkVersion;
	private RepositoryInfo repoSource;

	/**
	 * Base constructor. Sets up a ContextPlugin with bundleId = -1 and PluginInstallStatus.NOT_INSTALLED.
	 */
	public ContextPlugin() {
		// Setup initial state
		this.bundleId = -1;
		this.installStat = PluginInstallStatus.NOT_INSTALLED;
		supportedContextTypes = new Vector<String>();
		supportedPrivacyRisks = new HashMap<PrivacyRiskLevel, String>();
		permissions = new LinkedHashSet<Permission>();
		featureDependencies = new LinkedHashSet<DynamixFeatureInfo>();
	}

	/**
	 * Creates a new ContextPlugin.
	 * 
	 * @param id
	 *            The unique ID of the ContextPlugin. Note: This must be exactly the same as the 'Symbolic name'
	 *            declared in the OSGi Bundle manifest containing this plugin (or the plugin will fail to load).
	 * @param name
	 *            The name of the plugin for display purposes.
	 * @param provider
	 *            The provider of the plugin (e.g. organization)
	 * @param version
	 *            The version of plugin.
	 * @param description
	 *            A description of the plugin for display purposes.
	 * @param userControlledContextAcquisition
	 *            Whether or not this plugin requires user controlled context acquisition (e.g. through a GUI).
	 * @param supportedPrivacyRisks
	 *            The PrivacyRisks that this plugin supports.
	 * @param supportedContextTypes
	 *            The context types that this plugin supports (as strings).
	 * @param runtimeFactoryClass
	 *            Class capable of creating this ContextPlugin's runtime.
	 * @param requiresConfiguration
	 *            Indicates if this plugin requires configuration before it can be used.
	 * @param hasConfigurationView
	 *            Indicates if thie plugin has a configuration view that the user can adjust settings with.
	 * @param installUrl
	 *            The installation URL for this plug-in
	 * @param updateUrl
	 *            The update URL for this plug-in
	 * @param plugType
	 *            This plug-in's ContextPluginType
	 */
	public ContextPlugin(String id, String name, String provider, VersionInfo version, String description,
			Map<PrivacyRiskLevel, String> supportedPrivacyRisks, List<String> supportedContextTypes,
			String runtimeFactoryClass, boolean requiresConfiguration, boolean hasConfigurationView, String installUrl,
			String updateUrl, ContextPluginType plugType, RepositoryInfo repoSource) {
		// Call base constructor
		this();
		this.id = id;
		this.name = name;
		this.provider = provider;
		this.description = description;
		this.versionInfo = version;
		this.supportedPrivacyRisks = supportedPrivacyRisks;
		this.supportedContextTypes = supportedContextTypes;
		this.runtimeFactoryClass = runtimeFactoryClass;
		this.requiresConfiguration = requiresConfiguration;
		this.hasConfigurationView = hasConfigurationView;
		this.installUrl = installUrl;
		this.updateUrl = updateUrl;
		this.plugType = plugType;
		this.repoSource = repoSource;
	}

	/**
	 * Creates a new ContextPlugin.
	 * 
	 * @param id
	 *            The unique ID of the ContextPlugin. Note: This must be exactly the same as the 'Symbolic name'
	 *            declared in the OSGi Bundle manifest containing this plugin (or the plugin will fail to load).
	 * @param name
	 *            The name of the plugin for display purposes.
	 * @param provider
	 *            The provider of the plugin (e.g. organization).
	 * @param version
	 *            The version of plugin.
	 * @param description
	 *            A description of the plugin for display purposes.
	 * @param userControlledContextAcquisition
	 *            Whether or not this plugin requires user controlled context acquisition (e.g. through a GUI).
	 * @param supportedPrivacyRisks
	 *            The PrivacyRisks that this plugin supports.
	 * @param supportedContextTypes
	 *            The context types that this plugin supports (as strings).
	 * @param runtimeFactoryClass
	 *            Class capable of creating this ContextPlugin's runtime.
	 * @param requiresConfiguration
	 *            Indicates if this plugin requires configuration before it can be used.
	 * @param hasConfigurationView
	 *            Indicates if thie plugin has a configuration view that the user can adjust settings with.
	 * @param installUrl
	 *            The installation URL for this plug-in.
	 * @param updateUrl
	 *            The update URL for this plug-in.
	 * @param permissions
	 *            The permissions assigned to this plug-in.
	 * @param plugType
	 *            This plug-in's ContextPluginType.
	 */
	public ContextPlugin(String id, String name, String provider, VersionInfo version, String description,
			Map<PrivacyRiskLevel, String> supportedPrivacyRisks, List<String> supportedContextTypes,
			String runtimeFactoryClass, boolean requiresConfiguration, boolean hasConfigurationView, String installUrl,
			String updateUrl, Set<Permission> permissions, ContextPluginType plugType, RepositoryInfo repoSource) {
		this(id, name, provider, version, description, supportedPrivacyRisks, supportedContextTypes,
				runtimeFactoryClass, requiresConfiguration, hasConfigurationView, installUrl, updateUrl, plugType,
				repoSource);
		this.permissions = permissions;
	}

	/**
	 * Clones the ContextPlugin
	 */
	public ContextPlugin clone() {
		ContextPlugin plug = new ContextPlugin(getId(), getName(), getProvider(), getVersionInfo(), getDescription(),
				getSupportedPrivacyRiskLevels(), getSupportedContextTypes(), getRuntimeFactoryClass(),
				requiresConfiguration(), hasConfigurationView(), getInstallUrl(), getUpdateUrl(), getPermissions(),
				getContextPluginType(), getRepoSource());
		plug.setMinPlatformApiLevel(getMinPlatformApiLevel());
		plug.setMaxPlatformApiLevel(getMaxPlatformApiLevel());
		plug.setMinFrameworkVersion(getMinFrameworkVersion());
		plug.setMaxFrameworkVersion(getMaxFrameworkVersion());
		plug.setFeatureDependencies(getFeatureDependencies());
		return plug;
	}

	@Override
	public boolean equals(Object candidate) {
		// Check for null
		if (candidate == null)
			return false;
		// Determine if they are the same object reference
		if (this == candidate)
			return true;
		// Make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		// Make sure the id's and version numbers are the same
		ContextPlugin other = (ContextPlugin) candidate;
		if (other.getId().equalsIgnoreCase(this.getId()))
			if (other.getVersionInfo().equals(this.getVersionInfo()))
				// if (other.getRepoSource().equals(this.getRepoSource()))
				return true;
		return false;
	}

	/**
	 * Unique identification of a ContextPlugin requires both the String id and associated VersionInfo.
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + getId().hashCode() + getVersionInfo().hashCode();// + repoSource.hashCode();
		return result;
	}

	/**
	 * Returns the bundle identifier associated with this ContextPlugin's ContextPluginRuntime or -1 if the bundle is
	 * not installed.
	 */
	public long getBundleId() {
		return bundleId;
	}

	/**
	 * Returns the ContextPluginInfomration associated with the ContextPlugin
	 */
	public ContextPluginInformation getContextPluginInformation() {
		return new ContextPluginInformation(getId(), getName(), getDescription(), getVersionInfo(),
				getSupportedContextTypes(), getInstallStatus(), getContextPluginType(), requiresConfiguration(),
				isConfigured(), isEnabled());
	}

	/**
	 * Returns the ContextPluginSettings for this ContextPlugin, or null if there are no settings.
	 */
	public ContextPluginSettings getContextPluginSettings() {
		return settings;
	}

	/**
	 * Returns the ContextPluginType
	 */
	public ContextPluginType getContextPluginType() {
		return plugType;
	}

	/**
	 * Returns the description of this ContextPlugin.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the set of features that this ContextPlugin depends on.
	 */
	public Set<DynamixFeatureInfo> getFeatureDependencies() {
		return featureDependencies;
	}

	/**
	 * Returns the globally unique identifier for this ContextPlugin.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the current InstallStatus for this ContextPlugin
	 * 
	 * @see PluginInstallStatus
	 */
	public PluginInstallStatus getInstallStatus() {
		return this.installStat;
	}

	/**
	 * Returns the installation URL for the associated ContextPluginRuntime's OSGi bundle.
	 */
	public String getInstallUrl() {
		return installUrl;
	}

	/**
	 * Returns the maximum Dynamix framework version for this ContextPlugin.
	 */
	public VersionInfo getMaxFrameworkVersion() {
		return maxFrameworkVersion;
	}

	/**
	 * Gets the maximum platform api level for this ContextPlugin.
	 */
	public VersionInfo getMaxPlatformApiLevel() {
		return maxPlatformApiLevel;
	}

	/**
	 * Returns the minimum required Dynamix version
	 */
	public VersionInfo getMinFrameworkVersion() {
		return minFrameworkVersion;
	}

	/**
	 * Returns the minimum required platform API level for this plug-in
	 */
	public VersionInfo getMinPlatformApiLevel() {
		return minPlatformApiLevel;
	}

	/**
	 * Returns the name of the ContextPlugin.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the Set of Permissions associated with this ContextPlugin.
	 */
	public Set<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * Returns the provider of the plug-in
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * Gets the fully qualified classname of this class' ContextPluginRuntimeFactory.
	 * 
	 * @return
	 */
	public String getRuntimeFactoryClass() {
		return runtimeFactoryClass;
	}

	/**
	 * Returns a read-only List of the ContextPlugin's supported context types.
	 */
	public List<String> getSupportedContextTypes() {
		return supportedContextTypes;
	}

	/**
	 * Returns the ContextPlugin's supported privacy risk levels, along with a string-based description for each.
	 */
	public Map<PrivacyRiskLevel, String> getSupportedPrivacyRiskLevels() {
		return supportedPrivacyRisks;
	}

	/**
	 * Returns the target platform
	 */
	public PluginConstants.PLATFORM getTargetPlatform() {
		return targetPlatform;
	}

	/**
	 * Returns the update URL for the plug-in.
	 */
	public String getUpdateUrl() {
		return updateUrl;
	}

	/**
	 * Returns the version of this ContextPlugin.
	 */
	public VersionInfo getVersionInfo() {
		return versionInfo;
	}

	/**
	 * Returns true if the plugin has a configuration View; false otherwise.
	 */
	public boolean hasConfigurationView() {
		return hasConfigurationView;
	}

	/**
	 * Returns true if this ContextPlugin has feature dependencies; false otherwise.
	 */
	public boolean hasFeatureDependencies() {
		if (featureDependencies != null && featureDependencies.size() > 0)
			return true;
		else
			return false;
	}

	/**
	 * Returns true if this ContextPlugin has a maximum Dynamix framework version.
	 */
	public boolean hasMaxFrameworkVersion() {
		return maxFrameworkVersion != null ? true : false;
	}

	/**
	 * Returns true if this ContextPlugin has a maximum platform api level.
	 */
	public boolean hasMaxPlatformApiLevel() {
		return maxPlatformApiLevel != null ? true : false;
	}

	/**
	 * Returns true if this ContextPlugin has required Permissions; false otherwise.
	 */
	public boolean hasPermissions() {
		if (permissions != null && permissions.size() > 0)
			return true;
		else
			return false;
	}

	/**
	 * Returns true if the plugin is configured; false otherwise. For plug-ins that don't require configuraiton this
	 * method always returns true.
	 */
	public boolean isConfigured() {
		if (!requiresConfiguration)
			// If we don't require configuration, we're configured
			return true;
		else
			return configured;
	}

	/**
	 * Returns true if this ContextPlugin is enabled; false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Returns true if the ContextPlugin's ContextPluginRuntime is installed; false otherwise.
	 */
	public boolean isInstalled() {
		return getInstallStatus() == PluginInstallStatus.INSTALLED;
	}

	/**
	 * Returns true if the plugin requires configuration; false otherwise.
	 */
	public boolean requiresConfiguration() {
		return requiresConfiguration;
	}

	/**
	 * Sets the bundle identifier associated with this ContextPlugin's ContextPluginRuntime. Note that this value should
	 * be -1 if the bundle is not installed.
	 * 
	 * @param bundleId
	 */
	public void setBundleId(long bundleId) {
		this.bundleId = bundleId;
	}

	/**
	 * Sets if the plugin is configured.
	 */
	public void setConfigured(boolean configured) {
		this.configured = configured;
	}

	/**
	 * Sets the ContextPluginSettings for this ContextPlugin.
	 * 
	 * @param settings
	 */
	public void setContextPluginSettings(ContextPluginSettings settings) {
		this.settings = settings;
	}

	/**
	 * Sets the ContextPluginType
	 * 
	 * @param plugType
	 *            The ContextPluginType
	 */
	public void setContextPluginType(ContextPluginType plugType) {
		this.plugType = plugType;
	}

	/**
	 * Sets the description of this ContextPlugin.
	 * 
	 * @param description
	 *            The new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets whether or not this ContextPlugin is enabled.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Sets the features that this ContextPlugin depends on.
	 */
	public void setFeatureDependencies(Set<DynamixFeatureInfo> featureDependencies) {
		this.featureDependencies = featureDependencies;
	}

	/**
	 * Sets if the plugin has a configuration View.
	 */
	public void setHasConfigurationView(boolean hasConfigurationView) {
		this.hasConfigurationView = hasConfigurationView;
	}

	/**
	 * Sets the globally unique identifier for this ContextPlugin.
	 * 
	 * @param id
	 *            The new id.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the InstallStatus for this ContextPlugin.
	 * 
	 * @see PluginInstallStatus
	 */
	public void setInstallStatus(PluginInstallStatus stat) {
		this.installStat = stat;
	}

	/**
	 * Sets the installation URL for the associated ContextPluginRuntime's OSGi bundle.
	 */
	public void setInstallUrl(String installUrl) {
		this.installUrl = installUrl;
	}

	/**
	 * Sets the maximum Dynamix framework version for this ContextPlugin.
	 */
	public void setMaxFrameworkVersion(VersionInfo maxFrameworkVersion) {
		this.maxFrameworkVersion = maxFrameworkVersion;
	}

	/**
	 * Sets the maximum platform api level for this ContextPlugin.
	 */
	public void setMaxPlatformApiLevel(VersionInfo maxPlatformApiLevel) {
		this.maxPlatformApiLevel = maxPlatformApiLevel;
	}

	/**
	 * Sets the minimum required Dynamix version
	 */
	public void setMinFrameworkVersion(VersionInfo minFrameworkVersion) {
		this.minFrameworkVersion = minFrameworkVersion;
	}

	/**
	 * Sets the minimum required platform API level for this plug-in
	 */
	public void setMinPlatformApiLevel(VersionInfo minPlatformApiLevel) {
		this.minPlatformApiLevel = minPlatformApiLevel;
	}

	/**
	 * Sets the name of the ContextPlugin.
	 * 
	 * @param name
	 *            The new name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the Permissions for this ContextPlugin.
	 * 
	 * @param permissions
	 *            the set of permissions to set
	 */
	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	/**
	 * Sets the plug-in provider.
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * Sets if the plugin requires configuration
	 */
	public void setRequiresConfiguration(boolean requiresConfiguration) {
		this.requiresConfiguration = requiresConfiguration;
	}

	/**
	 * Sets the fully qualified classname of this class' ContextPluginRuntimeFactory. Note: runtimeFactoryClass must
	 * reference a class of type ContextPluginRuntimeFactory
	 * 
	 * @param runtimeFactoryClass
	 */
	public void setRuntimeFactoryClass(String runtimeFactoryClass) {
		this.runtimeFactoryClass = runtimeFactoryClass;
	}

	/**
	 * Sets the ContextPlugin's supported context types (as strings).
	 * 
	 * @param The
	 *            new List of supported context type strings.
	 */
	public void setSupportedContextTypes(List<String> supportedContextTypes) {
		this.supportedContextTypes = supportedContextTypes;
	}

	/**
	 * Sets the ContextPlugin's supported privacy risk levels, plus their descriptions.
	 * 
	 * @param supportedPrivacyRisks
	 *            The new PrivacyRisks.
	 */
	public void setSupportedPrivacyRiskLevels(Map<PrivacyRiskLevel, String> supportedPrivacyRisks) {
		this.supportedPrivacyRisks = supportedPrivacyRisks;
	}

	/**
	 * Sets the target platform
	 * 
	 * @param targetPlatform
	 *            The target platform string
	 */
	public void setTargetPlatform(PluginConstants.PLATFORM targetPlatform) {
		this.targetPlatform = targetPlatform;
	}

	/**
	 * Sets the update URL for the plug-in.
	 */
	public void setUpdateUrl(String updateUrl) {
		this.updateUrl = updateUrl;
	}

	/**
	 * Sets the version of this ContextPlugin.
	 */
	public void setVersionInfo(VersionInfo currentVersion) {
		this.versionInfo = currentVersion;
	}

	/**
	 * Returns true if the ContextPlugin supports the context type; false otherwise.
	 * 
	 * @param contextType
	 *            The context type string to check.
	 */
	public boolean supportsContextType(String contextType) {
		return supportedContextTypes.contains(contextType);
	}

	/**
	 * Returns the repository source for this plug-in.
	 */
	public RepositoryInfo getRepoSource() {
		return repoSource;
	}

	/**
	 * Sets the repository source for this plug-in.
	 */
	public void setRepoSource(RepositoryInfo repoSource) {
		this.repoSource = repoSource;
	}

	@Override
	public String toString() {
		return "ContextPlugin: Name= " + name + " | ID= " + id + " | Version= " + versionInfo + "| Repo source: "
				+ repoSource;
	}
}