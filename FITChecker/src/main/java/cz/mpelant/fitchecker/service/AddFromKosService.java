package cz.mpelant.fitchecker.service;

import android.accounts.AuthenticatorException;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.BuildConfig;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.downloader.KosServer;
import cz.mpelant.fitchecker.model.Subject;
import cz.mpelant.fitchecker.utils.MainThreadBus;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * AddFromKosService.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.service
 * @since 4/18/2014
 */
public class AddFromKosService extends Service {
    private static final String ACTION = BuildConfig.PACKAGE_NAME+".KOS_ADD_SUBJECTS";


    public static class KosException {
        private Exception mException;

        public KosException(Exception exception) {
            mException = exception;
        }

        public Exception getException() {
            return mException;
        }
    }

    public static enum KosStatus {
        STARTED, FINISHED
    }

    private static KosStatus lastStatus;


    private class Task extends Thread {
        @Override
        public void run() {
            try {
                List<String> subjectList = new KosServer().loadSubjects();
                ContentValues[] values = new ContentValues[subjectList.size()];
                for (int i = 0; i < subjectList.size(); i++) {
                    values[i] = new Subject(subjectList.get(i)).getContentValues();
                }
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                App.getInstance().getContentResolver().bulkInsert(DataProvider.getSubjectsUri(), values);
            } catch (IOException | AuthenticatorException | XmlPullParserException e) {
                onTaskException(new KosException(e));
                e.printStackTrace();
            }

            onTaskFinished();
        }


    }


    private Task mTask;
    private MainThreadBus bus;

    public static Intent generateIntent() {
        return new Intent(ACTION);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bus = App.getInstance().getBus();
        bus.register(this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void onTaskException(KosException e) {
        bus.postOnMainThread(e);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onNewTask();
        return START_STICKY;
    }


    private synchronized void onNewTask() {
        if (mTask != null) {
            return;
        }
        post(KosStatus.STARTED);
        mTask = new Task();
        mTask.start();
    }


    private synchronized void onTaskFinished() {
        mTask = null;
        post(KosStatus.FINISHED);
        stopSelf();
    }

    private void post(KosStatus status) {
        lastStatus = status;
        bus.postOnMainThread(status);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    public static KosStatus getLastStatus() {
        if (lastStatus == null) {
            return KosStatus.FINISHED;
        }

        return lastStatus;
    }

}
