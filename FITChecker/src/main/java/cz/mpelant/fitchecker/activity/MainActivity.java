package cz.mpelant.fitchecker.activity;

import android.content.Intent;
import android.os.Bundle;

import cz.mpelant.fitchecker.downloader.KosExamsServer;
import cz.mpelant.fitchecker.fragment.SubjectsListFragment;
import cz.mpelant.fitchecker.service.UpdateExamsService;

/**
 * MainActivity.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.activity
 * @since 4/17/2014
 */
public class MainActivity extends BaseFragmentActivity {
    @Override
    protected String getFragmentName() {
        return SubjectsListFragment.class.getName();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        clearFragmentBackStack();
    }


}
