package cz.mpelant.fitchecker.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
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

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("JobScheduler", "Job started");
        Intent serviceIntent = UpdateSubjectsService.generateIntent(new SubjectRequest(DataProvider.getSubjectsUri(), true));
        startService(serviceIntent);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("JobScheduler", "onStopJob");
        return false;
    }
}
