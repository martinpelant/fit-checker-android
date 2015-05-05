package cz.mpelant.fitchecker.auth;

import android.util.Log;
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
    private static final String DATA_STORE_FILE = "oauth.dat";

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * OAuth 2 scope.
     */
    private static final String SCOPE = "read";

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

    /**
     * Authorizes the installed application to access user's protected data.
     */
    private static Credential authorize() throws Exception {
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

        return new AuthorizationCodeInstalledCvut(flow).authorize("user");
    }

    private static void run(HttpRequestFactory requestFactory) throws IOException {
        GenericUrl url = new GenericUrl("https://kosapi.fit.cvut.cz/api/3/students/pelanma4/enrolledCourses");

        HttpRequest request = requestFactory.buildGetRequest(url);
        String response = request.execute().parseAsString();
        Log.d("Courses", response + "");
    }

    public static void test() {
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(new File(App.getInstance().getFilesDir(), DATA_STORE_FILE));
            final Credential credential = authorize();
            HttpRequestFactory requestFactory =
                    HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {
                            credential.initialize(request);
                            request.setParser(new JsonObjectParser(JSON_FACTORY));
                        }
                    });
            run(requestFactory);
            // Success!
            return;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.exit(1);
    }
}
