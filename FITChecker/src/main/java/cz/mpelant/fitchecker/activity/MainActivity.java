package cz.mpelant.fitchecker.activity;

import android.content.Intent;
import cz.mpelant.fitchecker.fragment.SubjectsListFragment;

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
