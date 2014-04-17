
package cz.mpelant.fitchecker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.utils.DataProvider;

@Deprecated public class AddSubject extends BaseActivity {
    public static String TAG = "fitchecker";
    private Spinner subjectType;
    private Button save;
    private EditText subject;
    private SharedPreferences sp;
    private static final String PREF_TYPE = "subjectPrefix";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setTitle(getResources().getString(R.string.add_subject) + " | " + getResources().getString(R.string.app_name));
        setContentView(R.layout.add_subject);
        subjectType = (Spinner) findViewById(R.id.course_prefix);
        subject = (EditText) findViewById(R.id.subject);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        subjectType.setSelection(sp.getInt(PREF_TYPE, 0));
        subjectType.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                Editor ed = sp.edit();
                ed.putInt(PREF_TYPE, pos);
                ed.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        subject.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    save.performClick();
                    return true;
                }
                return false;
            }
        });
        subject.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (!Character.isLetterOrDigit(currentChar) && currentChar != '.') {
                            sourceAsSpannableBuilder.delete(i, i + 1);
                        }
                    }
                    return source;
                } else {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char currentChar = source.charAt(i);
                        if (Character.isLetterOrDigit(currentChar) || currentChar == '.') {
                            filteredStringBuilder.append(currentChar);
                        }
                    }
                    return filteredStringBuilder.toString();
                }
            }
        }});
        save = (Button) findViewById(R.id.add);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DataProvider data = new DataProvider(AddSubject.this);
                data.open();
                long row = data.subjectCreate(subjectType.getSelectedItem().toString() + subject.getText().toString().replace('\'', '-').toUpperCase().trim());
                data.close();
                Log.d(TAG, "saved " + row);
                finish();
            }
        });

        Button addFromKos = (Button) findViewById(R.id.addSubjectsFromKOS);
        addFromKos.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddSubject.this, AddFromKosActivity.class));
                finish();
            }
        });

    }

}
