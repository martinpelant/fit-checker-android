package cz.mpelant.fitchecker.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import com.afollestad.materialdialogs.MaterialDialog;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.downloader.EduxServer;
import cz.mpelant.fitchecker.model.Exam;
import cz.mpelant.fitchecker.model.Subject;

import java.io.File;

/**
 * DeleteSubjectDialog.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment
 * @since 4/18/2014
 */
public class DeleteSubjectDialog extends BaseDialogFragment {
    public static final String SUBJECT = "subject";

    public static DeleteSubjectDialog newInstance(Subject s) {
        Bundle b = new Bundle();
        b.putParcelable(SUBJECT, s);
        DeleteSubjectDialog f = new DeleteSubjectDialog();
        f.setArguments(b);
        return f;
    }

    private Subject getSubject() {
        return getArguments().getParcelable(SUBJECT);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Subject subject = getSubject();
        Dialog d;
        final Thread callback = new Thread() {
            @Override
            public void run() {
                App.getInstance().getContentResolver().delete(DataProvider.getSubjectUri(subject.getId()), null, null);
                App.getInstance().getContentResolver().delete(DataProvider.getExamsUri(), Exam.COL_SUBJECT + " = ?", new String[]{subject.getName()});
                File cacheFile = EduxServer.getSubejctFile(subject.getName());
                //noinspection ResultOfMethodCallIgnored
                cacheFile.delete();
            }
        };


        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 21) {
            MaterialDialog.Builder ab = new MaterialDialog.Builder(getActivity());
            ab.title(R.string.delete_title)
                    .content(getResources().getString(R.string.delete_message, subject.getName()))
                    .positiveText(R.string.delete)
                    .negativeText(android.R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog materialDialog) {
                            callback.start();
                        }
                    });
            d = ab.build();
        } else {
            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setTitle(R.string.delete_title);
            ab.setMessage(getResources().getString(R.string.delete_message, subject.getName()));
            ab.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callback.start();
                }
            });
            ab.setNegativeButton(android.R.string.cancel, null);
            d = ab.create();
        }
        return d;
    }
}
