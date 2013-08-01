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
package org.ambientdynamix.core;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.security.Policy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ambientdynamix.security.JavaPermissions;

import android.util.Log;

/**
 * Experimental security manager.
 * 
 * @author Darren Carlson
 */
class DynamixSecurityManager extends SecurityManager {
	private final String TAG = this.getClass().getSimpleName();
	static private Map<Thread, JavaPermissions> threadPerms = new ConcurrentHashMap<Thread, JavaPermissions>();
	private static DynamixSecurityManager mgr = new DynamixSecurityManager();

	private DynamixSecurityManager() {
	}

	public static synchronized void addJavaPermissions(Thread thread, JavaPermissions perms) {
		threadPerms.put(thread, perms);
		Policy.getPolicy().refresh();
	}

	//
	public static SecurityManager getSecurityManager() {
		return mgr;
	}

	//
	public static synchronized void removeJavaPermissions(Thread thread) {
		threadPerms.remove(thread);
		Policy.getPolicy().refresh();
	}

	@Override
	public void checkAccept(String host, int port) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkAccept(host, port);
	}

	@Override
	public void checkAccess(Thread thread) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkAccess(thread);
	}

	@Override
	public void checkAccess(ThreadGroup group) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkAccess(group);
	}

	@Override
	public void checkAwtEventQueueAccess() {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkAwtEventQueueAccess();
	}

	@Override
	public void checkConnect(String host, int port) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkConnect(host, port);
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkConnect(host, port, context);
	}

	@Override
	public void checkCreateClassLoader() {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkCreateClassLoader();
	}

	@Override
	public void checkDelete(String file) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkDelete(file);
	}

	@Override
	public void checkExec(String cmd) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkExec(cmd);
	}

	@Override
	public void checkExit(int status) {
		// Thread t;
		// t.getThreadGroup()
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkExit(status);
	}

	@Override
	public void checkLink(String libName) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkLink(libName);
	}

	@Override
	public void checkListen(int port) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkListen(port);
	}

	@Override
	public void checkMemberAccess(Class<?> cls, int type) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkMemberAccess(cls, type);
	}

	@Override
	public void checkMulticast(InetAddress maddr) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkMulticast(maddr);
	}

	@Override
	public void checkMulticast(InetAddress maddr, byte ttl) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkMulticast(maddr, ttl);
	}

	@Override
	public void checkPackageAccess(String packageName) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkPackageAccess(packageName);
	}

	@Override
	public void checkPackageDefinition(String packageName) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkPackageDefinition(packageName);
	}

	@Override
	public void checkPermission(Permission permission) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkPermission(permission);
	}

	@Override
	public void checkPermission(Permission permission, Object context) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkPermission(permission, context);
	}

	@Override
	public void checkPrintJobAccess() {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkPrintJobAccess();
	}

	@Override
	public void checkPropertiesAccess() {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkPropertiesAccess();
	}

	@Override
	public void checkPropertyAccess(String key) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkPropertyAccess(key);
	}

	@Override
	public void checkRead(FileDescriptor fd) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkRead(fd);
	}

	@Override
	public void checkRead(String file) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkRead(file);
	}

	@Override
	public void checkRead(String file, Object context) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkRead(file, context);
	}

	@Override
	public void checkSecurityAccess(String target) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkSecurityAccess(target);
	}

	@Override
	public void checkSetFactory() {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkSetFactory();
	}

	@Override
	public void checkSystemClipboardAccess() {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkSystemClipboardAccess();
	}

	@Override
	public boolean checkTopLevelWindow(Object window) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			return perms.checkTopLevelWindow(window);
		else
			return super.checkTopLevelWindow(window);
	}

	@Override
	public void checkWrite(FileDescriptor fd) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkWrite(fd);
	}

	@Override
	public void checkWrite(String file) {
		JavaPermissions perms = getPermissions(Thread.currentThread());
		if (perms != null)
			perms.checkWrite(file);
	}

	@Override
	protected int classDepth(String name) {
		Log.i(TAG, "classDepth for: " + Thread.currentThread());
		return super.classDepth(name);
	}

	@Override
	protected int classLoaderDepth() {
		Log.i(TAG, "classLoaderDepth for: " + Thread.currentThread());
		return super.classLoaderDepth();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Log.i(TAG, "clone for: " + Thread.currentThread());
		return super.clone();
	}

	@Override
	protected ClassLoader currentClassLoader() {
		Log.i(TAG, "currentClassLoader for: " + Thread.currentThread());
		return super.currentClassLoader();
	}

	@Override
	protected Class<?> currentLoadedClass() {
		Log.i(TAG, "currentLoadedClass for: " + Thread.currentThread());
		return super.currentLoadedClass();
	}

	@Override
	protected Class[] getClassContext() {
		Log.i(TAG, "getClassContext for: " + Thread.currentThread());
		return super.getClassContext();
	}

	@Override
	protected boolean inClass(String name) {
		Log.i(TAG, "inClass for: " + Thread.currentThread());
		return super.inClass(name);
	}

	@Override
	protected boolean inClassLoader() {
		Log.i(TAG, "inClassLoader for: " + Thread.currentThread());
		return super.inClassLoader();
	}

	private JavaPermissions getPermissions(Thread t) {
		JavaPermissions perms = threadPerms.get(t);
		if (perms != null)
			Log.i(TAG, "Found permissions for Thread: " + t.toString());
		else
			Log.i(TAG, "No permissions for Thread: " + t.toString());
		return perms;
	}
}
