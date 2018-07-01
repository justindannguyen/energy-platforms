/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class ApplicationProperties {
  private static final String FILE = "application.properties";
  private final Properties props = new Properties();

  public String getConfigApiPath() {
    return props.get("config.api.path").toString();
  }

  public String getDeviceStatusTopic() {
    return props.getProperty("config.mqtt.deviceStatusTopic");
  }

  public String getEnergyReportTopic() {
    return props.getProperty("config.mqtt.energyReportTopic");
  }

  public String getFotaParameterTopic() {
    return props.getProperty("fota.mqtt.parameterTopic").toString();
  }

  public String getFotaSoftwareTopic() {
    return props.getProperty("fota.mqtt.softwareTopic").toString();
  }


  public String getSchemaVersion() {
    return props.getProperty("config.schema.version").toString();
  }

  public void load() throws IOException {
    final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    try (InputStream is = classloader.getResourceAsStream(FILE)) {
      props.load(is);
    }
  }
}
