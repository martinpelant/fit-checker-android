/**
 * RestClient.java
 *
 * @project NewLibrary
 * @package cz.newlib.net.rest
 * @author eMan s.r.o.
 * @since 4.1.2013
 *
 */

package cz.mpelant.fitchecker.utils;

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;

public class RestClient {
    private final static String TAG = "REST CLIENT";

    private final String mUri;
    private final HttpParams mBasicParams;
    private HttpClient mClient;

    private Integer mStatusCode; // response status code. NULL is default error
    private Header[] mHeaders; // response headers
    private HttpResponse mHttpResponse; // raw response


    private static final int DEFAULT_RETRIES_COUNT = 3;
    private static final int CONNECTION_TIMEOUT = 100000;
    private static final int SOCKET_TIMEOUT = 100000;

    private BasicHeader mContentType = new BasicHeader(HTTP.CONTENT_TYPE, "application/xml;charset=UTF-8");

    public static enum Methods {
        GET//, POST, PUT, DELETE, PATCH
    }

    /**
     * Create instance of REST client
     *
     * @param httpClient - httpClient for REST client
     * @params uri - Base URI (Whole method URI is consist of URI (entered in constructor) and methodURI_)
     */
    public RestClient(String uri, HttpClient httpClient) {
        this.mUri = uri;
        this.mClient = httpClient;

        this.mBasicParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(this.mBasicParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(this.mBasicParams, SOCKET_TIMEOUT);
        HttpProtocolParams.setVersion(this.mBasicParams, new ProtocolVersion("HTTP", 1, 0));
    }

    /**
     * Call server method
     * List of params is preferred over object body
     *
     * @param methodURI  - method URI, Whole URI is consist of mURI (entered in constructor) and methodURI
     * @param callMethod - call method (Methods = GET, POST, PUT, DELETE)
     * @param params     - call params
     * @param headers    - call headers
     * @return raw http response
     */
    synchronized public HttpResponse call(String methodURI, Methods callMethod, ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers) {
        String url = this.mUri + methodURI;
        HttpRequestBase request = null;
        try {
            switch (callMethod) {
                case GET:
                    if (params != null) {
                        if (url.contains("?")) { // Method GET params in URL
                            url += "&" + URLEncodedUtils.format(params, "utf-8"); // add next params
                        } else {
                            url += "?" + URLEncodedUtils.format(params, "utf-8"); // add first params
                        }
                    }
                    request = new HttpGet(url);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            Log.e(TAG, "BAD REQUEST PARAMS " + ex.getMessage());
            request = null;
        }

        if (request != null) {
            if (headers != null) {
                for (NameValuePair nameValuePair : headers) { // Set headers
                    request.setHeader(nameValuePair.getName(), nameValuePair.getValue());
                }
            }
            Log.i(TAG,
                    "REQUEST: URL: " + url + " METHOD: " + callMethod.toString() + " PARAMS: " + (params == null ? "none" : params.toString()) + " HEADERS: "
                            + (headers == null ? "none" : headers.toString())
            );
            this.callServer(request); // Call server
        } else { // cannot make request, show default error
            this.setSignalizedDefaultError();
        }

        return this.mHttpResponse;
    }


    /**
     * Call server
     * after call sets-up
     * mStatusCode - status code returned by server
     * mHeaders - response headers
     * mHttpResponse - raw server response
     *
     * @param request - httpRequest by requested method
     * @return - raw server response (mHttpResponse)
     */
    private HttpResponse callServer(HttpRequestBase request) {
        this.setSignalizedDefaultError();

        try {
            request.setParams(this.mBasicParams);

            for (int i = 0; i < DEFAULT_RETRIES_COUNT; i++) {
                try {
                    this.closeCennection(); // close previously connection (in case of long time connect)
                    this.mHttpResponse = this.mClient.execute(request);
                    this.mHeaders = this.mHttpResponse.getAllHeaders();
                    this.mStatusCode = this.mHttpResponse.getStatusLine().getStatusCode();
                    return this.mHttpResponse;
                } catch (Exception ex) {
                    if (i == (DEFAULT_RETRIES_COUNT - 1)) {
                        throw ex;
                    }
                    Thread.sleep(200 * (i + 1));
                }
            }

        } catch (Exception ex) {
            this.setSignalizedDefaultError();
        }
        return this.mHttpResponse;
    }

    /**
     * Set NULL in properties of server response
     * This state signalizes default error
     */
    private void setSignalizedDefaultError() {
        this.mStatusCode = null;
        this.mHeaders = null;
        this.closeCennection();
    }

    /**
     * Close connection, NULL raw response (because you cannot use raw response after close connection)
     */
    public void closeCennection() {
        if (this.mHttpResponse != null) {
            try {
                this.mHttpResponse.getEntity().consumeContent();
            } catch (Exception ex) {
                // ignore
            } finally {
                this.mHttpResponse = null;
            }
        }
    }

    public Integer getStatusCode() {
        return this.mStatusCode;
    }

    public Header[] getHeaders() {
        return this.mHeaders;
    }

    public HttpResponse getHttpResponse() {
        return this.mHttpResponse;
    }
}
