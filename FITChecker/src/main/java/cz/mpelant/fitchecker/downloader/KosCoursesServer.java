package cz.mpelant.fitchecker.downloader;

import android.accounts.AuthenticatorException;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import cz.mpelant.fitchecker.auth.KosAccount;
import cz.mpelant.fitchecker.auth.KosAccountManager;
import cz.mpelant.fitchecker.auth.OAuth;
import cz.mpelant.fitchecker.model.Subject;
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
public class KosCoursesServer extends KosServer {

    private static final String COURSES_METHOD = "students/%s/enrolledCourses";
    private final String TAG = KosCoursesServer.class.getSimpleName();

    @NonNull
    public List<String> loadSubjects() throws IOException, AuthenticatorException, XmlPullParserException {
        HttpRequestFactory factory =  getHttpFactory();
        GenericUrl url = new GenericUrl(String.format(KOS_API_URL + COURSES_METHOD, KosAccountManager.getAccount().getUsername()));
        url.put("fields", "entry(content(course))");
        HttpRequest request = factory.buildGetRequest(url);
        request.getHeaders().setAccept("application/xml;charset=UTF-8");
        String xmlResponse = request.execute().parseAsString();
        Log.d(TAG, xmlResponse);
        List<String> subjects;
        if (xmlResponse.contains("<")) {
            subjects = SubjectParser.parseSubjects(xmlResponse);
        } else {
            subjects = Arrays.asList(xmlResponse.split(","));
        }
        filterSubjects(subjects);

        return subjects;

    }

    private void filterSubjects(List<String> subjects) {
        for (int i = subjects.size() - 1; i >= 0; i--) {
            if (subjects.get(i) == null || subjects.get(i).trim().isEmpty()) {
                subjects.remove(i);
            }
        }
    }
}
