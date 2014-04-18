package cz.mpelant.fitchecker.activity;

import cz.mpelant.fitchecker.fragment.LoginFragment;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseFragmentActivity {

    @Override
    protected String getFragmentName() {
        return LoginFragment.class.getName();
    }
}



