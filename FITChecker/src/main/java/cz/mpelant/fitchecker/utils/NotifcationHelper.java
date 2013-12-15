
package cz.mpelant.fitchecker.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.activity.ListSubjects;
import cz.mpelant.fitchecker.activity.Settings;

public class NotifcationHelper {
    private Context mCtx;
    private SharedPreferences sp;
    private static final int LED=0xEE2576ac;
    private static final int LEDON=500;
    private static final int LEDOFF=2000;
    

    public NotifcationHelper(Context ctx) {
        mCtx = ctx;
        sp = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void displayNotification() {
        Log.d("NOTIFICATION", "found changes, displaying notification");

        NotificationManager mNotificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1, getNotification());
    }

    private PendingIntent getClickIntent() {
        Intent notificationIntent = new Intent(mCtx, ListSubjects.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(mCtx, 0, notificationIntent, 0);
    }

    private Notification getNotification() {
        NotificationCompat.Builder nb = new NotificationCompat.Builder(mCtx);
        nb.setTicker(mCtx.getString(R.string.notification_ticker));
        nb.setSmallIcon(R.drawable.ic_stat_icon);
        nb.setContentText(mCtx.getString(R.string.notification_message));
        nb.setContentTitle(mCtx.getString(R.string.app_name));
        nb.setContentIntent(getClickIntent());
        nb.setAutoCancel(true);
        
        if(sp.getBoolean(Settings.PREF_VIBRATE, false))
            nb.setDefaults(Notification.DEFAULT_VIBRATE);
        if(sp.getBoolean(Settings.PREF_LED, false))
            nb.setLights(LED, LEDON, LEDOFF);
        String ringtone=sp.getString(Settings.PREF_RINGTONE, null);
        if(ringtone!=null) 
            nb.setSound(Uri.parse(ringtone));
        
        
        
        
        return nb.getNotification();
    }

    private Notification getOldAPINotification() {
        Notification notification = new Notification(R.drawable.ic_stat_icon, mCtx.getString(R.string.notification_ticker), System.currentTimeMillis());

        notification.setLatestEventInfo(mCtx, mCtx.getString(R.string.app_name), mCtx.getString(R.string.notification_message), getClickIntent());
        notification.flags |= Notification.FLAG_SHOW_LIGHTS |Notification.FLAG_AUTO_CANCEL;
        if(sp.getBoolean(Settings.PREF_VIBRATE, false))
            notification.defaults=Notification.DEFAULT_VIBRATE;
        if(sp.getBoolean(Settings.PREF_LED, false)) {
            Log.d("tag", "led");
            notification.ledARGB=LED;
            notification.ledOnMS=LEDON;
            notification.ledOffMS=LEDOFF;
        }   
        String ringtone=sp.getString(Settings.PREF_RINGTONE, null);
        if(ringtone!=null) 
            notification.sound=Uri.parse(ringtone);
        return notification;
    }
}
