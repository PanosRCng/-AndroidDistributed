package org.ambientdynamix.contextplugins.twoplugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(TwoPluginRuntime.class, null, null);
	}
}