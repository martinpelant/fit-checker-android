package cz.mpelant.fitchecker.oldimport;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import cz.mpelant.fitchecker.auth.KosAccount;
import cz.mpelant.fitchecker.auth.KosAccountManager;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.model.Subject;

import java.util.Arrays;

/**
 * OldImport.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.oldimport
 * @since 4/22/2014
 */
public class OldImport {
    public static final String PREFERENCES_USERNAME = "username";
    public static final String PREFERENCES_PASSWORD = "password";

    public static boolean isUpgradeFromOldVersion(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String username = sp.getString(PREFERENCES_USERNAME, null);
        String password = sp.getString(PREFERENCES_PASSWORD, null);

        return username != null || password != null || Arrays.asList(context.databaseList()).contains(OldDataProvider.DATABASE_NAME);
    }

    public static void importDb(Context context) {
        OldDataProvider data = new OldDataProvider(context);
        data.open();
        Cursor c = data.subjectFetchAll();
        int nameCol = c.getColumnIndex(OldDataProvider.SUBJECTS_NAME);
        int changedCol = c.getColumnIndex(OldDataProvider.SUBJECTS_NEW);
        ContentValues[] values = new ContentValues[c.getCount()];
        while (c.moveToNext()) {
            String name = c.getString(nameCol);
            boolean read = c.getInt(changedCol) != 1;
            Subject subject = new Subject(name, read);
            values[c.getPosition()] = subject.getContentValues();
        }

        c.close();
        data.close();
        context.getContentResolver().bulkInsert(DataProvider.getSubjectsUri(), values);
        context.deleteDatabase(OldDataProvider.DATABASE_NAME);
    }

    public static void importCredentials(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String username = sp.getString(PREFERENCES_USERNAME, null);
        String password = sp.getString(PREFERENCES_PASSWORD, null);
        if (username != null && password != null) {
            KosAccountManager.saveAccount(new KosAccount(username, password));
        }
        sp.edit().remove(PREFERENCES_USERNAME).remove(PREFERENCES_PASSWORD).apply();
    }
}
