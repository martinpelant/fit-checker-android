package cz.mpelant.fitchecker.fragment.dialog;

import android.accounts.AuthenticatorException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.activity.LoginActivity;
import cz.mpelant.fitchecker.service.AddFromKosService;

import java.io.IOException;

/**
 * AddFromKosDialog.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.fragment.dialog
 * @since 4/18/2014
 */
public class AddFromKosDialog extends BaseDialogFragment {
    private Bus bus;
    private boolean serviceStarted;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus = App.getInstance().getBus();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getActivity().startService(AddFromKosService.generateIntent());
        }
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.loadingSubjects));
        return dialog;

    }

    @Subscribe
    public void onKosServiceChanged(AddFromKosService.KosStatus status) {
        if (status == AddFromKosService.KosStatus.FINISHED) {
            dismissAllowingStateLoss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        if (serviceStarted && AddFromKosService.getLastStatus() == AddFromKosService.KosStatus.FINISHED) {
            dismissAllowingStateLoss();
        }
        serviceStarted = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Subscribe
    public void onException(AddFromKosService.KosException exception) {
        String errorMessage = null;
        if (exception.getException() instanceof AuthenticatorException) {
            errorMessage = getString(R.string.kos_login_error);
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }
        if (exception.getException() instanceof IOException) {
            errorMessage = getString(R.string.error_kos_download);
        }

        if (errorMessage == null) {
            errorMessage = getString(R.string.unknown_error);
        }
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();

    }

}
