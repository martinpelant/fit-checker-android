package cz.mpelant.fitchecker.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import java.io.File;

import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.db.DataProvider;
import cz.mpelant.fitchecker.downloader.EduxServer;
import cz.mpelant.fitchecker.model.Subject;

/**
 * DeleteSubjectDialog.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment
 * @since 4/18/2014
 */
public class DeleteAllSubjectsDialog extends BaseDialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle(R.string.delete_title);
        ab.setMessage(R.string.delete_all_message);
        ab.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread() {
                    @Override
                    public void run() {
                        Cursor c = App.getInstance().getContentResolver().query(DataProvider.getSubjectsUri(), null, null, null, null);
                        while (c.moveToNext()) {
                            Subject subject = new Subject(c);
                            File cacheFile = EduxServer.getSubejctFile(subject.getName());
                            cacheFile.delete();
                        }
                        App.getInstance().getContentResolver().delete(DataProvider.getSubjectsUri(), null, null);
                        App.getInstance().getContentResolver().delete(DataProvider.getExamsUri(), null, null);
                    }
                }.start();
            }
        });
        ab.setNegativeButton(android.R.string.cancel, null);
        return ab.create();
    }
}
