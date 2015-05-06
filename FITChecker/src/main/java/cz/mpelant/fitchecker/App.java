package cz.mpelant.fitchecker;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.ViewConfiguration;
import cz.mpelant.fitchecker.activity.Settings;
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
    public static final String VERSION_CODE = "versionCode";
    private static App instance;
    private MainThreadBus mBus;

    @Override
    public void onCreate() {
        instance = this;
        mBus = new MainThreadBus();
        forceOverflowHack();
        super.onCreate();

        if (isNewVersion()) {
            performUpgrade();
        }
    }

    private void performUpgrade() {
        if (Settings.isNotifEnabled(this)) {//reenable auto refresh if needed
            Settings.stopAlarm(this);
            Settings.startAlarm(this);
        }
        if (OldImport.isUpgradeFromOldVersion(this)) {
            new Thread() {
                @Override
                public void run() {
                    OldImport.importCredentials(instance);
                    OldImport.importDb(instance);
                }
            }.start();
        }
    }

    /**
     * @return true if this is the first start of the app after new version is installed, false otherwise
     */
    private boolean isNewVersion() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getInt(VERSION_CODE, 0) < BuildConfig.VERSION_CODE) {
            sp.edit().putInt(VERSION_CODE, BuildConfig.VERSION_CODE).apply();
            return true;
        }
        return false;

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
