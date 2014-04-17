package cz.mpelant.fitchecker.activity;

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
}
