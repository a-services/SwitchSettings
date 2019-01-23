package io.appery.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

/**
 * Performs SAML login into Appery.io site.
 */
public class ApperySecurity {

    ApperyClient apperyClient;
    CloseableHttpClient httpclient;
    String host;
    String protocol;

    ApperySecurity(ApperyClient apperyClient) {
        this.apperyClient = apperyClient;
    	this.httpclient = apperyClient.httpclient;
    	this.host = apperyClient.host;
    	this.protocol = apperyClient.protocol;
    }

    /**
     * Main method.
     */
    void doLogin(String username, String password, String targetPath)
            throws ApperyException {
        String target = protocol + host + targetPath;
        String loginUrl = protocol + "idp." + host + "/idp/doLogin";

        Map<String, String> params = new HashMap<String, String>();
        params.put("cn", username);
        params.put("pwd", password);
        params.put("target", target);
        try {
            loginUrl = ApperyClient.addGetParams(loginUrl, params);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new ApperyException(e.getMessage());
		}

        HttpGet request = new HttpGet(loginUrl);
        HttpResultObj response;
        try {
            response = httpclient.execute(request, new HttpResponseHandler());
        } catch (IOException e) {
            throw new ApperyException(e.getMessage());
        }

        String htmlText = response.body;
        String samlKey = getSAMLDocumentFromPage(htmlText);
        if (samlKey==null) {
        	throw new ApperyException("Login error: SAML key not found");
        }
        String targetIdpUrl = getActionEndpointURL(htmlText);
        if (targetIdpUrl==null) {
        	throw new ApperyException("Login error: target IDP URL not found");
        }
        HttpPost samlRequest = new HttpPost(targetIdpUrl);
        samlRequest.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        ArrayList<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("SAMLResponse", samlKey));
        try {
            samlRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            response = httpclient.execute(samlRequest, new HttpResponseHandler());
        } catch (IOException e) {
            throw new ApperyException(e.getMessage());
		}
    }

    /**
     * Get SAML document during login.
     */
    private String getSAMLDocumentFromPage(String htmlpage_text) {
        Pattern samlPattern = Pattern.compile("VALUE=\"([^\"]+)");
        Matcher m = samlPattern.matcher(htmlpage_text);
        if (m.find()) {
            String saml = m.group();
            return saml.substring("VAlUE=\"".length());
        }
        return null;
    }

    /**
     * Get action endpoint during login.
     */
    private String getActionEndpointURL(String htmlpage_text) {
        Pattern actionPattern = Pattern.compile("ACTION=\"([^\"]+)");
        Matcher m = actionPattern.matcher(htmlpage_text);
        if (m.find()) {
            String action = m.group();
            return action.substring("ACTION=\"".length());
        }
        return null;
    }

    /**
     * Main method.
     */
    void standaloneLogin(String username, String password)
            throws ApperyException {
        String loginUrl = protocol + host + "/apiexpress/rest/auth/login";

        HttpPost samlRequest = new HttpPost(loginUrl);
        samlRequest.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        ArrayList<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("j_username", username));
        postParameters.add(new BasicNameValuePair("j_password", password));
        try {
            samlRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            httpclient.execute(samlRequest, new HttpResponseHandler());
        } catch (IOException e) {
            throw new ApperyException(e.getMessage());
        }
    }

}