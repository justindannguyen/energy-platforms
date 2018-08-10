/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class ApplicationProperties {
  public static final String GATEWAY_ID_ENV_KEY = "ENERGY_GATEWAY_ID";
  private static final String FILE = "application.properties";

  public static final String getConfiguredDeviceId() {
    final String gatewayId = System.getProperty(GATEWAY_ID_ENV_KEY);
    return gatewayId != null ? gatewayId : System.getenv("ENERGY_GATEWAY_ID");
  }

  protected final Properties props = new Properties();

  public String getApplicationVersion() {
    return getClass().getPackage().getImplementationVersion();
  }

  public void load() throws IOException {
    final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    try (InputStream is = classloader.getResourceAsStream(FILE)) {
      props.load(is);
    }
  }
}
