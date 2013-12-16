package cz.mpelant.fitchecker.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.R.string;
import cz.mpelant.fitchecker.activity.Login;
import cz.mpelant.fitchecker.utils.DataProvider;
import cz.mpelant.fitchecker.utils.MyReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class Downloader extends AsyncTask<String, String, Boolean> {
	private Context context;
	protected boolean cancelled;
	public static final String TAG = "fitchecker";
	public static final String COOKIE_FILE = "cookies.dat";
	public static final String URL_EDUX = "https://edux.fit.cvut.cz/";
	public static final int REQ_CODE_LOGIN = 3;
	private static final String ERROR_MISC = "-";
	private static final String ERROR_COOKIES = "--";
	public static final String ERROR_PATTERN_NOT_FOUND = "---";
	private static final String NOT_LOGGED_IN = "Přihlásit se";
	protected DefaultHttpClient client;
	private HttpContext localContext;
	private BasicCookieStore cookieStore;
	protected SharedPreferences sp;
	protected String errorMessage;
	protected int changed;
	private DataProvider data;
	private boolean checkForChanges;

	public Downloader(Context context) {
		this.context = context;
		// client = AndroidHttpClient.newInstance("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0b8) Gecko/20100101 Firefox/4.0b8");
		client = new DefaultHttpClient();
		localContext = new BasicHttpContext();
		cookieStore = new BasicCookieStore();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		cancelled = false;
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		data = new DataProvider(context);
		checkForChanges = false;
		changed = 0;

	}

	public Downloader(Context context, boolean checkForChanges) {
		this(context);
		this.checkForChanges = checkForChanges;
	}

	@Override
	protected void onPreExecute() {
		if (!sp.contains(Login.PREFERENCES_USERNAME) || !sp.contains(Login.PREFERENCES_PASSWORD)) {
			cancel();
			if (Activity.class.isInstance(context)) {
				Activity aContext = (Activity) context;
				aContext.startActivityForResult(new Intent(context, Login.class), REQ_CODE_LOGIN);
			} else
				context.startActivity(new Intent(context, Login.class));
		}
		super.onPreExecute();
	}

	private boolean checkForChanges(String text, File file) {

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
            e.printStackTrace();
			return true;
		}

        String oldText = MyReader.getString(fis).replaceAll("\\s+", "");
        String newText = text.replaceAll("\\s+", "");
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

	private boolean saveTable(String subject, String body) {
		Log.v(TAG, "saving table");
		File dir = context.getFilesDir();
		File file = new File(dir, subject + ".html");

		if (checkForChanges && checkForChanges(body, file)) {
			changed++;
			data.open();
			data.subjectChanged(subject);
			data.close();
		}
		FileOutputStream fos = null;
		boolean rtrn = true;
		try {
			fos = new FileOutputStream(file);
			fos.write(body.getBytes("utf-8"));
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

	protected boolean login(String username, String password, int auth) throws Exception {

		HttpPost post = new HttpPost(URL_EDUX + "start?do=login");
		Log.v(TAG, "auth: " + auth);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("u", username));
		nameValuePairs.add(new BasicNameValuePair("do", "login"));
		nameValuePairs.add(new BasicNameValuePair("authnProvider", auth + ""));
		nameValuePairs.add(new BasicNameValuePair("p", password));
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

	private String download(String subject) {
		cookieStore.clear();
		if (!loadCookies()) {
			Log.e(TAG, "cookies not found");
			return ERROR_COOKIES;
		}
        Log.v(TAG,getSubjectClassificationURL(subject, context, false) );
		HttpGet get = new HttpGet(URL_EDUX + getSubjectClassificationURL(subject, context, false));

		Log.v(TAG, "executing get - " + subject);
		publishProgress(context.getResources().getString(string.progress_connecting), subject);
		HttpResponse response = null;
		try {
			response = client.execute(get, localContext);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
			return ERROR_MISC;
		}

		publishProgress(context.getResources().getString(string.progress_downloading), subject);

		InputStream input;
		StringBuffer out = new StringBuffer();
		char buf[] = new char[500];
		int length;
		try {
			input = response.getEntity().getContent();
			InputStreamReader ir = new InputStreamReader(input);
			while ((length = ir.read(buf)) != -1 && !cancelled) {
				out.append(buf, 0, length);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
			return ERROR_MISC;
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

	@Override
	protected Boolean doInBackground(String... params) {
		for (String subject : params) {
			if (cancelled)
				return false;
			String body = "";
			body = download(subject);

			if (body.equals(ERROR_COOKIES) || body.contains(NOT_LOGGED_IN)) {
				boolean rtrn;
				try {
					publishProgress(context.getResources().getString(string.progress_logging_in));
					rtrn = login(sp.getString(Login.PREFERENCES_USERNAME, ""), sp.getString(Login.PREFERENCES_PASSWORD, ""), sp.getInt(Login.PREFERENCES_AUTH, 2));
				} catch (Exception e) {
					Log.e(TAG, e.toString());
					rtrn = false;
				}
				if (!rtrn) {
					errorMessage = context.getResources().getString(string.error_login);
					return false;
				}
				body = download(subject);
			}
			if (body.equals(ERROR_MISC) || body.equals(ERROR_COOKIES) || body.contains(NOT_LOGGED_IN)) {
				errorMessage = context.getResources().getString(string.error_download);
				return false;
			}

			if (cancelled)
				return false;
			publishProgress(context.getResources().getString(string.progress_processing), subject);
			if (cancelled)
				return false;
			String table = parseTable(body);
			if (cancelled)
				return false;
			if (!saveTable(subject, table) || cancelled)
				return false;
		}
		return true;
	}

}
