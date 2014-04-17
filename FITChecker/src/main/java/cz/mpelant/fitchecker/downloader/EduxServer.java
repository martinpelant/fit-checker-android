package cz.mpelant.fitchecker.downloader;

import android.accounts.AuthenticatorException;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.activity.Login;
import cz.mpelant.fitchecker.auth.KosAccount;
import cz.mpelant.fitchecker.auth.KosAccountManager;
import cz.mpelant.fitchecker.utils.MyReader;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EduxServer {

    public class CancelledException extends Exception {

    }

    private Context context;
    protected boolean cancelled;
    public static final String TAG = "CancelledException";
    public static final String COOKIE_FILE = "cookies.dat";
    public static final String URL_EDUX = "https://edux.fit.cvut.cz/";
    private static final String ERROR_COOKIES = "--";
    public static final String ERROR_PATTERN_NOT_FOUND = "---";
    private static final String NOT_LOGGED_IN = "Přihlásit se";
    protected DefaultHttpClient client;
    private HttpContext localContext;
    private BasicCookieStore cookieStore;

    public EduxServer(Context context) {
        this.context = context;
        client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        localContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    public static File getSubejctFile(String subject) {
        File dir = App.getInstance().getFilesDir();
        return new File(dir, subject + ".html");
    }


    private boolean isChangeDetected(String subject, String table) {

        FileInputStream fis;
        try {
            fis = new FileInputStream(getSubejctFile(subject));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return true;
        }

        String oldText = MyReader.getString(fis).replaceAll("\\s+", "");
        String newText = table.replaceAll("\\s+", "");
        Log.v("Checking for changes NEW", newText);
        Log.v("Checking for changes OLD", oldText);


        boolean rtrn = !oldText.equals(newText);
        try {
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rtrn;
    }

    private boolean saveTable(String subject, String table) {
        Log.v(TAG, "saving table");
        FileOutputStream fos = null;
        boolean rtrn = true;
        try {
            fos = new FileOutputStream(getSubejctFile(subject));
            fos.write(table.getBytes("utf-8"));
            fos.flush();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            rtrn = false;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                rtrn = false;
            }
        }
        return rtrn;
    }

    private boolean saveCookies() {
        List<MyCookie> myCookieList = new ArrayList<MyCookie>();
        for (Cookie cookie : cookieStore.getCookies()) {
            MyCookie myCookie = new MyCookie(cookie);
            // Log.v(TAG, myCookie.toString());
            myCookieList.add(myCookie);
        }
        try {
            FileOutputStream fos = context.openFileOutput(COOKIE_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(myCookieList);
            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean loadCookies() {
        try {
            FileInputStream fis = context.openFileInput(COOKIE_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<MyCookie> myCookieList = (List<MyCookie>) ois.readObject();
            ois.close();
            fis.close();
            for (MyCookie myCookie : myCookieList) {
                // Log.v(TAG, myCookie.toString());
                cookieStore.addCookie(myCookie.getCookie());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean login(KosAccount account) throws IOException {

        HttpPost post = new HttpPost(URL_EDUX + "start?do=login");
        Log.v(TAG, "auth: " + account.getAuthType());
        List<NameValuePair> nameValuePairs = new ArrayList<>(2);
        nameValuePairs.add(new BasicNameValuePair("u", account.getUsername()));
        nameValuePairs.add(new BasicNameValuePair("do", "login"));
        nameValuePairs.add(new BasicNameValuePair("authnProvider", account.getAuthType() + ""));
        nameValuePairs.add(new BasicNameValuePair("p", account.getPassword()));
        nameValuePairs.add(new BasicNameValuePair("r", "1"));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        Log.v(TAG, "executing post");
        HttpResponse response = client.execute(post, localContext);
        Log.v(TAG, "post executed");
        if (!response.containsHeader("Set-Cookie")) {
            boolean saved = saveCookies();
            Log.v(TAG, "saved cookies - " + saved);
            return true;
        }
        return false;

    }

    public void cancel() {
        cancelled = true;
    }

    public static String getSubjectClassificationURL(String subject, Context context) {
        return getSubjectClassificationURL(subject, context, true);
    }

    public static String getSubjectClassificationURL(String subject, Context context, boolean fullVersion) {
        String rtrn = "courses/" + subject;
        if (!fullVersion)
            rtrn += "/_export/xhtml";
        rtrn += "/classification/student/" + PreferenceManager.getDefaultSharedPreferences(context).getString(Login.PREFERENCES_USERNAME, "") + "/start?purge";
        return rtrn;
    }

    private String download(String subject) throws IOException, CancelledException {
        cookieStore.clear();
        if (!loadCookies()) {
            Log.e(TAG, "cookies not found");
            return ERROR_COOKIES;
        }
        Log.v(TAG, getSubjectClassificationURL(subject, context, false));
        HttpGet get = new HttpGet(URL_EDUX + getSubjectClassificationURL(subject, context, false));

        Log.v(TAG, "executing get - " + subject);
        HttpResponse response = client.execute(get, localContext);


        InputStream input;
        StringBuilder out = new StringBuilder();
        char buf[] = new char[500];
        int length;
        input = response.getEntity().getContent();
        InputStreamReader ir = new InputStreamReader(input);
        while ((length = ir.read(buf)) != -1) {
            checkIsCancelled();
            out.append(buf, 0, length);
        }
        return out.toString();
    }

    // upraveni relativni cesty na absolutni a pridani otevirani v novem okne (mimo fitChecker)
    private String parsePostProcess(String text) {
        Pattern p = Pattern.compile("<a[^>]*href=\"/", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        text = m.replaceAll("<a href=\"https://edux.fit.cvut.cz/");

        p = Pattern.compile("<a([^>]*)href", Pattern.CASE_INSENSITIVE);
        m = p.matcher(text);
        text = m.replaceAll("<a target=\"_blank\" href");
        return text;

    }

    // vyparsovani tabulky/tabulek z html
    private String parseTable(String body) {
        Pattern p = Pattern.compile("<table class=\"inline.*</table>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher m = p.matcher(body);
        if (m.find()) {
            return parsePostProcess(m.group(0));
        }
        Log.d(TAG, "table NOT found");
        return ERROR_PATTERN_NOT_FOUND;
    }


    private void checkIsCancelled() throws CancelledException {
        if (cancelled) {
            throw new CancelledException();
        }
    }

    public boolean downloadSubjectData(String subjectName) throws AuthenticatorException, IOException, CancelledException {
        if (!KosAccountManager.isAccount()) {
            throw new AuthenticatorException("No credentials found");
        }

        String body = download(subjectName);

        if (body.equals(ERROR_COOKIES) || body.contains(NOT_LOGGED_IN)) {
            if (!login(KosAccountManager.getAccount()))
                throw new AuthenticatorException("Login failed");
        }
        checkIsCancelled();


        body = download(subjectName);
        checkIsCancelled();

        if (body.equals(ERROR_COOKIES) || body.contains(NOT_LOGGED_IN)) {
            throw new AuthenticatorException("Not enough permission to view content");
        }

        String table = parseTable(body);
        boolean isChange = isChangeDetected(subjectName, table);
        saveTable(subjectName, table);

        return isChange;


    }
}
