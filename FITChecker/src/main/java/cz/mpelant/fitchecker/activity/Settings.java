
package cz.mpelant.fitchecker.activity;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.fragment.SettingsFragment;
import cz.mpelant.fitchecker.service.SubjectRequest;
import cz.mpelant.fitchecker.service.UpdateSubjectsService;

public class Settings extends ActionBarActivity {
    public static final String PREF_ALARM = "alarm";
    public static final String PREF_ALARM_INTERVAL = "alarmInterval";
    public static final String PREF_ALARM_LAST_RUN = "alarmLastRun";
    public static final String PREF_ACCOUNT = "account";
    public static final String PREF_VIBRATE = "vibrate";
    public static final String PREF_LED = "led";
    public static final String PREF_RINGTONE = "ringtone";
    public static final String PREF_DONATE = "donate";



    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getResources().getString(R.string.settings));

        View contentView = new FrameLayout(this);
        contentView.setId(R.id.baseActivityContent);
        setContentView(contentView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        Fragment fragment = getFragmentManager().findFragmentById(R.id.baseActivityContent);
        if ((fragment == null) && (savedInstanceState == null)) {
            fragment=new SettingsFragment();
            getFragmentManager().beginTransaction().add(R.id.baseActivityContent, fragment, fragment.getClass().getName()).commit();
        }
    }

    public static Intent generateIntent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new Intent(context, Settings.class);
        } else {
            return new Intent(context, SettingsCompat.class);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }





    public static boolean isNotifEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_ALARM, false);
    }


    public static void stopAlarm(Context context) {
        Intent serviceIntent = UpdateSubjectsService.generateIntent(new SubjectRequest(DataProvider.getSubjectsUri(), true));
        PendingIntent pi = PendingIntent.getService(context, 0, serviceIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }


    public static void startAlarm(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Intent serviceIntent = UpdateSubjectsService.generateIntent(new SubjectRequest(DataProvider.getSubjectsUri(), true));
        PendingIntent pi = PendingIntent.getService(context, 0, serviceIntent, 0);
        int interval = 0;
        try {
            interval = Integer.parseInt(sp.getString(PREF_ALARM_INTERVAL, "60"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (interval < 1)
            interval = 60;

        interval *= 60 * 1000;
        long firstTime = SystemClock.elapsedRealtime() + 60000;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, interval, pi);
    }

}
