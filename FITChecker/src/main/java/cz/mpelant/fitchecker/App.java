package cz.mpelant.fitchecker;

import android.app.Application;
import android.preference.PreferenceManager;
import android.view.ViewConfiguration;
import cz.mpelant.fitchecker.activity.Settings;
import cz.mpelant.fitchecker.fragment.SettingsFragment;
import cz.mpelant.fitchecker.oldimport.OldImport;
import cz.mpelant.fitchecker.utils.MainThreadBus;

import java.lang.reflect.Field;

/**
 * App.java
 *
 * @project FITChecker
 * @package cz.mpelant.fitchecker
 * @since 4/17/2014
 */
public class App extends Application {
    public static final String SP_IMPORTED = "imported";
    private static App instance;
    private MainThreadBus mBus;

    @Override
    public void onCreate() {
        instance = this;
        mBus = new MainThreadBus();
        forceOverflowHack();
        super.onCreate();
        if (!PreferenceManager.getDefaultSharedPreferences(instance).getBoolean(SP_IMPORTED, false) && OldImport.isUpgradeFromOldVersion(this)) {
            performUpgrade();
        } else {//no need to update, save it to SP so we don't have to check it again
            PreferenceManager.getDefaultSharedPreferences(instance).edit().putBoolean(SP_IMPORTED, true).commit();
        }
    }

    private void performUpgrade() {
        if(Settings.isNotifEnabled(this)){//reenable auto refresh if needed
            Settings.stopAlarm(this);
            Settings.startAlarm(this);
        }
        new Thread() {
            @Override
            public void run() {
                OldImport.importCredentials(instance);
                OldImport.importDb(instance);
                PreferenceManager.getDefaultSharedPreferences(instance).edit().putBoolean(SP_IMPORTED, true).commit();
            }
        }.start();
    }

    private void forceOverflowHack() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    public static App getInstance() {
        return instance;
    }

    public MainThreadBus getBus() {
        return mBus;
    }


}
