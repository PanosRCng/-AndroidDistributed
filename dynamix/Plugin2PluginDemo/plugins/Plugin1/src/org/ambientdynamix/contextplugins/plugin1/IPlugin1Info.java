package org.ambientdynamix.contextplugins.plugin1;

import java.util.Set;

public interface IPlugin1Info {
	public abstract String getStringRepresentation(String format);

	public abstract String getImplementingClassname();

	public abstract String getContextType();

	public abstract Set<String> getStringRepresentationFormats();

	public abstract double getNumber1();
}