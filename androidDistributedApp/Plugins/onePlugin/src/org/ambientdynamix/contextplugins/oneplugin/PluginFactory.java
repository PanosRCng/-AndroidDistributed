package org.ambientdynamix.contextplugins.oneplugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(OnePluginRuntime.class, null, null);
	}
}