package cz.mpelant.fitchecker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.downloader.Downloader;

@Deprecated public class Login extends BaseActivity {
	@Deprecated public static final String PREFERENCES_USERNAME = "username";
    @Deprecated  public static final String PREFERENCES_PASSWORD = "password";
    @Deprecated	public static final String PREFERENCES_AUTH = "auth";
	private EditText username;
	private EditText password;
	private Button save;
	private static ProgressDialog mProgressBar;
	private static LoginCheck loginCheck;
	private final int DIALOG_LOGGING_IN = 1;
	private final int DIALOG_LOGOUT = 2;
    private final int AUTH_OPTION=3;
	public static String TAG = "fitchecker";
	private SharedPreferences sp;

	private class LoginCheck extends Downloader {

		public LoginCheck(Activity context) {
			super(context);
		}

		@Override
		protected void onPreExecute() {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			showDialog(DIALOG_LOGGING_IN);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			while (!cancelled) {
				boolean rtrn;
				try {
					rtrn = login(params[0], params[1], Integer.parseInt(params[2]));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
					continue;
				}
				if (rtrn) {
					Editor ed = sp.edit();
					ed.putString(PREFERENCES_USERNAME, params[0]);
					ed.putString(PREFERENCES_PASSWORD, params[1]);
					ed.putInt(PREFERENCES_AUTH, Integer.parseInt(params[2]));
					ed.commit();
				}
				return rtrn;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			try {
				mProgressBar.dismiss();
				removeDialog(DIALOG_LOGGING_IN);
				
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			Log.d(TAG, "Result - " + result + ", cancelled: " + cancelled);
			if (cancelled)
				return;
			if (result) {
				setResult(RESULT_OK);
				Toast.makeText(Login.this, R.string.login_successful, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			Toast.makeText(Login.this, R.string.error_login, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		setTitle(getString(R.string.login));
		setContentView(R.layout.login);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		password.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					save.performClick();
					return true;
				}
				return false;
			}
		});
		username.setText(sp.getString(PREFERENCES_USERNAME, ""));
		password.setText(sp.getString(PREFERENCES_PASSWORD, ""));
		Button clear = (Button) findViewById(R.id.clear);
		clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_LOGOUT);
			}
		});
		save = (Button) findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (username.getText().length() < 3 || password.getText().length() < 1) {
					Toast.makeText(Login.this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
					return;
				}
				loginCheck = new LoginCheck(Login.this);
				loginCheck.execute(username.getText().toString().toLowerCase(), password.getText().toString(), AUTH_OPTION+"");
			}
		});
		
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOGGING_IN:
			mProgressBar = new ProgressDialog(Login.this);
			mProgressBar.setMessage(getResources().getString(R.string.progress_logging_in));
			mProgressBar.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					if (loginCheck != null)
						loginCheck.cancel();
					mProgressBar.dismiss();
					try {
						removeDialog(DIALOG_LOGGING_IN);
					} catch (Exception e) {
					}

				}
			});
			return mProgressBar;
		case DIALOG_LOGOUT:
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(Login.this);
			alertDialog.setTitle(R.string.logout_title);
			alertDialog.setMessage(R.string.logout_message);
			alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Editor ed = sp.edit();
					ed.remove(PREFERENCES_USERNAME);
					ed.remove(PREFERENCES_PASSWORD);
					ed.remove(PREFERENCES_AUTH);
					ed.commit();
					username.setText("");
					password.setText("");
					deleteFile(Downloader.COOKIE_FILE);
				}
			});
			alertDialog.setNegativeButton(android.R.string.no, null);
			return alertDialog.create();

		}
		return null;

	}

}
