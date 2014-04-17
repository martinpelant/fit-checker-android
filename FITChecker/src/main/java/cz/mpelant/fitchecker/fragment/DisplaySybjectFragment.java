package cz.mpelant.fitchecker.fragment;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.view.*;
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
import cz.mpelant.fitchecker.service.UpdateSubjectsService;
import cz.mpelant.fitchecker.utils.MyReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * DisplaySybjectFragment.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment
 * @since 4/17/2014
 */
public class DisplaySybjectFragment extends BaseFragment {
    public static final String SUBEJCT = "subject";

    private class HtmlLoader extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //Set this subject as read
            Subject subject = getSubject();
            subject.setRead(true);
            App.getInstance().getContentResolver().update(subjectUri, subject.getContentValuesReadOnly(), null, null);
            App.getInstance().getContentResolver().notifyChange(subjectUri, myObserver);

            //load html from resources
            String head = MyReader.getString(getResources().openRawResource(R.raw.head));
            File file = EduxServer.getSubejctFile(getSubject().getName());
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                UpdateSubjectsService.EduxRequest request = new UpdateSubjectsService.EduxRequest(subjectUri);
                App.getInstance().startService(UpdateSubjectsService.generateIntent(request));
                return false;
            }
            String text = MyReader.getString(fis);
            if (text.equals(EduxServer.ERROR_PATTERN_NOT_FOUND)) {
                text = MyReader.getString(getResources().openRawResource(R.raw.errorpage));
                text += "<h1>" + getResources().getString(R.string.error_table_not_found_title) + "</h1>";
                text += "<p>" + getResources().getString(R.string.error_table_not_found) + "</p>";
            } else {
                text = head + text;
                text += "<br>";
                text += "<center>";
            }

            text += "<a target=\"_blank\" href=\"" + EduxServer.URL_EDUX + EduxServer.getSubjectClassificationURL(getSubject().getName(), App.getInstance()) + "\">";
            text += getResources().getString(R.string.subject_open_in_browser) + "</a>";
            webContent = text;
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
    WebView webView;

    private String webContent;
    private Subject mSubject;
    private Bus mBus;
    private Uri subjectUri;


    public static Bundle generateArgs(Subject subject) {
        Bundle b = new Bundle();
        b.putParcelable(SUBEJCT, subject);
        return b;
    }

    public static DisplaySybjectFragment newInstance(Subject subject) {
        DisplaySybjectFragment f = new DisplaySybjectFragment();
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
        subjectUri = DataProvider.getSubjectUri(getSubject().getId());
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
        getActivity().getContentResolver().registerContentObserver(subjectUri, true, myObserver);
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
        return inflater.inflate(R.layout.display_subject, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        if(webContent==null){
            loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void loadData() {
        new HtmlLoader().execute();
    }

    private void onLoadFinished() {
        if (webContent != null && webView != null) {
            webView.loadDataWithBaseURL("file:///android_asset/", webContent, "text/html", "utf-8", null);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        }
    }


    @Subscribe
    public void onUpdateServiceChanged(UpdateSubjectsService.UpdateSubjectsStatus status) {
        boolean refreshing = status.getStatus() == UpdateSubjectsService.UpdateSubjectsStatus.Status.STARTED;
        setRefreshing(refreshing);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem;
        if (!isRefreshing()) {
            menuItem = menu.add(R.string.refresh);
            menuItem.setIcon(R.drawable.ic_refresh);
            MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    UpdateSubjectsService.EduxRequest request = new UpdateSubjectsService.EduxRequest(subjectUri);
                    App.getInstance().startService(UpdateSubjectsService.generateIntent(request));
                    return true;
                }
            });
        }

        menuItem = menu.add(R.string.settings);
        menuItem.setIcon(R.drawable.ic_settings);
        MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(getActivity(), Settings.class));
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


}
