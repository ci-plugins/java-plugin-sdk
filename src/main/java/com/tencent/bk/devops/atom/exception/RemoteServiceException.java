package com.tencent.bk.devops.atom.exception;

public class RemoteServiceException extends RuntimeException {

    private final int httpStatus;
    private final String responseContent;

    public RemoteServiceException(String errorMessage, int httpStatus, String responseContent) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.responseContent = responseContent;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getResponseContent() {
        return responseContent;
    }
}
