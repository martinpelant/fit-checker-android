
package cz.mpelant.fitchecker;

import cz.mpelant.fitchecker.activity.Settings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    public static String TAG = "fitchecker";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_ALARM, false)) {
            Settings.startAlarm(context);
            Log.d(TAG, "BOOT - alarm set");
        } else {
            Log.d(TAG, "BOOT - alarm NOT set");
        }

    }

}
