/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin;

import com.justin.exception.StartupException;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class Main {
  public static void main(final String[] args) throws StartupException {
    new EnergyGateway().start();
  }
}
