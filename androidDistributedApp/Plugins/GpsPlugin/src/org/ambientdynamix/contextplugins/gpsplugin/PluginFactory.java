package org.ambientdynamix.contextplugins.gpsplugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(GpsPluginRuntime.class, null, null);
	}
}