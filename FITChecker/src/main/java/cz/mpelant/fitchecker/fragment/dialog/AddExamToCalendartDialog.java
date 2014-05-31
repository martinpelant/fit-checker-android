package cz.mpelant.fitchecker.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.model.Exam;
import cz.mpelant.fitchecker.utils.Tools;

import java.util.Date;

/**
 * DeleteSubjectDialog.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment
 * @since 4/18/2014
 */
public class AddExamToCalendartDialog extends BaseDialogFragment {
    public static final String EXAM = "exam";
    public static final int DURATION = (60 + 30) * 60 * 1000;

    public static AddExamToCalendartDialog newInstance(Exam e) {
        Bundle b = new Bundle();
        b.putParcelable(EXAM, e);
        AddExamToCalendartDialog f = new AddExamToCalendartDialog();
        f.setArguments(b);
        return f;
    }

    private Exam getExam() {
        return getArguments().getParcelable(EXAM);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Exam exam = getExam();

        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle(R.string.add_to_calendar_);
        ab.setMessage(R.string.add_to_calendar_message);
        ab.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Tools.addToCalendar(exam.getSubject() + " " + getString(R.string.exam), exam.getRoom(), null, new Date(exam.getLongDate()), new Date(exam.getLongDate() + DURATION));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.calendar_not_found, Toast.LENGTH_SHORT).show();
                }

            }
        });
        ab.setNegativeButton(android.R.string.cancel, null);
        return ab.create();
    }
}
