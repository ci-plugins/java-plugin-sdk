package com.tencent.bk.devops.atom.exception;

public class RemoteServiceException extends RuntimeException {

  public RemoteServiceException() {}

  public RemoteServiceException(String errorMessage) {
    super(errorMessage);
  }

  public RemoteServiceException(String errorMessage, int httpStatus, String responseContent) {
    super(errorMessage);
  }
}
