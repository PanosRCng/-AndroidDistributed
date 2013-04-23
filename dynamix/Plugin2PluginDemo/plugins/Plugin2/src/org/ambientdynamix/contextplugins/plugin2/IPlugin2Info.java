package org.ambientdynamix.contextplugins.plugin2;

import java.util.Set;

public interface IPlugin2Info {
	public abstract String getStringRepresentation(String format);

	public abstract String getImplementingClassname();

	public abstract String getContextType();

	public abstract Set<String> getStringRepresentationFormats();

	public abstract double getNumber2();
}