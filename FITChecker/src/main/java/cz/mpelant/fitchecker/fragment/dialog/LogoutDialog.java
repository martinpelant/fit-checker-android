package cz.mpelant.fitchecker.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.auth.KosAccountManager;
import cz.mpelant.fitchecker.downloader.EduxServer;

/**
 * LogoutDialog.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment.dialog
 * @since 4/18/2014
 */
public class LogoutDialog extends BaseDialogFragment {
    public interface OnLogOutListener {
        void onLogOut();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.logout_title);
        alertDialog.setMessage(R.string.logout_message);
        alertDialog.setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                KosAccountManager.deleteAccount();
                App.getInstance().deleteFile(EduxServer.COOKIE_FILE);
                if (getTargetFragment() instanceof OnLogOutListener) {
                    ((OnLogOutListener) getTargetFragment()).onLogOut();
                }
            }
        });
        alertDialog.setNegativeButton(android.R.string.cancel, null);
        return alertDialog.create();
    }
}
