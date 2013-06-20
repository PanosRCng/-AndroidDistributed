package org.ambientdynamix.contextplugins.batteryLevelPlugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(BatteryLevelPluginRuntime.class, null, null);
	}
}