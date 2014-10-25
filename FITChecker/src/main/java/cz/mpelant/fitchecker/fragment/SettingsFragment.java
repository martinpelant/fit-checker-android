package cz.mpelant.fitchecker.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.activity.LoginActivity;
import cz.mpelant.fitchecker.activity.Settings;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SettingsFragment.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment
 * @since 10/25/2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {



    private SharedPreferences sp;
    public static String TAG = "fitchecker";
    private ListPreference listPreference;
    private Preference lastRun;
    private Preference ringtone;
    private Preference led;
    private Preference vibrate;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                return true;
            }
        });
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
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
            String name = ringtone.getTitle(getActivity());
            this.ringtone.setSummary(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        sp.registerOnSharedPreferenceChangeListener(this);
        ringtone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    Uri ringtoneUri = Uri.parse((String) newValue);
                    Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
                    String name = ringtone.getTitle(getActivity());
                    SettingsFragment.this.ringtone.setSummary(name);
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
                Settings.startAlarm(getActivity());
            else
                Settings.stopAlarm(getActivity());
            setNotificationOptions();
            if (key.equals(Settings.PREF_ALARM_INTERVAL))
                listPreference.setSummary(listPreference.getEntry());
        }

    }
}
