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
package org.ambientdynamix.update.contextplugin;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

/**
 * Simple Framework binder class for XML parsing.
 * @author Darren Carlson
 *
 */
@Element(name = "contextPlugins")
public class ContextPluginsBinder {
	@Attribute(required = false)
	String version;
	/*
	 * We need both the 'entry' and 'inline' attributes here
	 */
	@ElementList(entry="contextPlugin", inline=true)
	List<ContextPluginBinder> contextPlugin;
}
