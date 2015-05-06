
package cz.mpelant.fitchecker.activity;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.fragment.SettingsFragment;
import cz.mpelant.fitchecker.service.SubjectRequest;
import cz.mpelant.fitchecker.service.UpdateJobService;
import cz.mpelant.fitchecker.service.UpdateSubjectsService;

import java.text.DateFormat;
import java.text.Format;
import java.util.Date;

public class Settings extends AppCompatActivity {
    public static final String PREF_ALARM = "alarm";
    public static final String PREF_ALARM_INTERVAL = "alarmInterval";
    public static final String PREF_ALARM_LAST_RUN = "alarmLastRun";
    public static final String PREF_ACCOUNT = "account";
    public static final String PREF_VIBRATE = "vibrate";
    public static final String PREF_LED = "led";
    public static final String PREF_RINGTONE = "ringtone";
    public static final int JOB_ID = 534532;


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
            fragment = new SettingsFragment();
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void stopAlarm(Context context) {
        if (false) {//Build.VERSION.SDK_INT >= 21 TODO: return to jobs after crash is resolved https://code.google.com/p/android/issues/detail?id=104302
            @SuppressWarnings("ResourceType")
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancel(JOB_ID);
        }

        stopAlarmCompat(context); //need to caal it even if user runs Lollipop,
        // so we can disable the alarm if the app is updated and we continue using only the job scheduler


    }

    private static void stopAlarmCompat(Context context) {
        //cancel any previously set alarms when Job scheduler was not yet applied
        Intent serviceIntent = UpdateSubjectsService.generateIntent(new SubjectRequest(DataProvider.getSubjectsUri(), true));
        PendingIntent pi = PendingIntent.getService(context, 0, serviceIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void startAlarm(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        int interval = 0; //interval in minutes
        try {
            interval = Integer.parseInt(sp.getString(PREF_ALARM_INTERVAL, "60"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (interval < 1)
            interval = 60;

        interval *= 60 * 1000;

        if (false) {//Build.VERSION.SDK_INT >= 21 TODO: return to jobs after crash is resolved https://code.google.com/p/android/issues/detail?id=104302
            ComponentName serviceComponent = new ComponentName(context, UpdateJobService.class);

            @SuppressWarnings("ResourceType")
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo uploadTask = new JobInfo.Builder(JOB_ID, serviceComponent)
                    .setPeriodic(interval)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build();
            jobScheduler.cancel(JOB_ID);
            jobScheduler.schedule(uploadTask);
        } else {
            Intent serviceIntent = UpdateSubjectsService.generateIntent(new SubjectRequest(DataProvider.getSubjectsUri(), true));
            PendingIntent pi = PendingIntent.getService(context, 0, serviceIntent, 0);
            long firstTime = SystemClock.elapsedRealtime() + 60000;
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, interval, pi);
        }

    }


    public static class SettingsDelegate implements SharedPreferences.OnSharedPreferenceChangeListener {

        public interface PreferencesImpl {
            Context getContext();

            void addPreferencesFromResource(int preferences);

            Preference findPreference(CharSequence prefRingtone);

            void startActivity(Intent intent);
        }


        private SharedPreferences sp;
        public static String TAG = "fitchecker";
        private ListPreference listPreference;
        private Preference lastRun;
        private Preference ringtone;
        private Preference led;
        private Preference vibrate;

        private PreferencesImpl prefImpl;

        public SettingsDelegate(PreferencesImpl prefImpl) {
            this.prefImpl = prefImpl;
        }

        public void onCreate(Bundle savedInstanceState) {
            sp = PreferenceManager.getDefaultSharedPreferences(prefImpl.getContext());
            prefImpl.addPreferencesFromResource(R.xml.preferences);
            listPreference = (ListPreference) prefImpl.findPreference(Settings.PREF_ALARM_INTERVAL);
            ringtone = prefImpl.findPreference(Settings.PREF_RINGTONE);
            led = prefImpl.findPreference(Settings.PREF_LED);
            vibrate = prefImpl.findPreference(Settings.PREF_VIBRATE);

            setNotificationOptions();
            lastRun = prefImpl.findPreference(Settings.PREF_ALARM_LAST_RUN);

            displayRingtone(sp);
            displayLastRun();
            CharSequence entry = listPreference.getEntry();
            if (entry == null) {
                Log.d(TAG, "interval nenastaven");
                listPreference.setValueIndex(3);
                entry = listPreference.getEntry();
            }
            listPreference.setSummary(entry);
            Preference account = prefImpl.findPreference(Settings.PREF_ACCOUNT);
            account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    prefImpl.startActivity(new Intent(prefImpl.getContext(), LoginActivity.class));
                    return true;
                }
            });
        }


        private void displayLastRun() {
            if (sp.contains(Settings.PREF_ALARM_LAST_RUN)) {
                Format formatter = DateFormat.getDateTimeInstance();
                Date date = new Date(sp.getLong(Settings.PREF_ALARM_LAST_RUN, 0));
                lastRun.setSummary(formatter.format(date));
            }
        }

        private void displayRingtone(SharedPreferences sp) {
            try {
                String s = sp.getString(Settings.PREF_RINGTONE, null);
                Uri ringtoneUri = Uri.parse(s);
                Ringtone ringtone = RingtoneManager.getRingtone(prefImpl.getContext(), ringtoneUri);
                String name = ringtone.getTitle(prefImpl.getContext());
                this.ringtone.setSummary(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onPause() {
            sp.unregisterOnSharedPreferenceChangeListener(this);
        }

        public void onResume() {
            sp.registerOnSharedPreferenceChangeListener(this);
            ringtone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        Uri ringtoneUri = Uri.parse((String) newValue);
                        Ringtone ringtone = RingtoneManager.getRingtone(prefImpl.getContext(), ringtoneUri);
                        String name = ringtone.getTitle(prefImpl.getContext());
                        SettingsDelegate.this.ringtone.setSummary(name);
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


        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (key.equals(Settings.PREF_ALARM_INTERVAL) || key.equals(Settings.PREF_ALARM)) {
                if (sp.getBoolean(Settings.PREF_ALARM, false))
                    Settings.startAlarm(prefImpl.getContext());
                else
                    Settings.stopAlarm(prefImpl.getContext());
                setNotificationOptions();
                if (key.equals(Settings.PREF_ALARM_INTERVAL))
                    listPreference.setSummary(listPreference.getEntry());
            }

        }
    }
}
