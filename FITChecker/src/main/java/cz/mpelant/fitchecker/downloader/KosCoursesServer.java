package cz.mpelant.fitchecker.downloader;

import android.accounts.AuthenticatorException;
import android.support.annotation.NonNull;
import android.util.Log;

import cz.mpelant.fitchecker.auth.KosAccount;
import cz.mpelant.fitchecker.auth.KosAccountManager;
import cz.mpelant.fitchecker.utils.Base64;
import cz.mpelant.fitchecker.utils.RestClient;
import cz.mpelant.fitchecker.utils.SubjectParser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * KosServer.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.downloader
 * @since 4/18/2014
 */
public class KosCoursesServer {

    private static final String KOS_API_URL = "https://kosapi.fit.cvut.cz/api/3/students/%s/";
    private static final String COURSES_METHOD = "enrolledCourses";
    private final String TAG = KosCoursesServer.class.getSimpleName();

    @NonNull
    public List<String> loadSubjects() throws IOException, AuthenticatorException, XmlPullParserException {
        if (!KosAccountManager.isAccount()) {
            throw new AuthenticatorException("No credentials");
        }
        KosAccount account = KosAccountManager.getAccount();
        RestClient client = new RestClient(String.format(KOS_API_URL, account.getUsername()), new DefaultHttpClient());
        String credentials = account.getUsername() + ":" + account.getPassword();
        ArrayList<NameValuePair> headers = new ArrayList<>();
        String base64EncodedCredentials = Base64.encodeBytes(credentials.getBytes());
        headers.add(new BasicNameValuePair("Authorization", "Basic " + base64EncodedCredentials));
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("fields", "entry(content(course))"));
        HttpResponse response = client.call(COURSES_METHOD, RestClient.Methods.GET, params, headers);
        if (client.getStatusCode() != null && client.getStatusCode() == HttpStatus.SC_OK) {
            String xmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
            Log.d(TAG, xmlResponse);
            List<String> subjects;
            if (xmlResponse.contains("<")) {
                subjects = SubjectParser.parseSubjects(xmlResponse);
            } else {
                subjects = Arrays.asList(xmlResponse.split(","));
            }


            return subjects;
        } else if (client.getStatusCode() != null && client.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticatorException("User not authorized");
        } else {
            throw new IOException("Unknown error");
        }
    }
}
