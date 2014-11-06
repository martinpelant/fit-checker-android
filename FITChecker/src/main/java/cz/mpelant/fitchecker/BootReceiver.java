
package cz.mpelant.fitchecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.mpelant.fitchecker.activity.Settings;
import cz.mpelant.fitchecker.fragment.SettingsFragment;

public class BootReceiver extends BroadcastReceiver {
    public static String TAG = "fitchecker";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Settings.isNotifEnabled(context) && Build.VERSION.SDK_INT < 21) {
            Settings.startAlarm(context);
            Log.d(TAG, "BOOT - alarm set");
        } else {
            Log.d(TAG, "BOOT - alarm NOT set");
        }

    }

}
