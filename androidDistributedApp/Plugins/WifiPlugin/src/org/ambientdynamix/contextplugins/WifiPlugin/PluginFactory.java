package org.ambientdynamix.contextplugins.WifiPlugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(WifiPluginRuntime.class, null, null);
	}
}