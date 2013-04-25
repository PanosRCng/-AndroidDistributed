package org.ambientdynamix.contextplugins.plugin2;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(Plugin2Runtime.class, null, null);
	}
}