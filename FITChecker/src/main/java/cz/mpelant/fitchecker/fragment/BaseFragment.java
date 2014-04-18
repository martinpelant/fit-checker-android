package cz.mpelant.fitchecker.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.activity.LoginActivity;
import cz.mpelant.fitchecker.service.UpdateSubjectsService;

/**
 * BaseFragment.java
 *
 * @author eMan s.r.o.
 * @project chlist-an
 * @package cz.eman.chlist.fragment
 * @since 3/30/2014
 */
public class BaseFragment extends Fragment {
    public static final int REQ_LOGIN = 165;
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

    protected void setTitle(@StringRes int titleRes) {
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
        if(getActivity()==null){
            return;
        }
        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setRefreshing(UpdateSubjectsService.getLastStatus().getStatus().equals(UpdateSubjectsService.UpdateSubjectsStatus.Status.STARTED));
    }

    protected void onAuthError() {
        startActivityForResult(new Intent(getActivity(), LoginActivity.class), REQ_LOGIN);
    }

    protected void onIOError() {
        Toast.makeText(getActivity(), R.string.error_download, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_LOGIN && resultCode == Activity.RESULT_CANCELED) {
            getActivity().finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
