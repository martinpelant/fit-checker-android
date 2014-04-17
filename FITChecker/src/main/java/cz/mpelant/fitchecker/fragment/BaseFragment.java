package cz.mpelant.fitchecker.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import cz.mpelant.fitchecker.activity.BaseFragmentActivity;

/**
 * BaseFragment.java
 *
 * @author eMan s.r.o.
 * @project chlist-an
 * @package cz.eman.chlist.fragment
 * @since 3/30/2014
 */
public class BaseFragment extends Fragment {
    protected boolean mRefreshing;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onHomeAsUpSet();
    }

    protected void onHomeAsUpSet() {
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    protected void setTitle(String title) {
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    protected void setTitle(int titleRes) {
        setTitle(getString(titleRes));
    }

    protected void setRefreshing(boolean refreshing) {
        mRefreshing = refreshing;
        ((ActionBarActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(refreshing);
        getActivity().supportInvalidateOptionsMenu();
    }

    protected boolean isRefreshing() {
        return mRefreshing;
    }

    public void finish() {
        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            getActivity().finish();
        }
    }

}
