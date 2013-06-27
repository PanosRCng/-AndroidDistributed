package org.ambientdynamix.contextplugins.myExperimentPlugin;

import java.util.List;
import java.util.Set;

import android.os.Bundle;

public interface IExperimentPluginInfo {
	public abstract String getStringRepresentation(String format);

	public abstract String getImplementingClassname();

	public abstract String getContextType();
	
	public abstract void setContextType(String contextType);

	public abstract Set<String> getStringRepresentationFormats();

	public abstract List<String> getDependencies();
	
	public abstract String getState();
	
	public abstract void setDependencies(List<String> dependencies);
	
	public abstract Bundle getData();
	public abstract void setData(Bundle data);
	
}