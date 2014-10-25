
package cz.mpelant.fitchecker.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.fragment.SettingsFragment;
import cz.mpelant.fitchecker.service.SubjectRequest;
import cz.mpelant.fitchecker.service.UpdateSubjectsService;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsCompat extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private SharedPreferences sp;
    public static String TAG = "fitchecker";
    private ListPreference listPreference;
    private Preference lastRun;
    private Preference ringtone;
    private Preference led;
    private Preference vibrate;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getResources().getString(R.string.settings));
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.preferences);
        listPreference = (ListPreference) findPreference(Settings.PREF_ALARM_INTERVAL);
        ringtone = findPreference(Settings.PREF_RINGTONE);
        led = findPreference(Settings.PREF_LED);
        vibrate = findPreference(Settings.PREF_VIBRATE);

        setNotificationOptions();
        lastRun = findPreference(Settings.PREF_ALARM_LAST_RUN);

        displayRingtone(sp);
        displayLastRun();
        CharSequence entry = listPreference.getEntry();
        if (entry == null) {
            Log.d(TAG, "interval nenastaven");
            listPreference.setValueIndex(3);
            entry = listPreference.getEntry();
        }
        listPreference.setSummary(entry);
        Preference account = findPreference(Settings.PREF_ACCOUNT);
        account.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(SettingsCompat.this, LoginActivity.class));
                return true;
            }
        });
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

    private void displayLastRun() {
        if (sp.contains(Settings.PREF_ALARM_LAST_RUN)) {
            Format formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Date date = new Date(sp.getLong(Settings.PREF_ALARM_LAST_RUN, 0));
            lastRun.setSummary(formatter.format(date));
        }
    }

    private void displayRingtone(SharedPreferences sp) {
        try {
            String s = sp.getString(Settings.PREF_RINGTONE, null);
            Uri ringtoneUri = Uri.parse(s);
            Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            String name = ringtone.getTitle(this);
            this.ringtone.setSummary(name);
        } catch (Exception e) {
            e.printStackTrace();
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
                    Ringtone ringtone = RingtoneManager.getRingtone(SettingsCompat.this, ringtoneUri);
                    String name = ringtone.getTitle(SettingsCompat.this);
                    SettingsCompat.this.ringtone.setSummary(name);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }
        });
        displayLastRun();
    }


    private void setNotificationOptions() {
        boolean enabled = sp.getBoolean(Settings.PREF_ALARM, false);
        listPreference.setEnabled(enabled);
        ringtone.setEnabled(enabled);
        led.setEnabled(enabled);
        vibrate.setEnabled(enabled);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Settings.PREF_ALARM_INTERVAL) || key.equals(Settings.PREF_ALARM)) {
            if (sp.getBoolean(Settings.PREF_ALARM, false))
                Settings.startAlarm(this);
            else
                Settings.stopAlarm(this);
            setNotificationOptions();
            if (key.equals(Settings.PREF_ALARM_INTERVAL))
                listPreference.setSummary(listPreference.getEntry());
        }

    }


}
