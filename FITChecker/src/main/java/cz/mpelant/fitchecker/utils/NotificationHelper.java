
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
import android.view.ViewGroup;

import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.activity.MainActivity;
import cz.mpelant.fitchecker.activity.Settings;
import cz.mpelant.fitchecker.fragment.SettingsFragment;
import cz.mpelant.fitchecker.model.Subject;

import java.util.Collection;

public class NotificationHelper {
    private Context mCtx;
    private SharedPreferences sp;
    private static final int LED = 0xEE2576ac;
    private static final int LEDON = 500;
    private static final int LEDOFF = 2000;


    public enum NotificationType {
        EDUX, EXAM
    }


    public NotificationHelper(Context ctx) {
        mCtx = ctx;
        sp = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void displayNotification(Collection<Subject> changedSubejcts, NotificationType type) {
        Log.d("NOTIFICATION", "found changes, displaying notification");

        NotificationManager mNotificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1, getNotification(changedSubejcts, type));
    }

    private PendingIntent getClickIntent() {
        Intent notificationIntent = new Intent(mCtx, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(mCtx, 0, notificationIntent, 0);
    }

    private Notification getNotification(Collection<Subject> changedSubejcts, NotificationType type) {
        NotificationCompat.Builder nb = new NotificationCompat.Builder(mCtx);
        String ticker = "";
        int messageResId = 0;
        switch (type) {
            case EDUX:
                ticker = mCtx.getString(R.string.notification_ticker);
                messageResId = R.plurals.notification_message;
                break;
            case EXAM:
                ticker = mCtx.getString(R.string.notification_ticker_new_exam);
                messageResId = R.plurals.notification_message_exam;
                break;
        }
        nb.setTicker(ticker);
        nb.setSmallIcon(R.drawable.ic_stat_icon);
        String text = mCtx.getResources().getQuantityString(messageResId, changedSubejcts.size(), changedSubejcts.size());
        nb.setContentText(text);
        nb.setContentTitle(mCtx.getString(R.string.app_name));
        nb.setContentIntent(getClickIntent());
        nb.setAutoCancel(true);
        nb.setColor(mCtx.getResources().getColor(R.color.colorPrimary));


        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (Subject changedSubejct : changedSubejcts) {
            style.addLine(changedSubejct.getName());
        }

        style.setBigContentTitle(mCtx.getString(R.string.notification_message_big));
        nb.setStyle(style);


        if (sp.getBoolean(Settings.PREF_VIBRATE, false))
            nb.setDefaults(Notification.DEFAULT_VIBRATE);
        if (sp.getBoolean(Settings.PREF_LED, false))
            nb.setLights(LED, LEDON, LEDOFF);
        String ringtone = sp.getString(Settings.PREF_RINGTONE, null);
        if (ringtone != null)
            nb.setSound(Uri.parse(ringtone));


        return nb.getNotification();
    }


}
