package cz.mpelant.fitchecker.fragment;

import android.os.Bundle;
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
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            mListView.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            mProgressContainer.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            mListView.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            mProgressContainer.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
    }
}
