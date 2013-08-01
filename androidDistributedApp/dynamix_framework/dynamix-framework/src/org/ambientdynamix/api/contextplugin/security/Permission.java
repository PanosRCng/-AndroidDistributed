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

import java.io.Serializable;
import java.lang.reflect.Field;

import android.Manifest;
import android.content.Context;
import android.util.Log;

/**
 * Represents a permission that can be used to guard resources in the Dynamix Framework. Permissions are used in
 * combination with a SecuredContext to mediate access to Android services.
 */
public final class Permission implements Serializable {
	private static final long serialVersionUID = -1224045164394968379L;
	private final static String TAG = Permission.class.getSimpleName();
	// Private data
	private String friendlyName = "";
	private String description = "";
	private String permissionString = "";
	private boolean permissionGranted;

	// Needs a public constructor to support DB4o
	public Permission() {
	}

	// Private singleton constructor
	private Permission(String friendlyName, String description, String permissionString, boolean permissionGranted) {
		this.friendlyName = friendlyName;
		this.description = description;
		this.permissionString = permissionString;
		this.permissionGranted = permissionGranted;
	}

	/**
	 * Creates a Dynamix Permission from the incoming permissionString. The permissionString must describe an existing
	 * Android or Dynamix permission. This method takes a 'permissionString' that is formatted to match a static
	 * constant value of a class providing a permission value. For example, android.content.Context has a static class
	 * member for 'WIFI_SERVICE' that is used to obtain an 'android.net.wifi.WifiManager' from Android. This method
	 * creates a Permission using the underlying value of the constant String, which is extracted from the target class
	 * using Java reflection. Permission objects are always created initially as 'not granted', meaning that they will
	 * not allow access to the resource they are protecting by default.
	 * 
	 * @param permissionString
	 *            A properly formatted string representing a protected Android resource or permission
	 * @return Permission A default Permission, or null if the Permission cannot be created from the String.
	 */
	public static Permission createPermission(String permissionString) {
		// Trim the incoming string
		permissionString = permissionString.trim();
		Log.v(TAG, "createPermission for permissionString: " + permissionString);
		// Extract the target object from the permissionString
		String objectString = permissionString.substring(0, permissionString.lastIndexOf("."));
		// Extract the target field from the permissionString
		String field = permissionString.substring(permissionString.lastIndexOf(".") + 1, permissionString.length());
		// Use Java reflection to verify the existence of the underlying Field object
		try {
			Class<?> parentObject = getClass(objectString);
			if (parentObject != null) {
				/*
				 * Try to access the field. 'getDeclaredField' throws an exception if the field doesn't exist.
				 */
				Field f = parentObject.getDeclaredField(field);
				/*
				 * Finally, extract the static String's value by using g.get(). This will only succeed if the value is
				 * indeed a string and the field is static.
				 */
				String permString = (String) f.get(null);
				//Log.e(TAG, "Permission String: " + permString);
				return new Permission(f.getName(), permissionString, permString, true);
			}
			Log.w(TAG, "Could not find class for: " + objectString);
		} catch (Exception e) {
			Log.w(TAG, e.getMessage());
		}
		return null;
	}
	
	/**
	 * Creates a Dynamix Permission from the incoming permissionString. The permissionString must describe an existing
	 * Android or Dynamix permission. This method takes a 'permissionString' that is formatted to match a static
	 * constant value of a class providing a permission value. For example, android.content.Context has a static class
	 * member for 'WIFI_SERVICE' that is used to obtain an 'android.net.wifi.WifiManager' from Android. This method
	 * creates a Permission using the underlying value of the constant String, which is extracted from the target class
	 * using Java reflection. Permission objects are always created initially as 'not granted', meaning that they will
	 * not allow access to the resource they are protecting by default.
	 * 
	 * @param permissionString
	 *            A properly formatted string representing a protected Android resource or permission
	 * @return Permission A default Permission, or null if the Permission cannot be created from the String.
	 * 
	 * @author Peter Aufner
	 */
	public static Permission createPermissionFromAndroidPermission(String permissionString) {
		
		Log.v(TAG, "createPermissionFromAndroidPermission for permissionString: " + permissionString);
		
		permissionString = permissionString.trim();
		String shortName = permissionString.substring(permissionString.lastIndexOf('.')+1);
		try {
			Manifest.permission.class.getDeclaredField(shortName);
			Log.i(TAG, "Created Permissionclass with name: " + permissionString);
			return new Permission(shortName, permissionString, permissionString, true);
		} catch (SecurityException e1) {
			Log.e(TAG, "Received Security error!");
			return null;
		} catch (NoSuchFieldException e1) {
			Log.e(TAG, "Permission for String: " + permissionString + " not found!");
			return null;
		}
	}

	/**
	 * Utility method used to convert strings to Class objects.
	 * 
	 * @param clazz
	 * @return
	 */
	private static Class getClass(String clazz) {
		if (clazz.equalsIgnoreCase("CONTEXT"))
			return Context.class;
		if (clazz.equalsIgnoreCase("Manifest.permission"))
			return Manifest.permission.class;
		if (clazz.equalsIgnoreCase("Permissions"))
			return Permissions.class;
		return null;
	}

	/**
	 * Returns a description of the Permission
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the friendly name of the Permission
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * Returns the permission string underlying the Permission
	 */
	public String getPermissionString() {
		return permissionString;
	}

	/**
	 * Unique identification of a ContextPlugin requires both the String id and associated VersionInfo.
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + permissionString.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object candidate) {
		// first determine if they are the same object reference
		if (this == candidate)
			return true;
		// make sure they are the same class
		if (candidate == null || candidate.getClass() != getClass())
			return false;
		// Make sure the id's and version numbers are the same
		Permission other = (Permission) candidate;
		if (other.getPermissionString().equalsIgnoreCase(this.getPermissionString()))
			return true;
		return false;
	}

	/**
	 * Returns true if the Permission is granted; false otherwise.
	 */
	public boolean isPermissionGranted() {
		return permissionGranted;
	}

	/**
	 * Sets whether or not the Permission is granted.
	 */
	public void setPermissionGranted(boolean permissionGranted) {
		this.permissionGranted = permissionGranted;
	}

	@Override
	public String toString() {
		return "Permission: " + permissionString + " Granted = " + isPermissionGranted();
	}
}