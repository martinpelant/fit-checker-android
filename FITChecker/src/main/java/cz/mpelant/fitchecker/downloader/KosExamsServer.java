package cz.mpelant.fitchecker.downloader;

import android.accounts.AuthenticatorException;
import android.support.annotation.NonNull;
import android.util.Log;

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
import java.util.Set;

import cz.mpelant.fitchecker.auth.KosAccount;
import cz.mpelant.fitchecker.auth.KosAccountManager;
import cz.mpelant.fitchecker.model.Exam;
import cz.mpelant.fitchecker.utils.Base64;
import cz.mpelant.fitchecker.utils.ExamParser;
import cz.mpelant.fitchecker.utils.RestClient;
import cz.mpelant.fitchecker.utils.SubjectParser;

/**
 * KosServer.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.downloader
 * @since 4/18/2014
 */
public class KosExamsServer {
    private static final String KOS_API_BASE = "https://kosapi.fit.cvut.cz/api/3/";
    private static final String REGISTERED_EXAMS_METHOD = "students/%s/registeredExams";
    private static final String KOS_API_URL = "https://kosapi.fit.cvut.cz/api/3/courses/%s/";
    private static final String EXAMS_METHOD = "exams";
    private final String TAG = KosExamsServer.class.getSimpleName();

    @NonNull
    public List<Exam> loadExams(String subject) throws IOException, AuthenticatorException, XmlPullParserException {
        if (!KosAccountManager.isAccount()) {
            throw new AuthenticatorException("No credentials");
        }
        KosAccount account = KosAccountManager.getAccount();
        RestClient client = new RestClient(String.format(KOS_API_URL, subject), new DefaultHttpClient());
        String credentials = account.getUsername() + ":" + account.getPassword();
        ArrayList<NameValuePair> headers = new ArrayList<>();
        String base64EncodedCredentials = Base64.encodeBytes(credentials.getBytes());
        headers.add(new BasicNameValuePair("Authorization", "Basic " + base64EncodedCredentials));
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("fields", "entry(id,content(capacity,occupied,startDate,room))"));
        HttpResponse response = client.call(EXAMS_METHOD, RestClient.Methods.GET, params, headers);
        if (client.getStatusCode() != null && client.getStatusCode() == HttpStatus.SC_OK) {
            String xmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
            Log.d(TAG, xmlResponse);
            List<Exam> exams;
            exams = ExamParser.parseExams(xmlResponse);
            return exams;
        } else if (client.getStatusCode() != null && client.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticatorException("User not authorized");
        } else {
            throw new IOException("Unknown error");
        }
    }

    public Set<String> getRegisteredExams() throws AuthenticatorException, IOException, XmlPullParserException {
        if (!KosAccountManager.isAccount()) {
            throw new AuthenticatorException("No credentials");
        }
        KosAccount account = KosAccountManager.getAccount();
        RestClient client = new RestClient(KOS_API_BASE, new DefaultHttpClient());
        String credentials = account.getUsername() + ":" + account.getPassword();
        ArrayList<NameValuePair> headers = new ArrayList<>();
        String base64EncodedCredentials = Base64.encodeBytes(credentials.getBytes());
        headers.add(new BasicNameValuePair("Authorization", "Basic " + base64EncodedCredentials));
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("fields", "entry(content(exam))"));
        HttpResponse response = client.call(String.format(REGISTERED_EXAMS_METHOD, account.getUsername()), RestClient.Methods.GET, params, headers);
        if (client.getStatusCode() != null && client.getStatusCode() == HttpStatus.SC_OK) {
            String xmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
            Log.d(TAG, xmlResponse);
            Set<String> examIds;
            examIds = ExamParser.parseRegisteredExams(xmlResponse);
            return examIds;
        } else if (client.getStatusCode() != null && client.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticatorException("User not authorized");
        } else {
            throw new IOException("Unknown error");
        }
    }
}
