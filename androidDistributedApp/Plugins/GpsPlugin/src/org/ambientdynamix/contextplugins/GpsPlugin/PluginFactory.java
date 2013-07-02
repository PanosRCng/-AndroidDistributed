package org.ambientdynamix.contextplugins.GpsPlugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(GpsPluginRuntime.class, null, null);
	}
}