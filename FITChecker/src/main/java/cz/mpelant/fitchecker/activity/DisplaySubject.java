
package cz.mpelant.fitchecker.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;


import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.downloader.Downloader;
import cz.mpelant.fitchecker.downloader.EduxServer;
import cz.mpelant.fitchecker.utils.DataProvider;
import cz.mpelant.fitchecker.utils.MyReader;

@Deprecated public class DisplaySubject extends BaseActivity {
    public static final String INTENT_NAME = "name";
    public static final int DIALOG_REFRESH = 1;
    private WebView webView;
    private static Downloader myDownloader;
    private String head;
    public static String TAG = "fitchecker";
    private DataProvider data;
    private String subject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        subject = i.getStringExtra(INTENT_NAME);
        if (subject == null)
            subject = "BI-ZDM";
        data = new DataProvider(this);
        data.open();
        data.subjectRead(subject);
        data.close();
        setTitle(subject);
        setContentView(R.layout.display_subject);
        webView = (WebView) findViewById(R.id.wvBody);
        head = MyReader.getString(getResources().openRawResource(R.raw.head));
        loadData();

    }

    private void loadData() {
        File dir = getFilesDir();
        File file = new File(dir, subject + ".html");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
            myDownloader = new MyDownloader(this);
            myDownloader.execute(subject);
            return;
        }
        String text = MyReader.getString(fis);
        if (text.equals(Downloader.ERROR_PATTERN_NOT_FOUND)) {
            text = MyReader.getString(getResources().openRawResource(R.raw.errorpage));
            text += "<h1>" + getResources().getString(R.string.error_table_not_found_title) + "</h1>";
            text += "<p>" + getResources().getString(R.string.error_table_not_found) + "</p>";
        } else {
            text = head + text;
            text += "<br>";
            text += "<center>";
        }

        text += "<a target=\"_blank\" href=\"" + Downloader.URL_EDUX + Downloader.getSubjectClassificationURL(subject, this) + "\">";
        text += getResources().getString(R.string.subject_open_in_browser) + "</a>";
        webView.loadDataWithBaseURL("file:///android_asset/", text, "text/html", "utf-8", null);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        try {
            fis.close();
        } catch (Exception e) {
        }
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
                    myDownloader = new MyDownloader(DisplaySubject.this);
                    myDownloader.execute(subject);
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
                startActivity(new Intent(DisplaySubject.this, Settings.class));
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Downloader.REQ_CODE_LOGIN && resultCode == RESULT_OK) {
            loadData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyDownloader extends Downloader {
        public MyDownloader(Activity context) {
            super(context);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!cancelled)
                setRefreshing(true);
            // showDialog(DIALOG_REFRESH);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // try {
            // mProgressBar.dismiss();
            // removeDialog(DIALOG_REFRESH);
            // } catch (Exception e) {
            // }
            setRefreshing(false);
            if (result)
                loadData();
            else if (!cancelled)
                if (errorMessage != null)
                    Toast.makeText(DisplaySubject.this, errorMessage, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(DisplaySubject.this, R.string.error_download, Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onProgressUpdate(String... values) {
            // try {
            // mProgressBar.setMessage(values[0]);
            // } catch (Exception e) {
            // }
            // super.onProgressUpdate(values);
        }

    }
}
