/*
 * Copyright (C) The Ambient Dynamix Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambientdynamix.contextplugins.myExperimentPlugin;

import org.ambientdynamix.api.application.IContextInfo;

import java.util.List;
import java.util.Set;

import android.os.Bundle;

public interface IExperimentPluginInfo extends IContextInfo
{
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