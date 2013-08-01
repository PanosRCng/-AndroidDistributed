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
package org.ambientdynamix.api.contextplugin.security;

import java.io.InputStream;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Secured version of Android Resources, which limits access to critical system resources. Designed to be used by
 * plug-ins.
 * 
 * @author Darren Carlson
 * 
 */
public class SecuredResources extends Resources {
	public SecuredResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
		super(assets, metrics, config);
		// TODO Auto-generated constructor stub
	}

	/*
	 * Needed by interactive plugins: getConfiguration, updateConfiguration.
	 */

	@Override
	public InputStream openRawResource(int id) throws NotFoundException {
		throw new SecurityException();
	}

	@Override
	public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
		throw new SecurityException();
	}
}
