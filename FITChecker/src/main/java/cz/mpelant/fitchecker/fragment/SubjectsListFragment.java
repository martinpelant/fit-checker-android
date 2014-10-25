package cz.mpelant.fitchecker.fragment;

import android.accounts.AuthenticatorException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.activity.BaseFragmentActivity;
import cz.mpelant.fitchecker.activity.Settings;
import cz.mpelant.fitchecker.adapter.SubjectAdapter;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.fragment.dialog.DeleteAllSubjectsDialog;
import cz.mpelant.fitchecker.fragment.dialog.DeleteSubjectDialog;
import cz.mpelant.fitchecker.model.Subject;
import cz.mpelant.fitchecker.service.SubjectRequest;
import cz.mpelant.fitchecker.service.UpdateSubjectsService;

import java.io.IOException;

/**
 * SubjectsListFragment.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment
 * @since 4/17/2014
 */
public class SubjectsListFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    private SubjectAdapter mAdapter;
    private Bus bus;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus = App.getInstance().getBus();
        mAdapter = new SubjectAdapter(getActivity(), null, Context.BIND_ADJUST_WITH_ACTIVITY);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) listViewContainer;
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(R.color.refresh_color1, R.color.refresh_color2, R.color.refresh_color3, R.color.refresh_color4);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle(R.string.app_name);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onHomeAsUpSet() {
        ((ActionBarActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), DataProvider.getSubjectsUri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        setListShown(true);
        mAdapter.swapCursor(data);
        if (data != null && data.getCount() == 0) {
            setEmptyText(getString(R.string.subjects_empty_list));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
        setListShown(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Subject subject = (Subject) view.findViewById(R.id.list_item).getTag();
        ((BaseFragmentActivity) getActivity()).replaceFragment(SubjectFragment.newInstance(subject));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Subject subject = (Subject) view.findViewById(R.id.list_item).getTag();
        DeleteSubjectDialog.newInstance(subject).show(getFragmentManager(), "delete");
        return true;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        MenuItem menuItem;


        menuItem = menu.add(R.string.add);
        menuItem.setIcon(R.drawable.ic_action_add);
        MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ((BaseFragmentActivity) getActivity()).replaceFragment(new AddSubjectFragment());
                return true;
            }
        });


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
        menuItem = menu.add(R.string.deleteAllSubjects);
        MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                deleteAllSubjects();
                return true;
            }
        });

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

    private void deleteAllSubjects() {
        new DeleteAllSubjectsDialog().show(getFragmentManager(), "deleteAll");
    }


    @Override
    protected void setRefreshing(boolean refreshing) {
        super.setRefreshing(refreshing);
        if (mSwipeRefreshLayout.isRefreshing() != refreshing)
            mSwipeRefreshLayout.setRefreshing(refreshing);
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
        SubjectRequest request = new SubjectRequest(DataProvider.getSubjectsUri());
        App.getInstance().startService(UpdateSubjectsService.generateIntent(request));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_LOGIN && resultCode == Activity.RESULT_OK) {
            onRefresh();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}