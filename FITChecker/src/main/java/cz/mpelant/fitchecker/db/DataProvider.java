
package cz.mpelant.fitchecker.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import cz.mpelant.fitchecker.BuildConfig;
import cz.mpelant.fitchecker.model.AbstractEntity;
import cz.mpelant.fitchecker.model.Subject;


/**
 * Content provider for entity model in database
 *
 * @author Martin Pelant
 */
public class DataProvider extends ContentProvider {

    private static final String AUTHORITY = BuildConfig.PACKAGE_NAME + ".subjectsprovider";

    private static final String SUBJECTS_BASE_PATH = "subjects";

    private static final int SUBJECTS = 1;
    private static final int SUBJECT_SINGLE_ROW = 2;

    private static final Uri CONTENT_CHECKLIST_URI = Uri.parse("content://" + AUTHORITY);

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, SUBJECTS_BASE_PATH, SUBJECTS);
        uriMatcher.addURI(AUTHORITY, SUBJECTS_BASE_PATH + "/#", SUBJECT_SINGLE_ROW);
    }

    private DatabaseHelper dbHelper;


    public static Uri getSubjectUri(long subjectId) {
        return Uri.parse(DataProvider.CONTENT_CHECKLIST_URI + "/" + SUBJECTS_BASE_PATH + "/" + subjectId);
    }

    public static Uri getSubjectsUri() {
        return Uri.parse(DataProvider.CONTENT_CHECKLIST_URI + "/" + SUBJECTS_BASE_PATH);
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String table;

        switch (uriMatcher.match(uri)) {
            case SUBJECTS:
                table = Subject.TABLE_NAME;
                break;
            default:
                return null;

        }

        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        long newId;
        try {
            newId = sqlDB.insertOrThrow(table, null, values);
        } catch (SQLiteConstraintException e) {
            Log.d("DB", "Subject already in DB: " + values);
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }


        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(uri + "/" + newId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table = Subject.TABLE_NAME;
        String id = null;

        switch (uriMatcher.match(uri)) {
            case SUBJECTS:
                break;
            case SUBJECT_SINGLE_ROW:
                id = uri.getLastPathSegment();
                break;
            default:
                return 0;

        }
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int deleted;
        if (id != null) {
            deleted = sqlDB.delete(table, AbstractEntity.INTERNAL_ID_COLUMN_NAME + " = ?", new String[]{id});
        } else {
            deleted = sqlDB.delete(table, null, null);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return deleted;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor toReturn = null;
        switch (uriMatcher.match(uri)) {
            case SUBJECTS:
                toReturn = dbHelper.getReadableDatabase().query(Subject.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder != null ? sortOrder : Subject.NAME);
                break;
            case SUBJECT_SINGLE_ROW:
                toReturn = dbHelper.getReadableDatabase().query(Subject.TABLE_NAME, projection, Subject.INTERNAL_ID_COLUMN_NAME + "=?", new String[]{uri.getLastPathSegment()}, null, null, null);
                break;
            default:
                return null;
        }
        toReturn.setNotificationUri(getContext().getContentResolver(), uri);
        return toReturn;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table;
        String id = null;
        switch (uriMatcher.match(uri)) {
            case SUBJECT_SINGLE_ROW:
                table = Subject.TABLE_NAME;
                id = uri.getLastPathSegment();
                break;

            case SUBJECTS:
                table = Subject.TABLE_NAME;
                break;

            default:
                return 0;
        }

        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int updateRows;
        if (id != null) {// update only one row
            updateRows = sqlDB.update(table, values, AbstractEntity.INTERNAL_ID_COLUMN_NAME + " = ?", new String[]{id});
        } else {// update by args
            updateRows = sqlDB.update(table, values, selection, selectionArgs);
        }
        return updateRows;

    }
}
