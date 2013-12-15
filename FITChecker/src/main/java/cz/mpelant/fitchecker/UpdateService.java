package cz.mpelant.fitchecker;

import java.util.ArrayList;
import java.util.List;

import cz.mpelant.fitchecker.activity.ListSubjects;
import cz.mpelant.fitchecker.activity.Login;
import cz.mpelant.fitchecker.activity.Settings;
import cz.mpelant.fitchecker.downloader.Downloader;
import cz.mpelant.fitchecker.utils.DataProvider;
import cz.mpelant.fitchecker.utils.NotifcationHelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpdateService extends Service{
	public static String TAG = "fitchecker";
	private DataProvider data;
	private List<String> subjects;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    handleCommand();
	    return START_NOT_STICKY;
	}

	private void handleCommand(){
		Log.i(TAG, "received");
		ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		if(!mgr.getBackgroundDataSetting()){
			stopSelf();
			return;
		}
		Log.d(TAG, "downloading");
		subjects = new ArrayList<String>();
		data = new DataProvider(this);
		data.open();
		Cursor c = data.subjectFetchAll();
		int name = c.getColumnIndex(DataProvider.SUBJECTS_NAME);
		if (c != null) {
			if (c.moveToFirst() && c.getCount() > 0) {
				do {
					subjects.add(c.getString(name));
				} while (c.moveToNext());
			}
		}
		c.close();
		data.close();
		MyDownloader downloader = new MyDownloader(this);
		downloader.execute(subjects.toArray(new String[0]));
		
	}
	
	
	private class MyDownloader extends Downloader{

		public MyDownloader(Context context) {
			super(context, true);
		}
		@Override
		protected void onPreExecute() {
			if (!sp.contains(Login.PREFERENCES_USERNAME) || !sp.contains(Login.PREFERENCES_PASSWORD)) {
				cancel();
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(changed>0)
				new NotifcationHelper(UpdateService.this).displayNotification();
			Editor ed = PreferenceManager.getDefaultSharedPreferences(UpdateService.this).edit();
			ed.putLong(Settings.PREF_ALARM_LAST_RUN, System.currentTimeMillis());
			ed.commit();
			Log.d(TAG, "download finished, stopping service");
			stopSelf();
		}
		
	}

}
