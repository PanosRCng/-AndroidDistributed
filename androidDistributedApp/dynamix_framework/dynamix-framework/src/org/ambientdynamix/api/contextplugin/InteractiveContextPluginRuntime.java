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

import org.ambientdynamix.api.contextplugin.security.PrivacyRiskLevel;

/**
 * Base class for all ContextPluginRuntime implementations that provide a user interface, which allows the user to
 * control the plug-in's context sensing or acting capabilities. Note that requesting applications must hold appropriate
 * context support and security credentials to launch an interactive plug-in. In particular, an
 * InteractiveContextPluginRuntime defines a minimum PrivacyRiskLevel that the calling application must be granted in
 * order to launch the context acquisition interface.
 * <p>
 * Note: Each ContextPluginRuntime operate in conjunction with an associated ContextPlugin, which provides meta-data
 * describing the plugin's name, description, version, supported fidelity levels, etc. There are various types of C
 * ontextPluginRuntimes, each with different runtime behavior. Currently, the available types are
 * AutoContextPluginRuntime, ReactiveContextPluginRuntime and AutoReactiveContextPluginRuntime. Please see the
 * documentation accompanying these classes for details.
 * 
 * @see ContextPlugin
 * @see ContextPluginRuntime
 * @see AutoContextPluginRuntime
 * @see AutoReactiveContextPluginRuntime
 * @author Darren Carlson
 */
public abstract class InteractiveContextPluginRuntime extends UnicastEventPluginRuntimeBase {
	/**
	 * Returns the minimum PrivacyRiskLevel required by an application to launch the plug-in's context acquisition
	 * interface.
	 */
	public abstract PrivacyRiskLevel getMinLaunchLevel();

	/**
	 * Returns the desired screen orientation for the plug-in's interface. The return value should be a value from the
	 * 'ActivityInfo' screen orientation values e.g., 'ActivityInfo.SCREEN_ORIENTATION_PORTRAIT'
	 */
	public abstract int getScreenOrientation();
}