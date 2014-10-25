package cz.mpelant.fitchecker.fragment;

import android.accounts.AuthenticatorException;
import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.activity.Settings;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.downloader.EduxServer;
import cz.mpelant.fitchecker.model.Subject;
import cz.mpelant.fitchecker.service.SubjectRequest;
import cz.mpelant.fitchecker.service.UpdateSubjectsService;
import cz.mpelant.fitchecker.utils.MyReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * DisplaySubjectFragment.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment
 * @since 4/17/2014
 */
public class DisplaySubjectFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String SUBEJCT = "subject";

    private class HtmlLoader extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //Set this subject as read
            Subject subject = getSubject();
            subject.setRead(true);
            App.getInstance().getContentResolver().update(mSubjectUri, subject.getContentValuesReadOnly(), null, null);
            App.getInstance().getContentResolver().notifyChange(mSubjectUri, myObserver);

            //load html from resources
            String head = MyReader.getString(App.getInstance().getResources().openRawResource(R.raw.head));
            File file = EduxServer.getSubejctFile(getSubject().getName());
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                onRefresh();
                return false;
            }
            String text = MyReader.getString(fis);
            if (text.equals(EduxServer.ERROR_PATTERN_NOT_FOUND)) {
                text = MyReader.getString(App.getInstance().getResources().openRawResource(R.raw.errorpage));
                text += "<h1>" + App.getInstance().getResources().getString(R.string.error_table_not_found_title) + "</h1>";
                text += "<p>" + App.getInstance().getResources().getString(R.string.error_table_not_found) + "</p>";
            } else {
                text = head + text;
                text += "<br>";
                text += "<center>";
            }

            text += "<a target=\"_blank\" href=\"" + EduxServer.URL_EDUX + EduxServer.getSubjectClassificationURL(getSubject().getName()) + "\">";
            text += App.getInstance().getResources().getString(R.string.subject_open_in_browser) + "</a>";
            mWebContent = text;
            try {
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (Boolean.TRUE.equals(result)) {
                onLoadFinished();
            }
        }

    }

    private ContentObserver myObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            loadData();
        }
    };


    @InjectView(R.id.wvBody)
    WebView mWebView;
    @InjectView(R.id.progressContainer)
    View mProgressContainer;
    @InjectView(R.id.content)
    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean mContentShown = true;

    private String mWebContent;
    private Subject mSubject;
    private Bus mBus;
    private Uri mSubjectUri;


    public static Bundle generateArgs(Subject subject) {
        Bundle b = new Bundle();
        b.putParcelable(SUBEJCT, subject);
        return b;
    }

    public static DisplaySubjectFragment newInstance(Subject subject) {
        DisplaySubjectFragment f = new DisplaySubjectFragment();
        f.setArguments(generateArgs(subject));
        return f;
    }

    private Subject getSubject() {
        if (mSubject == null) {
            mSubject = (Subject) getArguments().get(SUBEJCT);
        }
        return mSubject;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBus = App.getInstance().getBus();
        mSubjectUri = DataProvider.getSubjectUri(getSubject().getId());
        setHasOptionsMenu(true);
//        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
        getActivity().getContentResolver().registerContentObserver(mSubjectUri, true, myObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(this);
        getActivity().getContentResolver().unregisterContentObserver(myObserver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle(getSubject().getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.display_subject, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.refresh_color1, R.color.refresh_color2, R.color.refresh_color3, R.color.refresh_color4);
        if (mWebContent == null) {
            setContentShown(false);
            loadData();
        } else {
            onLoadFinished();
        }
    }

    private void loadData() {
        new HtmlLoader().execute();
    }

    private void onLoadFinished() {
        if (mWebContent != null && mWebView != null) {
            mWebView.loadDataWithBaseURL("file:///android_asset/", mWebContent, "text/html", "utf-8", null);
            mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            setContentShown(true);
        } else {
            setContentShown(false);
        }


    }


    @Subscribe
    public void onUpdateServiceChanged(UpdateSubjectsService.UpdateSubjectsStatus status) {
        boolean refreshing = status.getStatus() == UpdateSubjectsService.UpdateSubjectsStatus.Status.STARTED;
        setRefreshing(refreshing);
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Subscribe
    public void onException(UpdateSubjectsService.UpdateSubjectsException exception) {
        if (exception.getException() instanceof AuthenticatorException) {
            onAuthError();
            return;
        }
        if (exception.getException() instanceof IOException) {
            onIOError();
        }

    }


    @Override
    protected void setRefreshing(final boolean refreshing) {
        super.setRefreshing(refreshing);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout.isRefreshing() != refreshing)
                    mSwipeRefreshLayout.setRefreshing(refreshing);
                if (getActivity() != null) {
                    getActivity().supportInvalidateOptionsMenu();
                }
            }
        });
    }


    @Override
    protected boolean isRefreshing() {
        if (mSwipeRefreshLayout == null) {
            return false;
        }
        return mSwipeRefreshLayout.isRefreshing();
    }

    @Override
    public void onRefresh() {
        SubjectRequest request = new SubjectRequest(mSubjectUri);
        App.getInstance().startService(UpdateSubjectsService.generateIntent(request));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem;
        if (!isRefreshing()) {
            menuItem = menu.add(R.string.refresh);
            MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    onRefresh();
                    return true;
                }
            });
        }

        menuItem = menu.add(R.string.settings);
        MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(Settings.generateIntent(getActivity()));
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    public void setContentShown(boolean shown) {
        if (mContentShown == shown) {
            return;
        }
        mContentShown = shown;
        if (shown) {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            mWebView.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            mProgressContainer.setVisibility(View.INVISIBLE);
            mWebView.setVisibility(View.VISIBLE);
        } else {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            mWebView.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            mProgressContainer.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_LOGIN && resultCode == Activity.RESULT_OK) {
            onRefresh();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
