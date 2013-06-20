package org.ambientdynamix.contextplugins.batteryTemperaturePlugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(BatteryTemperaturePluginRuntime.class, null, null);
	}
}