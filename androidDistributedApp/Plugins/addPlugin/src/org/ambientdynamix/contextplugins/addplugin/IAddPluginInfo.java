package org.ambientdynamix.contextplugins.addplugin;

import java.util.List;
import java.util.Set;

import android.os.Bundle;

public interface IAddPluginInfo {
	public abstract String getStringRepresentation(String format);

	public abstract String getImplementingClassname();

	public abstract String getContextType();

	public abstract Set<String> getStringRepresentationFormats();

	public abstract List<String> getDependencies();
	
	public abstract String getState();
	
	public abstract Bundle getData();
	public abstract void setData(Bundle data);
	
}