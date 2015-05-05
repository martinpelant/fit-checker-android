package cz.mpelant.fitchecker.downloader;

import android.accounts.AuthenticatorException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
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
public class KosExamsServer extends KosServer {
    private static final String REGISTERED_EXAMS_METHOD = "students/%s/registeredExams";
    private static final String EXAMS_METHOD = "courses/%s/exams";
    private final String TAG = KosExamsServer.class.getSimpleName();

    @NonNull
    public List<Exam> loadExams(String subject) throws IOException, AuthenticatorException, XmlPullParserException {
        HttpRequestFactory factory =  getHttpFactory();
        GenericUrl url = new GenericUrl(String.format(KOS_API_URL + EXAMS_METHOD, subject));
        url.put("fields", "entry(id,content(capacity,occupied,startDate,room,termType))");
        HttpRequest request = factory.buildGetRequest(url);
        request.getHeaders().setAccept("application/xml;charset=UTF-8");
        String xmlResponse = request.execute().parseAsString();

        Log.d(TAG, xmlResponse);
        List<Exam> exams;
        exams = ExamParser.parseExams(xmlResponse);
        return exams;
    }

    public Set<String> getRegisteredExams() throws AuthenticatorException, IOException, XmlPullParserException {
        HttpRequestFactory factory =  getHttpFactory();
        GenericUrl url = new GenericUrl(String.format(KOS_API_URL + REGISTERED_EXAMS_METHOD, KosAccountManager.getAccount().getUsername()));
        url.put("fields", "entry(content(exam))");
        HttpRequest request = factory.buildGetRequest(url);
        request.getHeaders().setAccept("application/xml;charset=UTF-8");
        String xmlResponse = request.execute().parseAsString();
        Log.d(TAG, xmlResponse);
        Set<String> examIds;
        examIds = ExamParser.parseRegisteredExams(xmlResponse);
        return examIds;
    }
}
