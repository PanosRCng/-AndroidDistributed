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

import java.util.UUID;

/**
 * The ContextPluginRuntimeFactory provides instantiation services for concrete ContextPluginRuntime implementations
 * loaded as OSGi bundles. This class should be extended by concrete implementations that inject their associated
 * ContextPluginRuntime implementation Class into the ContextPluginRuntimeFactory's constructor. The injected Class must
 * be an extension of ContextPluginRuntime. During a ContextPlugin's lifecycle, the injected ContextPluginRuntime is
 * dynamically generated using the ContextPluginRuntimeFactory's makeContextPluginRuntime. The
 * ContextPluginRuntimeFactory also supports the (optional) creation of both a acquisitionViewFactory and
 * settingsViewFactory, which are used by ContextPlugins that provide user interfaces.
 * 
 * @see IContextPluginInteractionViewFactory
 * @author Darren Carlson
 */
public class ContextPluginRuntimeFactory implements IContextPluginRuntimeFactory {
	// Private data
	private static final long serialVersionUID = -1520648719101645529L;
	private final String TAG = this.getClass().getSimpleName();
	private Class<ContextPluginRuntime> runtimeClass;
	private Class<IContextPluginInteractionViewFactory> acquisitionViewFactory;
	private Class<IContextPluginConfigurationViewFactory> settingsViewFactory;

	/**
	 * Constructor used by ContextPlugins to configure their concrete IContextPluginRuntimeFactory. Note that optional
	 * parameters should be simply set to null if they are not needed.
	 * 
	 * @param runtimeClass
	 *            The ContextPlugin's ContextPluginRuntime Class (required)
	 * @param acquisitionViewFactory
	 *            The ContextPlugin's acquisitionViewFactory IContextPluginViewFactory (optional)
	 * @param settingsViewFactory
	 *            The ContextPlugin's settingsViewFactory IContextPluginViewFactory (optional)
	 */
	public ContextPluginRuntimeFactory(Class<? extends ContextPluginRuntime> runtimeClass,
			Class<? extends IContextPluginInteractionViewFactory> acquisitionViewFactory,
			Class<? extends IContextPluginConfigurationViewFactory> settingsViewFactory) {
		this.runtimeClass = (Class<ContextPluginRuntime>) runtimeClass;
		if (acquisitionViewFactory != null)
			this.acquisitionViewFactory = (Class<IContextPluginInteractionViewFactory>) acquisitionViewFactory;
		if (settingsViewFactory != null)
			this.settingsViewFactory = (Class<IContextPluginConfigurationViewFactory>) settingsViewFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ContextPluginRuntime makeContextPluginRuntime(ContextPlugin parentPlugin, IPluginFacade facade,
			IPluginEventHandler handler, UUID sessionId) throws Exception {
		if(parentPlugin == null || facade == null || handler == null || sessionId == null)
			throw new Exception("Missing Parameters");
		ContextPluginRuntime runtime = runtimeClass.newInstance();
		runtime.setParentPlugin(parentPlugin);
		runtime.setPluginFacade(facade);
		runtime.setEventHandler(handler);
		runtime.setSessionId(sessionId);
		runtime.setAcquisitionViewFactory(acquisitionViewFactory);
		runtime.setSettingsViewFactory(settingsViewFactory);
		return runtime;
	}
}