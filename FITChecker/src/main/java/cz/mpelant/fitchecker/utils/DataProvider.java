package cz.mpelant.fitchecker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

@Deprecated public class DataProvider {
	public static String TAG = "fitchecker";

	private static final String DATABASE_NAME = "data";

	public static final String SUBJECTS_DB_NAME = "subjects";

	public static final String SUBJECTS_NAME = "name";
	public static final String SUBJECTS_NEW = "new";

	private static final int DATABASE_VERSION = 1;

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "oncreate");
			String dbCreate = "create table " + SUBJECTS_DB_NAME + " ( " + SUBJECTS_NAME + " text primary key not null, " + SUBJECTS_NEW + " integer default 1 );";
			db.execSQL(dbCreate);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + SUBJECTS_DB_NAME + ";");
			onCreate(db);
		}
	}

	public DataProvider(Context ctx) {
		this.mCtx = ctx;
	}

	public DataProvider open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	// ------------------------------------------------------------

	public long subjectCreate(String name) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(SUBJECTS_NAME, name);
		return mDb.insert(SUBJECTS_DB_NAME, null, initialValues);
	}

	public boolean subjectRead(String name) {
		ContentValues args = new ContentValues();
		args.put(SUBJECTS_NEW, 0);
		return mDb.update(SUBJECTS_DB_NAME, args, SUBJECTS_NAME + "='" + name + "'", null) > 0;
	}

	public boolean subjectChanged(String name) {
		ContentValues args = new ContentValues();
		args.put(SUBJECTS_NEW, 1);
		return mDb.update(SUBJECTS_DB_NAME, args, SUBJECTS_NAME + "='" + name + "'", null) > 0;
	}

	public boolean subjectDelete(String name) {
		return mDb.delete(SUBJECTS_DB_NAME, SUBJECTS_NAME + "='" + name + "'", null) > 0;
	}

	public Cursor subjectFetchAll() {
		return mDb.query(SUBJECTS_DB_NAME, new String[] { SUBJECTS_NAME, SUBJECTS_NEW }, null, null, null, null, SUBJECTS_NAME);
	}
}
