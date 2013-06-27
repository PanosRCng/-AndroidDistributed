package org.ambientdynamix.contextplugins.myExperimentPlugin;

import org.ambientdynamix.api.contextplugin.ContextPluginRuntimeFactory;

public class PluginFactory extends ContextPluginRuntimeFactory {
	public PluginFactory() {
		super(MyExperimentPluginRuntime.class, null, null);
	}
}