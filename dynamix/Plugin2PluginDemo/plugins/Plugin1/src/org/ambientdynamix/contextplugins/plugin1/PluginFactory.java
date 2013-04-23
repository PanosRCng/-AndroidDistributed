package org.ambientdynamix.contextplugins.plugin1;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(Plugin1Runtime.class, null, null);
	}
}