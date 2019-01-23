package io.appery.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

/**
 * Access to Appery.io REST API services.
 */
public class ApperyRestClient {

    CloseableHttpClient httpclient = HttpClients.createDefault();

    String host;

    String protocol;

    ApperyRestClient() {
		// set host
        host = "appery.io";
        protocol = "https://";
        String envHost = System.getenv("AU_BACKEND");
        if (envHost!=null) {
            host = envHost;
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * Performs HTTP GET.
     */
    String makeGet(String serviceUrl, Map<String, String> params, Map<String, String> headers)
           throws ApperyException {
        HttpGet req;
        try {
            URIBuilder ub = new URIBuilder(protocol + host + serviceUrl);
            if (params!=null) {
                params.forEach((key,value) -> ub.setParameter(key, value));
            }
            req = new HttpGet(ub.build());

        } catch(URISyntaxException e) {
            throw new ApperyException("[URISyntaxException] " + e.getMessage());
        }
        req.addHeader(new BasicHeader("Accept", "application/json"));
        req.addHeader(new BasicHeader("User-Agent", "AU-Test-Agent"));
        if (headers!=null) {
            headers.forEach((key,value) -> req.addHeader(new BasicHeader(key, value)));
        }
        try {
            CloseableHttpResponse response = httpclient.execute(req);
            String result = "";
            try {
                int status = response.getStatusLine().getStatusCode();
                assert200(status);
                result = EntityUtils.toString(response.getEntity());
                if (result.startsWith("<HTML>")) {
                    throw new ApperyException("SAML authentication error");
                }
            } finally {
                response.close();
            }
            return result;
        } catch(IOException e) {
            throw new ApperyException(e.getMessage());
        }
    }

    void assert200(int status) throws ApperyException {
        if (status != 200) {
            throw new ApperyException("HTTP status expected: 200, received: " + status);
        }
    }

    /**
     * Performs HTTP GET.
     */
    String makeGet(String serviceUrl)
           throws ApperyException {
        return makeGet(serviceUrl, null, null);
    }

    /**
     * Add parameters to GET URL.
     */
    static String addGetParams(String url, Map<String, String> parameters)
                  throws MalformedURLException, URISyntaxException {
        URL u = new URL(url);
        URIBuilder uriBuilder = new URIBuilder()
                .setScheme(u.getProtocol())
                .setHost(u.getHost())
                .setPath(u.getPath());

        parameters.entrySet().stream().forEach(e ->
            uriBuilder.addParameter(e.getKey(), e.getValue())
        );
        /* parameters.each { name, value ->
            uriBuilder.addParameter(name, value);
        } */

        return uriBuilder.build().toString();
    }

    /**
     * Performs HTTP POST.
     */
    String makePost(String serviceUrl, String data)
           throws ApperyException {
        try {
            HttpPost req = new HttpPost(protocol + host + serviceUrl);
            req.addHeader(new BasicHeader("Content-Type", "application/json"));
            req.addHeader(new BasicHeader("Accept", "application/json"));
            req.addHeader(new BasicHeader("User-Agent", "AU-Test-Agent"));
            req.addHeader(new BasicHeader("isrestapicall", "true"));
            req.setEntity(new StringEntity(data));
            CloseableHttpResponse response = httpclient.execute(req);
            String result = "";
            try {
                int status = response.getStatusLine().getStatusCode();
                assert200(status);
                result = EntityUtils.toString(response.getEntity());
                if (result.startsWith("<HTML>")) {
                    throw new ApperyException("SAML authentication error");
                }
            } finally {
                response.close();
            }
            return result;
        } catch(IOException e) {
            throw new ApperyException(e.getMessage());
        }
    }

    /**
     * Performs HTTP PUT.
     */
    String makePut(String serviceUrl, String data)
           throws ApperyException {
        try {
            HttpPut req = new HttpPut(protocol + host + serviceUrl);
            req.addHeader(new BasicHeader("Content-Type", "application/json"));
            req.addHeader(new BasicHeader("Accept", "application/json"));
            req.addHeader(new BasicHeader("User-Agent", "AU-Test-Agent"));
            req.addHeader(new BasicHeader("isrestapicall", "true"));
            req.setEntity(new StringEntity(data));
            CloseableHttpResponse response = httpclient.execute(req);
            String result = "";
            try {
                int status = response.getStatusLine().getStatusCode();
                assert200(status);
                result = EntityUtils.toString(response.getEntity());
                if (result.startsWith("<HTML>")) {
                    throw new ApperyException("SAML authentication error");
                }
            } finally {
                response.close();
            }
            return result;
        } catch(IOException e) {
            throw new ApperyException(e.getMessage());
        }
    }

}
