
package cz.mpelant.fitchecker.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.*;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.downloader.Downloader;
import cz.mpelant.fitchecker.utils.DataProvider;

@Deprecated
public class ListSubjects extends BaseActivity {
    private List<Subject> subjects;
    private static final int ADD = 1;
    public static String TAG = "fitchecker";
    private ListView lw;
    private DataProvider data;
    private static MyDownloader myDownloader;
    private boolean dataLoaded = false;

    private class Subject {
        public String name;
        public int changed;

        public Subject(String name, int changed) {
            this.name = name;
            this.changed = changed;
        }
    }

    private class MyAdapter extends BaseAdapter {
        private List<Subject> elements;
        private Context mContext;

        public MyAdapter(Context mContext, List<Subject> elements) {
            this.mContext = mContext;
            this.elements = elements;
        }

        @Override
        public int getCount() {
            return elements.size();
        }

        @Override
        public Object getItem(int position) {
            return elements.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout rowLayout;
            final Subject subject = elements.get(position);
            if (convertView == null) {
                rowLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.row_subject, parent, false);
            } else {
                rowLayout = (LinearLayout) convertView;
            }

            TextView text = (TextView) rowLayout.findViewById(R.id.subject);
            ImageView changed = (ImageView) rowLayout.findViewById(R.id.changed);
            text.setText(subject.name);
            if (subject.changed == 1)
                changed.setVisibility(View.VISIBLE);
            else
                changed.setVisibility(View.INVISIBLE);

            return rowLayout;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.list);
        data = new DataProvider(this);
        lw = (ListView) findViewById(R.id.subjectsList);
        subjects = new ArrayList<Subject>();

        lw.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String subject = subjects.get(position).name;
                Intent i = new Intent(ListSubjects.this, DisplaySubject.class);
                i.putExtra(DisplaySubject.INTENT_NAME, subject);
                startActivity(i);

            }

        });
        lw.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ListSubjects.this);
                alertDialog.setTitle(R.string.delete_title);
                alertDialog.setMessage(R.string.delete_message);
                alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        data.open();
                        data.subjectDelete(subjects.get(position).name);
                        data.close();
                        File cacheDir = getCacheDir();
                        File cacheFile = new File(cacheDir, subjects.get(position).name + ".html");
                        cacheFile.delete();
                        if (!loadData(true))
                            startActivityForResult(new Intent(ListSubjects.this, AddSubject.class), ADD);
                    }
                });
                alertDialog.setNegativeButton(android.R.string.no, null);
                alertDialog.show();
                return true;
            }
        });
        if (!loadData())
            startActivityForResult(new Intent(this, AddSubject.class), ADD);

    }

    @Override
    protected void onPause() {
        dataLoaded = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        loadData();
        super.onResume();
    }

    private boolean loadData() {
        return loadData(false);
    }

    private boolean loadData(boolean reload) {
        if (dataLoaded && !reload)
            return subjects.size() == 0 ? false : true;
        subjects.clear();
        data.open();
        Cursor c = data.subjectFetchAll();
        int name = c.getColumnIndex(DataProvider.SUBJECTS_NAME);
        int changed = c.getColumnIndex(DataProvider.SUBJECTS_NEW);
        if (c != null) {
            if (c.moveToFirst() && c.getCount() > 0) {
                do {
                    subjects.add(new Subject(c.getString(name), c.getInt(changed)));
                } while (c.moveToNext());
            }
        }
        c.close();
        data.close();
        if (subjects.size() == 0)
            return false;
        MyAdapter adapter = new MyAdapter(this, subjects);
        lw.setAdapter(adapter);
        dataLoaded = true;
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;
        if (!isRefreshing) {
            menuItem = menu.add(R.string.refresh);
            menuItem.setIcon(R.drawable.ic_refresh);
            MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    myDownloader = new MyDownloader(ListSubjects.this);
                    myDownloader.execute(getSubjectNames());
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
                startActivityForResult(new Intent(ListSubjects.this, AddSubject.class), ADD);
                return true;
            }
        });

        menuItem = menu.add(R.string.settings);
        menuItem.setIcon(R.drawable.ic_settings);
        MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(ListSubjects.this, Settings.class));
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private String[] getSubjectNames() {
        String names[] = new String[subjects.size()];
        for (int i = 0; i < subjects.size(); i++) {
            names[i] = subjects.get(i).name;
        }
        return names;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Downloader.REQ_CODE_LOGIN:
                if (resultCode == RESULT_OK) {
                    myDownloader = new MyDownloader(this);
                    myDownloader.execute(getSubjectNames());
                }
            case ADD:
                if (!loadData())
                    finish();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    class MyDownloader extends Downloader {
        private int stage = 0;

        public MyDownloader(Activity context) {
            super(context, true);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!cancelled) {
                setRefreshing(true);
//                showDialog(DIALOG_REFRESH);
//                mProgressBar.setProgress(0);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
//            try {
//                mProgressBar.dismiss();
//            } catch (Exception e) {
//            }
            setRefreshing(false);
            if (result)
                loadData(true);

            else if (!cancelled)
                if (errorMessage != null)
                    Toast.makeText(ListSubjects.this, errorMessage, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ListSubjects.this, R.string.error_download, Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onProgressUpdate(String... values) {
//            try {
//                if (values.length == 2 && values[1] != null) {
//
//                    mProgressBar.setMessage(values[1] + " - " + values[0]);
//                    int progress = (int) (stage++ * (100.0 / (subjects.size() * 3)));
//                    if (progress > 100)
//                        progress = 100;
//                    mProgressBar.setProgress(progress);
//                } else
//                    mProgressBar.setMessage(values[0]);
//            } catch (Exception e) {
//            }
//            super.onProgressUpdate(values);
        }

    }

}
