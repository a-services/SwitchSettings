package io.appery.rest;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;

/**
 * Response handler for HttpClient.
 */
public class HttpResponseHandler implements ResponseHandler<HttpResultObj> {

    @Override
    public HttpResultObj handleResponse(HttpResponse response) throws ClientProtocolException {
        HttpResultObj resp = new HttpResultObj(response);
        try {
            resp.body = dump(response);
            if (response.getStatusLine().getStatusCode() == 302){
                //resp.body = dump(response);
                resp.body = response.getLastHeader("Location").getValue();
            } else {
                Header xApperyStatusHdr = response.getLastHeader("X-Appery-Status");
                String xApperyStatus = (xApperyStatusHdr != null)?
                                        xApperyStatusHdr.getValue() : null;
                if ( "403".equals(xApperyStatus) ){
                    resp.status = 403;
                }
                //resp.body = dump(response);
            }
        } catch (Exception ex){
            System.err.println("Handler Exception: " + ex.getMessage());
        }

        return resp;
    }

    String dump(HttpResponse response) throws Exception {
        //println("-----------");
        //dumpHeader(response);
        String body = "";
        if (response.getStatusLine().getStatusCode() < 300) {
            try {
                BasicResponseHandler brh = new BasicResponseHandler();
                body = brh.handleResponse(response);
                //println(body);
            } catch (Exception ex) {
                System.err.println("Dump Exception: " + ex.getLocalizedMessage());
            }
        }
        return body;
    }
}
