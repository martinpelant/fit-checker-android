package cz.mpelant.fitchecker.auth;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.http.*;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

/**
 * TokenRequestGet.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.auth
 * @since 5/5/2015
 */
public class TokenRequestGet extends TokenRequest {
    private String code;
    private String redirectUri;

    public TokenRequestGet(TokenRequest src) {
        super(src.getTransport(), src.getJsonFactory(), src.getTokenServerUrl(), src.getGrantType());
        setRequestInitializer(src.getRequestInitializer());
        setClientAuthentication(src.getClientAuthentication());

        setScopes(src.getScopes() == null ? null : Arrays.asList(src.getScopes().split(" ")));
        if (src instanceof AuthorizationCodeTokenRequest) {
            code = ((AuthorizationCodeTokenRequest) src).getCode();
            redirectUri = ((AuthorizationCodeTokenRequest) src).getRedirectUri();
        }
    }

    @Override
    public TokenResponse execute() throws IOException {
        return executeUnparsedGet().parseAs(TokenResponse.class);
    }

    public HttpResponse executeUnparsedGet() throws IOException {
        // must set clientAuthentication as last execute interceptor in case it needs to sign request
        HttpRequestFactory requestFactory =
                getTransport().createRequestFactory(new HttpRequestInitializer() {

                    public void initialize(HttpRequest request) throws IOException {
                        if (getRequestInitializer() != null) {
                            getRequestInitializer().initialize(request);
                        }
                    }
                });

        TokenAuthUrl url = new TokenAuthUrl(getTokenServerUrl().toURL())
                .setClientId(((ClientParametersAuthentication) getClientAuthentication()).getClientId())
                .setClientSecret(((ClientParametersAuthentication) getClientAuthentication()).getClientSecret())
                .setCode(code)
                .setRedirectUri(redirectUri)
                .setGrantType(getGrantType())
                .setScopes(getScopes());

        // make request
        HttpRequest request = requestFactory.buildGetRequest(url);
        request.setParser(new JsonObjectParser(getJsonFactory()));
        request.setThrowExceptionOnExecuteError(false);
        HttpResponse response = request.execute();
        if (response.isSuccessStatusCode()) {
            return response;
        }
        throw TokenResponseException.from(getJsonFactory(), response);
    }

    public TokenRequestGet setCode(String code) {
        this.code = code;
        return this;
    }

    public TokenRequestGet setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public static class TokenAuthUrl extends GenericUrl {


        /**
         * URI that the authorization server directs the resource owner's user-agent back to the client
         * after a successful authorization grant (as specified in <a
         * href="http://tools.ietf.org/html/rfc6749#section-3.1.2">Redirection Endpoint</a>) or
         * {@code null} for none.
         */
        @Key("redirect_uri")
        private String redirectUri;

        /**
         * Space-separated list of scopes (as specified in <a
         * href="http://tools.ietf.org/html/rfc6749#section-3.3">Access Token Scope</a>) or {@code null}
         * for none.
         */
        @Key("scope")
        private String scopes;

        /**
         * Client identifier.
         */
        @Key("client_id")
        private String clientId;

        @Key("client_secret")
        private String clientSecret;

        /**
         * Grant type ({@code "authorization_code"}, {@code "password"}, {@code "client_credentials"},
         * {@code "refresh_token"} or absolute URI of the extension grant type).
         */
        @Key("grant_type")
        private String grantType;

        /**
         * Authorization code received from the authorization server.
         */
        @Key
        private String code;


        public TokenAuthUrl() {
        }

        public TokenAuthUrl(String encodedUrl) {
            super(encodedUrl);
        }

        public TokenAuthUrl(URI uri) {
            super(uri);
        }

        public TokenAuthUrl(URL url) {
            super(url);
        }

        public TokenAuthUrl setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public TokenAuthUrl setScopes(String scopes) {
            this.scopes = scopes;
            return this;
        }

        public TokenAuthUrl setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public TokenAuthUrl setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public TokenAuthUrl setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public TokenAuthUrl setCode(String code) {
            this.code = code;
            return this;
        }
    }

}