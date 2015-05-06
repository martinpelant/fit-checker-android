package cz.mpelant.fitchecker.auth;

import android.util.Log;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.ConnectionFactory;
import com.google.api.client.http.javanet.DefaultConnectionFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * OAuthApp.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.auth
 * @since 5/4/2015
 */
public class AuthorizationCodeInstalledCvut {


    public static final String LOGIN_URL = "https://auth.fit.cvut.cz/oauth/login.do";
    public static final String APPROVE_URL = "https://auth.fit.cvut.cz/oauth/oauth/authorize";
    /**
     * Authorization code flow.
     */
    private final AuthorizationCodeFlow flow;
    private KosAccount mKosAccount;

    public AuthorizationCodeInstalledCvut(AuthorizationCodeFlow flow, KosAccount kosAccount) {
        this.flow = Preconditions.checkNotNull(flow);
        mKosAccount = kosAccount;
    }


    /**
     * Authorizes the installed application to access user's protected data.
     *
     * @param userId user ID or {@code null} if not using a persisted credential store
     * @return credential
     */
    public Credential authorize(String userId) throws IOException {
        Credential credential = flow.loadCredential(userId);
        if (credential != null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() > 60)) {
            return credential;
        }

        //handle http requests
        // open in browser
        String redirectUri = OAuth2ClientCredentials.CALLBACK;
        AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri);

        // receive authorization code and exchange it for an access token
        String code = onAuthorization(authorizationUrl);
        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
        // store credential and return it
        return flow.createAndStoreCredential(response, userId);
    }


    private class CodeFoundException extends RuntimeException {
        String code;

        public CodeFoundException(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    protected String onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        HttpTransport transport = new NetHttpTransport.Builder().setConnectionFactory(new ConnectionFactory() {
                @Override
                public HttpURLConnection openConnection(URL url) throws IOException, ClassCastException {
                    String urlStr = url.toString();
                    Log.v("URL", urlStr);
                    if (urlStr.startsWith(OAuth2ClientCredentials.CALLBACK)) {
                        throw new CodeFoundException(extractCode(urlStr));
                    }
                    return new DefaultConnectionFactory().openConnection(url);
                }
            }).build();
        HttpRequestFactory factory = transport.createRequestFactory();
        HttpResponse response = factory.buildGetRequest(authorizationUrl).execute();
        String page = response.parseAsString();

        if (page.contains(LoginForm.PASSWORD_FIELD)) {
            try {
                response = factory
                        .buildPostRequest(new GenericUrl(LOGIN_URL), new UrlEncodedContent(new LoginForm(mKosAccount.getUsername(), mKosAccount.getPassword())))
                        .setLoggingEnabled(true)
                        .execute();
            } catch (CodeFoundException e) {
                return e.getCode();
            }

            page = response.parseAsString();
        }

        if (page.contains(ApprovalForm.APPROVE)) {
            try {
                response = factory
                        .buildPostRequest(new GenericUrl(APPROVE_URL), new UrlEncodedContent(new ApprovalForm()))
                        .setLoggingEnabled(true)
                        .execute();
            } catch (CodeFoundException e) {
                return e.getCode();
            }
            Log.w("OAuth", "Code not found" + response.parseAsString());
        }
        Log.w("OAuth", "Login not found" + response.parseAsString());
        return "";
    }

    private String extractCode(String url) {
        return url.replaceFirst(OAuth2ClientCredentials.CALLBACK + "\\?code=", "");
    }

    @SuppressWarnings("unused")
    private static class LoginForm extends GenericData {
        public static final String PASSWORD_FIELD = "j_password";
        public static final String USERNAME_FIELD = "j_username";
        @Key(USERNAME_FIELD)
        private String username;

        @Key(PASSWORD_FIELD)
        private String password;

        public LoginForm(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @SuppressWarnings("unused")
    private static class ApprovalForm extends GenericData {
        public static final String APPROVE = "user_oauth_approval";
        @Key(APPROVE)
        private Boolean approve = true;
    }
}
