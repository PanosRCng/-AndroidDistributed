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

import java.io.File;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import android.content.Context;
import android.util.Log;

/**
 * Experimental ISGi Security Service. Unused.
 * 
 * @author Darren Carlson
 * 
 */
public class DynamixSecurityService {
	public final static String TAG = DynamixSecurityService.class.getSimpleName();
	BundleContext bundleContext;
	Context androidContext;

	public DynamixSecurityService(Context androidContext, BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		this.androidContext = androidContext;
	}

	/*
	 * Notes: From:
	 * http://stackoverflow.com/questions/5081175/in-osgi-my-permission-is-denied-in-main-thread-but-allowed
	 * -in-edt-thread In OSGi, the framework does not consult the policy file for the permissions of bundles. The
	 * permissions of bundles are set via the ConditionalPermissionAdmin (and the older PermissionAdmin) service. As a
	 * bootstrap, all bundles are granted AllPermission until some bundle "asserts" control and sets permissions for
	 * bundles. So, until you set permission information via ConditionalPermissionAdmin, all the bundles are running
	 * with AllPermission.
	 */
	public synchronized void addManagmentAllPermission() throws Exception {
		// Log.i(TAG, System.getSecurityManager().getSecurityContext().toString());
		// //if(true) return;
		// final ConditionalPermissionAdmin cpa = (ConditionalPermissionAdmin) bundleContext.getService(bundleContext
		// .getServiceReference(ConditionalPermissionAdmin.class.getName()));
		// final ConditionalPermissionUpdate u = cpa.newConditionalPermissionUpdate();
		// List<ConditionalPermissionInfo> permlist = u.getConditionalPermissionInfos();
		// permlist.clear();
		// // Give the System Bundle AllPermissions
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { "*" }) },
		// new PermissionInfo[] { new PermissionInfo(AllPermission.class.getName(), "*", "*") },
		// ConditionalPermissionInfo.ALLOW));
		// // Allow the first two system bundles (Log and ConfigAdmin) to import org.osgi.framework
		// // permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// // BundleLocationCondition.class.getName(), new String[] { bundleContext.getBundle(1).getLocation() }) },
		// // new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(), "org.osgi.framework",
		// // PackagePermission.IMPORT) }, ConditionalPermissionInfo.ALLOW));
		// // permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// // BundleLocationCondition.class.getName(), new String[] { bundleContext.getBundle(2).getLocation() }) },
		// // new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(), "org.osgi.framework",
		// // PackagePermission.IMPORT) }, ConditionalPermissionInfo.ALLOW));
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { "*" }) },
		// new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(),
		// "org.ambientdynamix.api.application", PackagePermission.IMPORT) },
		// ConditionalPermissionInfo.ALLOW));
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { "*" }) },
		// new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(),
		// "org.ambientdynamix.api.contextplugin", PackagePermission.IMPORT) },
		// ConditionalPermissionInfo.ALLOW));
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { "*" }) },
		// new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(),
		// "org.ambientdynamix.api.contextplugin.security", PackagePermission.IMPORT) },
		// ConditionalPermissionInfo.ALLOW));
		// if (!u.commit()) {
		// throw new ConcurrentModificationException("Permissions changed during update");
		// }
		// Log.i(TAG, "Perms Table: " + bundleContext);
		// for (Object info : u.getConditionalPermissionInfos()) {
		// Log.i(TAG, info.toString());
		// }
	}

	// private List<ConditionalPermissionInfo> getFinalPerms(ConditionalPermissionAdmin cpa) {
	// final List<ConditionalPermissionInfo> permlist = new Vector<ConditionalPermissionInfo>();
	// //BundleContext context = context.getBundleContext();
	// // Add final DENY
	// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
	// BundleLocationCondition.class.getName(), new String[] { "*" }) },
	// new PermissionInfo[] { new PermissionInfo(AllPermission.class.getName(), "", "") },
	// ConditionalPermissionInfo.DENY));
	// return permlist;
	// }
	public synchronized void lockDownBundle(Bundle b) {
		Log.e(TAG, "lockDownBundle!!!");
		//
		// final ConditionalPermissionAdmin cpa = (ConditionalPermissionAdmin) bundleContext.getService(bundleContext
		// .getServiceReference(ConditionalPermissionAdmin.class.getName()));
		// final ConditionalPermissionUpdate u = cpa.newConditionalPermissionUpdate();
		// final List<ConditionalPermissionInfo> permlist = u.getConditionalPermissionInfos();
		// permlist.clear();
		// // Give the System Bundle AllPermissions
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { bundleContext.getBundle(0).getLocation() }) },
		// new PermissionInfo[] { new PermissionInfo(AllPermission.class.getName(), "*", "*") },
		// ConditionalPermissionInfo.ALLOW));
		// Allow the first two system bundles (Log and ConfigAdmin) to import org.osgi.framework
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { bundleContext.getBundle(1).getLocation() }) },
		// new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(), "org.osgi.framework",
		// PackagePermission.IMPORT) }, ConditionalPermissionInfo.ALLOW));
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { bundleContext.getBundle(2).getLocation() }) },
		// new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(), "org.osgi.framework",
		// PackagePermission.IMPORT) }, ConditionalPermissionInfo.ALLOW));
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { "*" }) },
		// new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(),
		// "org.ambientdynamix.api.application", PackagePermission.IMPORT) },
		// ConditionalPermissionInfo.ALLOW));
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { "*" }) },
		// new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(),
		// "org.ambientdynamix.api.contextplugin", PackagePermission.IMPORT) },
		// ConditionalPermissionInfo.ALLOW));
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { "*" }) },
		// new PermissionInfo[] { new PermissionInfo(PackagePermission.class.getName(),
		// "org.ambientdynamix.api.contextplugin.security", PackagePermission.IMPORT) },
		// ConditionalPermissionInfo.ALLOW));
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { getBundleRootPath(b) }) },
		// new PermissionInfo[] {
		// new PermissionInfo(FilePermission.class.getName(), "<<ALL FILES>>", "read,write,delete"),
		// new PermissionInfo(NetPermission.class.getName(), getBundleRootPath(b), "") },
		// ConditionalPermissionInfo.DENY));
		// permlist.add(cpa.newConditionalPermissionInfo(null, new ConditionInfo[] { new ConditionInfo(
		// BundleLocationCondition.class.getName(), new String[] { getBundleRootPath(b) }) },
		// new PermissionInfo[] { new PermissionInfo(AllPermission.class.getName(), "", "") },
		// ConditionalPermissionInfo.DENY));
		// if (!u.commit()) {
		// throw new ConcurrentModificationException("Permissions changed during update");
		// }
		// Log.i(TAG, "Perms Table: " + bundleContext);
		// for (Object info : u.getConditionalPermissionInfos()) {
		// Log.i(TAG, info.toString());
		// }
	}

	// private synchronized void updatePermissions(List<String> encodedInfos, boolean clear) throws Exception {
	// //BundleContext context = osgiFramework.getBundleContext();
	// Log.d(TAG, "updatePermissions for: " + context);
	// ConditionalPermissionAdmin cpa = getConditionalPermissionAdmin(context);
	// Log.d(TAG, "updatePermissions got ConditionalPermissionAdmin: " + cpa);
	// ConditionalPermissionUpdate u = cpa.newConditionalPermissionUpdate();
	// Log.d(TAG, "updatePermissions got ConditionalPermissionUpdate: " + u);
	// List<ConditionalPermissionInfo> infos = u.getConditionalPermissionInfos();
	// if (clear)
	// infos.clear();
	// for (String encodedInfo : encodedInfos) {
	// Log.i(TAG, "Adding permission: " + encodedInfo);
	// infos.add(0, cpa.newConditionalPermissionInfo(encodedInfo));
	// }
	// if (!u.commit()) {
	// throw new ConcurrentModificationException("Permissions changed during update");
	// }
	// Log.i(TAG, "Updated Permissions using BundleContext: " + context);
	// for (Object info : u.getConditionalPermissionInfos()) {
	// Log.i(TAG, info.toString());
	// }
	// }
	private String getBundleRootPath(Bundle b) {
		String appDir = androidContext.getFilesDir().getAbsolutePath();
		String osgiDeploymentDir = "file://" + appDir.concat(File.separator).concat("felix");
		return osgiDeploymentDir.concat(File.separator).concat("felix-cache").concat(File.separator).concat("bundle")
				.concat(String.valueOf(b.getBundleId()).concat(File.separator).concat("-"));
	}
	// private ConditionalPermissionAdmin getConditionalPermissionAdmin(BundleContext context) throws BundleException {
	// ServiceReference ref = context.getServiceReference(ConditionalPermissionAdmin.class.getName());
	// ConditionalPermissionAdmin result = null;
	// if (ref != null) {
	// result = (ConditionalPermissionAdmin) context.getService(ref);
	// }
	// return result;
	// }
}
