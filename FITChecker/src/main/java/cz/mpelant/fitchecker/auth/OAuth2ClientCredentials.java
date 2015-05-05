package cz.mpelant.fitchecker.auth;

/**
 * OAuth2ClientCredentials.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.auth
 * @since 5/4/2015
 */
public class OAuth2ClientCredentials {

    /** Value of the "API Key". */
    public static final String API_KEY = "b03f64b8-50fb-4aae-bbf2-ceecf8cc6260";

    /** Value of the "API Secret". */
    public static final String API_SECRET = "35FaspXeZCL8MgGMl7fgW4J81ZLv09L8";

    /** Port in the "Callback URL". */
    public static final int PORT = 80;

    /** Domain name in the "Callback URL". */
    public static final String CALLBACK = "http://127.0.0.1/callback";

    public static void errorIfNotSpecified() {
        if (API_KEY.startsWith("Enter ") || API_SECRET.startsWith("Enter ")) {
            System.out.println(
                    "Enter API Key and API Secret"
                            + " into API_KEY and API_SECRET in " + OAuth2ClientCredentials.class);
            System.exit(1);
        }
    }
}
