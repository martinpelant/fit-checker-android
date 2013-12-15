
package cz.mpelant.fitchecker.activity;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.UpdateService;

public class Settings extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
    public static final String PREF_ALARM = "alarm";
    public static final String PREF_ALARM_INTERVAL = "alarmInterval";
    public static final String PREF_ALARM_LAST_RUN = "alarmLastRun";
    public static final String PREF_ACCOUNT = "account";
    public static final String PREF_VIBRATE = "vibrate";
    public static final String PREF_LED = "led";
    public static final String PREF_RINGTONE = "ringtone";
    public static final String PREF_DONATE = "donate";
    private SharedPreferences sp;
    public static String TAG = "fitchecker";
    private ListPreference listPreference;
    private Preference lastRun;
    private Preference ringtone;
    private Preference led;
    private Preference vibrate;
    private Preference donate;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getResources().getString(R.string.settings));
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.preferences);
        listPreference = (ListPreference) findPreference(PREF_ALARM_INTERVAL);
        ringtone = findPreference(PREF_RINGTONE);
        led = findPreference(PREF_LED);
        vibrate = findPreference(PREF_VIBRATE);

        setNotificationOptions();
        lastRun = findPreference(PREF_ALARM_LAST_RUN);

        displayRingtone(sp);
        displayLastRun();
        CharSequence entry = listPreference.getEntry();
        if (entry == null) {
            Log.d(TAG, "interval nenastaven");
            listPreference.setValueIndex(3);
            entry = listPreference.getEntry();
        }
        listPreference.setSummary(entry);
        Preference account = findPreference(PREF_ACCOUNT);
        account.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Settings.this, Login.class));
                return true;
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, ListSubjects.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayLastRun() {
        if (sp.contains(PREF_ALARM_LAST_RUN)) {
            Format formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Date date = new Date(sp.getLong(PREF_ALARM_LAST_RUN, 0));
            lastRun.setSummary(formatter.format(date));
        }
    }

    private void displayRingtone(SharedPreferences sp) {
        try {
            String s = sp.getString(PREF_RINGTONE, null);
            Uri ringtoneUri = Uri.parse(s);
            Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            String name = ringtone.getTitle(this);
            this.ringtone.setSummary(name);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sp.registerOnSharedPreferenceChangeListener(this);
        ringtone.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    Uri ringtoneUri = Uri.parse((String) newValue);
                    Ringtone ringtone = RingtoneManager.getRingtone(Settings.this, ringtoneUri);
                    String name = ringtone.getTitle(Settings.this);
                    Settings.this.ringtone.setSummary(name);
                } catch (Exception e) {
                }

                return true;
            }
        });
        displayLastRun();
    }

    public static void startAlarm(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, UpdateService.class), 0);
        int interval = 0;
        try {
            interval = Integer.parseInt(sp.getString(PREF_ALARM_INTERVAL, "60"));
        } catch (Exception e) {
        }
        if (interval < 1)
            interval = 60;

        interval *= 60 * 1000;
        long firstTime = SystemClock.elapsedRealtime() + 60000;
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, interval, pi);
    }

    public static void stopAlarm(Context context) {
        PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, UpdateService.class), 0);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pi);
    }

    private void setNotificationOptions() {
        boolean enabled = sp.getBoolean(PREF_ALARM, false);
        listPreference.setEnabled(enabled);
        ringtone.setEnabled(enabled);
        led.setEnabled(enabled);
        vibrate.setEnabled(enabled);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PREF_ALARM_INTERVAL) || key.equals(PREF_ALARM)) {
            if (sp.getBoolean(PREF_ALARM, false))
                startAlarm(this);
            else
                stopAlarm(this);
            setNotificationOptions();
            if (key.equals(PREF_ALARM_INTERVAL))
                listPreference.setSummary(listPreference.getEntry());
        }

    }


}
