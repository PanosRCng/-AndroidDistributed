package org.ambientdynamix.contextplugins.WifiScanPlugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(WifiScanPluginRuntime.class, null, null);
	}
}