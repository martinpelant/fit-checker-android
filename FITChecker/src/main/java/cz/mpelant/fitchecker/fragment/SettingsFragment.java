package cz.mpelant.fitchecker.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import cz.mpelant.fitchecker.activity.Settings;

/**
 * SettingsFragment.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment
 * @since 10/25/2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFragment extends PreferenceFragment implements Settings.SettingsDelegate.PreferencesImpl {

    private Settings.SettingsDelegate mDelegate;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate = new Settings.SettingsDelegate(this);
        mDelegate.onCreate(savedInstanceState);
    }


    @Override
    public void onPause() {
        mDelegate.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDelegate.onResume();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }


}
