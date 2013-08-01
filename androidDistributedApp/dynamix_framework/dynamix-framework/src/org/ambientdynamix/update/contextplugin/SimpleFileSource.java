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

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.ambientdynamix.api.application.VersionInfo;
import org.ambientdynamix.api.contextplugin.PluginConstants.PLATFORM;
import org.ambientdynamix.util.RepositoryInfo;

import android.os.Environment;
import android.util.Log;

/**
 * Supports extracting ContextPlugin updates from a file source.
 * 
 * @author Darren Carlson
 */
public class SimpleFileSource extends SimpleSourceBase implements IContextPluginConnector, Serializable {
	// Private data
	private static final long serialVersionUID = 5634095436990952985L;
	private final String TAG = this.getClass().getSimpleName();
	private RepositoryInfo repo;
	private boolean cancel;

	/*
	 * File System Source Test
	 * http://stackoverflow.com/questions/1209469/storing-android-application-data-on-sd-card
	 * http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
	 * http://stackoverflow.com/questions/5760435/android-xml-parsing-error-couldnt-open-directory-container-xml
	 */
	
	/**
	 * Creates a SimpleFileSource using the source path for the local repository.
	 * 
	 * @param sourcePath
	 *            The fully qualified source path of the local repository.
	 */
	public SimpleFileSource(RepositoryInfo repo) {
		if (repo != null) {
			this.repo = repo;
		} else
			Log.w(TAG, "Repo is null");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() {
		cancel = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		else
			return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DiscoveredContextPlugin> getContextPlugins(PLATFORM platform, VersionInfo platformVersion,
			VersionInfo frameworkVersion) throws Exception {
		/*
		 * TODO: Look into packaging plug-ins are self-contained zip files. Possibly keep config data in
		 * PLUG_INF/plug.xml http://stackoverflow.com/questions/4473256/reading-text-files-in-a-zip-archive
		 */
		cancel = false;
		List<DiscoveredContextPlugin> updates = new Vector<DiscoveredContextPlugin>();
		List<File> files = new Vector<File>();
		Log.i(TAG, "Checking for context plug-ins using: " + repo.getAlias());
		Log.i(TAG, "Repository URL is: " + repo.getUrl());
		File sourceFile = new File(repo.getUrl());
		if (sourceFile.isDirectory()) {
			for (File file : sourceFile.listFiles()) {
				if (cancel)
					break;
				if (file.isFile()
						&& (file.getAbsolutePath().endsWith(".xml") || file.getAbsolutePath().endsWith(".XML"))) {
					files.add(file);
				}
			}
		} else
			files.add(sourceFile);
		if (!cancel) {
			for (File f : files) {
				try {
					updates.addAll(createDiscoveredPlugins(repo, new FileInputStream(f), platform,
							platformVersion, frameworkVersion, false));
					
				} catch (Exception e) {
					Log.w(TAG, "Update exception: " + e);
					updates.add(new DiscoveredContextPlugin(e.toString()));
				}
			}
			/*
			 * Because the path to user-manageable external storage may vary depending on the device, the path to each
			 * plugin Bundle JAR MUST be relative to the root of external storage. Dynamix automatically rewrites the
			 * install path using the proper URL format and external storage directory.
			 */
			for (DiscoveredContextPlugin update : updates) {
				if (!update.hasError()) {
					String relativeInstallPath = update.getContextPlugin().getInstallUrl();
					update.getContextPlugin().setInstallUrl(
							"file:/" + Environment.getExternalStorageDirectory() + "/" + relativeInstallPath);
				}
			}
		}
		return updates;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + this.getClass().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return TAG;
	}
}