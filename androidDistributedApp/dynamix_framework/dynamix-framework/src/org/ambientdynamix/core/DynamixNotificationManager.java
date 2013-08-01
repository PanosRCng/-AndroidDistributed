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

import java.util.List;
import java.util.Vector;

import org.ambientdynamix.data.DynamixPreferences;
import org.ambientdynamix.util.AndroidNotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

/**
 * Handles Android notifications.
 * 
 * @author Darren Carlson
 * 
 */
public class DynamixNotificationManager {
	// Private data
	private final static String TAG = DynamixNotificationManager.class.getSimpleName();
	private static Handler uiHandler = new Handler();
	private static List<AndroidNotification> notifications = new Vector<AndroidNotification>();
	private NotificationManager nm;
	private Context androidContext;

	DynamixNotificationManager(Context androidContext) {
		this.androidContext = androidContext.getApplicationContext();
		nm = (NotificationManager) androidContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	void addNotification(AndroidNotification note) {
		if (!notifications.contains(note))
			notifications.add(note);
	}

	boolean removeNotification(AndroidNotification note) {
		nm.cancel(note.getNotificationId());
		return notifications.remove(note);
	}

	void removeAllNotifications() {
		clearAllNotifications();
		notifications.clear();
	}

	void showAllNotifications() {
		// Clear current
		clearAllNotifications();
		// Show all current notifications
		for (AndroidNotification n : notifications) {
			showNotification(n);
		}
	}

	/**
	 * Clears all AndroidNotifications from the Android tray and clears the notifications list.
	 */
	void clearAllNotifications() {
		for (AndroidNotification n : notifications) {
			Log.d(TAG, "Cancelling notification id: " + n.getNotificationId());
			nm.cancel(n.getNotificationId());
		}
		nm.cancelAll();
	}

	/**
	 * Handles setting notifications in the Android status bar. Reference Link:
	 * http://androidgps.blogspot.com/2008/10/icon-in-status-bar.html
	 */
	private void showNotification(final AndroidNotification notification) {
		// Only show notifications if Dynamix is running
		Log.d(TAG, "showNotification " + notification.getNotificationId() + " for androidContext: " + androidContext);
		//Log.d(TAG, "Using DynamixNotificationManager: " + nm);
		// Create the new Notification using the DynamixNotification as a template
		Notification n = new Notification(notification.getIconID(),
				androidContext.getText(R.string.dynamix_notification_titlebar), System.currentTimeMillis());
		n.flags |= Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
		//n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.number += 1;
		// Place the newly created Notification into a Bundle within an Intent that targets the BaseActivity
		Bundle bundle = new Bundle();
		bundle.putSerializable("notification", notification);
		Intent futureIntent = new Intent(androidContext, BaseActivity.class);
		futureIntent.putExtras(bundle);
		// Create a PendingIntent using the futureIntent
		PendingIntent pendingIntent = PendingIntent.getActivity(androidContext, (int) System.currentTimeMillis(),
				futureIntent, Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		// Update the Notification with the notification title, description and pendingIntent
		n.setLatestEventInfo(androidContext, androidContext.getText(R.string.dynamix_notification_title),
				notification.getDescription(), pendingIntent);
		if (DynamixPreferences.vibrationAlertsEnabled(androidContext))
			n.defaults |= Notification.DEFAULT_VIBRATE;
		if (DynamixPreferences.audibleAlertsEnabled(androidContext))
			n.defaults |= Notification.DEFAULT_SOUND;
		// Finally, use the DynamixNotificationManager to notify the user with the parameterized Notification
		nm.notify(notification.getNotificationId(), n);
	}
}
