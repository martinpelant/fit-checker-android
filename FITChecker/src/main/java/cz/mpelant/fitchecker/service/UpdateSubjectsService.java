package cz.mpelant.fitchecker.service;

import android.accounts.AuthenticatorException;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
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

/**
 * UpdateSubjectsService.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.service
 * @since 4/17/2014
 */
public class UpdateSubjectsService extends Service {
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
        private SubjectRequest mRequest;

        public UpdateSubjectsStatus(Status status, SubjectRequest request) {
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
        public SubjectRequest getRequest() {
            return mRequest;
        }
    }


    private class Task extends Thread {
        private SubjectRequest mRequest;
        private SubjectResponse mResponse;

        Task(SubjectRequest request) {
            mRequest = request;
            mResponse = new SubjectResponse();
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

        private void onTaskException(SubjectRequest request, Exception e) {
            mResponse.errorOccured = true;
            bus.postOnMainThread(new UpdateSubjectsException(e));
        }
    }


    private int lastId;
    private static UpdateSubjectsStatus lastStatus;
    private MainThreadBus bus;
    private int tasksCount;

    public static Intent generateIntent(SubjectRequest request) {
        Intent i = new Intent(App.getInstance(), UpdateSubjectsService.class);
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
        tasksCount = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SubjectRequest request = new SubjectRequest(intent);
        lastId = startId;
        onNewTask(request);
        startService(UpdateExamsService.generateIntent(request));
        return START_REDELIVER_INTENT;
    }


    private synchronized void onNewTask(SubjectRequest request) {
        post(new UpdateSubjectsStatus(UpdateSubjectsStatus.Status.STARTED, request));
        if (bus == null) {
            bus = App.getInstance().getBus();
            bus.register(this);
        }
        tasksCount++;
        new Task(request).start();
    }

    private synchronized void onTaskFinished(SubjectRequest request, SubjectResponse result) {
        tasksCount--;
        if (request.showNotifications) {//save last run time
            SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(this).edit();
            ed.putLong(Settings.PREF_ALARM_LAST_RUN, System.currentTimeMillis());
            ed.apply();
            if (result.isChangesDetected()) {
                new NotificationHelper(this).displayNotification(result.getChangedSubjects(), NotificationHelper.NotificationType.EDUX);
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
        if (bus != null) {
            bus.postOnMainThread(status);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bus != null) {
            bus.unregister(this);
        }
    }

    @Produce
    public static UpdateSubjectsStatus getLastStatus() {
        if (lastStatus == null) {
            return new UpdateSubjectsStatus(UpdateSubjectsStatus.Status.FINISHED);
        }

        return lastStatus;
    }


}
