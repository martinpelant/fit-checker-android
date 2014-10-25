
package cz.mpelant.fitchecker.activity;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import cz.mpelant.fitchecker.R;

public class SettingsCompat extends PreferenceActivity implements Settings.SettingsDelegate.PreferencesImpl {

    private Settings.SettingsDelegate mDelegate;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getResources().getString(R.string.settings));
        mDelegate = new Settings.SettingsDelegate(this);
        mDelegate.onCreate(savedInstanceState);
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

    @Override
    protected void onPause() {
        mDelegate.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDelegate.onResume();
    }

    @Override
    public Context getContext() {
        return this;
    }
}
