package org.ambientdynamix.contextplugins.addplugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(AddPluginRuntime.class, null, null);
	}
}