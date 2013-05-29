package org.ambientdynamix.contextplugins.oneplugin;

import java.util.Set;

public interface IOnePluginInfo {
	public abstract String getStringRepresentation(String format);

	public abstract String getImplementingClassname();

	public abstract String getContextType();

	public abstract Set<String> getStringRepresentationFormats();

	public abstract double getCounter();
}