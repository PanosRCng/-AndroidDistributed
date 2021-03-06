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
package org.ambientdynamix.api.contextplugin;

import java.util.UUID;

import android.content.Context;
import android.view.View;

/**
 * Provide a means of programatically creating an Android context interaction View. This functionality is used by
 * ContextPlugins that interact with the user via some type of user interface. Since dynamically loaded ContextPlugins
 * cannot provide their own Activities (due to Android security restrictions), Views generated by the factory are
 * injected into a hosting Activity provided by the Dynamix Framework, allowing a ContextPlugin to interact with the
 * user for configuration and/or context interactions.
 * 
 * @author Darren Carlson
 */
public interface IContextPluginInteractionViewFactory {
	/**
	 * Initializes the view.
	 * @param context The Android context.
	 * @param runtime The plug-in runtime.
	 * @param requestId The requestId.
	 * @param contextType The type of context to acquire (may be null).
	 * @param titleBarHeight The title bar height.
	 * @return The initialized View.
	 */
	public View initializeView(Context context, InteractiveContextPluginRuntime runtime, UUID requestId,
			String contextType, int titleBarHeight);


	/**
	 * Destroy the view.
	 */
	public void destroyView();
}