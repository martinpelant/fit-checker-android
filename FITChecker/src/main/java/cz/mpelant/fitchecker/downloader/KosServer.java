package cz.mpelant.fitchecker.downloader;

import android.accounts.AuthenticatorException;
import com.google.api.client.http.HttpRequestFactory;
import cz.mpelant.fitchecker.auth.KosAccount;
import cz.mpelant.fitchecker.auth.KosAccountManager;
import cz.mpelant.fitchecker.auth.OAuth;

/**
 * KosServer.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.downloader
 * @since 5/5/2015
 */
public class KosServer {
    protected static final String KOS_API_URL = "https://kosapi.fit.cvut.cz/api/3/";


    protected HttpRequestFactory getHttpFactory() throws AuthenticatorException {

        if (!KosAccountManager.isAccount()) {
            throw new AuthenticatorException("No credentials");
        }
        try {
            return OAuth.createRequestFactory(KosAccountManager.getAccount());
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticatorException("User not authorized");
        }

    }
}
