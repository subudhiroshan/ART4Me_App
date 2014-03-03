package org.trial.hiv;

import org.trial.hiv.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


public class Notifier {
	
	public static void displayNotice(Context context){
	NotificationManager noteMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	Notification notify = new Notification(R.drawable.stat_app_icon, "ART4Me Pill Reminder System", System.currentTimeMillis());
	
	notify.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
	notify.defaults |= Notification.DEFAULT_ALL;
	
	Intent notificationIntent = new Intent(context, HIVActivity.class);
	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	
	notify.setLatestEventInfo(context.getApplicationContext(), "ART4Me Pill Reminder System", "Heed to this reminder", contentIntent);
    noteMgr.notify(0, notify);
	}
}
