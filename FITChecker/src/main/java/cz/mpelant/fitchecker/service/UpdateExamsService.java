package cz.mpelant.fitchecker.service;

import android.accounts.AuthenticatorException;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.squareup.otto.Produce;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.BuildConfig;
import cz.mpelant.fitchecker.activity.Settings;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.downloader.KosCoursesServer;
import cz.mpelant.fitchecker.downloader.KosExamsServer;
import cz.mpelant.fitchecker.model.Exam;
import cz.mpelant.fitchecker.model.Subject;
import cz.mpelant.fitchecker.utils.MainThreadBus;
import cz.mpelant.fitchecker.utils.NotificationHelper;

/**
 * AddFromKosService.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.service
 * @since 4/18/2014
 */
public class UpdateExamsService extends Service {
    private static final String TAG = UpdateExamsService.class.getName();
    private static final String ACTION = BuildConfig.PACKAGE_NAME + ".KOS_ADD_EXAMS";


    public static class KosException {
        private Exception mException;

        public KosException(Exception exception) {
            mException = exception;
        }

        public Exception getException() {
            return mException;
        }
    }


    private class Task extends Thread {
        private final SubjectResponse mResponse;
        private SubjectRequest mRequest;

        public Task(SubjectRequest request) {
            this.mRequest = request;
            mResponse = new SubjectResponse();
        }

        @Override
        public void run() {
            try {
                Cursor subjects = App.getInstance().getContentResolver().query(mRequest.mUri, null, null, null, null);
                ArrayList<ContentValues> cvs = new ArrayList<>();
                KosExamsServer server = new KosExamsServer();
                Set<String> registeredExams = server.getRegisteredExams();
                ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                while (subjects.moveToNext()) {

                    Subject subject = new Subject(subjects);
                    ContentProviderOperation.Builder ob = ContentProviderOperation.newDelete(DataProvider.getExamsUri()).withSelection(Exam.COL_SUBJECT + " = ?", new String[]{subject.getName()});
                    batch.add(ob.build());
                    Cursor tmp = App.getInstance().getContentResolver().query(DataProvider.getExamsUri(), null, Exam.COL_SUBJECT + " = ?", new String[]{subject.getName()}, null);
                    tmp.moveToFirst();
                    int examCnt = tmp.getCount();
                    tmp.close();
                    List<Exam> examList = server.loadExams(subject.getName());
                    for (Exam e : examList) {
                        Log.d(TAG, e.toString());
                        e.setSubject(subject.getName());
                        e.setIsRegistered(registeredExams.contains(e.getExamId()));
                        ob = ContentProviderOperation.newInsert(DataProvider.getExamsUri()).withValues(e.getContentValues());
                        batch.add(ob.build());
                    }
                    if (examList.size() > examCnt) {
                        onSubjectChanged(subject);
                    }
                }
                subjects.close();
                try {
                    getContentResolver().applyBatch(DataProvider.AUTHORITY, batch);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();
                }
            } catch (IOException | AuthenticatorException | XmlPullParserException e) {
                onTaskException(new KosException(e));
                e.printStackTrace();
            }

            onTaskFinished(mRequest, mResponse);
        }


        private void onSubjectChanged(Subject subject) {
            mResponse.setSubjectChanged(subject);
            subject.setRead(false);
            Uri uri = DataProvider.getSubjectUri(subject.getId());
            App.getInstance().getContentResolver().update(uri, subject.getContentValuesReadOnly(), null, null);
            App.getInstance().getContentResolver().notifyChange(uri, null);
        }


    }

    public static class UpdateExamStatus {
        public static enum Status {
            STARTED, FINISHED
        }

        private Status mStatus;
        private SubjectRequest mRequest;

        public UpdateExamStatus(Status status, SubjectRequest request) {
            mStatus = status;
            mRequest = request;
        }

        public UpdateExamStatus(Status status) {
            mStatus = status;
        }

        public Status getStatus() {
            return mStatus;
        }


        /**
         * @return null means everything is done and the service is shutting down
         */
        @Nullable
        public SubjectRequest getRequest() {
            return mRequest;
        }
    }

    private int lastId;
    private static UpdateExamStatus lastStatus;
    private int tasksCount;
    private Task mTask;
    private MainThreadBus bus;

    public static Intent generateIntent(SubjectRequest request) {
        Intent i = new Intent(ACTION);
        request.applyToIntent(i);
        return i;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bus = App.getInstance().getBus();
        bus.register(this);
        tasksCount = 0;
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
        lastId = startId;
        onNewTask(new SubjectRequest(intent));
        return START_REDELIVER_INTENT;
    }


    private synchronized void onNewTask(SubjectRequest request) {
        post(new UpdateExamStatus(UpdateExamStatus.Status.STARTED, request));
        tasksCount++;
        new Task(request).start();
    }

    private synchronized void onTaskFinished(SubjectRequest request, SubjectResponse result) {
        tasksCount--;
        if (request.showNotifications) {//save last run time
            if (result.isChangesDetected()) {
                new NotificationHelper(this).displayNotification(result.getChangedSubjects(), NotificationHelper.NotificationType.EXAM);
            }
        }

        post(new UpdateExamStatus(UpdateExamStatus.Status.FINISHED, request));
        if (tasksCount <= 0) {
            post(new UpdateExamStatus(UpdateExamStatus.Status.FINISHED));
            stopSelf(lastId);
        }
    }

    private void post(UpdateExamStatus status) {
        lastStatus = status;
        bus.postOnMainThread(status);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Produce
    public static UpdateExamStatus getLastStatus() {
        if (lastStatus == null) {
            return new UpdateExamStatus(UpdateExamStatus.Status.FINISHED);
        }
        return lastStatus;
    }

}
