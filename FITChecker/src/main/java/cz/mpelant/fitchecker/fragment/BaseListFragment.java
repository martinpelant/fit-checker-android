package cz.mpelant.fitchecker.fragment;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import cz.mpelant.fitchecker.R;

/**
 * BaseListFragment.java
 *
 * @author eMan s.r.o.
 * @project shoplist
 * @package cz.eman.shoplist.fragment
 * @since 8/14/13
 */
public abstract class BaseListFragment extends BaseFragment {
    private View mProgressContainer;
    protected View listViewContainer;
    private TextView mEmptyTextView;
    boolean mListShown = true;
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(getLayoutId(), container, false);
        listViewContainer = ButterKnife.findById(layout, R.id.listContainer);
        mProgressContainer = ButterKnife.findById(layout, R.id.progressContainer);
        mListView = ButterKnife.findById(layout, android.R.id.list);
        mEmptyTextView = ButterKnife.findById(layout, android.R.id.empty);

        setListShown(false);
        return layout;
    }

    protected int getLayoutId() {
        return R.layout.activity_lists;
    }

    protected ListView getListView() {
        return mListView;
    }


    public void setEmptyText(CharSequence text) {
        if(mEmptyTextView==null){
            return;
        }
        mEmptyTextView.setText(text);
        mListView.setEmptyView(mEmptyTextView);
    }

    /**
     * Control whether the list is being displayed. You can make it not
     * displayed if you are waiting for the initial data to show in it. During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown If true, the list view is shown; if false, the progress
     *              indicator. The initial value is true.
     */
    public void setListShown(boolean shown) {
        if(mProgressContainer==null){
            return;
        }
        if (mListShown == shown) {
            return;
        }
        Log.v("Show/Hide", shown + "");
        mListShown = shown;
        long duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);


        if(Build.VERSION.SDK_INT>=12){
            if (shown) {
                mProgressContainer.animate().alpha(0).setDuration(duration);
                mListView.animate().alpha(1).setDuration(duration);
            } else {
                mProgressContainer.animate().alpha(1).setDuration(duration);
                mListView.animate().alpha(0).setDuration(duration);

            }

        }else{
            if (shown) {
                mProgressContainer.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            } else {
                mProgressContainer.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);

            }
        }

    }
}
