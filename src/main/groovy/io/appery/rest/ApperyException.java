package io.appery.rest;

public class ApperyException extends RuntimeException {

    private String reason;

    private static final long serialVersionUID = -701020384107089414L;

    public ApperyException(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

}
