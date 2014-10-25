package cz.mpelant.fitchecker.fragment;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import com.squareup.otto.Subscribe;

import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.activity.BaseFragmentActivity;
import cz.mpelant.fitchecker.activity.Settings;
import cz.mpelant.fitchecker.adapter.ExamAdapter;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.fragment.dialog.AddExamToCalendartDialog;
import cz.mpelant.fitchecker.model.Exam;
import cz.mpelant.fitchecker.model.Subject;
import cz.mpelant.fitchecker.service.SubjectRequest;
import cz.mpelant.fitchecker.service.UpdateExamsService;
import cz.mpelant.fitchecker.service.UpdateSubjectsService;
import cz.mpelant.fitchecker.utils.MainThreadBus;

/**
 * Fragment with exam list
 * Created by David Bilik[david.bilik@ackee.cz] on 15. 5. 2014.
 */
public class ExamListFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {
    protected static final String TAG = ExamListFragment.class.getName();
    private ExamAdapter mAdapter;
    private MainThreadBus mBus;

    public static ExamListFragment newInstance(Subject subject) {
        ExamListFragment f = new ExamListFragment();
        f.setArguments(DisplaySubjectFragment.generateArgs(subject));
        return f;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new ExamAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        getLoaderManager().initLoader(0, null, this);
        setListShown(false);
        getListView().setAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
        setHasOptionsMenu(true);

        ((SwipeRefreshLayout) listViewContainer).setOnRefreshListener(this);
        ((SwipeRefreshLayout) listViewContainer).setColorSchemeResources(R.color.refresh_color1, R.color.refresh_color2, R.color.refresh_color3, R.color.refresh_color4);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBus = App.getInstance().getBus();

    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), DataProvider.getExamsUri(), null, Exam.COL_SUBJECT + " = ?", new String[]{getSubject().getName()}, Exam.COL_DATE + " ASC");
    }

    private Subject getSubject() {
        return getArguments().getParcelable(DisplaySubjectFragment.SUBEJCT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) {
            setEmptyText(getString(R.string.noExams));
        }
        mAdapter.swapCursor(data);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
                startActivity(new Intent(getActivity(), Settings.class));
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onRefresh() {
        SubjectRequest request = new SubjectRequest(DataProvider.getSubjectUri(getSubject().getId()));
        App.getInstance().startService(UpdateSubjectsService.generateIntent(request));
    }

    @Override
    protected void setRefreshing(final boolean refreshing) {
        super.setRefreshing(refreshing);

        listViewContainer.post(new Runnable() {
            @Override
            public void run() {
                if (((SwipeRefreshLayout) listViewContainer).isRefreshing() != refreshing)
                    ((SwipeRefreshLayout) listViewContainer).setRefreshing(refreshing);
                if (getActivity() != null) {
                    getActivity().supportInvalidateOptionsMenu();
                }
            }
        });

    }


    @Override
    protected boolean isRefreshing() {
        return (listViewContainer) != null && ((SwipeRefreshLayout) listViewContainer).isRefreshing();
    }

    @Subscribe
    public void onUpdateServiceChanged(UpdateExamsService.UpdateExamStatus status) {
        boolean refreshing = status.getStatus() == UpdateExamsService.UpdateExamStatus.Status.STARTED;
        setRefreshing(refreshing);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Exam exam = (Exam) view.findViewById(R.id.list_item).getTag();
        AddExamToCalendartDialog.newInstance(exam).show(getFragmentManager(), "calendar");
    }
}
