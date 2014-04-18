package cz.mpelant.fitchecker.service;

import android.accounts.AuthenticatorException;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.otto.Produce;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.activity.Settings;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.downloader.EduxServer;
import cz.mpelant.fitchecker.model.Subject;
import cz.mpelant.fitchecker.utils.MainThreadBus;
import cz.mpelant.fitchecker.utils.NotificationHelper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * UpdateSubjectsService.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.service
 * @since 4/17/2014
 */
public class UpdateSubjectsService extends Service {


    private static final String ACTION = "cz.mpelant.fitchecker.UPDATE_SUBJECTS";

    public static class UpdateSubjectsException {
        private Exception mException;

        public UpdateSubjectsException(Exception exception) {
            mException = exception;
        }

        public Exception getException() {
            return mException;
        }
    }

    public static class UpdateSubjectsStatus {
        public static enum Status {
            STARTED, FINISHED
        }

        private Status mStatus;
        private EduxRequest mRequest;

        public UpdateSubjectsStatus(Status status, EduxRequest request) {
            mStatus = status;
            mRequest = request;
        }

        public UpdateSubjectsStatus(Status status) {
            mStatus = status;
        }

        public Status getStatus() {
            return mStatus;
        }


        /**
         * @return null means everything is done and the service is shutting down
         */
        @Nullable
        public EduxRequest getRequest() {
            return mRequest;
        }
    }

    public static class EduxRequest {
        private static final String URI = "uri";
        private static final String NOTIF = "notif";
        @NonNull
        private Uri mUri;
        private boolean showNotifications;


        public EduxRequest(@NonNull Uri uri) {
            this(uri, false);
        }

        public EduxRequest(@NonNull Uri uri, boolean showNotifications) {
            mUri = uri;
            this.showNotifications = showNotifications;
        }

        EduxRequest(Intent intent) {
            showNotifications = intent.getBooleanExtra(NOTIF, false);
            mUri = intent.getParcelableExtra(URI);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EduxRequest that = (EduxRequest) o;
            return showNotifications == that.showNotifications && mUri.equals(that.mUri);

        }

        @Override
        public int hashCode() {
            int result = mUri.hashCode();
            result = 31 * result + (showNotifications ? 1 : 0);
            return result;
        }


        void applyToIntent(Intent intent) {
            intent.putExtra(URI, mUri);
            intent.putExtra(NOTIF, showNotifications);
        }
    }

    public static class EduxResponse {
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
        private EduxRequest mRequest;
        private EduxResponse mResponse;

        Task(EduxRequest request) {
            mRequest = request;
            mResponse = new EduxResponse();
        }

        @Override
        public void run() {
            EduxServer server = new EduxServer(App.getInstance());
            Cursor c = App.getInstance().getContentResolver().query(mRequest.mUri, null, null, null, null);
            while (c.moveToNext()) {
                Subject s = new Subject(c);
                try {
                    boolean changes = server.downloadSubjectData(s.getName());
                    if (changes) {
                        onSubjectChanged(s);
                    }
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                    onTaskException(mRequest, e);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    onTaskException(mRequest, e);
                    break;
                } catch (EduxServer.CancelledException e) {
                    e.printStackTrace();
                    break;
                }
            }
            c.close();
            onTaskFinished(mRequest, mResponse);
        }

        private void onSubjectChanged(Subject subject) {
            mResponse.setSubjectChanged(subject);
            subject.setRead(false);
            Uri uri = DataProvider.getSubjectUri(subject.getId());
            App.getInstance().getContentResolver().update(uri, subject.getContentValuesReadOnly(), null, null);
            App.getInstance().getContentResolver().notifyChange(uri, null);
        }

        private void onTaskException(EduxRequest request, Exception e) {
            mResponse.errorOccured = true;
            bus.postOnMainThread(new UpdateSubjectsException(e));
        }
    }


    private int lastId;
    private static UpdateSubjectsStatus lastStatus;
    private MainThreadBus bus;
    private int tasksCount;

    public static Intent generateIntent(EduxRequest request) {
        Intent i = new Intent(ACTION);
        request.applyToIntent(i);
        return i;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bus = App.getInstance().getBus();
        bus.register(this);
        tasksCount = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastId = startId;
        onNewTask(new EduxRequest(intent));
        return START_REDELIVER_INTENT;
    }


    private synchronized void onNewTask(EduxRequest request) {
        post(new UpdateSubjectsStatus(UpdateSubjectsStatus.Status.STARTED, request));
        tasksCount++;
        new Task(request).start();
    }

    private synchronized void onTaskFinished(EduxRequest request, EduxResponse result) {
        tasksCount--;
        if (request.showNotifications) {//save last run time
            SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(this).edit();
            ed.putLong(Settings.PREF_ALARM_LAST_RUN, System.currentTimeMillis());
            ed.commit();
            if (result.isChangesDetected()) {
                new NotificationHelper(this).displayNotification(result.changedSubjects);
            }
        }

        post(new UpdateSubjectsStatus(UpdateSubjectsStatus.Status.FINISHED, request));
        if (tasksCount <= 0) {
            post(new UpdateSubjectsStatus(UpdateSubjectsStatus.Status.FINISHED));
            stopSelf(lastId);
        }
    }

    private void post(UpdateSubjectsStatus status) {
        lastStatus = status;
        bus.postOnMainThread(status);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Produce
    public static UpdateSubjectsStatus getLastStatus() {
        if (lastStatus == null) {
            return new UpdateSubjectsStatus(UpdateSubjectsStatus.Status.FINISHED);
        }

        return lastStatus;
    }


}
