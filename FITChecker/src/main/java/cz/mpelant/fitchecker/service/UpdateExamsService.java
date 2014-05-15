package cz.mpelant.fitchecker.service;

import android.accounts.AuthenticatorException;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
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

    public static class ExamRequest {
        private static final String NOTIF = "notif";
        @NonNull
        private boolean showNotifications;

        public ExamRequest() {
            showNotifications = false;
        }

        public ExamRequest(boolean showNotifications) {
            this.showNotifications = showNotifications;
        }

        ExamRequest(Intent intent) {
            showNotifications = intent.getBooleanExtra(NOTIF, false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ExamRequest that = (ExamRequest) o;
            return showNotifications == that.showNotifications;
        }

        @Override
        public int hashCode() {
            return (showNotifications ? 1 : 0);
        }

        void applyToIntent(Intent intent) {
            intent.putExtra(NOTIF, showNotifications);
        }
    }

    public static class ExamResponse {
        private Set<Subject> changedSubjects = new HashSet<>();
        private boolean errorOccured;

        public void setSubjectChanged(Subject subject) {
            changedSubjects.add(subject);
        }

        public boolean isChangesDetected() {
            return changedSubjects.size() > 0;
        }

        public void setErrorsOccured() {
            errorOccured = true;
        }

    }

    private class Task extends Thread {
        private final ExamResponse mResponse;
        private ExamRequest mRequest;

        public Task(ExamRequest request) {
            this.mRequest = request;
            mResponse = new ExamResponse();
        }

        @Override
        public void run() {
            try {
                Cursor subjects = App.getInstance().getContentResolver().query(DataProvider.getSubjectsUri(), null, null, null, null);
                ArrayList<ContentValues> cvs = new ArrayList<>();
                while (subjects.moveToNext()) {
                    Subject subject = new Subject(subjects);
                    Cursor tmp = App.getInstance().getContentResolver().query(DataProvider.getExamsUri(), null, Exam.COL_SUBJECT + " = ?", new String[]{subject.getName()}, null);
                    tmp.moveToFirst();
                    int examCnt = tmp.getCount();
                    tmp.close();
                    KosExamsServer server = new KosExamsServer();
                    List<Exam> examList = server.loadExams(subject.getName());
                    List<String> registeredExams = server.getRegisteredExams();
                    for (Exam e : examList) {
                        Log.d(TAG, e.toString());
                        e.setSubject(subject.getName());
                        e.setIsRegistered(contains(registeredExams, e.getExamId()));
                        cvs.add(e.getContentValues());
                    }
                    if (examList.size() > examCnt) {
                        onSubjectChanged(subject);
                    }
                }
                subjects.close();
                ContentValues cvArr[] = new ContentValues[cvs.size()];
                for (int i = 0; i < cvArr.length; i++) {
                    cvArr[i] = cvs.get(i);
                }
                App.getInstance().getContentResolver().bulkInsert(DataProvider.getExamsUri(), cvArr);
            } catch (IOException | AuthenticatorException | XmlPullParserException e) {
                onTaskException(new KosException(e));
                e.printStackTrace();
            }

            onTaskFinished(mRequest, mResponse);
        }

        private boolean contains(List<String> registeredExams, String examId) {
            for (String re : registeredExams) {
                if (re.equals(examId)) {
                    return true;
                }
            }
            return false;
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
        private ExamRequest mRequest;

        public UpdateExamStatus(Status status, ExamRequest request) {
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
        public ExamRequest getRequest() {
            return mRequest;
        }
    }

    private int lastId;
    private static UpdateExamStatus lastStatus;
    private int tasksCount;
    private Task mTask;
    private MainThreadBus bus;

    public static Intent generateIntent(ExamRequest request) {
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
        onNewTask(new ExamRequest(intent));
        return START_REDELIVER_INTENT;
    }


    private synchronized void onNewTask(ExamRequest request) {
        post(new UpdateExamStatus(UpdateExamStatus.Status.STARTED, request));
        tasksCount++;
        new Task(request).start();
    }

    private synchronized void onTaskFinished(ExamRequest request, ExamResponse result) {
        tasksCount--;
        if (request.showNotifications) {//save last run time
            if (result.isChangesDetected()) {
                new NotificationHelper(this).displayNotification(result.changedSubjects, NotificationHelper.NotificationType.EXAM);
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
