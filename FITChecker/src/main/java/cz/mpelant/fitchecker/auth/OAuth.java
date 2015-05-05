package cz.mpelant.fitchecker.auth;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.store.FileDataStoreFactory;
import cz.mpelant.fitchecker.App;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * OAuth.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.auth
 * @since 5/4/2015
 */
public class OAuth {
    /**
     * Directory to store user credentials.
     */
    private static final String DATA_STORE_FILE = "oauth2";

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * OAuth 2 scope.
     */
    private static final String SCOPE = "urn:ctu:oauth:kosapi:public.readonly";

    /**
     * Global instance of the HTTP transport.
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Global instance of the JSON factory.
     */
    static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

    private static final String TOKEN_SERVER_URL = "https://auth.fit.cvut.cz/oauth/oauth/token";
    private static final String AUTHORIZATION_SERVER_URL = "https://auth.fit.cvut.cz/oauth/oauth/authorize";


    private static void deleteRecursively(File file){
        if(getAuthFile().isDirectory()){
            String[] children = file.list();
            for (int i = 0; children!=null && i < children.length; i++) {
                deleteRecursively(new File(file, children[i]));
            }
        }
        file.delete();
    }

    public static void reset() {
        deleteRecursively(getAuthFile());
        DATA_STORE_FACTORY = null;
    }

    private static File getAuthFile() {
        return new File(App.getInstance().getFilesDir(), DATA_STORE_FILE);
    }

    /**
     * Authorizes the installed application to access user's protected data.
     */
    public static Credential authorize(KosAccount account) throws Exception {
        if (DATA_STORE_FACTORY == null) {
            DATA_STORE_FACTORY = new FileDataStoreFactory(getAuthFile());
        }
        OAuth2ClientCredentials.errorIfNotSpecified();
        // set up authorization code flow
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken
                .authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(TOKEN_SERVER_URL),
                new ClientParametersAuthentication(
                        OAuth2ClientCredentials.API_KEY, OAuth2ClientCredentials.API_SECRET),
                OAuth2ClientCredentials.API_KEY,
                AUTHORIZATION_SERVER_URL).setScopes(Arrays.asList(SCOPE))
                .setDataStoreFactory(DATA_STORE_FACTORY).build();

        return new AuthorizationCodeInstalledCvut(flow, account).authorize("user");
    }

    public static HttpRequestFactory createRequestFactory(KosAccount account) throws Exception {
        final Credential credential = authorize(account);
        return HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                credential.initialize(request);
                request.setParser(new JsonObjectParser(JSON_FACTORY));
            }
        });
    }


}
