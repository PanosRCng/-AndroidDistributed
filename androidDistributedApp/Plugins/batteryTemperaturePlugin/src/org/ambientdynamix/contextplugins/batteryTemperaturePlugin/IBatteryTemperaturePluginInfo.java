package org.ambientdynamix.contextplugins.batteryTemperaturePlugin;

import java.util.Set;

public interface IBatteryTemperaturePluginInfo {
	public abstract String getStringRepresentation(String format);

	public abstract String getImplementingClassname();

	public abstract String getContextType();

	public abstract Set<String> getStringRepresentationFormats();
	
	public abstract String getState();

	public abstract void setState(String state);
	
	public abstract int getTemperature();
}