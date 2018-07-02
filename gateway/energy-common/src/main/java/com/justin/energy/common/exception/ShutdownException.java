/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common.exception;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class ShutdownException extends Exception {
  private static final long serialVersionUID = 1L;

  public ShutdownException(final String message) {
    super(message);
  }

  public ShutdownException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
