
package cz.mpelant.fitchecker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import cz.mpelant.fitchecker.model.Exam;
import cz.mpelant.fitchecker.model.Subject;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Helper class which creates/updates our database and provides the DAOs.
 *
 * @author martin pelant
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "fitchecker.db";
    private static final int DATABASE_VERSION = 4;

    private Context mCtx;

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        mCtx = context.getApplicationContext();
    }

    public Context getContext() {
        return mCtx;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, final ConnectionSource connectionSource) {

        try {
            TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    TableUtils.createTableIfNotExists(connectionSource, Subject.class);
                    TableUtils.createTableIfNotExists(connectionSource, Exam.class);
                    return true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            try {
                TableUtils.createTableIfNotExists(connectionSource, Exam.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else if (oldVersion <=3) {
            try {
                TableUtils.dropTable(connectionSource, Exam.class, true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        onCreate(sqLiteDatabase, connectionSource);

    }

}
