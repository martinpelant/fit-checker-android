package cz.mpelant.fitchecker.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.fragment.dialog.AddFromKosDialog;
import cz.mpelant.fitchecker.model.Subject;

/**
 * AddSubjectFragment.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment
 * @since 4/17/2014
 */
public class AddSubjectFragment extends BaseFragment {
    @InjectView(R.id.course_prefix)
    Spinner subjectType;
    @InjectView(R.id.subject)
    EditText subject;
    private static final String PREF_TYPE = "subjectPrefix";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.add_subject, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ButterKnife.inject(this, view);
        subjectType.setSelection(sp.getInt(PREF_TYPE, 0));
        subjectType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                SharedPreferences.Editor ed = sp.edit();
                ed.putInt(PREF_TYPE, pos);
                ed.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        subject.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    addSubject();
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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            subject.requestFocus();
        }
        setTitle(getResources().getString(R.string.add_subject));
    }

    @Override
    public void onStop() {
        super.onStop();
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.add)
    public void addSubject() {
        String subjectName = subjectType.getSelectedItem().toString() + subject.getText().toString().replace('\'', '-').toUpperCase().trim();
        final Subject subject = new Subject(subjectName);
        new Thread() {
            @Override
            public void run() {
                App.getInstance().getContentResolver().insert(DataProvider.getSubjectsUri(), subject.getContentValues());
            }
        }.start();
        finish();
    }

    @OnClick(R.id.addSubjectsFromKOS)
    public void addFromKos() {
        FragmentActivity activity = getActivity();
        finish();
        new AddFromKosDialog().show(activity.getSupportFragmentManager(), "kos");
    }
}
