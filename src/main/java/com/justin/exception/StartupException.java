/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.exception;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class StartupException extends Exception {
  private static final long serialVersionUID = 1L;

  public StartupException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
