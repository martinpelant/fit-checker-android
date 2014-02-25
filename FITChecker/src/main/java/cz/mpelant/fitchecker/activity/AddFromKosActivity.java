package cz.mpelant.fitchecker.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.utils.DataProvider;
import cz.mpelant.fitchecker.utils.RestClient;
import cz.mpelant.fitchecker.utils.Base64;
import cz.mpelant.fitchecker.utils.SubjectParser;

/**
 * Activity that downloads subjects of student from KOS API and store it as student subjects
 * Created by David Bilik[david.bilik@eman.cz] on 22.2.14.
 */
public class AddFromKosActivity extends BaseActivity {

    private static final int LOGIN_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_add_from_kos);
        if (sp.contains(Login.PREFERENCES_USERNAME)) {
            new DownloadAsynctask().execute();
        } else {
            startActivityForResult(new Intent(this, Login.class), LOGIN_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            new DownloadAsynctask().execute();
        }
    }

    private class DownloadAsynctask extends AsyncTask<Void, Void, Integer> {

        private final int OK = 1;
        private final int ERROR = 2;

        private static final String KOS_API_URL = "https://kosapi.fit.cvut.cz/api/3/students/%s/";
        private static final String COURSES_METHOD = "enrolledCourses";
        private final String TAG = DownloadAsynctask.class.getSimpleName();

        @Override
        protected Integer doInBackground(Void... p) {
            String login, password;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AddFromKosActivity.this);
            password = sp.getString(Login.PREFERENCES_PASSWORD, "");
            login = sp.getString(Login.PREFERENCES_USERNAME, "");
            try {
                RestClient client = new RestClient(String.format(KOS_API_URL, login), new DefaultHttpClient());
                String credentials = login + ":" + password;
                ArrayList<NameValuePair> headers = new ArrayList<>();
                String base64EncodedCredentials = Base64.encodeBytes(credentials.getBytes());
                headers.add(new BasicNameValuePair("Authorization", "Basic " + base64EncodedCredentials));
                ArrayList<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("fields", "entry(content(course))"));
                HttpResponse response = client.call(COURSES_METHOD, RestClient.Methods.GET, params, null, headers);
                if (client.getStatusCode() != null && client.getStatusCode() == HttpStatus.SC_OK) {
                    String xmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                    Log.d(TAG, xmlResponse);
                    List<String> subjects;
                    try {
                        subjects = SubjectParser.parseSubjects(xmlResponse);
                    }catch (XmlPullParserException e){
                        if(xmlResponse.contains("<")){
                            throw e; //re-throw exeption if response is in xml format
                        }
                        subjects =  Arrays.asList(xmlResponse.split(","));//try to split subjects in case api returns MI-SUB1,MI-SUB2... instead of XML response
                    }

                    DataProvider data = new DataProvider(AddFromKosActivity.this);
                    data.open();
                    for (String subject : subjects) {
                        long row = data.subjectCreate(subject.toUpperCase().trim());
                        Log.d(TAG, "saved " + row);
                    }
                    data.close();
                    return OK;
                } else {
                    Log.d(TAG, client.getStatusCode().toString());
                    return ERROR;
                }
            } catch (Exception e) {
                return ERROR;
            }
        }




        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            finish();
            return;
        }
    }
}

