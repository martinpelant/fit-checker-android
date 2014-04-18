package cz.mpelant.fitchecker.fragment;

import android.accounts.AuthenticatorException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
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
import cz.mpelant.fitchecker.fragment.dialog.DeleteSubjectDialog;
import cz.mpelant.fitchecker.model.Subject;
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
public class SubjectsListFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private SubjectAdapter mAdapter;
    private Bus bus;

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
        mAdapter.changeCursor(data);
        if (data != null && data.getCount() == 0) {
            setEmptyText(getString(R.string.subjects_empty_list));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setListShown(false);
        mAdapter.changeCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Subject subject = (Subject) view.findViewById(R.id.list_item).getTag();
        ((BaseFragmentActivity) getActivity()).replaceFragment(DisplaySubjectFragment.newInstance(subject));
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
        if (!isRefreshing()) {
            menuItem = menu.add(R.string.refresh);
            menuItem.setIcon(R.drawable.ic_refresh);
            MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    UpdateSubjectsService.EduxRequest request = new UpdateSubjectsService.EduxRequest(DataProvider.getSubjectsUri());
                    App.getInstance().startService(UpdateSubjectsService.generateIntent(request));
                    return true;
                }
            });
        }

        menuItem = menu.add(R.string.add);
        menuItem.setIcon(R.drawable.ic_new);
        MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ((BaseFragmentActivity) getActivity()).replaceFragment(new AddSubjectFragment());
                return true;
            }
        });

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