package cz.mpelant.fitchecker.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import com.afollestad.materialdialogs.MaterialDialog;
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
        final Runnable callback = new Runnable() {
            @Override
            public void run() {
                KosAccountManager.deleteAccount();
                App.getInstance().deleteFile(EduxServer.COOKIE_FILE);
                if (getTargetFragment() instanceof OnLogOutListener) {
                    ((OnLogOutListener) getTargetFragment()).onLogOut();
                }
            }
        };
        Dialog d;
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 21) {
            MaterialDialog.Builder ab = new MaterialDialog.Builder(getActivity());
            ab.title(R.string.logout_title)
                    .content(R.string.logout_message)
                    .positiveText(R.string.logout)
                    .negativeText(android.R.string.cancel)
                    .callback(new MaterialDialog.SimpleCallback() {
                        @Override
                        public void onPositive(MaterialDialog materialDialog) {
                            callback.run();
                        }
                    });
            d = ab.build();
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle(R.string.logout_title);
            alertDialog.setMessage(R.string.logout_message);
            alertDialog.setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callback.run();
                }
            });
            alertDialog.setNegativeButton(android.R.string.cancel, null);
            d=alertDialog.create();
        }
        return d;
    }
}
