package cz.mpelant.fitchecker.auth;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.activity.Login;

/**
 * KosAccountManager.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.auth
 * @since 4/17/2014
 */
public class KosAccountManager {
    private static final String PREFERENCES_USERNAME = "username";
    private static final String PREFERENCES_PASSWORD = "password";
    static final int AUTH_OPTION = 3;


    public static void saveAccount(KosAccount account) {//TODO: encrypt using device UUID
        SharedPreferences.Editor ed = getSp().edit();
        ed.putString(PREFERENCES_USERNAME, account.getUsername());
        ed.putString(PREFERENCES_PASSWORD, account.getPassword());
        ed.commit();
    }

    public static boolean isAccount() {
        return getSp().contains(PREFERENCES_USERNAME) && getSp().contains(PREFERENCES_PASSWORD);
    }

    public static KosAccount getAccount() {
        return new KosAccount(getSp().getString(PREFERENCES_USERNAME, null), getSp().getString(PREFERENCES_PASSWORD, null), AUTH_OPTION);
    }

    public static void deleteAccount() {
        SharedPreferences.Editor ed = getSp().edit();
        ed.remove(PREFERENCES_USERNAME);
        ed.remove(PREFERENCES_PASSWORD);
        ed.commit();
    }

    private static SharedPreferences getSp() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance());
    }
}
