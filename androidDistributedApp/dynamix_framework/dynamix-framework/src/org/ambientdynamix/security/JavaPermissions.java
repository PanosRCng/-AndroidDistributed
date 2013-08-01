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
package org.ambientdynamix.security;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * Experimental JavaPermissions class. Unused.
 * 
 * @author Darren Carlson
 * 
 */
public abstract class JavaPermissions {
	public void checkAccept(String host, int port) {
		throw new SecurityException();
	}

	public void checkAccess(Thread thread) {
		throw new SecurityException();
	}

	public void checkAccess(ThreadGroup group) {
		throw new SecurityException();
	}

	public void checkAwtEventQueueAccess() {
		throw new SecurityException();
	}

	public void checkConnect(String host, int port) {
		throw new SecurityException();
	}

	public void checkConnect(String host, int port, Object context) {
		throw new SecurityException();
	}

	public void checkCreateClassLoader() {
		throw new SecurityException();
	}

	public void checkDelete(String file) {
		throw new SecurityException();
	}

	public void checkExec(String cmd) {
		throw new SecurityException();
	}

	public void checkExit(int status) {
		throw new SecurityException();
	}

	public void checkLink(String libName) {
		throw new SecurityException();
	}

	public void checkListen(int port) {
		throw new SecurityException();
	}

	public void checkMemberAccess(Class<?> cls, int type) {
		throw new SecurityException();
	}

	public void checkMulticast(InetAddress maddr) {
		throw new SecurityException();
	}

	public void checkMulticast(InetAddress maddr, byte ttl) {
		throw new SecurityException();
	}

	public void checkPackageAccess(String packageName) {
		throw new SecurityException();
	}

	public void checkPackageDefinition(String packageName) {
		throw new SecurityException();
	}

	public void checkPermission(Permission permission) {
		throw new SecurityException();
	}

	public void checkPermission(Permission permission, Object context) {
		throw new SecurityException();
	}

	public void checkPrintJobAccess() {
		throw new SecurityException();
	}

	public void checkPropertiesAccess() {
		throw new SecurityException();
	}

	public void checkPropertyAccess(String key) {
		throw new SecurityException();
	}

	public void checkRead(FileDescriptor fd) {
		throw new SecurityException();
	}

	public void checkRead(String file) {
		throw new SecurityException();
	}

	public void checkRead(String file, Object context) {
		throw new SecurityException();
	}

	public void checkSecurityAccess(String target) {
		throw new SecurityException();
	}

	public void checkSetFactory() {
		throw new SecurityException();
	}

	public void checkSystemClipboardAccess() {
		throw new SecurityException();
	}

	public boolean checkTopLevelWindow(Object window) {
		return false;
	}

	public void checkWrite(FileDescriptor fd) {
		throw new SecurityException();
	}

	public void checkWrite(String file) {
		throw new SecurityException();
	}
}