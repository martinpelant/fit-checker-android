package cz.mpelant.fitchecker.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.squareup.otto.Subscribe;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.db.DataProvider;

/**
 * UpdateJobService.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.service
 * @since 11/6/2014
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class UpdateJobService extends JobService {
    private JobParameters mParams;

    @Override
    public void onCreate() {
        super.onCreate();
        App.getInstance().getBus().register(this);
    }

    @Override
    public void onDestroy() {
        App.getInstance().getBus().unregister(this);
        super.onDestroy();
    }


    @Subscribe
    public void onTaskFinished(UpdateExamsService.UpdateExamStatus status) {
        if (status.getRequest() == null && status.getStatus() == UpdateExamsService.UpdateExamStatus.Status.FINISHED) {
            jobFinished(mParams, false);
        }
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        if (!params.isOverrideDeadlineExpired()) {
            mParams = params;
            Log.d("JobScheduler", "Job started");
            Intent serviceIntent = UpdateSubjectsService.generateIntent(new SubjectRequest(DataProvider.getSubjectsUri(), true));
            startService(serviceIntent);
        } else {
            return false;
        }
        return true;

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("JobScheduler", "onStopJob");
        Intent serviceIntent = UpdateSubjectsService.generateIntent(new SubjectRequest(DataProvider.getSubjectsUri(), true));
        stopService(serviceIntent);
        return false;
    }
}
