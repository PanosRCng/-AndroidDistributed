package org.ambientdynamix.contextplugins.addplugin;

import java.util.Set;

public interface IAddPluginInfo {
	public abstract String getStringRepresentation(String format);

	public abstract String getImplementingClassname();

	public abstract String getContextType();

	public abstract Set<String> getStringRepresentationFormats();

	public abstract String getMessage();
}